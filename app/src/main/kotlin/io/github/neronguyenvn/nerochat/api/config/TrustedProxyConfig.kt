package io.github.neronguyenvn.nerochat.api.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "trusted-proxy")
data class TrustedProxyConfig(
    val trustedIps: List<String> = emptyList(),
    val requireProxy: Boolean = true
)
