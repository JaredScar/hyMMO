package com.jaredscarito.hymmo.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jaredscarito.hymmo.service.SkillPanel;
import com.jaredscarito.hymmo.service.SkillService;
import com.jaredscarito.hymmo.skill.Skill;
import com.jaredscarito.hymmo.skill.SkillNodes;
import com.jaredscarito.hymmo.text.Texts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SkillTreePage extends InteractiveCustomUIPage<SkillTreePage.SkillPageEvent> {
    private static final String PAGE = "Pages/HyMmoPage.ui";

    private final PlayerRef player;
    private final SkillService skills;
    private Skill selected;
    private String notice;

    public SkillTreePage(PlayerRef player, SkillService skills, Skill selected) {
        super(player, CustomPageLifetime.CanDismiss, SkillPageEvent.CODEC);
        this.player = player;
        this.skills = skills;
        this.selected = selected == null ? Skill.MINING : selected;
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder commands, UIEventBuilder events, Store<EntityStore> store) {
        commands.append(PAGE);
        Skill[] values = Skill.values();
        for (int index = 0; index < values.length; index++) {
            Skill skill = values[index];
            SkillPanel panel = skills.panel(player, skill);
            commands.appendInline("#Skills", skillButton(skill == selected));
            Message summary = Message.join(
                    Message.raw(skill.display).color(skill.color).bold(true),
                    Texts.text("\nLv " + panel.level, skill == selected ? "#ffffff" : "#8ea0b3"));
            if (panel.points > 0) {
                summary = Message.join(summary, Texts.text("  ·  " + panel.points + " to spend", "#E8A93B"));
            }
            commands.set("#Skills[" + index + "].TextSpans", summary);
            events.addEventBinding(CustomUIEventBindingType.Activating, "#Skills[" + index + "]", EventData.of("Skill", skill.name()));
        }

        SkillPanel panel = skills.panel(player, selected);
        commands.set("#SkillName.TextSpans", Texts.text(selected.display, selected.color));
        Message meta = Texts.info("Level " + panel.level);
        if (panel.points > 0) {
            String points = panel.points == 1 ? "1 point to spend" : panel.points + " points to spend";
            meta = Message.join(meta, Texts.text("    " + points, "#E8A93B"));
        }
        commands.set("#SkillMeta.TextSpans", meta);
        float progress = panel.capped || panel.next <= 0 ? 1f : Math.min(1f, panel.xp / (float) panel.next);
        commands.set("#XpBar.Value", progress);
        commands.set("#XpLabel.TextSpans", Texts.info(panel.capped ? "Level cap" : panel.xp + " / " + panel.next + " XP"));
        commands.set("#Ability.TextSpans", Texts.text(ability(panel), "#d5deea"));
        boolean showNotice = notice != null && !notice.isBlank();
        commands.set("#Notice.Visible", showNotice);
        if (showNotice) {
            commands.set("#Notice.TextSpans", Texts.text(notice, "#e8a93b"));
        }
        commands.set("#TreeHint.TextSpans", Texts.text("Click a gold rank to spend a point. Green is learned. Gray is locked.", "#9eb0c3"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#Respec", EventData.of("Action", "respec"));
        commands.set("#RespecHint.TextSpans", Texts.text(
                "Refunds the ranks bought on " + selected.display + " and gives the points back. Level and XP stay.",
                "#9eb0c3"));

        Map<String, List<SkillPanel.NodeRow>> paths = new LinkedHashMap<>();
        for (SkillPanel.NodeRow node : panel.nodes) {
            paths.computeIfAbsent(node.branch, key -> new ArrayList<>()).add(node);
        }
        List<List<SkillPanel.NodeRow>> columns = new ArrayList<>(paths.values());
        fillPath(commands, events, "#LeftBranch", "#LeftPath", columns.isEmpty() ? List.of() : columns.get(0));
        fillPath(commands, events, "#RightBranch", "#RightPath", columns.size() < 2 ? List.of() : columns.get(1));
    }

    private void fillPath(UICommandBuilder commands, UIEventBuilder events, String title, String list, List<SkillPanel.NodeRow> nodes) {
        commands.set(title + ".TextSpans", Texts.text(nodes.isEmpty() ? "" : nodes.get(0).branch, selected.color));
        for (int index = 0; index < nodes.size(); index++) {
            SkillPanel.NodeRow node = nodes.get(index);
            commands.appendInline(list, nodeButton(node.color));
            commands.set(list + "[" + index + "].TextSpans", Message.join(
                    Message.raw(node.name).color(node.color).bold(true),
                    Message.raw("\n" + node.status).color(node.color),
                    Message.raw("\n" + node.detail).color("#c5d0dc")));
            events.addEventBinding(CustomUIEventBindingType.Activating, list + "[" + index + "]", EventData.of("Node", node.id));
        }
    }

    private static String skillButton(boolean selected) {
        String background = selected ? "#2c4a62" : "#101820(0.45)";
        String hover = selected ? "#3a6280" : "#1c2c3c";
        return "TextButton {"
                + " Padding: (Horizontal: 12, Vertical: 8);"
                + " Anchor: (Bottom: 4, Height: 50);"
                + " Style: ("
                + " Default: (LabelStyle: (FontSize: 14, Wrap: true, HorizontalAlignment: Start, VerticalAlignment: Center), Background: " + background + "),"
                + " Hovered: (LabelStyle: (FontSize: 14, Wrap: true, HorizontalAlignment: Start, VerticalAlignment: Center), Background: " + hover + ")"
                + " );"
                + "}";
    }

    private String ability(SkillPanel panel) {
        if (!selected.active) {
            return selected.about;
        }
        String state = switch (panel.status) {
            case "locked" -> "Unlocks at level " + skills.unlockLevel() + ".";
            case "active" -> "Active right now.";
            case "cooldown" -> "Cooling down.";
            default -> "Ready. Right-click the tool to start it.";
        };
        return selected.about + "\n" + state;
    }

    private static String nodeButton(String tone) {
        String background = "#121a24";
        String hover = "#1d2c3c";
        if ("#55FF55".equals(tone)) {
            background = "#13241a";
            hover = "#1c3326";
        } else if ("#E8A93B".equals(tone)) {
            background = "#2a2412";
            hover = "#3d3318";
        }
        return "TextButton {"
                + " Padding: (Horizontal: 12, Vertical: 8);"
                + " Anchor: (Bottom: 8, Height: 86);"
                + " Style: ("
                + " Default: (LabelStyle: (FontSize: 14, Wrap: true, HorizontalAlignment: Start, VerticalAlignment: Center), Background: " + background + "),"
                + " Hovered: (LabelStyle: (FontSize: 14, Wrap: true, HorizontalAlignment: Start, VerticalAlignment: Center), Background: " + hover + ")"
                + " );"
                + "}";
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, SkillPageEvent data) {
        if (data == null) {
            return;
        }
        if (data.skill != null) {
            Skill parsed = Skill.parse(data.skill);
            if (parsed != null) {
                selected = parsed;
                notice = null;
            }
        } else if (data.node != null) {
            SkillNodes.Node node = SkillNodes.byId(data.node);
            if (node != null) {
                selected = node.skill();
            }
            notice = skills.purchase(player, data.node);
        } else if ("respec".equals(data.action)) {
            notice = skills.respec(player, selected);
        }
        rebuild();
    }

    public static final class SkillPageEvent {
        public static final BuilderCodec<SkillPageEvent> CODEC = codec();
        public String skill;
        public String node;
        public String action;

        private static BuilderCodec<SkillPageEvent> codec() {
            BuilderCodec.Builder<SkillPageEvent> builder = BuilderCodec.builder(SkillPageEvent.class, SkillPageEvent::new);
            builder.append(new KeyedCodec<>("Skill", Codec.STRING), (event, value) -> event.skill = value, event -> event.skill).add();
            builder.append(new KeyedCodec<>("Node", Codec.STRING), (event, value) -> event.node = value, event -> event.node).add();
            builder.append(new KeyedCodec<>("Action", Codec.STRING), (event, value) -> event.action = value, event -> event.action).add();
            return builder.build();
        }
    }
}
