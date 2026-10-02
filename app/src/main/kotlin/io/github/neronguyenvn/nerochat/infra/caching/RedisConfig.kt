package io.github.neronguyenvn.nerochat.infra.caching

import org.redisson.api.RedissonClient
import org.redisson.spring.cache.CacheConfig
import org.redisson.spring.cache.RedissonSpringCacheManager
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig {

    @Bean
    fun cacheManager(
        redissonClient: RedissonClient
    ): CacheManager {
        val config = mapOf(
            CacheNames.MESSAGES to CacheConfig(
                MESSAGES_TTL.toMillis(),
                MESSAGES_MAX_IDLE.toMillis()
            )
        )

        return RedissonSpringCacheManager(
            redissonClient,
            config
        )
    }

    private companion object {
        val MESSAGES_TTL: Duration = Duration.ofMinutes(30)
        val MESSAGES_MAX_IDLE: Duration = Duration.ZERO
    }
}
