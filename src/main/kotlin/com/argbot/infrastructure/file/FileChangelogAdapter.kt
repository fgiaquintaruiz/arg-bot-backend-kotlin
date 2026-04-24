package com.argbot.infrastructure.file

import com.argbot.domain.model.Changelog
import com.argbot.domain.port.output.ChangelogPort
import com.argbot.infrastructure.annotation.ExternalApiAdapter
import java.io.File

@ExternalApiAdapter
class FileChangelogAdapter : ChangelogPort {
    override fun getChangelog(): Changelog {
        val file = File("CHANGELOG.md")
        return if (file.exists()) {
            Changelog(file.readText())
        } else {
            Changelog("No CHANGELOG.md found in root.")
        }
    }
}
