package io.github.neronguyenvn.nerochat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ChatApp

/** Starts the application with [args] passed to Spring Boot. */
fun main(args: Array<String>) {
	runApplication<ChatApp>(*args)
}