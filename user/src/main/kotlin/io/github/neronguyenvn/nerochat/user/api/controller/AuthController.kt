package io.github.neronguyenvn.nerochat.user.api.controller

import io.github.neronguyenvn.nerochat.user.api.config.IpRateLimiting
import io.github.neronguyenvn.nerochat.user.api.dto.AuthenticatedUserDto
import io.github.neronguyenvn.nerochat.user.api.dto.UserDto
import io.github.neronguyenvn.nerochat.user.api.dto.asDto
import io.github.neronguyenvn.nerochat.user.api.request.*
import io.github.neronguyenvn.nerochat.user.domain.exception.UserNotFoundException
import io.github.neronguyenvn.nerochat.user.service.AuthService
import io.github.neronguyenvn.nerochat.user.service.EmailVerificationService
import io.github.neronguyenvn.nerochat.user.service.PasswordResetService
import io.github.neronguyenvn.nerochat.user.service.ratelimiting.EmailRateLimitingService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
    private val emailRateLimit: EmailRateLimitingService
) {

    @PostMapping("/register")
    @IpRateLimiting
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {
        return userService.register(
            email = body.email,
            displayName = body.displayName,
            password = body.password
        ).asDto()
    }

    @PostMapping("/resend-verification")
    @IpRateLimiting
    fun resendVerification(
        @Valid @RequestBody body: EmailRequest
    ) {
        emailRateLimit(body.email) {
            emailVerificationService.resendVerificationEmail(body.email)
        }
    }

    @PostMapping("/login")
    @IpRateLimiting
    fun login(
        @Valid @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return userService.login(
            email = body.email,
            password = body.password
        ).asDto()
    }

    @PostMapping("/refresh-token")
    @IpRateLimiting
    fun refreshToken(
        @Valid @RequestBody body: RefreshTokenRequest
    ): AuthenticatedUserDto {
        return userService.refreshToken(
            refreshToken = body.refreshToken
        ).asDto()
    }

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody body: RefreshTokenRequest
    ) {
        userService.logout(refreshToken = body.refreshToken)
    }

    @GetMapping("/verify-email")
    fun verifyEmail(
        @RequestParam token: String
    ) {
        emailVerificationService.verifyEmail(token)
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @IpRateLimiting
    fun forgotPassword(
        @Valid @RequestBody body: EmailRequest
    ) {
        try {
            passwordResetService.requestPasswordReset(body.email)
        } catch (_: UserNotFoundException) {
            // Intentionally swallowed — never reveal whether the email is registered
        }
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody body: ResetPasswordRequest
    ) {
        passwordResetService.resetPassword(
            token = body.token,
            newPassword = body.newPassword
        )
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody body: ChangePasswordRequest,
        @AuthenticationPrincipal userId: UUID
    ) {
        passwordResetService.changePassword(
            userId = userId,
            oldPassword = body.oldPassword,
            newPassword = body.newPassword
        )
    }
}