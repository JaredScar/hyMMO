package com.jaredscarito.hymmo.skill;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

import java.util.Locale;

public final class Blocks {
    private Blocks() {}

    public static Skill skillFor(ToolKind tool, BlockType block) {
        if (tool == null || block == null || block.getId() == null) {
            return null;
        }
        String id = block.getId().toLowerCase(Locale.ROOT);
        if (crafted(id)) {
            return null;
        }
        String gather = breakGather(block);
        boolean soft = softOrHarvest(block);
        return switch (tool) {
            case PICK -> isMine(id) || gatherIs(gather, "rock", "ore", "stone", "mine", "pick", "metal") ? Skill.MINING : null;
            case AXE -> isWood(id) || gatherIs(gather, "wood", "log", "tree", "axe", "chop") ? Skill.WOODCUTTING : null;
            case SHOVEL -> isDig(id) || gatherIs(gather, "soil", "earth", "dirt", "sand", "dig", "shovel", "gravel") ? Skill.EXCAVATION : null;
            case HOE -> isPlant(id) || soft || gatherIs(gather, "plant", "crop", "herb", "fiber", "harvest", "soft", "flower") ? Skill.HERBALISM : null;
            case HAND -> isPlant(id) ? Skill.HERBALISM : null;
            default -> null;
        };
    }

    public static boolean isOre(String id) {
        return has(id, "ore", "gem", "crystal", "mineral", "coal", "vein", "metal", "ingot");
    }

    public static boolean isLeaf(String id) {
        return has(id, "leaf", "leaves", "foliage");
    }

    public static boolean isWood(String id) {
        return has(id, "log", "trunk", "stem", "bark", "branch", "timber", "wood", "leaf", "leaves", "foliage");
    }

    public static boolean isMine(String id) {
        return has(id, "ore", "stone", "rock", "cobble", "mineral", "gem", "crystal", "coal", "slate", "granite", "marble", "basalt", "andesite", "diorite", "obsidian", "brick", "boulder", "pebble", "metal");
    }

    public static boolean isDig(String id) {
        return has(id, "dirt", "soil", "sand", "gravel", "clay", "mud", "snow", "silt", "ash", "earth", "podzol", "loam", "peat", "dust");
    }

    public static boolean isPlant(String id) {
        return has(id, "crop", "plant", "flower", "mushroom", "herb", "berry", "sapling", "vine", "cactus", "kelp", "reed", "fiber", "flax", "cotton", "wheat", "carrot", "potato", "melon", "pumpkin", "bush", "fern", "moss", "petal", "fruit", "fungi", "fungus", "algae", "lily", "grass", "shrub", "bloom", "seed", "leaf", "leaves");
    }

    private static boolean crafted(String id) {
        return has(id, "plank", "door", "fence", "chest", "furniture", "sign", "button", "torch", "slab", "stair", "wall", "gate", "frame", "lever", "trapdoor", "hatch");
    }

    private static String breakGather(BlockType block) {
        BlockGathering gathering = block.getGathering();
        if (gathering == null || gathering.getBreaking() == null || gathering.getBreaking().getGatherType() == null) {
            return "";
        }
        return gathering.getBreaking().getGatherType().toLowerCase(Locale.ROOT);
    }

    private static boolean softOrHarvest(BlockType block) {
        BlockGathering gathering = block.getGathering();
        return gathering != null && (gathering.isSoft() || gathering.isHarvestable());
    }

    private static boolean gatherIs(String gather, String... tokens) {
        return has(gather, tokens);
    }

    private static boolean has(String text, String... tokens) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
