package com.mitch.fontpicker.data.room.util

@Suppress("UNUSED")
object ValidationUtils {
    fun isValidUrl(url: String): Boolean {
        val urlRegex = "^(https?://)?([\\w.-]+)+(:\\d+)?(/\\S*)?$"
        return url.matches(urlRegex.toRegex())
    }
}