package com.compuspulse.utils

import androidx.core.text.HtmlCompat

object HtmlUtils {

    @JvmStatic
    fun plainText(value: String?): String {
        if (value == null) {
            return ""
        }
        return HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_LEGACY)
            .toString()
            .trim()
    }
}
