package com.argbot.infrastructure.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class VersionController(
    @Value("\${spring.application.version:unknown}") private val version: String
) {
    @GetMapping("/version")
    fun getVersion(): Map<String, String> = mapOf("version" to version)
}
