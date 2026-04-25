package com.argbot.infrastructure.web

import org.springframework.boot.info.BuildProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class VersionController(private val buildProperties: BuildProperties) {
    @GetMapping("/version")
    fun getVersion(): Map<String, String> = mapOf("version" to buildProperties.version)
}
