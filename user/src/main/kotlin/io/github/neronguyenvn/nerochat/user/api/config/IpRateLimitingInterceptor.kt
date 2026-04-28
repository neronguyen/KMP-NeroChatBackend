package io.github.neronguyenvn.nerochat.user.api.config

import io.github.neronguyenvn.nerochat.user.domain.exception.RateLimitExceededException
import io.github.neronguyenvn.nerochat.user.infra.resolver.IpResolver
import io.github.neronguyenvn.nerochat.user.service.ratelimiting.IpRateLimitingService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration

@Component
class IpRateLimitingInterceptor(
    private val ipRateLimit: IpRateLimitingService,
    private val ipResolver: IpResolver,
    @param:Value($$"${rate-limit.ip.apply-limit}")
    private val applyLimit: Boolean
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (handler is HandlerMethod && applyLimit) {
            val annotation = handler.getMethodAnnotation(
                IpRateLimiting::class.java
            ) ?: return true

            val clientIp = ipResolver.getClientIp(request)
            val methodKey = "${handler.beanType.simpleName}:${handler.method.name}"
            val compositeKey = "$clientIp:$methodKey"

            return try {
                ipRateLimit(
                    key = compositeKey,
                    maxRequestsPerIp = annotation.maxRequestsPerIp,
                    resetIn = Duration.of(
                        annotation.resetIn,
                        annotation.timeUnit.toChronoUnit()
                    ),
                    action = { true }
                )
                true
            } catch (_: RateLimitExceededException) {
                response.sendError(HttpStatus.TOO_MANY_REQUESTS.value())
                false
            }
        }

        return true
    }
}