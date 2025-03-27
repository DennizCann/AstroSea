package com.denizcan.astrosea.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
private fun DrawerItem(
    icon: Int,
    text: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        modifier = Modifier.height(48.dp),
        icon = { 
            Icon(
                painter = painterResource(id = icon),
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        },
        label = { 
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        selected = false,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Black.copy(alpha = 0.6f),
            selectedContainerColor = Color.Black.copy(alpha = 0.8f)
        )
    )
}

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
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier.width(screenWidth / 2)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.anamenu),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NavigationDrawerItem(
                                modifier = Modifier.height(48.dp),
                                icon = { 
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Profil",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = { 
                                    Text(
                                        "Profil",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                selected = false,
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                    }
                                    onNavigateToProfile()
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Black.copy(alpha = 0.6f),
                                    selectedContainerColor = Color.Black.copy(alpha = 0.8f)
                                )
                            )

                            NavigationDrawerItem(
                                modifier = Modifier.height(48.dp),
                                icon = { 
                                    Icon(
                                        Icons.Default.ExitToApp,
                                        contentDescription = "Çıkış Yap",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = { 
                                    Text(
                                        "Çıkış Yap",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                selected = false,
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                    }
                                    onSignOut()
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Black.copy(alpha = 0.6f),
                                    selectedContainerColor = Color.Black.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) {
        content()
    }
} 