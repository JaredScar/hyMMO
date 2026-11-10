package com.jaredscarito.hymmo.system;

import com.jaredscarito.hymmo.service.SkillService;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class CombatSkills extends EntityEventSystem<EntityStore, Damage> {
    private final SkillService skills;

    public CombatSkills(SkillService skills) {
        super(Damage.class);
        this.skills = skills;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public SystemGroup<EntityStore> getGroup() {
        DamageModule module = DamageModule.get();
        return module == null ? null : module.getFilterDamageGroup();
    }

    @Override
    public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer, Damage event) {
        PlayerRef victim = chunk.getComponent(index, PlayerRef.getComponentType());
        Player entity = chunk.getComponent(index, Player.getComponentType());
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        skills.onDamage(store, ref, victim, entity, event);
    }
}
