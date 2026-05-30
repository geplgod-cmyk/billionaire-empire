package com.billionaire.empire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.billionaire.empire.ui.screens.*
import com.billionaire.empire.ui.theme.BillionaireTheme
import com.billionaire.empire.viewmodel.AppScreen
import com.billionaire.empire.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            BillionaireTheme {
                val screen = vm.screen

                when (screen) {
                    is AppScreen.Splash ->
                        SplashScreen(vm.loadingMsg)

                    is AppScreen.NoConnection ->
                        NoConnectionScreen(onRetry = { vm.startup(this@MainActivity) })

                    is AppScreen.Login ->
                        AuthScreen(vm, this@MainActivity)

                    is AppScreen.SessionExpired ->
                        SessionExpiredScreen(onLogin = {
                            vm.screen = AppScreen.Login
                        })

                    is AppScreen.Loading ->
                        SplashScreen(vm.loadingMsg)

                    is AppScreen.Game ->
                        MainScreen(
                            vm      = vm,
                            ctx     = this@MainActivity,
                            onLogout = { vm.logout(this@MainActivity) }
                        )
                }
            }
        }

        // Kick off startup flow
        vm.startup(this)
    }

    override fun onPause() {
        super.onPause()
        // Server saves on every action — no extra save needed here
    }

    override fun onStop() {
        super.onStop()
        // Heartbeat still running via coroutine loop
    }
}
