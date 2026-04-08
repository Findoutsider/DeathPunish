package com.deathPunish.commands;

import com.deathPunish.DeathPunish;
import com.deathPunish.service.CustomItemService;

public record CommandContext(DeathPunish plugin, CustomItemService customItemService) {}
