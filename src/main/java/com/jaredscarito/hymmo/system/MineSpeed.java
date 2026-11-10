package com.jaredscarito.hymmo.system;

import com.jaredscarito.hymmo.service.SkillService;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class MineSpeed extends EntityEventSystem<EntityStore, DamageBlockEvent> {
    private final SkillService skills;

    public MineSpeed(SkillService skills) {
        super(DamageBlockEvent.class);
        this.skills = skills;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }

    @Override
    public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer, DamageBlockEvent event) {
        if (event.isCancelled()) {
            return;
        }
        PlayerRef player = chunk.getComponent(index, PlayerRef.getComponentType());
        Player entity = chunk.getComponent(index, Player.getComponentType());
        skills.onMine(player, event.getItemInHand(), event.getBlockType(), event, entity != null && entity.getGameMode() == GameMode.Creative);
    }
}
