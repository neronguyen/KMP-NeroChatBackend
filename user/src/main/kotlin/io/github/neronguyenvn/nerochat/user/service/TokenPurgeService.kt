package io.github.neronguyenvn.nerochat.user.service

import io.github.neronguyenvn.nerochat.user.infra.database.repository.AuthTokenRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TokenPurgeService(
    private val authTokenRepository: AuthTokenRepository,
) {
    @Scheduled(cron = "0 0 3 * * *") // every day at 3 AM
    @Transactional
    fun purgeExpiredTokens() {
        val now = Instant.now()
        authTokenRepository.deleteByExpiredAtBefore(now)
    }
}
