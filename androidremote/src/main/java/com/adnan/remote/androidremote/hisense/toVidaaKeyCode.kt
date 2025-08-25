package com.adnan.remote.androidremote.hisense

import android.view.KeyEvent
import androidx.annotation.Keep

@Keep
fun Int.toVidaaKeyCode(): String = when (this) {
    KeyEvent.KEYCODE_POWER -> "KEY_POWER"

    KeyEvent.KEYCODE_0 -> "KEY_0"
    KeyEvent.KEYCODE_1 -> "KEY_1"
    KeyEvent.KEYCODE_2 -> "KEY_2"
    KeyEvent.KEYCODE_3 -> "KEY_3"
    KeyEvent.KEYCODE_4 -> "KEY_4"
    KeyEvent.KEYCODE_5 -> "KEY_5"
    KeyEvent.KEYCODE_6 -> "KEY_6"
    KeyEvent.KEYCODE_7 -> "KEY_7"
    KeyEvent.KEYCODE_8 -> "KEY_8"
    KeyEvent.KEYCODE_9 -> "KEY_9"

    KeyEvent.KEYCODE_DPAD_LEFT -> "KEY_LEFT"
    KeyEvent.KEYCODE_DPAD_RIGHT -> "KEY_RIGHT"
    KeyEvent.KEYCODE_DPAD_UP -> "KEY_UP"
    KeyEvent.KEYCODE_DPAD_DOWN -> "KEY_DOWN"
    KeyEvent.KEYCODE_ENTER,
    KeyEvent.KEYCODE_DPAD_CENTER -> "KEY_OK"

    KeyEvent.KEYCODE_HOME -> "KEY_HOME"
    KeyEvent.KEYCODE_MENU -> "KEY_MENU"
    KeyEvent.KEYCODE_BACK -> "KEY_RETURNS"
    KeyEvent.KEYCODE_GUIDE -> "KEY_EPG"
    KeyEvent.KEYCODE_ESCAPE -> "KEY_EXIT"
    KeyEvent.KEYCODE_CAPTIONS -> "KEY_SUBTITLE"

    KeyEvent.KEYCODE_MEDIA_PLAY -> "KEY_PLAY"
    KeyEvent.KEYCODE_MEDIA_PAUSE -> "KEY_PAUSE"
    KeyEvent.KEYCODE_MEDIA_STOP -> "KEY_STOP"
    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> "KEY_FORWARDS"
    KeyEvent.KEYCODE_MEDIA_REWIND -> "KEY_BACK"
    KeyEvent.KEYCODE_MEDIA_NEXT -> "KEY_NEXT"
    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> "KEY_PREV"
    KeyEvent.KEYCODE_MEDIA_RECORD -> "KEY_REC"

    KeyEvent.KEYCODE_VOLUME_UP -> "KEY_VOLUMEUP"
    KeyEvent.KEYCODE_VOLUME_DOWN -> "KEY_VOLUMEDOWN"
    KeyEvent.KEYCODE_MUTE -> "KEY_MUTE"
    KeyEvent.KEYCODE_VOLUME_MUTE -> "KEY_MUTE"
    KeyEvent.KEYCODE_CHANNEL_UP -> "KEY_CHANNELUP"
    KeyEvent.KEYCODE_CHANNEL_DOWN -> "KEY_CHANNELDOWN"

    KeyEvent.KEYCODE_PROG_BLUE -> "KEY_BLUE"
    KeyEvent.KEYCODE_PROG_GREEN -> "KEY_GREEN"
    KeyEvent.KEYCODE_PROG_RED -> "KEY_RED"
    KeyEvent.KEYCODE_PROG_YELLOW -> "KEY_YELLOW"

    KeyEvent.KEYCODE_PERIOD -> "KEY_CHANNELDOT"
    KeyEvent.KEYCODE_SETTINGS -> "KEY_MENU"
    KeyEvent.KEYCODE_DEL -> "BACKSPACE"

    else -> ""
}

