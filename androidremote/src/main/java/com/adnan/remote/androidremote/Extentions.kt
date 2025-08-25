package com.adnan.remote.androidremote

import android.app.Activity
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.annotation.Keep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

@Keep
fun Activity.name(): String {
    return name1(this)
}

private fun name1(activity: Activity): String {
    return "Adnan Amin ${activity.packageName}"
}

@Keep
fun Activity.allow(): Boolean {
    return allow1(this)
}

val pkgNameArray = arrayOf(
    "Y29tLmZpcmV0di5hbWF6b24ubm9kZWpzaW5hbmRyb2lk",
    "Y29tLmFkbmFuLnJlbW90ZS5yZW1vdGVyZXBvMTIz"
)

private fun allow1(activity: Activity): Boolean {


    return pkgNameArray.any {
        val expected = it.decode()
        Log.i("TAG11", "allow1: $expected")
        (activity.packageName == expected)
    }
}


fun String.decode(): String {
    return String(Base64.decode(this, Base64.DEFAULT))
}


@Keep
suspend fun isPortOpen(ip: String?, port: Int): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port), 2000)  // Timeout after 2 seconds
            socket.close()
            true
        } catch (_: Exception) {
            false
        }
    }
}

@Keep
fun String.isValidIP(): Boolean {
    // Regular expression patterns for IPv4 and IPv6
    val ipv4Pattern =
        """^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$""".toRegex()
    val ipv6Pattern =
        """^([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})?$""".toRegex()

    return ipv4Pattern.matches(this) || ipv6Pattern.matches(this)
}


@Keep
fun Int.getFireKey() =
    when (this) {
        KeyEvent.KEYCODE_DPAD_UP -> "dpad_up"
        KeyEvent.KEYCODE_DPAD_DOWN -> "dpad_down"
        KeyEvent.KEYCODE_DPAD_LEFT -> "dpad_left"
        KeyEvent.KEYCODE_DPAD_RIGHT -> "dpad_right"
        KeyEvent.KEYCODE_DPAD_CENTER -> "select"
        KeyEvent.KEYCODE_BACK -> "back"
        KeyEvent.KEYCODE_MENU -> "menu"
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> "play"
        KeyEvent.KEYCODE_VOLUME_DOWN -> "volume_down"
        KeyEvent.KEYCODE_VOLUME_UP -> "volume_up"
        KeyEvent.KEYCODE_VOLUME_MUTE -> "mute"
        KeyEvent.KEYCODE_POWER -> "sleep"
        KeyEvent.KEYCODE_HOME -> "home"
        KeyEvent.KEYCODE_INFO -> "info"
        KeyEvent.KEYCODE_MEDIA_NEXT -> "search"
        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> "previous"
        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> "dpad_right"
        KeyEvent.KEYCODE_MEDIA_REWIND -> "dpad_left"
        KeyEvent.KEYCODE_CHANNEL_UP -> "channel_up"
        KeyEvent.KEYCODE_CHANNEL_DOWN -> "channel_down"
        KeyEvent.KEYCODE_DEL -> "backspace"
        KeyEvent.KEYCODE_SEARCH -> "search"
        else -> ""
    }



