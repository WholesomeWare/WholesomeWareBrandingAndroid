package com.csakitheone.wholesomeware

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.csakitheone.wholesomeware.service.RadioService
import com.csakitheone.wholesomeware.ui.navigation.Main
import com.csakitheone.wholesomeware.ui.navigation.Navigator
import com.csakitheone.wholesomeware.ui.navigation.RadioExperiment
import com.csakitheone.wholesomeware.ui.navigation.rememberNavigationState
import com.csakitheone.wholesomeware.ui.navigation.toEntries
import com.csakitheone.wholesomeware.ui.screens.MainScreen
import com.csakitheone.wholesomeware.ui.screens.RadioExperimentScreen

class MainActivity : ComponentActivity() {

    private var radioService by mutableStateOf<RadioService?>(null)
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RadioService.RadioBinder
            radioService = binder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            radioService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val navigationState = rememberNavigationState(
                startRoute = Main,
                topLevelRoutes = setOf(Main)
            )
            val navigator = remember { Navigator(navigationState) }

            val entryProvider = entryProvider {
                entry<Main> {
                    MainScreen(navigator = navigator)
                }
                entry<RadioExperiment> {
                    RadioExperimentScreen(navigator = navigator, radioService = radioService)
                }
            }

            NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() }
            )
        }

        val intent = Intent(this, RadioService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        askNotifyPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    private fun askNotifyPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
    }
}
