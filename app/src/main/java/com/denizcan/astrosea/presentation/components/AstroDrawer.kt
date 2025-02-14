package com.denizcan.astrosea.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstroDrawer(
    drawerState: DrawerState,
    scope: CoroutineScope,
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit,
    content: @Composable () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val drawerWidth = screenWidth * 0.5f

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                    label = { Text("Profil") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        onNavigateToProfile()
                    }
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış Yap") },
                    label = { Text("Çıkış Yap") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        FirebaseAuth.getInstance().signOut()
                        onSignOut()
                    }
                )
            }
        }
    ) {
        content()
    }
} 