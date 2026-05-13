package io.github.neronguyenvn.nerochat.user.api.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@MustBeDocumented
@Constraint(validatedBy = [PasswordValidator::class])
annotation class Password(
    val message: String = "Password must be at least 10 characters and contain at least 1 digit and 1 letter",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class PasswordValidator : ConstraintValidator<Password, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) {
            return false
        }

        if (value.length < MIN_PASSWORD_LENGTH) {
            return false
        }

        val hasLetter = value.any { it.isLetter() }
        val hasDigit = value.any { it.isDigit() }
        return hasLetter && hasDigit
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 10
    }
}