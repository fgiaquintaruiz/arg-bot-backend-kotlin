package com.argbot.domain.port.input

import com.argbot.domain.model.Changelog

interface GetChangelogUseCase {
    fun execute(): Changelog
}
