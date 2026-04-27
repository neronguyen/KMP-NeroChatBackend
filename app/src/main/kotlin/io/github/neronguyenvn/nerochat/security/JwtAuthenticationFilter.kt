package io.github.neronguyenvn.nerochat.security

import io.github.neronguyenvn.nerochat.user.domain.exception.UserNotFoundException
import io.github.neronguyenvn.nerochat.user.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (token != null && jwtService.validateAccessToken(token)) {
            try {
                val auth = jwtService.getAuthentication(token)
                val principal = auth.principal as? UserDetails
                if (principal != null && principal.isEnabled) {
                    SecurityContextHolder.getContext().authentication = auth
                }
            } catch (_: UserNotFoundException) {
                // leave context anonymous; downstream authorization will return 401
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER) ?: return null
        if (!bearerToken.startsWith(AUTHORIZATION_PREFIX)) return null
        return bearerToken.substring(AUTHORIZATION_PREFIX.length)
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val AUTHORIZATION_PREFIX = "Bearer "
    }
}