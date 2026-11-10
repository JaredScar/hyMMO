package com.jaredscarito.hymmo.command;

import com.jaredscarito.hymmo.service.SkillService;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

final class AdminReloadCommand extends AbstractPlayerCommand {
    private final SkillService skills;

    AdminReloadCommand(SkillService skills) {
        super("reload", "Reload the hyMMO config");
        requirePermission("hymmo.admin");
        this.skills = skills;
    }

    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef player, World world) {
        Replies.reply(player, skills.reload());
    }
}
