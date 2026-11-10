package com.jaredscarito.hymmo.command;

import com.jaredscarito.hymmo.service.SkillService;
import com.jaredscarito.hymmo.skill.Skill;
import com.jaredscarito.hymmo.text.Texts;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

final class TopCommand extends AbstractPlayerCommand {
    private final SkillService skills;
    private final RequiredArg<String> skillArg;

    TopCommand(SkillService skills) {
        super("top", "Show the leaders for a skill");
        requireNoPermission();
        addAliases("rank", "mctop");
        this.skills = skills;
        this.skillArg = withRequiredArg("skill", "Skill name", ArgTypes.STRING);
    }

    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef player, World world) {
        Skill skill = Skill.parse(context.get(skillArg));
        if (skill == null) {
            Replies.reply(player, Texts.err("Unknown skill. Use mining, woodcutting, excavation, herbalism, swords, axes, unarmed, archery, or acrobatics."));
            return;
        }
        skills.top(player, skill);
    }
}
