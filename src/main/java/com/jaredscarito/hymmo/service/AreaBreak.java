package com.jaredscarito.hymmo.service;

import com.jaredscarito.hymmo.skill.Blocks;
import com.jaredscarito.hymmo.skill.Skill;
import com.jaredscarito.hymmo.skill.ToolKind;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class AreaBreak {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final int[][] AROUND = {
            {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},
            {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1}
    };
    private static final int[][] TOUCH = {
            {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
    };

    private AreaBreak() {}

    public static boolean nested() {
        return DEPTH.get() > 0;
    }

    public static List<Vector3i> collect(World world, Skill skill, int x, int y, int z, int cap) {
        List<Vector3i> found = new ArrayList<>();
        if (skill == Skill.WOODCUTTING) {
            ArrayDeque<Vector3i> queue = new ArrayDeque<>();
            HashSet<String> seen = new HashSet<>();
            enqueue(queue, seen, x, y, z);
            while (!queue.isEmpty() && found.size() < cap) {
                Vector3i next = queue.removeFirst();
                if (next.x == x && next.y == y && next.z == z) {
                    for (int[] delta : TOUCH) {
                        enqueue(queue, seen, next.x + delta[0], next.y + delta[1], next.z + delta[2]);
                    }
                    continue;
                }
                BlockType type = blockAt(world, next.x, next.y, next.z);
                if (type == null || Blocks.skillFor(ToolKind.AXE, type) != Skill.WOODCUTTING) {
                    continue;
                }
                found.add(next);
                if (found.size() >= cap) {
                    break;
                }
                for (int[] delta : TOUCH) {
                    enqueue(queue, seen, next.x + delta[0], next.y + delta[1], next.z + delta[2]);
                }
            }
            return found;
        }
        for (int[] delta : AROUND) {
            int nx = x + delta[0];
            int nz = z + delta[2];
            BlockType type = blockAt(world, nx, y, nz);
            if (type == null) {
                continue;
            }
            if (skill == Skill.EXCAVATION && Blocks.skillFor(ToolKind.SHOVEL, type) == Skill.EXCAVATION) {
                found.add(new Vector3i(nx, y, nz));
            } else if (skill == Skill.HERBALISM && Blocks.skillFor(ToolKind.HOE, type) == Skill.HERBALISM) {
                found.add(new Vector3i(nx, y, nz));
            }
        }
        return found;
    }

    public static void run(Ref<EntityStore> ref, ItemStack tool, Store<EntityStore> store, World world, List<Vector3i> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        int depth = DEPTH.get();
        DEPTH.set(depth + 1);
        try {
            BlockHarvestUtils.performBlockBreak(ref, tool, blocks, 0, store, world.getChunkStore().getStore());
        } finally {
            DEPTH.set(depth);
        }
    }

    public static BlockType blockAt(World world, int x, int y, int z) {
        if (world == null || y < 0 || y >= ChunkUtil.HEIGHT) {
            return null;
        }
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(x, y, z);
        if (section == null || !section.isValid()) {
            return null;
        }
        BlockSection blocks = world.getChunkStore().getStore().getComponent(section, BlockSection.getComponentType());
        if (blocks == null) {
            return null;
        }
        int id = blocks.get(x & ChunkUtil.SIZE_MASK, y & ChunkUtil.SIZE_MASK, z & ChunkUtil.SIZE_MASK);
        if (id == BlockType.EMPTY_ID || id == BlockType.UNKNOWN_ID) {
            return null;
        }
        BlockType type = BlockType.getAssetMap().getAsset(id);
        if (type == null || type.isUnknown() || type.getId() == null || type.getId().isBlank()) {
            return null;
        }
        return type;
    }

    private static void enqueue(ArrayDeque<Vector3i> queue, HashSet<String> seen, int x, int y, int z) {
        if (seen.add(x + ":" + y + ":" + z)) {
            queue.add(new Vector3i(x, y, z));
        }
    }
}
