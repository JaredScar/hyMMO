package com.jaredscarito.hymmo.command;

import com.jaredscarito.hymmo.service.SkillService;
import com.jaredscarito.hymmo.skill.Skill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class HyMmoCommand extends AbstractPlayerCommand {
    private final SkillService skills;

    public HyMmoCommand(SkillService skills) {
        super("mmo", "Skill levels and abilities");
        requireNoPermission();
        addAliases("hymmo", "mcmmo", "mcstats");
        this.skills = skills;
        for (Skill skill : Skill.values()) {
            addSubCommand(new SkillPageCommand(skills, skill));
        }
        addSubCommand(new ActivateCommand(skills));
        addSubCommand(new TopCommand(skills));
        addSubCommand(new AdminCommand(skills));
    }

    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef player, World world) {
        Pages.skills(skills, store, ref, player, null);
    }
}
