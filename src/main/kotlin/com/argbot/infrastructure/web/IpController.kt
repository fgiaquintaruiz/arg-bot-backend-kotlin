package com.argbot.infrastructure.web

import com.argbot.domain.port.input.GetPublicIpUseCase
import com.argbot.infrastructure.annotation.WebAdapter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@WebAdapter
@RequestMapping("/api")
class IpController(private val getPublicIp: GetPublicIpUseCase) {

    @GetMapping("/ip")
    fun getIp(): Map<String, String> {
        return mapOf("ip" to getPublicIp.execute().ip)
    }
}
