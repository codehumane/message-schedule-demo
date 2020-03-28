package com.codehumane.messageschedule

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MessageScheduleApplication

fun main(args: Array<String>) {
	runApplication<MessageScheduleApplication>(*args)
}
