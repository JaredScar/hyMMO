package com.jaredscarito.hymmo.system;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.MouseButtonEvent;
import com.hypixel.hytale.protocol.MouseButtonState;
import com.hypixel.hytale.protocol.MouseButtonType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.jaredscarito.hymmo.service.SkillService;

public final class AbilityUse {
    private AbilityUse() {
    }

    public static void onMouse(SkillService skills, PlayerMouseButtonEvent event) {
        MouseButtonEvent button = event.getMouseButton();
        if (button == null || button.mouseButtonType != MouseButtonType.Right || button.state != MouseButtonState.Pressed) {
            return;
        }
        Player player = event.getPlayer();
        if (player != null && player.getGameMode() == GameMode.Creative) {
            return;
        }
        PlayerRef ref = event.getPlayerRefComponent();
        if (ref == null) {
            return;
        }
        skills.activate(ref, event.getItemInHand());
    }
}
