package com.jaredscarito.hymmo.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jaredscarito.hymmo.data.Config;
import com.jaredscarito.hymmo.data.Profile;
import com.jaredscarito.hymmo.data.SaveData;
import com.jaredscarito.hymmo.data.SkillState;
import com.jaredscarito.hymmo.skill.Blocks;
import com.jaredscarito.hymmo.skill.Skill;
import com.jaredscarito.hymmo.skill.SkillNodes;
import com.jaredscarito.hymmo.skill.SkillNodes.Effect;
import com.jaredscarito.hymmo.skill.SkillNodes.Node;
import com.jaredscarito.hymmo.skill.ToolKind;
import com.jaredscarito.hymmo.text.Texts;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

public final class SkillService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final SkillState EMPTY = new SkillState();

    private final JavaPlugin plugin;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "hymmo");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<UUID, Profile> profiles = new LinkedHashMap<>();
    private final Map<UUID, Long> abilityNotice = new LinkedHashMap<>();

    private Config config = Config.defaults();
    private boolean loaded;
    private boolean dirty;

    public SkillService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized void load() {
        try {
            Path directory = plugin.getDataDirectory();
            Files.createDirectories(directory);
            readConfig(directory);
            Path path = directory.resolve("skills.json");
            profiles.clear();
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    SaveData data = GSON.fromJson(reader, SaveData.class);
                    if (data != null && data.profiles != null) {
                        for (Profile profile : data.profiles) {
                            if (profile == null || profile.id == null) {
                                continue;
                            }
                            try {
                                profiles.put(UUID.fromString(profile.id), profile);
                            } catch (IllegalArgumentException exception) {
                                // Skip a broken id in the save.
                            }
                        }
                    }
                }
            }
            loaded = true;
            long now = System.currentTimeMillis();
            for (Profile profile : profiles.values()) {
                if (profile.skills == null || profile.id == null) {
                    continue;
                }
                UUID playerId;
                try {
                    playerId = UUID.fromString(profile.id);
                } catch (IllegalArgumentException exception) {
                    continue;
                }
                for (SkillState state : profile.skills) {
                    Skill skill = state == null ? null : Skill.parse(state.skill);
                    if (skill != null && state.abilityUntil > now) {
                        scheduler.schedule(() -> finishAbility(playerId, skill, state.abilityUntil), state.abilityUntil - now, TimeUnit.MILLISECONDS);
                    }
                }
            }
            scheduler.scheduleAtFixedRate(this::saveIfDirty, 30, 30, TimeUnit.SECONDS);
            if (Universe.get() != null) {
                for (PlayerRef online : Universe.get().getPlayers()) {
                    ensure(online);
                }
            }
            plugin.getLogger().atInfo().log("hyMMO data: %s (%d profiles)", directory, profiles.size());
        } catch (IOException exception) {
            plugin.getLogger().atSevere().withCause(exception).log("Failed to load hyMMO data");
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
        synchronized (this) {
            if (save()) {
                dirty = false;
            }
        }
    }

    public synchronized void remember(PlayerRef player) {
        if (loaded && player != null) {
            ensure(player);
        }
    }

    public synchronized void onBreak(PlayerRef player, Ref<EntityStore> ref, Store<EntityStore> store, World world, ItemStack tool, BlockType block, Vector3i pos, boolean creative) {
        if (!loaded || creative || player == null || block == null || pos == null || AreaBreak.nested()) {
            if (loaded && !creative && player != null && block != null && AreaBreak.nested()) {
                awardBreak(player, ref, store, tool, block);
            }
            return;
        }
        Skill skill = awardBreak(player, ref, store, tool, block);
        if (skill != Skill.WOODCUTTING && skill != Skill.EXCAVATION && skill != Skill.HERBALISM) {
            return;
        }
        Profile profile = ensure(player);
        if (!abilityOn(profile.state(skill))) {
            return;
        }
        int cap = (skill == Skill.WOODCUTTING ? config.treeCap : 8) + (int) Math.round(effect(profile, skill, Effect.TREE));
        List<Vector3i> extra = AreaBreak.collect(world, skill, pos.x, pos.y, pos.z, cap);
        AreaBreak.run(ref, tool, store, world, extra);
    }

    public synchronized void onMine(PlayerRef player, ItemStack tool, BlockType block, DamageBlockEvent event, boolean creative) {
        if (!loaded || creative || player == null || block == null || event == null) {
            return;
        }
        if (ToolKind.of(tool) != ToolKind.PICK || Blocks.skillFor(ToolKind.PICK, block) != Skill.MINING) {
            return;
        }
        Profile profile = ensure(player);
        if (!abilityOn(profile.state(Skill.MINING))) {
            return;
        }
        float speed = config.superBreakerSpeed + (float) effect(profile, Skill.MINING, Effect.SPEED);
        event.setDamage(event.getDamage() * speed);
    }

    public void onDamage(Store<EntityStore> store, Ref<EntityStore> victimRef, PlayerRef victim, Player victimEntity, Damage event) {
        if (!loaded || event == null || event.isCancelled() || event.getAmount() <= 0 || event.getCause() == null) {
            return;
        }
        DamageCause cause = event.getCause();
        if (victim != null && causeMatches(cause, DamageCause.FALL)) {
            synchronized (this) {
                rollFall(victim, victimEntity, event);
            }
            return;
        }
        if (causeMatches(cause, DamageCause.ENVIRONMENT) || causeMatches(cause, DamageCause.DROWNING)
                || causeMatches(cause, DamageCause.SUFFOCATION) || causeMatches(cause, DamageCause.OUT_OF_WORLD)
                || causeMatches(cause, DamageCause.COMMAND)) {
            return;
        }
        if (!(event.getSource() instanceof Damage.EntitySource source) || source.getRef() == null || !source.getRef().isValid()) {
            return;
        }
        PlayerRef attacker = store.getComponent(source.getRef(), PlayerRef.getComponentType());
        Player attackerEntity = store.getComponent(source.getRef(), Player.getComponentType());
        if (attacker == null || creative(attackerEntity) || attacker.getUuid().equals(victim == null ? null : victim.getUuid())) {
            return;
        }
        boolean projectile = source instanceof Damage.ProjectileSource || causeMatches(cause, DamageCause.PROJECTILE);
        Bleed bleed = null;
        synchronized (this) {
            if (victim != null && !creative(victimEntity) && dodge(victim, event)) {
                return;
            }
            Skill skill = projectile ? Skill.ARCHERY : ToolKind.of(hand(attackerEntity)).combatSkill();
            if (skill == null) {
                return;
            }
            Profile profile = ensure(attacker);
            SkillState state = profile.state(skill);
            float bonus = 1f + (float) Math.min(config.damageBonusCap, state.level / config.damageBonusDivisor);
            bonus += (float) effect(profile, skill, Effect.DAMAGE);
            if (abilityOn(state)) {
                bonus *= (float) config.abilityDamageMultiplier;
            }
            event.setAmount(event.getAmount() * bonus);
            int xp = Math.max(1, (int) Math.round(event.getAmount() * config.xpCombatPerDamage * (1 + effect(profile, skill, Effect.XP))));
            int levels = gain(profile, skill, xp);
            tellLevel(attacker, skill, state.level, levels);
            double bleedChance = Math.min(0.8, Math.min(config.bleedChanceCap, state.level / config.bleedDivisor) + effect(profile, skill, Effect.BLEED));
            if (skill == Skill.SWORDS && config.bleedTicks > 0 && roll(bleedChance)) {
                float amount = config.bleedDamageBase + state.level * config.bleedDamagePerLevel;
                bleed = new Bleed(store.getExternalData().getWorld(), source.getRef(), victimRef, amount);
            }
        }
        if (bleed != null) {
            bleed.schedule();
        }
    }

    public synchronized void overview(PlayerRef player) {
        if (!ready(player)) {
            return;
        }
        Profile profile = ensure(player);
        player.sendMessage(Texts.ok("hyMMO"));
        for (Skill skill : Skill.values()) {
            SkillState state = profile.state(skill);
            player.sendMessage(Message.join(
                    Texts.text(skill.display, skill.color),
                    Texts.info("  " + state.level + "   " + points(profile, skill) + " pts   " + status(profile, skill))));
        }
        player.sendMessage(Texts.info("/mmo opens the skill tree. Right-click a tool to start its ability. /mmo top <skill>"));
    }

    public synchronized void inspect(PlayerRef player, Skill skill) {
        if (!ready(player)) {
            return;
        }
        Profile profile = ensure(player);
        SkillState state = profile.state(skill);
        player.sendMessage(Message.join(Texts.text(skill.display, skill.color), Texts.info(" level " + state.level)));
        if (state.level >= config.maxLevel) {
            player.sendMessage(Texts.info("This skill is at the level cap."));
        } else {
            player.sendMessage(Texts.info("XP " + state.xp + " / " + xpToNext(state.level)));
        }
        int unspent = points(profile, skill);
        player.sendMessage(Texts.info(unspent + (unspent == 1 ? " point" : " points") + " to spend. /mmo opens the tree."));
        if (!skill.active) {
            int reduction = (int) Math.round(100 * rollReduction(profile, state));
            int dodge = (int) Math.round(100 * dodgeChance(profile, state));
            player.sendMessage(Texts.info("Roll reduces falls by " + reduction + "%. Dodge chance is " + dodge + "%."));
            return;
        }
        int drops = (int) Math.round(100 * dropChance(profile, skill, state, false));
        player.sendMessage(Texts.info(skill.ability + " " + status(profile, skill) + ". Double drops " + drops + "%."));
        if (state.level < config.abilityUnlockLevel) {
            player.sendMessage(Texts.info(skill.ability + " unlocks at level " + config.abilityUnlockLevel + "."));
        } else {
            player.sendMessage(Texts.info("It lasts " + abilitySeconds(profile, skill, state.level) + " seconds. Right-click the tool to start it."));
        }
    }

    public synchronized SkillPanel panel(PlayerRef player, Skill skill) {
        if (!loaded || player == null) {
            return new SkillPanel(skill, 0, 0, xpToNext(0), 0, false, "loading", java.util.List.of());
        }
        Profile profile = ensure(player);
        SkillState state = profile.state(skill);
        java.util.List<SkillPanel.NodeRow> rows = new java.util.ArrayList<>();
        for (Node node : SkillNodes.of(skill)) {
            rows.add(row(profile, state, node));
        }
        return new SkillPanel(skill, state.level, state.xp, xpToNext(state.level), points(profile, skill), state.level >= config.maxLevel, status(profile, skill), rows);
    }

    public synchronized String purchase(PlayerRef player, String nodeId) {
        if (!ready(player)) {
            return "hyMMO is still loading.";
        }
        Node node = SkillNodes.byId(nodeId);
        if (node == null) {
            return "That rank is not on the tree.";
        }
        Profile profile = ensure(player);
        SkillState state = profile.state(node.skill());
        if (state.owns(node.id())) {
            return node.name() + " is already learned.";
        }
        if (node.requires() != null && !state.owns(node.requires())) {
            Node parent = SkillNodes.byId(node.requires());
            return "Learn " + (parent == null ? "the previous rank" : parent.name()) + " first.";
        }
        if (state.level < node.level()) {
            return node.name() + " needs " + node.skill().display + " level " + node.level() + ".";
        }
        if (points(profile, node.skill()) < node.cost()) {
            return "You need " + node.cost() + (node.cost() == 1 ? " point." : " points.");
        }
        state.grant(node.id());
        dirty = true;
        return "Learned " + node.name() + ".";
    }

    public synchronized String respec(PlayerRef player, Skill skill) {
        if (!ready(player)) {
            return "hyMMO is still loading.";
        }
        SkillState state = ensure(player).state(skill);
        if (state.nodes == null || state.nodes.isEmpty()) {
            return skill.display + " has nothing to refund.";
        }
        state.refund();
        dirty = true;
        return skill.display + " points were refunded.";
    }

    public synchronized void activate(PlayerRef player, ItemStack hand) {
        activate(player, ToolKind.of(hand), true);
    }

    public synchronized void activate(PlayerRef player, Item item) {
        activate(player, ToolKind.of(item), false);
    }

    public synchronized int unlockLevel() {
        return config.abilityUnlockLevel;
    }

    private void activate(PlayerRef player, ToolKind tool, boolean complain) {
        if (!ready(player)) {
            return;
        }
        Profile profile = ensure(player);
        List<Skill> matched = skillsFor(tool);
        if (matched.isEmpty()) {
            if (complain) {
                player.sendMessage(Texts.err("Hold a pickaxe, axe, shovel, hoe, sword, bow, or an empty hand."));
            }
            return;
        }
        boolean repeat = recentlyTold(player);
        boolean spoke = false;
        long now = System.currentTimeMillis();
        for (Skill skill : matched) {
            SkillState state = profile.state(skill);
            if (state.level < config.abilityUnlockLevel) {
                if (!repeat) {
                    player.sendMessage(Texts.err(skill.ability + " unlocks at " + skill.display + " level " + config.abilityUnlockLevel + "."));
                    spoke = true;
                }
                continue;
            }
            if (abilityOn(state)) {
                if (!repeat) {
                    player.sendMessage(Texts.info(skill.ability + " is already active."));
                    spoke = true;
                }
                continue;
            }
            if (state.cooldownUntil > now) {
                if (!repeat) {
                    long seconds = Math.max(1, (state.cooldownUntil - now + 999) / 1000);
                    player.sendMessage(Texts.err(skill.ability + " is ready in " + seconds + " seconds."));
                    spoke = true;
                }
                continue;
            }
            int duration = abilitySeconds(profile, skill, state.level);
            long ends = now + duration * 1000L;
            int factor = Math.max(1, config.cooldownFactor + (int) Math.round(effect(profile, skill, Effect.COOLDOWN)));
            state.abilityUntil = ends;
            state.cooldownUntil = ends + duration * (long) factor * 1000L;
            dirty = true;
            player.sendMessage(Message.join(Texts.text(skill.ability, skill.color), Texts.ok(" is active for " + duration + " seconds.")));
            spoke = true;
            scheduler.schedule(() -> finishAbility(player.getUuid(), skill, ends), duration, TimeUnit.SECONDS);
        }
        if (spoke) {
            abilityNotice.put(player.getUuid(), now);
        }
    }

    private boolean recentlyTold(PlayerRef player) {
        Long last = abilityNotice.get(player.getUuid());
        return last != null && System.currentTimeMillis() - last < 4000;
    }

    public synchronized void top(PlayerRef player, Skill skill) {
        if (!ready(player)) {
            return;
        }
        List<Profile> ranked = new ArrayList<>();
        for (Profile profile : profiles.values()) {
            SkillState state = peek(profile, skill);
            if (state.level > 0 || state.xp > 0) {
                ranked.add(profile);
            }
        }
        ranked.sort(Comparator.comparingInt((Profile profile) -> peek(profile, skill).level).reversed()
                .thenComparing(Comparator.comparingInt((Profile profile) -> peek(profile, skill).xp).reversed()));
        player.sendMessage(Message.join(Texts.text(skill.display, skill.color), Texts.info(" leaders")));
        if (ranked.isEmpty()) {
            player.sendMessage(Texts.info("Nobody has earned this skill yet."));
            return;
        }
        int place = 1;
        boolean shown = false;
        for (Profile profile : ranked) {
            if (place <= 8) {
                SkillState state = peek(profile, skill);
                player.sendMessage(Texts.info(place + ". " + nameOf(profile) + "  " + state.level));
            }
            if (player.getUuid().toString().equals(profile.id)) {
                shown = true;
                if (place > 8) {
                    player.sendMessage(Texts.info("You are " + place + "."));
                }
            }
            place++;
        }
        if (!shown) {
            player.sendMessage(Texts.info("You are unranked."));
        }
    }

    public synchronized Message adminSet(String name, String skillName, int level) {
        Profile profile = named(name);
        Skill skill = Skill.parse(skillName);
        if (profile == null) {
            return Texts.err("No hyMMO profile for that player.");
        }
        if (skill == null) {
            return Texts.err("Unknown skill. Use mining, woodcutting, excavation, herbalism, swords, axes, unarmed, archery, or acrobatics.");
        }
        if (level < 0 || level > config.maxLevel) {
            return Texts.err("Level must be from 0 to " + config.maxLevel + ".");
        }
        SkillState state = profile.state(skill);
        state.level = level;
        state.xp = 0;
        dirty = true;
        return Texts.ok(nameOf(profile) + " is now " + skill.display + " level " + level + ".");
    }

    public synchronized Message adminAdd(String name, String skillName, int amount) {
        Profile profile = named(name);
        Skill skill = Skill.parse(skillName);
        if (profile == null) {
            return Texts.err("No hyMMO profile for that player.");
        }
        if (skill == null) {
            return Texts.err("Unknown skill. Use mining, woodcutting, excavation, herbalism, swords, axes, unarmed, archery, or acrobatics.");
        }
        if (amount <= 0) {
            return Texts.err("XP must be above zero.");
        }
        SkillState state = profile.state(skill);
        int levels = gain(profile, skill, amount);
        PlayerRef online = online(profile);
        if (online != null) {
            tellLevel(online, skill, state.level, levels);
        }
        return Texts.ok("Gave " + amount + " " + skill.display + " XP to " + nameOf(profile) + ". They are level " + state.level + ".");
    }

    public synchronized Message reload() {
        try {
            readConfig(plugin.getDataDirectory());
            return Texts.ok("hyMMO config reloaded.");
        } catch (IOException exception) {
            plugin.getLogger().atSevere().withCause(exception).log("Failed to reload hyMMO config");
            return Texts.err("Could not reload the hyMMO config.");
        }
    }

    private Skill awardBreak(PlayerRef player, Ref<EntityStore> ref, Store<EntityStore> store, ItemStack tool, BlockType block) {
        Skill skill = Blocks.skillFor(ToolKind.of(tool), block);
        if (skill == null) {
            return null;
        }
        Profile profile = ensure(player);
        SkillState state = profile.state(skill);
        int amount = xpFor(profile, skill, block.getId());
        int levels = gain(profile, skill, amount);
        tellLevel(player, skill, state.level, levels);
        boolean guarantee = abilityOn(state) && (skill == Skill.MINING || skill == Skill.HERBALISM);
        if (roll(dropChance(profile, skill, state, guarantee))) {
            bonusDrop(block, ref, store);
        }
        return skill;
    }

    private void rollFall(PlayerRef victim, Player victimEntity, Damage event) {
        if (creative(victimEntity)) {
            return;
        }
        Profile profile = ensure(victim);
        SkillState state = profile.state(Skill.ACROBATICS);
        float original = event.getAmount();
        float reduction = (float) rollReduction(profile, state);
        event.setAmount(Math.max(0f, original * (1f - reduction)));
        int levels = gain(profile, Skill.ACROBATICS, Math.max(1, (int) Math.round(original * config.xpFallPerDamage)));
        tellLevel(victim, Skill.ACROBATICS, state.level, levels);
    }

    private boolean dodge(PlayerRef victim, Damage event) {
        Profile profile = ensure(victim);
        SkillState state = profile.state(Skill.ACROBATICS);
        if (!roll(dodgeChance(profile, state))) {
            return false;
        }
        event.setCancelled(true);
        victim.sendMessage(Texts.info("Acrobatics dodged the hit."));
        int levels = gain(profile, Skill.ACROBATICS, config.xpDodge);
        tellLevel(victim, Skill.ACROBATICS, state.level, levels);
        return true;
    }

    private void finishAbility(UUID playerId, Skill skill, long endsAt) {
        synchronized (this) {
            Profile profile = profiles.get(playerId);
            if (profile == null || profile.state(skill).abilityUntil != endsAt) {
                return;
            }
        }
        PlayerRef online = online(playerId);
        if (online != null) {
            online.sendMessage(Message.join(Texts.text(skill.ability, skill.color), Texts.info(" wore off.")));
        }
    }

    private int xpFor(Profile profile, Skill skill, String id) {
        int base = switch (skill) {
            case MINING -> Blocks.isOre(id) ? config.xpOre : config.xpStone;
            case WOODCUTTING -> Blocks.isLeaf(id) ? config.xpLeaf : config.xpLog;
            case EXCAVATION -> config.xpDig;
            case HERBALISM -> config.xpPlant;
            default -> 0;
        };
        return Math.max(0, (int) Math.round(base * (1 + effect(profile, skill, Effect.XP))));
    }

    private int points(Profile profile, Skill skill) {
        SkillState state = profile.state(skill);
        return Math.max(0, state.level - spent(state, skill));
    }

    private int spent(SkillState state, Skill skill) {
        int total = 0;
        for (Node node : SkillNodes.of(skill)) {
            if (state.owns(node.id())) {
                total += node.cost();
            }
        }
        return total;
    }

    private double effect(Profile profile, Skill skill, Effect effect) {
        SkillState state = peek(profile, skill);
        double total = 0;
        for (Node node : SkillNodes.of(skill)) {
            if (node.effect() == effect && state.owns(node.id())) {
                total += node.amount();
            }
        }
        return total;
    }

    private SkillPanel.NodeRow row(Profile profile, SkillState state, Node node) {
        if (state.owns(node.id())) {
            return new SkillPanel.NodeRow(node.id(), node.branch(), node.name(), "Learned", node.detail(), "#55FF55");
        }
        String gate = gate(profile, state, node);
        if (gate == null) {
            String cost = node.cost() == 1 ? "Spend 1 point" : "Spend " + node.cost() + " points";
            return new SkillPanel.NodeRow(node.id(), node.branch(), node.name(), cost, node.detail(), "#E8A93B");
        }
        return new SkillPanel.NodeRow(node.id(), node.branch(), node.name(), gate, node.detail(), "#8ea0b3");
    }

    private String gate(Profile profile, SkillState state, Node node) {
        if (node.requires() != null && !state.owns(node.requires())) {
            Node parent = SkillNodes.byId(node.requires());
            return "Learn " + (parent == null ? "the previous rank" : parent.name()) + " first";
        }
        if (state.level < node.level()) {
            return "Needs level " + node.level();
        }
        if (points(profile, node.skill()) < node.cost()) {
            return "Needs " + node.cost() + (node.cost() == 1 ? " point" : " points");
        }
        return null;
    }

    private double rollReduction(Profile profile, SkillState state) {
        return Math.min(0.8, Math.min(config.rollCap, state.level / config.rollDivisor) + effect(profile, Skill.ACROBATICS, Effect.ROLL));
    }

    private double dodgeChance(Profile profile, SkillState state) {
        return Math.min(0.5, Math.min(config.dodgeCap, state.level / config.dodgeDivisor) + effect(profile, Skill.ACROBATICS, Effect.DODGE));
    }

    private int gain(Profile profile, Skill skill, int amount) {
        if (amount <= 0) {
            return 0;
        }
        SkillState state = profile.state(skill);
        if (state.level >= config.maxLevel) {
            return 0;
        }
        state.xp += amount;
        int gained = 0;
        while (state.level < config.maxLevel && state.xp >= xpToNext(state.level)) {
            state.xp -= xpToNext(state.level);
            state.level++;
            gained++;
        }
        if (state.level >= config.maxLevel) {
            state.xp = 0;
        }
        dirty = true;
        return gained;
    }

    private void tellLevel(PlayerRef player, Skill skill, int level, int gained) {
        if (player != null && gained > 0) {
            player.sendMessage(Message.join(Texts.text(skill.display, skill.color), Texts.ok(" reached level " + level + ".")));
        }
    }

    private void bonusDrop(BlockType block, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || store == null) {
            return;
        }
        BlockGathering gathering = block.getGathering();
        String itemId = null;
        String dropList = null;
        int quantity = 1;
        if (gathering != null && gathering.getBreaking() != null) {
            itemId = gathering.getBreaking().getItemId();
            dropList = gathering.getBreaking().getDropListId();
            if (gathering.getBreaking().getQuantity() > 0) {
                quantity = gathering.getBreaking().getQuantity();
            }
        }
        if (itemId == null && dropList == null && gathering != null && gathering.getSoft() != null) {
            itemId = gathering.getSoft().getItemId();
            dropList = gathering.getSoft().getDropListId();
        }
        if (itemId == null && dropList == null && gathering != null && gathering.getHarvest() != null) {
            itemId = gathering.getHarvest().getItemId();
            dropList = gathering.getHarvest().getDropListId();
        }
        List<ItemStack> drops = BlockHarvestUtils.getDrops(block, quantity, itemId, dropList);
        if (drops == null) {
            return;
        }
        for (ItemStack stack : drops) {
            if (!ItemStack.isEmpty(stack)) {
                Player.giveItem(stack, ref, store);
            }
        }
    }

    private List<Skill> skillsFor(ToolKind tool) {
        List<Skill> skills = new ArrayList<>();
        switch (tool) {
            case PICK -> skills.add(Skill.MINING);
            case AXE -> {
                skills.add(Skill.WOODCUTTING);
                skills.add(Skill.AXES);
            }
            case SHOVEL -> skills.add(Skill.EXCAVATION);
            case HOE -> skills.add(Skill.HERBALISM);
            case SWORD -> skills.add(Skill.SWORDS);
            case BOW -> skills.add(Skill.ARCHERY);
            case HAND -> skills.add(Skill.UNARMED);
            default -> {
            }
        }
        return skills;
    }

    private String status(Profile profile, Skill skill) {
        if (!skill.active) {
            return "passive";
        }
        SkillState state = profile.state(skill);
        if (state.level < config.abilityUnlockLevel) {
            return "locked";
        }
        if (abilityOn(state)) {
            return "active";
        }
        if (state.cooldownUntil > System.currentTimeMillis()) {
            return "cooldown";
        }
        return "ready";
    }

    private boolean abilityOn(SkillState state) {
        return state.abilityUntil > System.currentTimeMillis();
    }

    private int abilitySeconds(Profile profile, Skill skill, int level) {
        int seconds = config.baseAbilitySeconds + Math.max(0, level) / config.levelsPerAbilitySecond;
        seconds = Math.min(config.maxAbilitySeconds, Math.max(config.baseAbilitySeconds, seconds));
        return seconds + (int) Math.round(effect(profile, skill, Effect.ABILITY));
    }

    private int xpToNext(int level) {
        return config.baseXp + Math.max(0, level) * config.xpPerLevel;
    }

    private double dropChance(Profile profile, Skill skill, SkillState state, boolean guarantee) {
        if (guarantee) {
            return 1;
        }
        double chance = state.level <= 0 ? 0 : Math.min(config.doubleDropCap, state.level / config.doubleDropDivisor);
        return Math.min(1, chance + effect(profile, skill, Effect.DOUBLE));
    }

    private boolean roll(double chance) {
        return chance > 0 && ThreadLocalRandom.current().nextDouble() < chance;
    }

    private boolean ready(PlayerRef player) {
        if (!loaded) {
            if (player != null) {
                player.sendMessage(Texts.err("hyMMO is still loading."));
            }
            return false;
        }
        return player != null;
    }

    private Profile ensure(PlayerRef player) {
        Profile profile = profiles.get(player.getUuid());
        if (profile == null) {
            profile = new Profile();
            profile.id = player.getUuid().toString();
            profiles.put(player.getUuid(), profile);
            dirty = true;
        }
        if (player.getUsername() != null && !player.getUsername().equals(profile.name)) {
            profile.name = player.getUsername();
            dirty = true;
        }
        return profile;
    }

    private Profile named(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (Profile profile : profiles.values()) {
            if (name.equalsIgnoreCase(profile.name)) {
                return profile;
            }
        }
        if (Universe.get() == null) {
            return null;
        }
        for (PlayerRef online : Universe.get().getPlayers()) {
            if (name.equalsIgnoreCase(online.getUsername())) {
                return ensure(online);
            }
        }
        return null;
    }

    private static SkillState peek(Profile profile, Skill skill) {
        if (profile.skills == null) {
            return EMPTY;
        }
        for (SkillState state : profile.skills) {
            if (state != null && skill.name().equals(state.skill)) {
                return state;
            }
        }
        return EMPTY;
    }

    private static String nameOf(Profile profile) {
        return profile.name == null || profile.name.isBlank() ? "Unknown" : profile.name;
    }

    private PlayerRef online(Profile profile) {
        try {
            return online(UUID.fromString(profile.id));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private PlayerRef online(UUID playerId) {
        return Universe.get() == null ? null : Universe.get().getPlayer(playerId);
    }

    private static ItemStack hand(Player player) {
        if (player == null || player.getInventory() == null) {
            return ItemStack.EMPTY;
        }
        Inventory inventory = player.getInventory();
        ItemStack stack = inventory.usingToolsItem() ? inventory.getActiveToolItem() : inventory.getActiveHotbarItem();
        return stack == null ? ItemStack.EMPTY : stack;
    }

    private static boolean creative(Player player) {
        return player != null && player.getGameMode() == GameMode.Creative;
    }

    private static boolean causeMatches(DamageCause cause, DamageCause expected) {
        return cause != null && expected != null && cause.isOrInherits(expected);
    }

    private synchronized void saveIfDirty() {
        if (dirty && save()) {
            dirty = false;
        }
    }

    private boolean save() {
        if (!loaded) {
            return false;
        }
        try {
            Path directory = plugin.getDataDirectory();
            Files.createDirectories(directory);
            SaveData data = new SaveData();
            data.profiles.addAll(profiles.values());
            writeJson(directory.resolve("skills.json"), data);
            return true;
        } catch (IOException exception) {
            plugin.getLogger().atSevere().withCause(exception).log("Failed to save hyMMO data");
            return false;
        }
    }

    private void readConfig(Path directory) throws IOException {
        Path configPath = directory.resolve("config.json");
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                JsonObject file = GSON.fromJson(reader, JsonObject.class);
                if (file != null) {
                    JsonObject defaults = GSON.toJsonTree(Config.defaults()).getAsJsonObject();
                    boolean added = false;
                    for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
                        if (!file.has(entry.getKey())) {
                            file.add(entry.getKey(), entry.getValue());
                            added = true;
                        }
                    }
                    Config read = GSON.fromJson(file, Config.class);
                    if (read != null) {
                        config = read;
                    }
                    config.applyDefaults();
                    if (added) {
                        writeJson(configPath, config);
                    }
                }
            }
            return;
        }
        config.applyDefaults();
        writeJson(configPath, config);
    }

    private static void writeJson(Path path, Object value) throws IOException {
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            GSON.toJson(value, writer);
        }
        Files.move(temp, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
    }

    private final class Bleed {
        private final World world;
        private final Ref<EntityStore> attacker;
        private final Ref<EntityStore> victim;
        private final float amount;

        private Bleed(World world, Ref<EntityStore> attacker, Ref<EntityStore> victim, float amount) {
            this.world = world;
            this.attacker = attacker;
            this.victim = victim;
            this.amount = amount;
        }

        private void schedule() {
            if (world == null || attacker == null || victim == null || DamageCause.ENVIRONMENT == null || amount <= 0) {
                return;
            }
            for (int tick = 1; tick <= config.bleedTicks; tick++) {
                scheduler.schedule(() -> world.execute(this::hit), tick, TimeUnit.SECONDS);
            }
        }

        private void hit() {
            if (!attacker.isValid() || !victim.isValid() || DamageCause.ENVIRONMENT == null) {
                return;
            }
            Damage damage = new Damage(new Damage.EntitySource(attacker), DamageCause.ENVIRONMENT, amount);
            DamageSystems.executeDamage(victim, victim.getStore(), damage);
        }
    }
}
