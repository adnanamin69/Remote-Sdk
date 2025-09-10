package com.sdk.remote.remoterepo.ui.screens

import android.Manifest
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sdk.remote.androidremote.ConnectionListener
import com.sdk.remote.androidremote.RemoteNDKHelper
import com.sdk.remote.remoterepo.utils.apps
import com.utils.live.commonsdk.PcmAudioRecorder
import com.utils.live.commonsdk.viewmodels_repo.SmartViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private const val TAG = "RemoteControlScreen"

@Composable
fun MicrophonePermissionHandler() {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val smartViewModel: SmartViewModel = koinViewModel()
    val state = smartViewModel.repo.state.collectAsState()

    var showRationale by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val pcmRecorder: PcmAudioRecorder = koinInject<PcmAudioRecorder>()

    val scope = rememberCoroutineScope()
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Microphone permission granted ✅", Toast.LENGTH_SHORT).show()
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                // Permanently denied
                //  showSettings = true
                Toast.makeText(context, "Show settings", Toast.LENGTH_SHORT).show()
            } else {
                // Denied once
                Toast.makeText(context, "Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                scope.launch {
                    state.value.connectedDevice?.type?.let {
                        pcmRecorder.recordPcmAudio(it) {
                            smartViewModel.activateVoice()
                            smartViewModel.senVoicePayload(it)
                            smartViewModel.endVoice()

                        }
                    }
                    Toast.makeText(context, "Already granted", Toast.LENGTH_SHORT).show()
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity!!,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

                Toast.makeText(context, "Show rational", Toast.LENGTH_SHORT).show()

            }

            else -> {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // UI Button to trigger check
    Column(Modifier.padding(16.dp)) {
        Button(onClick = { checkAndRequestPermission() }) {
            Text("Request Microphone Permission")
        }
    }
}


@Composable
fun RemoteControlScreen(
    modifier: Modifier = Modifier
) {
    val smartViewModel: SmartViewModel = koinViewModel()
    val state = smartViewModel.repo.state.collectAsState()
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Greeting(
            name = "Android Remote ",
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
                                /*  scope.launch(Dispatchers.IO) {
                                      delay(3000)
                                      smartViewModel.disconnect()
                                  }*/
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
            text = "Secret Key: ${RemoteNDKHelper.getSecretKey()}",
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
                Row {
                    Text(
                        text = "Remote Control",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Up Button
                    Button(
                        onClick = {
                            smartViewModel.sendCommand(android.view.KeyEvent.KEYCODE_POWER)
                        },
                    ) {
                        Text("Power")
                    }

                }

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


                var text by remember {
                    mutableStateOf("")
                }
                TextField(text, onValueChange = {
                    if (it.length > text.length)
                        smartViewModel.onSendText(it.last().toString())
                    else if (it.length < text.length)
                        smartViewModel.sendCommand(KeyEvent.KEYCODE_DEL)
                    text = it


                }, trailingIcon = {
                    IconButton({
                        smartViewModel.sendCommand(KeyEvent.KEYCODE_DEL)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                })

                MicrophonePermissionHandler()


                if (state.value.connectedDevice != null) {
                    val installedApps = smartViewModel.getApps(apps)
                    installedApps?.forEach {
                        Button(
                            onClick = {
                                smartViewModel.onAppPress(it)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(it.name)
                        }
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
