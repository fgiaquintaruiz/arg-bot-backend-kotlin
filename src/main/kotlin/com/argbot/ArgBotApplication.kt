package com.argbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ArgBotApplication

fun main(args: Array<String>) {
    runApplication<ArgBotApplication>(*args)
}
