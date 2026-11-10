package com.jaredscarito.hymmo.command;

import com.jaredscarito.hymmo.service.SkillService;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

final class AdminXpCommand extends AbstractPlayerCommand {
    private final SkillService skills;
    private final RequiredArg<String> nameArg;
    private final RequiredArg<String> skillArg;
    private final RequiredArg<Integer> amountArg;

    AdminXpCommand(SkillService skills) {
        super("addxp", "Give a player skill XP");
        requirePermission("hymmo.admin");
        this.skills = skills;
        this.nameArg = withRequiredArg("player", "Player name", ArgTypes.STRING);
        this.skillArg = withRequiredArg("skill", "Skill name", ArgTypes.STRING);
        this.amountArg = withRequiredArg("amount", "XP to add", ArgTypes.INTEGER);
    }

    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef player, World world) {
        Replies.reply(player, skills.adminAdd(context.get(nameArg), context.get(skillArg), context.get(amountArg)));
    }
}
