package com.deathPunish.service;

import com.deathPunish.utils.LoggerUtils;
import org.bukkit.command.CommandSender;

public class MessageService {
    public static final String PLUGIN_PREFIX = "§8[§bDeathPunish§8]§r ";

    private final LoggerUtils logger;

    public MessageService(LoggerUtils logger) {
        this.logger = logger;
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void error(String message) {
        logger.err(message);
    }

    public void info(CommandSender sender, String message) {
        sender.sendMessage(PLUGIN_PREFIX + "§a" + message);
    }

    public void warn(CommandSender sender, String message) {
        sender.sendMessage(PLUGIN_PREFIX + "§6" + message);
    }

    public void error(CommandSender sender, String message) {
        sender.sendMessage(PLUGIN_PREFIX + "§c" + message);
    }
}
