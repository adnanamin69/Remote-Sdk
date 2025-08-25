package com.adnan.remote.remoterepo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.adnan.remote.remoterepo.ui.navigation.MainNavigation
import com.adnan.remote.remoterepo.ui.screens.RemoteControlScreen
import com.adnan.remote.remoterepo.ui.theme.RemoteRepoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemoteRepoTheme {
                MainNavigation(
                    remoteContent = {
                        RemoteControlScreen(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    RemoteRepoTheme {
        MainNavigation(
            remoteContent = {
                RemoteControlScreen()
            }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RemoteRepoTheme {
        Greeting("Android")
    }
}
