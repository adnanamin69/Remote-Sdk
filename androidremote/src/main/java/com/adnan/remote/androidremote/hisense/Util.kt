package com.adnan.remote.androidremote.hisense

import java.security.MessageDigest

object Util {
    const val T = "VidaaService"

    fun computeMD5(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(input.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    fun crossSum(number: Long): Int {
        return number.toString().sumOf { it.toString().toInt() }
    }

    fun generateRandom48BitInteger(): Long {
        return (1L..0xFFFFFFFFFFFFL).random()
    }

    fun formatMACAddressTo48Bit(macInt: Long): String {
        return String.format(
            "%02X:%02X:%02X:%02X:%02X:%02X",
            (macInt shr 40) and 0xFF,
            (macInt shr 32) and 0xFF,
            (macInt shr 24) and 0xFF,
            (macInt shr 16) and 0xFF,
            (macInt shr 8) and 0xFF,
            macInt and 0xFF
        )
    }


}
