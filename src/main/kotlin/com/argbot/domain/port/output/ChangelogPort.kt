package com.argbot.domain.port.output

import com.argbot.domain.model.Changelog

interface ChangelogPort {
    fun getChangelog(): Changelog
}
