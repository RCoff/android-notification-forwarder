package com.rco.notificationforwarder

import org.junit.Test


class HTTPUtilsTest {
    @Test
    fun makeHttpRequest_isSuccessful() {
        // Test the makeHttpRequest function with a dummy URL and method.
        val url = "192.168.10.131"
        val method = "POST"
        val headers = mapOf("Content-Type" to "application/json")
        val payload = """{"key":"value"}"""

        // Call the function and check the response code.
        val responseCode = makeHttpRequest(url, method, headers, payload)
    }
}