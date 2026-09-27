package com.edwin.bekal

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.edwin.bekal.core.security.RootDetector
import com.edwin.bekal.data.local.AppPreferencesLocalDataSource
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

    @Inject
    lateinit var appPreferences: AppPreferencesLocalDataSource

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("MainActivity", "Initial permissions result: $permissions")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isRooted = rootDetector.isRooted()
        if (isRooted) {
            val result = rootDetector.getRootDetectionResult()
            Log.w("SecurityWarning", "Root access detected: ${result.detectedThreats}")
        } else {
            checkAndRequestInitialPermissions(savedInstanceState)
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

    private fun checkAndRequestInitialPermissions(savedInstanceState: Bundle?) {
        if (savedInstanceState == null && appPreferences.isFirstLaunch()) {
            appPreferences.setFirstLaunchCompleted()
            val permissions = buildList {
                add(Manifest.permission.CAMERA)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }.toTypedArray()
            permissionLauncher.launch(permissions)
        }
    }
}