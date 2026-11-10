package com.jaredscarito.hymmo.data;

import java.util.ArrayList;
import java.util.List;

public final class SkillState {
    public String skill;
    public int level;
    public int xp;
    public long abilityUntil;
    public long cooldownUntil;
    public List<String> nodes;

    public boolean owns(String id) {
        return nodes != null && nodes.contains(id);
    }

    public void grant(String id) {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
        if (!nodes.contains(id)) {
            nodes.add(id);
        }
    }

    public void refund() {
        if (nodes != null) {
            nodes.clear();
        }
    }
}
