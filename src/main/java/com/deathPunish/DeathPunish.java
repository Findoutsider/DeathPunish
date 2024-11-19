package com.deathPunish;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathPunish extends JavaPlugin {

    @Override
    public void onEnable() {
        say("[DeathPunish] 插件已加载");
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
    }

    @Override
    public void onDisable() {
        // 插件禁用时的处理
        say("[DeathPunish] 插件已卸载");
    }

    public void say(String s) {
        CommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage(s);
    }
}

