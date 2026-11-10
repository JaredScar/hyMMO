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

import java.util.Locale;

final class SkillPageCommand extends AbstractPlayerCommand {
    private final SkillService skills;
    private final Skill skill;

    SkillPageCommand(SkillService skills, Skill skill) {
        super(skill.name().toLowerCase(Locale.ROOT), "Show your " + skill.display);
        requireNoPermission();
        switch (skill) {
            case MINING -> addAliases("mine");
            case WOODCUTTING -> addAliases("wood", "wc");
            case EXCAVATION -> addAliases("excav", "dig");
            case HERBALISM -> addAliases("herb", "herbs");
            case SWORDS -> addAliases("sword");
            case AXES -> addAliases("axe");
            case UNARMED -> addAliases("fist");
            case ARCHERY -> addAliases("bow");
            case ACROBATICS -> addAliases("acro");
        }
        this.skills = skills;
        this.skill = skill;
    }

    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef player, World world) {
        Pages.skills(skills, store, ref, player, skill);
    }
}
