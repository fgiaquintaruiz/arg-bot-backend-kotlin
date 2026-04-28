package com.argbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ArgBotApplication

fun main(args: Array<String>) {
    runApplication<ArgBotApplication>(*args)
}
