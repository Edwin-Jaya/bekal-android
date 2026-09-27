package com.edwin.bekal

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.edwin.bekal.core.security.RootDetector
import com.edwin.bekal.navigation.RootNavHost
import com.edwin.bekal.presentation.security.RootedDeviceScreen
import com.edwin.bekal.ui.theme.BekalTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rootDetector: RootDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isRooted = rootDetector.isRooted()
        if (isRooted) {
            val result = rootDetector.getRootDetectionResult()
            Log.w("SecurityWarning", "Root access detected: ${result.detectedThreats}")
        }

        setContent {
            BekalTheme {
                if (isRooted) {
                    RootedDeviceScreen(
                        onExitApp = {
                            finishAffinity()
                            exitProcess(0)
                        }
                    )
                } else {
                    RootNavHost()
                }
            }
        }
    }
}