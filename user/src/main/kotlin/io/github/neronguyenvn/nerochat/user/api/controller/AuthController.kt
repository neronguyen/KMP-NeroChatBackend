package io.github.neronguyenvn.nerochat.user.api.controller

import io.github.neronguyenvn.nerochat.user.api.dto.AuthenticatedUserDto
import io.github.neronguyenvn.nerochat.user.api.dto.UserDto
import io.github.neronguyenvn.nerochat.user.api.mapper.asDto
import io.github.neronguyenvn.nerochat.user.api.request.LoginRequest
import io.github.neronguyenvn.nerochat.user.api.request.RefreshTokenRequest
import io.github.neronguyenvn.nerochat.user.api.request.RegisterRequest
import io.github.neronguyenvn.nerochat.user.service.AuthService
import io.github.neronguyenvn.nerochat.user.service.EmailVerificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: AuthService,
    private val emailVerificationService: EmailVerificationService,
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {
        return userService.register(
            email = body.email,
            password = body.password
        ).asDto()
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return userService.login(
            email = body.email,
            password = body.password
        ).asDto()
    }

    @PostMapping("/refresh-token")
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
}
