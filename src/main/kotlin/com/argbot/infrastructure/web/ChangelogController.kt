package com.argbot.infrastructure.web

import com.argbot.domain.port.input.GetChangelogUseCase
import com.argbot.infrastructure.annotation.WebAdapter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@WebAdapter
@RequestMapping("/api")
class ChangelogController(private val getChangelog: GetChangelogUseCase) {

    @GetMapping("/changelog")
    fun getChangelog(): Map<String, String> {
        return mapOf("content" to getChangelog.execute().content)
    }
}
