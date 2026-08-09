package com.playermp3

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.playermp3.playback.PlayerViewModel
import com.playermp3.ui.CadenceApp
import com.playermp3.ui.theme.CadenceTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installCrashLogging()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            CadenceTheme(settings.themeMode) {
                CadenceApp(viewModel)
            }
        }
    }

    private fun installCrashLogging() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("CadenceCrash", "Crash on ${thread.name}", throwable)
                val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val entry = "---- $stamp ----\n" + throwable.stackTraceToString() + "\n"
                File(filesDir, "crash.log").appendText(entry)
            } catch (_: Exception) {
            }
            defaultHandler?.uncaughtException(thread, throwable)
                ?: android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
}