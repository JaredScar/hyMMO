package com.jaredscarito.hymmo.skill;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemToolSpec;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.util.Locale;

public enum ToolKind {
    PICK,
    AXE,
    SHOVEL,
    HOE,
    SWORD,
    BOW,
    HAND,
    OTHER;

    public static ToolKind of(ItemStack stack) {
        if (stack == null || ItemStack.isEmpty(stack)) {
            return HAND;
        }
        Item item = stack.getItem();
        if (item != null) {
            return of(item);
        }
        if (stack.getItemId() == null || stack.getItemId().isBlank()) {
            return HAND;
        }
        return fromText(stack.getItemId());
    }

    public static ToolKind of(Item item) {
        if (item == null || item.getId() == null || item.getId().isBlank()) {
            return HAND;
        }
        return fromText(describe(item));
    }

    private static ToolKind fromText(String text) {
        text = text.toLowerCase(Locale.ROOT);
        if (contains(text, "pick")) {
            return PICK;
        }
        if (contains(text, "shovel", "spade")) {
            return SHOVEL;
        }
        if (contains(text, "hoe", "scythe")) {
            return HOE;
        }
        if (contains(text, "sword", "blade", "saber", "dagger")) {
            return SWORD;
        }
        if (contains(text, "bow", "crossbow")) {
            return BOW;
        }
        if (contains(text, "axe", "hatchet", "cleaver")) {
            return AXE;
        }
        return OTHER;
    }

    public Skill combatSkill() {
        return switch (this) {
            case SWORD -> Skill.SWORDS;
            case AXE -> Skill.AXES;
            case BOW -> Skill.ARCHERY;
            case HAND -> Skill.UNARMED;
            default -> null;
        };
    }

    private static String describe(Item item) {
        StringBuilder builder = new StringBuilder(item.getId());
        if (item.getCategories() != null) {
            for (String category : item.getCategories()) {
                if (category != null) {
                    builder.append(' ').append(category);
                }
            }
        }
        if (item.getTool() != null && item.getTool().getSpecs() != null) {
            for (ItemToolSpec spec : item.getTool().getSpecs()) {
                if (spec != null && spec.getGatherType() != null) {
                    builder.append(' ').append(spec.getGatherType());
                }
            }
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private static boolean contains(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
