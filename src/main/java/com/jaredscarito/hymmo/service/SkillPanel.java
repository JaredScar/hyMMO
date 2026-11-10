package com.jaredscarito.hymmo.service;

import com.jaredscarito.hymmo.skill.Skill;

import java.util.List;

public final class SkillPanel {
    public final Skill skill;
    public final int level;
    public final int xp;
    public final int next;
    public final int points;
    public final boolean capped;
    public final String status;
    public final List<NodeRow> nodes;

    public SkillPanel(Skill skill, int level, int xp, int next, int points, boolean capped, String status, List<NodeRow> nodes) {
        this.skill = skill;
        this.level = level;
        this.xp = xp;
        this.next = next;
        this.points = points;
        this.capped = capped;
        this.status = status;
        this.nodes = nodes;
    }

    public static final class NodeRow {
        public final String id;
        public final String branch;
        public final String name;
        public final String status;
        public final String detail;
        public final String color;

        public NodeRow(String id, String branch, String name, String status, String detail, String color) {
            this.id = id;
            this.branch = branch;
            this.name = name;
            this.status = status;
            this.detail = detail;
            this.color = color;
        }
    }
}
