package com.jaredscarito.hymmo;

import com.jaredscarito.hymmo.command.HyMmoCommand;
import com.jaredscarito.hymmo.service.SkillService;
import com.jaredscarito.hymmo.system.AbilityUse;
import com.jaredscarito.hymmo.system.CombatSkills;
import com.jaredscarito.hymmo.system.GatherSkills;
import com.jaredscarito.hymmo.system.MineSpeed;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public final class HyMmoPlugin extends JavaPlugin {
    private final SkillService skills;

    public HyMmoPlugin(JavaPluginInit init) {
        super(init);
        this.skills = new SkillService(this);
    }

    @Override
    protected void setup() {
        getCommandRegistry().registerCommand(new HyMmoCommand(skills));
        getEntityStoreRegistry().registerSystem(new GatherSkills(skills));
        getEntityStoreRegistry().registerSystem(new MineSpeed(skills));
        getEntityStoreRegistry().registerSystem(new CombatSkills(skills));
        getEventRegistry().register(PlayerConnectEvent.class, event -> skills.remember(event.getPlayerRef()));
        getEventRegistry().register(PlayerMouseButtonEvent.class, event -> AbilityUse.onMouse(skills, event));
    }

    @Override
    protected void start() {
        skills.load();
    }

    @Override
    protected void shutdown() {
        skills.shutdown();
    }
}
