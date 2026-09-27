package com.edwin.bekal.core.security

import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

interface ShellCommandExecutor {
    fun canExecuteSu(): Boolean
}

@Singleton
class DefaultShellCommandExecutor @Inject constructor() : ShellCommandExecutor {
    override fun canExecuteSu(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            line != null && line.isNotEmpty()
        } catch (_: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }
}
