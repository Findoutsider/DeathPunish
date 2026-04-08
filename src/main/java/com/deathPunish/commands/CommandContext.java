package com.deathPunish.commands;

import com.deathPunish.DeathPunish;
import com.deathPunish.service.CustomItemService;
import com.deathPunish.service.MessageService;

public record CommandContext(DeathPunish plugin, CustomItemService customItemService, MessageService messageService) {}
