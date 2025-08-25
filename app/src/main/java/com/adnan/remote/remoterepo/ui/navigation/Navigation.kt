package com.adnan.remote.remoterepo.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.adnan.remote.remoterepo.ui.screens.ChatIconShowcase
import com.adnan.remote.remoterepo.ui.screens.ChatScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Remote : Screen("remote", "Remote", Icons.Default.Call)
    object Chat : Screen("chat", "Chat", Icons.Default.Home)
    object Showcase : Screen("showcase", "Showcase", Icons.Default.Home)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    remoteContent: @Composable () -> Unit
) {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Chat) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(Screen.Remote, Screen.Chat, Screen.Showcase).forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedScreen) {
                Screen.Remote -> remoteContent()
                Screen.Chat -> ChatScreen()
                Screen.Showcase -> ChatIconShowcase()
            }
        }
    }
}
