package io.github.neronguyenvn.nerochat.user.infra.resolver

import io.github.neronguyenvn.nerochat.user.infra.config.NginxConfig
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import java.net.Inet4Address
import java.net.Inet6Address

@Component
class IpResolver(
    private val nginxConfig: NginxConfig
) {
    private val logger = LoggerFactory.getLogger(IpResolver::class.java)

    private val trustedMatchers: List<IpAddressMatcher> = nginxConfig
        .trustedIps
        .map { proxy ->
            val cidr = when {
                proxy.contains("/") -> proxy
                proxy.contains(":") -> "$proxy/128"
                else -> "$proxy/32"
            }

            IpAddressMatcher(cidr)
        }

    /**
     * Returns a normalized X-Real-IP value only when the connection is from a trusted proxy.
     * When proxy use is optional, falls back to the remote address for untrusted connections or
     * invalid/missing headers. Private client addresses are accepted.
     *
     * @throws SecurityException when proxy use is required and the proxy or client header is invalid.
     */
    fun getClientIp(request: HttpServletRequest): String {
        val remoteAddr = request.remoteAddr
        if (!isFromTrustedProxy(remoteAddr)) {
            if (nginxConfig.requireProxy) {
                logger.warn("Direct connection attempt from $remoteAddr, not via a trusted proxy")
                throw SecurityException("Request not from a trusted proxy")
            }

            return remoteAddr
        }

        val clientIp = extractRealIpFromNginx(request, remoteAddr)
        if (clientIp == null) {
            logger.warn("No valid client IP in proxy headers")
            if (nginxConfig.requireProxy) {
                throw SecurityException("No valid client IP in proxy headers")
            }
        }

        return clientIp ?: remoteAddr
    }

    /** Returns the normalized X-Real-IP header, or null when it is missing or invalid. */
    private fun extractRealIpFromNginx(
        request: HttpServletRequest,
        proxyIp: String
    ): String? {
        return request.getHeader(NGINX_IP_HEADER)?.let { header ->
            validateAndNormalizeIp(header, proxyIp)
        }
    }

    /**
     * Trims and normalizes an IPv4 or IPv6 address, including private addresses.
     * Returns null for blank or configured invalid values and for address parsing failures.
     */
    private fun validateAndNormalizeIp(
        ip: String,
        proxyIp: String
    ): String? {
        val trimmedIp = ip.trim()
        val headerName = NGINX_IP_HEADER

        if (trimmedIp.isBlank() || invalidIps.contains(trimmedIp)) {
            logger.debug("Invalid IP in $headerName: $ip from proxy $proxyIp")
            return null
        }

        return try {
            val inetAddr = when {
                trimmedIp.contains(":") -> Inet6Address.getByName(trimmedIp)
                trimmedIp.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) -> {
                    Inet4Address.getByName(trimmedIp)
                }

                else -> throw IllegalArgumentException("Not a recognised IPv4 or IPv6 address: $trimmedIp")
            }

            if (isPrivateIp(inetAddr.hostAddress)) {
                logger.debug("Private IP in $headerName: $trimmedIp from proxy $proxyIp")
            }

            inetAddr.hostAddress

        } catch (e: Exception) {
            logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp", e)
            null
        }
    }

    /** Checks the configured private, loopback, and IPv6 link-local address ranges. */
    private fun isPrivateIp(ip: String): Boolean {
        return privateMatchers.any { it.matches(ip) }
    }

    /** Returns whether [ip] matches any configured trusted proxy address or CIDR range. */
    private fun isFromTrustedProxy(ip: String): Boolean {
        return trustedMatchers.any { matcher ->
            matcher.matches(ip)
        }
    }

    companion object {
        private const val NGINX_IP_HEADER = "X-Real-IP"

        private val privateMatchers = listOf(
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16",
            "127.0.0.0/8",
            "::1/128",
            "fc00::/7",
            "fe80::/10"
        ).map { IpAddressMatcher(it) }

        private val invalidIps = listOf(
            "unknown",
            "unavailable",
            "0.0.0.0",
            "::"
        )
    }
}