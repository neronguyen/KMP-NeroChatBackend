package io.github.neronguyenvn.nerochat.notification.service

import io.github.neronguyenvn.nerochat.domain.type.UserId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import kotlin.time.Duration

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    private val templateService: EmailTemplateService,
    @param:Value($$"${email.from}")
    private val emailFrom: String,
    @param:Value($$"${email.url}")
    private val baseUrl: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendVerificationEmail(
        userId: UserId,
        email: String,
        displayName: String,
        token: String
    ) {
        logger.info("Sending verification email for user $userId")

        val verificationUrl = UriComponentsBuilder
            .fromUriString("$baseUrl/api/auth/verify-email")
            .queryParam("token", token)
            .build()
            .toUriString()

        val htmlContent = templateService.processTemplate(
            templateName = "emails/account-verification",
            variables = mapOf(
                "displayName" to displayName,
                "verificationUrl" to verificationUrl
            )
        )

        sendHtmlEmail(
            to = email,
            subject = "Verify your Nero Chat account",
            html = htmlContent
        )
    }

    fun sendPasswordResetEmail(
        userId: UserId,
        email: String,
        displayName: String,
        token: String,
        expiresIn: Duration
    ) {
        logger.info("Sending password reset email for user $userId")

        val resetPasswordUrl = UriComponentsBuilder
            .fromUriString("$baseUrl/api/auth/reset-password")
            .queryParam("token", token)
            .build()
            .toUriString()

        val htmlContent = templateService.processTemplate(
            templateName = "emails/reset-password",
            variables = mapOf(
                "displayName" to displayName,
                "resetPasswordUrl" to resetPasswordUrl,
                "expiresInMinutes" to expiresIn.inWholeMinutes
            )
        )

        sendHtmlEmail(
            to = email,
            subject = "Reset your Nero Chat password",
            html = htmlContent
        )
    }

    private fun sendHtmlEmail(
        to: String,
        subject: String,
        html: String
    ) {
        val message = javaMailSender.createMimeMessage()
        MimeMessageHelper(message, true, Charsets.UTF_8.name()).apply {
            setFrom(emailFrom)
            setTo(to)
            setSubject(subject)
            setText(html, true)
        }

        try {
            javaMailSender.send(message)
        } catch (e: MailException) {
            logger.error("Could not send email", e)
        }
    }
}
