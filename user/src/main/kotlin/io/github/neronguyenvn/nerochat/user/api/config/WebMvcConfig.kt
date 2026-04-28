package io.github.neronguyenvn.nerochat.user.api.config

import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class WebMvcConfig(
    private val ipRateLimitingInterceptor: IpRateLimitingInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(ipRateLimitingInterceptor)
            .addPathPatterns("/api/**")
    }
}