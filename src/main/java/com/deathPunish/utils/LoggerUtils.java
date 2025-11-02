package com.deathPunish.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;


public class LoggerUtils {
    public static CommandSender console;
    public static final String prefix = "§8[§bDeathPunish§8]§r ";

    public LoggerUtils() {
        console = Bukkit.getConsoleSender();
    }


    public void info(String message) {
        info(ChatColor.GREEN, message);
    }

    public void info(ChatColor color, String s) {
        console.sendMessage(prefix + (color == null ? ChatColor.GREEN : color) + s);
    }

    public void severe_info(String message, Boolean italic) {
        serve_info(ChatColor.GREEN, message, italic);
    }

    public void serve_info(ChatColor color, String s, Boolean italic) {
        if (italic) s = "§o" + s;
        console.sendMessage(prefix + (color == null ? ChatColor.GREEN : color) + "§l" + s);
    }

    public void warn(String message) {
        warn(ChatColor.GOLD, message);
    }

    public void warn(ChatColor color, String s) {
        console.sendMessage(prefix + (color == null ? ChatColor.GOLD : color) + s);
    }

    public void serve_warn(String message, Boolean italic) {
        serve_warn(ChatColor.GOLD, message, italic);
    }

    public void serve_warn(ChatColor color, String s, Boolean italic) {
        if (italic) s = "§o" + s;
        console.sendMessage(prefix + (color == null ? ChatColor.GOLD : color) + "§l" + s);
    }

    public void err(String message) {
        err(ChatColor.RED, message);
    }

    public void err(ChatColor color, String s) {
        console.sendMessage(prefix + (color == null ? ChatColor.RED : color)  + s);
    }

    public void severe_err(String message, Boolean italic) {
        serve_err(ChatColor.RED, message, italic);
    }

    public void serve_err(ChatColor color, String s, Boolean italic) {
        if (italic) s = "§o" + s;
        console.sendMessage(prefix + (color == null ? ChatColor.RED : color) + "§l" + s);
    }
}
