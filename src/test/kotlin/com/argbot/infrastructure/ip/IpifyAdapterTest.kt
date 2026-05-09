package com.argbot.infrastructure.ip

import com.argbot.infrastructure.binance.testsupport.mockRestClientGet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for IpifyAdapter.
 *
 * Happy-path: verifies that the adapter returns the IP wrapped in PublicIp.
 * Null-body: verifies that IpifyApiException is thrown instead of silent "unknown" fallback.
 */
class IpifyAdapterTest {

    @Test
    fun `getIp returns PublicIp with address from response body`() {
        val client = mockRestClientGet(returnBody = "203.0.113.42")
        val adapter = IpifyAdapter(client)

        val result = adapter.getIp()

        assertEquals("203.0.113.42", result.ip)
    }

    @Test
    fun `getIp throws IpifyApiException when body is null`() {
        val client = mockRestClientGet(returnBody = null)
        val adapter = IpifyAdapter(client)

        assertThrows<IpifyApiException> { adapter.getIp() }
    }
}
