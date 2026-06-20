package com.denizcan.astrosea.navigation

import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Hizli cift tiklamada navigation stack'in bozulmasini onler.
 */
class AppNavigator(
    private val navController: NavController,
    private val scope: CoroutineScope
) {
    @Volatile
    private var locked = false

    fun run(block: () -> Unit) {
        if (locked) return
        locked = true
        block()
        scope.launch {
            delay(450)
            locked = false
        }
    }

    fun navigate(route: String, builder: (androidx.navigation.NavOptionsBuilder.() -> Unit)? = null) {
        run {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute == route) return@run
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
                builder?.invoke(this)
            }
        }
    }

    fun popBack() {
        run {
            navController.popBackStack()
        }
    }

    /** Ana sayfaya don — stack uzerindeki ekranlari temizler, Home'u silmez. */
    fun popBackToHome() {
        run {
            val onHome = navController.currentDestination?.route == Screen.Home.route
            if (onHome) return@run
            val popped = navController.popBackStack(Screen.Home.route, inclusive = false)
            if (!popped) {
                navController.navigate(Screen.Home.route) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }
}
