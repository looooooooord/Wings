package com.otaku.wings.ui.common

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Categories of failure the UI can explain to the user.
 * Raw exception text is logged, never shown.
 */
enum class ErrorKind { OFFLINE, GENERIC }

internal fun classifyError(error: Throwable?): ErrorKind = when (error) {
    is UnknownHostException, is SocketTimeoutException -> ErrorKind.OFFLINE
    is IOException -> if (error.message?.contains("Failed to connect", true) == true ||
        error.message?.contains("timeout", true) == true ||
        error.message?.contains("Unable to resolve host", true) == true
    ) ErrorKind.OFFLINE else ErrorKind.GENERIC

    else -> ErrorKind.GENERIC
}
