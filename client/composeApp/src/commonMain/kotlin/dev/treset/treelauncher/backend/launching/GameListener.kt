package dev.treset.treelauncher.backend.launching

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException
import java.util.*

class GameListener(
    val gameProcess: Process,
    private val exitCallback: (duration: Long, error: String?) -> Unit
) {
    var isValid = false
        private set
    var isRunning = false
        private set
    var isExited = false
        private set
    var exitCode = -1
        private set
    private var playStart: Long = 0

    init {
        isValid = true
    }

    fun start() {
        playStart = System.currentTimeMillis()
        val t = Thread({ listenToGameOutput() }, "GameListener")
        t.start()
    }

    fun stop() {
        Thread {
            if (isRunning) {
                gameProcess.destroy()
            }
            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                LOGGER.warn(e) { "Game listener interrupted, restarting: pid=${gameProcess.pid()}" }
            }
            if (isRunning) {
                gameProcess.destroyForcibly()
            }
        }.start()
    }

    private fun listenToGameOutput() {
        isRunning = true
        LOGGER.info { "Listening to game process: pid=${gameProcess.pid()}" }
        try {
            gameProcess.inputReader().use { reader ->
                reader.lines().iterator().forEachRemaining { value: String -> LOGGER.debug { "Game: $value" } }
            }
        } catch (e: IOException) {
            LOGGER.debug(e) { "Game output forwarding failed: pid=${gameProcess.pid()}" }
        }
        while (gameProcess.isAlive) {
            try {
                gameProcess.waitFor()
            } catch (e: InterruptedException) {
                LOGGER.warn(e) { "Game listener interrupted, restarting: pid=${gameProcess.pid()}" }
                isValid = false
            }
        }
        onGameExit()
    }

    private fun onGameExit() {
        isRunning = false
        exitCode = gameProcess.exitValue()
        var error: String? = null
        if (exitCode != 0) {
            try {
                gameProcess.errorReader().use { reader ->
                    val out = StringJoiner("\n")
                    reader.lines().iterator().forEachRemaining { newElement: String? -> out.add(newElement) }
                    LOGGER.warn { "Game process exited with non-zero code: code=${exitCode}, pid=${gameProcess.pid()}, error=${out}" }
                    error = out.toString()
                }
            } catch (e: IOException) {
                LOGGER.warn(e) { "Game process exited with non-zero code: pid=${gameProcess.pid()}, error=unable to read error stream" }
                error = "Unable to read error stream"
            }
        } else {
            LOGGER.info { "Game exited" }
        }
        isExited = true
        val playDuration = (System.currentTimeMillis() - playStart) / 1000
        exitCallback(playDuration, error)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
