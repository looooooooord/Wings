package com.otaku.wings.ui.common

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorKindTest {

    @Test
    fun `unknown host is reported as offline`() {
        assertEquals(ErrorKind.OFFLINE, classifyError(UnknownHostException("api.test")))
    }

    @Test
    fun `timeout is reported as offline`() {
        assertEquals(ErrorKind.OFFLINE, classifyError(SocketTimeoutException("timeout")))
    }

    @Test
    fun `connect failure is reported as offline`() {
        assertEquals(ErrorKind.OFFLINE, classifyError(IOException("Failed to connect to api.test")))
    }

    @Test
    fun `other IO errors stay generic`() {
        assertEquals(ErrorKind.GENERIC, classifyError(IOException("Unexpected end of stream")))
    }

    @Test
    fun `programming errors stay generic`() {
        assertEquals(ErrorKind.GENERIC, classifyError(IllegalStateException("boom")))
    }

    @Test
    fun `null error is generic`() {
        assertEquals(ErrorKind.GENERIC, classifyError(null))
    }
}
