package com.argbot.infrastructure.binance.testsupport

import io.mockk.every
import io.mockk.mockk
import org.springframework.web.client.RestClient

/**
 * Helpers to set up MockK chains for RestClient GET and POST.
 *
 * - [returnBody] → the mock returns this value from `body(...)`.
 * - [throws]     → the mock throws this exception instead of returning a body.
 *                  The throw is injected at `retrieve()` level so it fires before
 *                  `body(...)` is called, matching Spring's actual eager-throw behaviour.
 *
 * The returned [RestClient] mock covers all header/body chaining calls via relaxed
 * matchers so callers don't need to know the exact parameter values used by the adapter.
 */

internal fun mockRestClientGet(returnBody: Any? = null, throws: Throwable? = null): RestClient {
    val client       = mockk<RestClient>()
    val uriSpec      = mockk<RestClient.RequestHeadersUriSpec<*>>()
    val headersSpec  = mockk<RestClient.RequestHeadersSpec<*>>()
    val responseSpec = mockk<RestClient.ResponseSpec>()

    every { client.get() } returns uriSpec
    every { uriSpec.uri(any<String>()) } returns headersSpec
    every { headersSpec.header(any(), any()) } returns headersSpec
    every { headersSpec.retrieve() } returns responseSpec

    if (throws != null) {
        every { responseSpec.body(any<Class<*>>()) } throws throws
    } else {
        every { responseSpec.body(any<Class<*>>()) } returns returnBody
    }

    return client
}

internal fun mockRestClientPost(returnBody: Any? = null, throws: Throwable? = null): RestClient {
    val client       = mockk<RestClient>()
    val bodyUriSpec  = mockk<RestClient.RequestBodyUriSpec>()
    val bodySpec     = mockk<RestClient.RequestBodySpec>()
    val responseSpec = mockk<RestClient.ResponseSpec>()

    every { client.post() } returns bodyUriSpec
    every { bodyUriSpec.uri(any<String>()) } returns bodySpec
    every { bodySpec.header(any(), any()) } returns bodySpec
    every { bodySpec.body(any<String>()) } returns bodySpec
    every { bodySpec.retrieve() } returns responseSpec

    if (throws != null) {
        every { responseSpec.body(any<Class<*>>()) } throws throws
    } else {
        every { responseSpec.body(any<Class<*>>()) } returns returnBody
    }

    return client
}
