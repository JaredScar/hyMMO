package com.jaredscarito.hymmo.skill;

import java.util.Locale;

public enum Skill {
    MINING("Mining", "Super Breaker", "#55FFFF", true,
            "Break stone and ore with a pickaxe. Higher levels can drop extra ore.\nSuper Breaker mines faster and guarantees a double drop. Right-click the pickaxe."),
    WOODCUTTING("Woodcutting", "Tree Feller", "#55FF55", true,
            "Chop logs with an axe. Higher levels can drop extra wood.\nTree Feller breaks the connected tree. Right-click the axe."),
    EXCAVATION("Excavation", "Giga Drill", "#FFFF55", true,
            "Dig soil, sand, and gravel with a shovel.\nGiga Drill breaks a 3 by 3 area. Right-click the shovel."),
    HERBALISM("Herbalism", "Green Terra", "#00AA00", true,
            "Harvest plants with a hoe.\nGreen Terra harvests a 3 by 3 area and doubles the drop. Right-click the hoe."),
    SWORDS("Swords", "Serrated Strikes", "#FF5555", true,
            "Hit with a sword. Higher levels hit harder and can cause bleed.\nSerrated Strikes hits harder. Right-click the sword."),
    AXES("Axes", "Skull Splitter", "#FFAA00", true,
            "Hit with an axe. Higher levels hit harder.\nSkull Splitter hits harder. Right-click the axe. That click also starts Tree Feller."),
    UNARMED("Unarmed", "Berserk", "#FFFFFF", true,
            "Hit with an empty hand. Higher levels hit harder.\nBerserk hits harder. Right-click with nothing held."),
    ARCHERY("Archery", "Skill Shot", "#FF55FF", true,
            "Land hits with a bow. Higher levels hit harder.\nSkill Shot hits harder. Right-click the bow."),
    ACROBATICS("Acrobatics", "Roll", "#55AAFF", false,
            "Falling and dodging train this skill.\nRoll reduces fall damage. Dodge can cancel a hit.");

    public final String display;
    public final String ability;
    public final String color;
    public final boolean active;
    public final String about;

    Skill(String display, String ability, String color, boolean active, String about) {
        this.display = display;
        this.ability = ability;
        this.color = color;
        this.active = active;
        this.about = about;
    }

    public static Skill parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "mining", "mine", "pick" -> MINING;
            case "woodcutting", "wood", "wc", "axeskill" -> WOODCUTTING;
            case "excavation", "excav", "dig", "digging" -> EXCAVATION;
            case "herbalism", "herb", "herbs", "farming" -> HERBALISM;
            case "swords", "sword" -> SWORDS;
            case "axes", "axe" -> AXES;
            case "unarmed", "fist", "fists" -> UNARMED;
            case "archery", "bow", "bows" -> ARCHERY;
            case "acrobatics", "acro", "roll", "dodge" -> ACROBATICS;
            default -> null;
        };
    }
}
