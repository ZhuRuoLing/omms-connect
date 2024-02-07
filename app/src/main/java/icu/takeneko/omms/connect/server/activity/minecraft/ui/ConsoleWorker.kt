package icu.takeneko.omms.connect.server.activity.minecraft.ui

import android.util.Log
import androidx.core.text.PrecomputedTextCompat
import icu.takeneko.omms.connect.server.activity.minecraft.ui.management.ConsoleFragment
import java.util.concurrent.atomic.AtomicBoolean

class ConsoleWorker(private val consoleFragment: ConsoleFragment) : Thread("ConsoleWorkerThread") {

    private val running = AtomicBoolean(true)
    private val stopped = AtomicBoolean(false)
    private val cachedLogLines = mutableListOf<String>()
    private val logBuffer = mutableListOf<String>()
    private lateinit var precomputedTextCompat: PrecomputedTextCompat
    override fun run() {
        while (running.get()) {
            synchronized(cachedLogLines) {
                synchronized(logBuffer) {
                    if (logBuffer.isNotEmpty()) {
                        logBuffer.forEach {
                            appendLogLine(it)
                        }
                        logBuffer.clear()
                        precomputeText()
                        consoleFragment.displayLog(precomputedTextCompat)
                    }
                }
            }
            sleep(10)
        }
        stopped.set(true)
    }

    private fun precomputeText() {
        val params = consoleFragment.retrieveTextMetricsParams()
        precomputedTextCompat =
            PrecomputedTextCompat.create(cachedLogLines.joinToString(separator = "\n"), params)
    }

    private fun appendLogLine(string: String) {
        if (cachedLogLines.size >= 500) {
            dropFirstLine()
            dropFirstLine()
        }
        cachedLogLines += string.split("\n").map {
            it.replace("\r", "")
                .replace("\n", "")
        }
    }

    private fun dropFirstLine() {
        cachedLogLines.removeAt(0)
    }

    fun append(line: String) {
        synchronized(logBuffer) {
            logBuffer += line
        }
    }

    fun clear() {
        synchronized(cachedLogLines) {
            cachedLogLines.clear()
            synchronized(logBuffer) {
                logBuffer.clear()
            }
            precomputeText()
            consoleFragment.displayLog(precomputedTextCompat)
        }
    }

    fun shutdown() {
        running.set(false)
        while (!stopped.get()) sleep(10)
    }

    fun dumpLogs() {
        for (s in cachedLogLines) {
            Log.i("OMMS", s)
        }
    }
}