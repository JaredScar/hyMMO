package com.jaredscarito.hymmo.command;

import com.jaredscarito.hymmo.service.SkillService;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

final class ActivateCommand extends AbstractPlayerCommand {
    private final SkillService skills;

    ActivateCommand(SkillService skills) {
        super("activate", "Start the ability for the tool in your hand");
        requireNoPermission();
        addAliases("ability", "ready");
        this.skills = skills;
    }

    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef player, World world) {
        Player entity = store.getComponent(ref, Player.getComponentType());
        ItemStack hand = ItemStack.EMPTY;
        if (entity != null && entity.getInventory() != null) {
            hand = entity.getInventory().usingToolsItem() ? entity.getInventory().getActiveToolItem() : entity.getInventory().getActiveHotbarItem();
            if (hand == null) {
                hand = ItemStack.EMPTY;
            }
        }
        skills.activate(player, hand);
    }
}
