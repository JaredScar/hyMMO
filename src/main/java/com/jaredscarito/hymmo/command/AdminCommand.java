package com.jaredscarito.hymmo.command;

import com.jaredscarito.hymmo.service.SkillService;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

final class AdminCommand extends AbstractCommandCollection {
    AdminCommand(SkillService skills) {
        super("admin", "Staff tools for hyMMO");
        requirePermission("hymmo.admin");
        addSubCommand(new AdminSetCommand(skills));
        addSubCommand(new AdminXpCommand(skills));
        addSubCommand(new AdminReloadCommand(skills));
    }
}
