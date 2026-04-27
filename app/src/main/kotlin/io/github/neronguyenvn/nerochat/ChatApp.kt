package io.github.neronguyenvn.nerochat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ChatApp

fun main(args: Array<String>) {
	runApplication<ChatApp>(*args)
}