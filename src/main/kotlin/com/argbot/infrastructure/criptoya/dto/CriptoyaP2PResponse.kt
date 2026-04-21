package com.argbot.infrastructure.criptoya.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CriptoyaP2PResponse(val ask: Double)
