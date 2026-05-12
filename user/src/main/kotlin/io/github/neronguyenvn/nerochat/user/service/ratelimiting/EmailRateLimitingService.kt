package io.github.neronguyenvn.nerochat.user.service.ratelimiting

import io.github.neronguyenvn.nerochat.user.domain.exception.RateLimitExceededException
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
class EmailRateLimitingService(private val redisson: RedissonClient) {

    operator fun invoke(
        email: String,
        action: () -> Unit
    ) {
        val normalizedEmail = email.lowercase().trim()
        val lockKey = "$EMAIL_LOCK_PREFIX:$normalizedEmail"
        val rateLimitKey = "$EMAIL_RATE_LIMIT_PREFIX:$normalizedEmail"
        val attemptCountKey = "$EMAIL_ATTEMPT_COUNT_PREFIX:$normalizedEmail"

        val lock = redisson.getLock(lockKey)
        if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
            try {
                val rateLimitBucket = redisson.getBucket<String>(rateLimitKey)
                val ttlMillis = rateLimitBucket.remainTimeToLive()
                if (ttlMillis > 0) {
                    throw RateLimitExceededException(ttlMillis / 1000)
                }

                action()
                val attemptCounter = redisson.getAtomicLong(attemptCountKey)
                val currentCount = attemptCounter.getAndIncrement()

                val backoffSeconds = when (currentCount) {
                    0L -> 60L
                    1L -> 300L
                    else -> 3600L
                }

                rateLimitBucket.set("1", Duration.ofSeconds(backoffSeconds))
                attemptCounter.expire(Duration.ofHours(24))

            } finally {
                if (lock.isHeldByCurrentThread) {
                    lock.unlock()
                }
            }
        } else {
            throw RuntimeException("Server is busy. Please try again later.")
        }
    }

    companion object {
        private const val EMAIL_LOCK_PREFIX = "lock:email"
        private const val EMAIL_RATE_LIMIT_PREFIX = "rate_limit:email"
        private const val EMAIL_ATTEMPT_COUNT_PREFIX = "attempt_count:email"
    }
}