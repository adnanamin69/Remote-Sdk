package com.adnan.remote.remoterepo.ui.screens

import android.R.attr.allow
import android.R.attr.name
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adnan.remote.androidremote.ConnectionListener
import com.adnan.remote.androidremote.YourNDKHelper
import com.adnan.remote.androidremote.allow
import com.adnan.remote.androidremote.name
import com.adnan.remote.remoterepo.viewmodels.SmartViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private const val TAG = "RemoteControlScreen"

@Composable
fun RemoteControlScreen(
    modifier: Modifier = Modifier
) {
    val smartViewModel: SmartViewModel = koinViewModel()
    val state = smartViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Greeting(
            name = "Android Remote " + activity?.name() + " " + activity?.allow(),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        var showPin by remember { mutableStateOf(false) }
        var error1 by remember { mutableStateOf("") }

        // TV List
        Text(
            text = "Available TVs:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        state.value.tvs.forEach {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        smartViewModel.connect(it, object : ConnectionListener {
                            override fun onConnect() {
                                Log.i(TAG, "onConnect: ")
                                showPin = false
                                error1 = "Connected"
                            }

                            override fun onFailedToConnect(error: String) {
                                Log.i(TAG, "onFailedToConnect: ")
                                error1 = error
                                scope.launch(Dispatchers.IO) {
                                    delay(3000)
                                    smartViewModel.disconnect()
                                }
                            }

                            override fun onPinRequired(size: Int) {
                                Log.i(TAG, "onPinRequired: ")
                                showPin = true
                                error1 = "Pin size $size"
                            }

                            override fun onDisconnect() {
                                Log.i(TAG, "onDisconnect: ")
                                showPin = false
                            }

                            override fun pinError(string: String) {
                                error1 = string
                            }
                        })
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = it.friendlyName,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PIN Input
        var code by remember { mutableStateOf("") }
        if (showPin) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Enter PIN Code:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("PIN Code") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { smartViewModel.pinCode(code) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Connect")
                    }
                }
            }
        }

        // Status
        if (error1.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (error1 == "Connected")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error1,
                    modifier = Modifier.padding(12.dp),
                    color = if (error1 == "Connected")
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secret Key
        Text(
            text = "Secret Key: ${YourNDKHelper.getSecretKey()}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Remote Control Pad
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Remote Control",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    // Up Button
                    Button(
                        onClick = {
                            smartViewModel.sendCommand(android.view.KeyEvent.KEYCODE_DPAD_UP)
                        },
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Text("Up")
                    }

                    // Down Button
                    Button(
                        onClick = {
                            smartViewModel.sendCommand(android.view.KeyEvent.KEYCODE_DPAD_DOWN)
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Text("Down")
                    }

                    // Left Button
                    Button(
                        onClick = {
                            smartViewModel.sendCommand(android.view.KeyEvent.KEYCODE_DPAD_LEFT)
                        },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Text("Left")
                    }

                    // Right Button
                    Button(
                        onClick = {
                            smartViewModel.sendCommand(android.view.KeyEvent.KEYCODE_DPAD_RIGHT)
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text("Right")
                    }

                    // Center/OK Button
                    Button(
                        onClick = {
                            smartViewModel.sendCommand(android.view.KeyEvent.KEYCODE_DPAD_CENTER)
                        },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
        style = MaterialTheme.typography.headlineSmall
    )
}
