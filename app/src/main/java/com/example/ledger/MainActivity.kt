package com.example.ledger

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ledger.data.AuthSession
import com.example.ledger.ui.PersonalCenterScreen
import com.example.ledger.ui.screen.HomeScreen
import com.example.ledger.ui.screen.SplashScreen
import com.example.ledger.ui.theme.LedgerTheme
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable data object MainRoute
@Serializable data object PersonalCenterRoute

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthSession.init(this)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Force highest display refresh rate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.let { win ->
                val display = win.windowManager.defaultDisplay
                val modes = display.supportedModes
                val maxRefreshRateMode = modes.maxByOrNull { it.refreshRate }
                if (maxRefreshRateMode != null) {
                    val lp = win.attributes
                    lp.preferredDisplayModeId = maxRefreshRateMode.modeId
                    win.attributes = lp
                }
            }
        }

        setContent {
            LedgerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showSplash by remember { mutableStateOf(true) }

                    LaunchedEffect(Unit) {
                        delay(1800L)
                        showSplash = false
                    }

                    if (showSplash) {
                        SplashScreen()
                    } else {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = MainRoute) {
                            composable<MainRoute> {
                                HomeScreen(
                                    onNavigateToPersonalCenter = { navController.navigate(PersonalCenterRoute) }
                                )
                            }
                            composable<PersonalCenterRoute> {
                                PersonalCenterScreen(onNavigateBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}
