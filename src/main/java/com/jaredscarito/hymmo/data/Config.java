package com.jaredscarito.hymmo.data;

public final class Config {
    public int maxLevel = 1000;
    public int baseXp = 120;
    public int xpPerLevel = 20;
    public int abilityUnlockLevel = 5;
    public int baseAbilitySeconds = 2;
    public int levelsPerAbilitySecond = 50;
    public int maxAbilitySeconds = 16;
    public int cooldownFactor = 3;
    public int treeCap = 48;
    public float superBreakerSpeed = 3f;
    public double doubleDropCap = 0.6;
    public double doubleDropDivisor = 500;
    public double damageBonusCap = 0.5;
    public double damageBonusDivisor = 1000;
    public double abilityDamageMultiplier = 1.5;
    public double dodgeCap = 0.2;
    public double dodgeDivisor = 1500;
    public double rollCap = 0.5;
    public double rollDivisor = 800;
    public double bleedChanceCap = 0.35;
    public double bleedDivisor = 800;
    public int bleedTicks = 3;
    public float bleedDamageBase = 1f;
    public float bleedDamagePerLevel = 0.005f;
    public int xpStone = 4;
    public int xpOre = 25;
    public int xpLog = 12;
    public int xpLeaf = 2;
    public int xpDig = 3;
    public int xpPlant = 10;
    public double xpCombatPerDamage = 3;
    public double xpFallPerDamage = 2;
    public int xpDodge = 20;
    public String note = "hyMMO levels Mining, Woodcutting, Excavation, Herbalism, Swords, Axes, Unarmed, Archery, and Acrobatics. /mmo opens the skill tree. Each level gives one point for that skill. Right-click the matching tool to start its ability. Acrobatics is passive. Fishing, repair, taming, alchemy, and parties are not included. Grant hymmo.admin for /mmo admin.";

    public static Config defaults() {
        return new Config();
    }

    public void applyDefaults() {
        if (maxLevel <= 0) {
            maxLevel = 1000;
        }
        if (baseXp <= 0) {
            baseXp = 120;
        }
        if (xpPerLevel < 0) {
            xpPerLevel = 20;
        }
        if (abilityUnlockLevel < 0) {
            abilityUnlockLevel = 5;
        }
        if (baseAbilitySeconds <= 0) {
            baseAbilitySeconds = 2;
        }
        if (levelsPerAbilitySecond <= 0) {
            levelsPerAbilitySecond = 50;
        }
        if (maxAbilitySeconds < baseAbilitySeconds) {
            maxAbilitySeconds = 16;
        }
        if (cooldownFactor <= 0) {
            cooldownFactor = 3;
        }
        if (treeCap <= 0) {
            treeCap = 48;
        }
        if (superBreakerSpeed < 1f) {
            superBreakerSpeed = 3f;
        }
        if (doubleDropCap <= 0) {
            doubleDropCap = 0.6;
        }
        if (doubleDropDivisor <= 0) {
            doubleDropDivisor = 500;
        }
        if (damageBonusCap < 0) {
            damageBonusCap = 0.5;
        }
        if (damageBonusDivisor <= 0) {
            damageBonusDivisor = 1000;
        }
        if (abilityDamageMultiplier < 1) {
            abilityDamageMultiplier = 1.5;
        }
        if (dodgeCap < 0) {
            dodgeCap = 0.2;
        }
        if (dodgeDivisor <= 0) {
            dodgeDivisor = 1500;
        }
        if (rollCap < 0) {
            rollCap = 0.5;
        }
        if (rollDivisor <= 0) {
            rollDivisor = 800;
        }
        if (bleedChanceCap < 0) {
            bleedChanceCap = 0.35;
        }
        if (bleedDivisor <= 0) {
            bleedDivisor = 800;
        }
        if (bleedTicks < 0) {
            bleedTicks = 3;
        }
        if (bleedDamageBase < 0) {
            bleedDamageBase = 1f;
        }
        if (bleedDamagePerLevel < 0) {
            bleedDamagePerLevel = 0.005f;
        }
        if (xpStone < 0) {
            xpStone = 4;
        }
        if (xpOre < 0) {
            xpOre = 25;
        }
        if (xpLog < 0) {
            xpLog = 12;
        }
        if (xpLeaf < 0) {
            xpLeaf = 2;
        }
        if (xpDig < 0) {
            xpDig = 3;
        }
        if (xpPlant < 0) {
            xpPlant = 10;
        }
        if (xpCombatPerDamage < 0) {
            xpCombatPerDamage = 3;
        }
        if (xpFallPerDamage < 0) {
            xpFallPerDamage = 2;
        }
        if (xpDodge < 0) {
            xpDodge = 20;
        }
        if (note == null || note.isBlank()) {
            note = defaults().note;
        }
    }
}
