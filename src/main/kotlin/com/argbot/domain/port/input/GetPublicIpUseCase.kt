package com.argbot.domain.port.input

import com.argbot.domain.model.PublicIp

interface GetPublicIpUseCase {
    fun execute(): PublicIp
}
