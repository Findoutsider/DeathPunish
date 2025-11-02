package com.deathPunish.utils;

public class ColorUtils {
    public static String colorize(String text) {
        if (text == null) {
            return null;
        }
        return text.replace("&", "§");
    }
}
