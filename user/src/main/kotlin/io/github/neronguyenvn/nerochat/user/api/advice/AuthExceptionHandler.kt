package io.github.neronguyenvn.nerochat.user.api.advice

import io.github.neronguyenvn.nerochat.user.domain.exception.EmailNotVerifiedException
import io.github.neronguyenvn.nerochat.user.domain.exception.InvalidTokenException
import io.github.neronguyenvn.nerochat.user.domain.exception.SamePasswordException
import io.github.neronguyenvn.nerochat.user.domain.exception.UserAlreadyExistsException
import io.github.neronguyenvn.nerochat.user.domain.exception.UserNotFoundException
import io.github.neronguyenvn.nerochat.user.domain.exception.WrongPasswordException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onUserAlreadyExists(e: UserAlreadyExistsException) = mapOf(
        "code" to "USER_EXISTS",
        "message" to e.message
    )

    @ExceptionHandler(InvalidTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onInvalidToken(e: InvalidTokenException) = mapOf(
        "code" to "INVALID_TOKEN",
        "message" to e.message
    )

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onUserNotFound(e: UserNotFoundException) = mapOf(
        "code" to "USER_NOT_FOUND",
        "message" to e.message
    )

    @ExceptionHandler(WrongPasswordException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onPasswordMismatch(e: WrongPasswordException) = mapOf(
        "code" to "PASSWORD_MISMATCH",
        "message" to e.message
    )

    @ExceptionHandler(EmailNotVerifiedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onEmailNotVerified(e: EmailNotVerifiedException) = mapOf(
        "code" to "EMAIL_NOT_VERIFIED",
        "message" to e.message
    )

    @ExceptionHandler(SamePasswordException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onSamePassword(e: SamePasswordException) = mapOf(
        "code" to "SAME_PASSWORD",
        "message" to e.message
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onMethodArgumentNotValid(e: MethodArgumentNotValidException): Map<String, Any> {
        val errors = e.bindingResult.allErrors.map {
            it.defaultMessage ?: "Invalid value"
        }
        return mapOf(
            "code" to "ARGUMENT_NOT_VALID",
            "message" to errors.first(),
            "errors" to errors
        )
    }
}
