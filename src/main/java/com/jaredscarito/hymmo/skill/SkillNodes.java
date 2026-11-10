package com.jaredscarito.hymmo.skill;

import java.util.ArrayList;
import java.util.List;

public final class SkillNodes {
    public enum Effect {
        DOUBLE,
        XP,
        ABILITY,
        COOLDOWN,
        SPEED,
        TREE,
        DAMAGE,
        BLEED,
        ROLL,
        DODGE
    }

    public record Node(
            String id,
            Skill skill,
            String branch,
            String name,
            String detail,
            int cost,
            int level,
            String requires,
            Effect effect,
            double amount
    ) {
    }

    private static final List<Node> ALL = build();

    private SkillNodes() {
    }

    public static List<Node> of(Skill skill) {
        List<Node> nodes = new ArrayList<>();
        for (Node node : ALL) {
            if (node.skill == skill) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    public static Node byId(String id) {
        if (id == null) {
            return null;
        }
        for (Node node : ALL) {
            if (node.id.equals(id)) {
                return node;
            }
        }
        return null;
    }

    private static List<Node> build() {
        List<Node> nodes = new ArrayList<>();
        path(nodes, Skill.MINING, "Yield",
                node("Prospector", "+15% double drops", Effect.DOUBLE, 0.15),
                node("Rich Vein", "+50% mining XP", Effect.XP, 0.5),
                node("Motherlode", "+25% double drops", Effect.DOUBLE, 0.25));
        path(nodes, Skill.MINING, "Tempo",
                node("Quick Hands", "Super Breaker is faster", Effect.SPEED, 1),
                node("Long Shift", "+4 seconds", Effect.ABILITY, 4),
                node("Relentless", "shorter cooldown", Effect.COOLDOWN, -1));
        path(nodes, Skill.WOODCUTTING, "Timber",
                node("Lumberjack", "+50% woodcutting XP", Effect.XP, 0.5),
                node("Wide Crown", "Tree Feller reaches farther", Effect.TREE, 24),
                node("Second Cut", "+20% double drops", Effect.DOUBLE, 0.2));
        path(nodes, Skill.WOODCUTTING, "Pace",
                node("Sap", "+4 seconds", Effect.ABILITY, 4),
                node("Short Rest", "shorter cooldown", Effect.COOLDOWN, -1),
                node("Seasoned", "+35% woodcutting XP", Effect.XP, 0.35));
        path(nodes, Skill.EXCAVATION, "Spoil",
                node("Loose Earth", "+20% double drops", Effect.DOUBLE, 0.2),
                node("Deep Scoop", "+50% excavation XP", Effect.XP, 0.5),
                node("Full Bucket", "+20% double drops", Effect.DOUBLE, 0.2));
        path(nodes, Skill.EXCAVATION, "Pace",
                node("Steady Spade", "+4 seconds", Effect.ABILITY, 4),
                node("Short Rest", "shorter cooldown", Effect.COOLDOWN, -1),
                node("Surveyor", "+35% excavation XP", Effect.XP, 0.35));
        path(nodes, Skill.HERBALISM, "Harvest",
                node("Green Thumb", "+20% double drops", Effect.DOUBLE, 0.2),
                node("Bumper Crop", "+50% herbalism XP", Effect.XP, 0.5),
                node("Full Basket", "+25% double drops", Effect.DOUBLE, 0.25));
        path(nodes, Skill.HERBALISM, "Pace",
                node("Living Soil", "+4 seconds", Effect.ABILITY, 4),
                node("Short Rest", "shorter cooldown", Effect.COOLDOWN, -1),
                node("Forager", "+35% herbalism XP", Effect.XP, 0.35));
        path(nodes, Skill.SWORDS, "Edge",
                node("Keen", "+10% sword damage", Effect.DAMAGE, 0.1),
                node("Bleed", "+15% bleed chance", Effect.BLEED, 0.15),
                node("Execution", "+15% sword damage", Effect.DAMAGE, 0.15));
        path(nodes, Skill.SWORDS, "Pace",
                node("Serrated Time", "+4 seconds", Effect.ABILITY, 4),
                node("Short Rest", "shorter cooldown", Effect.COOLDOWN, -1),
                node("Duelist", "+35% sword XP", Effect.XP, 0.35));
        path(nodes, Skill.AXES, "Weight",
                node("Heavy", "+10% axe damage", Effect.DAMAGE, 0.1),
                node("Follow Through", "+10% axe damage", Effect.DAMAGE, 0.1),
                node("Split", "+15% axe damage", Effect.DAMAGE, 0.15));
        path(nodes, Skill.AXES, "Pace",
                node("Skull Time", "+4 seconds", Effect.ABILITY, 4),
                node("Short Rest", "shorter cooldown", Effect.COOLDOWN, -1),
                node("Lumber Warrior", "+35% axe XP", Effect.XP, 0.35));
        path(nodes, Skill.UNARMED, "Fist",
                node("Knuckle", "+10% unarmed damage", Effect.DAMAGE, 0.1),
                node("Flurry", "+10% unarmed damage", Effect.DAMAGE, 0.1),
                node("Rage", "+15% unarmed damage", Effect.DAMAGE, 0.15));
        path(nodes, Skill.UNARMED, "Pace",
                node("Long Berserk", "+4 seconds", Effect.ABILITY, 4),
                node("Short Rest", "shorter cooldown", Effect.COOLDOWN, -1),
                node("Brawler", "+35% unarmed XP", Effect.XP, 0.35));
        path(nodes, Skill.ARCHERY, "Aim",
                node("Draw", "+10% archery damage", Effect.DAMAGE, 0.1),
                node("Vital", "+15% archery damage", Effect.DAMAGE, 0.15),
                node("Hunter", "+35% archery XP", Effect.XP, 0.35));
        path(nodes, Skill.ARCHERY, "Pace",
                node("Long Aim", "+4 seconds", Effect.ABILITY, 4),
                node("Short Rest", "shorter cooldown", Effect.COOLDOWN, -1),
                node("Fletcher", "+35% archery XP", Effect.XP, 0.35));
        path(nodes, Skill.ACROBATICS, "Roll",
                node("Tuck", "+10% fall reduction", Effect.ROLL, 0.1),
                node("Deep Roll", "+10% fall reduction", Effect.ROLL, 0.1),
                node("Catfall", "+10% fall reduction", Effect.ROLL, 0.1));
        path(nodes, Skill.ACROBATICS, "Dodge",
                node("Sidestep", "+5% dodge chance", Effect.DODGE, 0.05),
                node("Feather", "+5% dodge chance", Effect.DODGE, 0.05),
                node("Untouchable", "+5% dodge chance", Effect.DODGE, 0.05));
        return List.copyOf(nodes);
    }

    private record Draft(String name, String detail, Effect effect, double amount) {
    }

    private static Draft node(String name, String detail, Effect effect, double amount) {
        return new Draft(name, detail, effect, amount);
    }

    private static void path(List<Node> nodes, Skill skill, String branch, Draft first, Draft second, Draft third) {
        add(nodes, skill, branch, 1, null, first);
        add(nodes, skill, branch, 2, id(skill, branch, 1), second);
        add(nodes, skill, branch, 3, id(skill, branch, 2), third);
    }

    private static void add(List<Node> nodes, Skill skill, String branch, int tier, String requires, Draft draft) {
        int level = switch (tier) {
            case 1 -> 1;
            case 2 -> 8;
            default -> 20;
        };
        nodes.add(new Node(id(skill, branch, tier), skill, branch, draft.name, draft.detail, tier, level, requires, draft.effect, draft.amount));
    }

    private static String id(Skill skill, String branch, int tier) {
        return skill.name() + "." + branch + "." + tier;
    }
}
