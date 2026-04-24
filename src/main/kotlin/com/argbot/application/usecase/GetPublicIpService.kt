package com.argbot.application.usecase

import com.argbot.application.annotation.UseCase
import com.argbot.domain.model.PublicIp
import com.argbot.domain.port.input.GetPublicIpUseCase
import com.argbot.domain.port.output.PublicIpPort

@UseCase
class GetPublicIpService(private val publicIpPort: PublicIpPort) : GetPublicIpUseCase {
    override fun execute(): PublicIp = publicIpPort.getIp()
}
