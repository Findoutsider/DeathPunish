package com.deathPunish.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    boolean execute(CommandSender sender, String[] args);

    default List<String> complete(CommandSender sender, String[] args) {
        return null;
    }
}
