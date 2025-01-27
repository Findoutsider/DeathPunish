package com.deathPunish;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;


public class LoggerUtils {
    public static CommandSender console;
    public static final String prefix = "¡ì8[¡ìcDeathPunish¡ì8]¡ìr ";

    public LoggerUtils() {
        console = Bukkit.getConsoleSender();
    }

    public void info(String s) {
        s = "¡ìb" + s;
        console.sendMessage(prefix + s);
    }

    public void warn(String s) {
        console.sendMessage(prefix + "¡ìe" + s);
    }

    public void serve_warn(String s, Boolean italic) {
        if (italic) s = "¡ìo" + s;
        console.sendMessage(prefix + "¡ìe¡ìl" + s);
    }

    public void err(String s) {
        console.sendMessage(prefix + "¡ìc" + s);
    }

    public void serve_err(String s, Boolean italic) {
        if (italic) s = "¡ìo" + s;
        console.sendMessage(prefix + "¡ìc¡ìl" + s);
    }
}
