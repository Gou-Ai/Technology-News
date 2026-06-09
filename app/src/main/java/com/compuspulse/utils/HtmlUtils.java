package com.compuspulse.utils;

import androidx.core.text.HtmlCompat;

public class HtmlUtils {

    private HtmlUtils() {
    }

    public static String plainText(String value) {
        if (value == null) {
            return "";
        }
        return HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_LEGACY)
                .toString()
                .trim();
    }
}
