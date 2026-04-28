package io.github.neronguyenvn.nerochat.user.service.ratelimiting

import io.github.neronguyenvn.nerochat.user.domain.exception.RateLimitExceededException
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
class IpRateLimitingService(private val redisson: RedissonClient) {

    operator fun invoke(
        key: String,
        maxRequestsPerIp: Int,
        resetIn: Duration,
        action: () -> Boolean
    ): Boolean {
        val normalizedKey = key.lowercase().trim()
        val lockKey = "$IP_LOCK_PREFIX:$normalizedKey"
        val rateLimitKey = "$IP_RATE_LIMIT_PREFIX:$normalizedKey"

        val lock = redisson.getLock(lockKey)
        if (lock.tryLock(3, 5, TimeUnit.SECONDS)) {
            try {
                val bucket = redisson.getAtomicLong(rateLimitKey)
                val currentCount = bucket.get()
                val ttlMillis = bucket.remainTimeToLive()

                if (currentCount >= maxRequestsPerIp && ttlMillis > 0) {
                    throw RateLimitExceededException(ttlMillis / 1000)
                }

                if (currentCount == 0L || ttlMillis <= 0) {
                    bucket.set(1)
                    bucket.expire(resetIn)
                } else {
                    bucket.incrementAndGet()
                }

                return action()

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
        private const val IP_LOCK_PREFIX = "lock:ip"
        private const val IP_RATE_LIMIT_PREFIX = "rate_limit:ip"
    }
}