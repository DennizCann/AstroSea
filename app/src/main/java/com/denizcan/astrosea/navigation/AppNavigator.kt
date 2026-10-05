package com.denizcan.astrosea.navigation

import androidx.navigation.NavController
import androidx.lifecycle.Lifecycle
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
        if (locked || navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return
        locked = true
        try {
            block()
        } finally {
            scope.launch {
                try { delay(450) } finally { locked = false }
            }
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
            // Popping the last destination leaves NavHost empty (only the window background).
            if (navController.previousBackStackEntry != null) navController.popBackStack()
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
