package com.argbot.domain.port.output

import com.argbot.domain.model.PublicIp

interface PublicIpPort {
    fun getIp(): PublicIp
}
