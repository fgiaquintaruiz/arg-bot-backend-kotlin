package com.argbot.application.usecase

import com.argbot.application.annotation.UseCase
import com.argbot.domain.model.Changelog
import com.argbot.domain.port.input.GetChangelogUseCase
import com.argbot.domain.port.output.ChangelogPort

@UseCase
class GetChangelogService(private val changelogPort: ChangelogPort) : GetChangelogUseCase {
    override fun execute(): Changelog = changelogPort.getChangelog()
}
