package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry.Activity;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry.Alertness;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry.Diet;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry.FeedingSpeed;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry.Habitat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// 钓鱼手册界面：原版书与笔风格。
//
// 左页：书名 + 6 行筛选（水域 / 食性 / 活动 / 警惕性 / 进食速度 / 钓获状态，点击切换，
// 同类别多选取并集、跨类别取交集、不选即不限）+ 战利品列表（滚轮滚动）。
// 右页：选中条目的图标与各项属性。
public class FishingHandbookScreen extends Screen {

    private static final ResourceLocation BOOK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    flavor_immersed_daily_argo.MODID, "textures/gui/fishing_handbook.png");
// 虚拟书尺寸：书纹 256×192 放大 1.5 倍后的布局空间（所有坐标都在这个空间里设计）
    private static final int BOOK_W = 384;
    private static final int BOOK_H = 288;
// 设计缩放上限；窗口太小时整体等比缩小，保证整本书可见且居中
    private static final float DESIGN_SCALE = 1.5F;
    private static final int FILTER_ROW_H = 12;
    private static final int LIST_ROW_H = 16;
// 实体图标缩放基准：溺尸身高，所有实体条目统一按此比例缩放以保持相对大小
    private static final float ENTITY_ICON_REF_HEIGHT = 1.95F;

// 布局坐标（init 中按虚拟书空间计算）
// 书本左上角在变换空间中的坐标（书居中于原点）
    private int bookX;
    private int bookY;
// 整体渲染缩放，让 384×288 的书适配当前窗口
    private float uiScale;
    private int pageTop;
    private int pageBottom;
    private int leftX;
    private int leftRight;
    private int rightX;
    private int rightRight;
    private int rightCenterX;

// 筛选状态
    private final Set<Habitat> habitatSel = EnumSet.noneOf(Habitat.class);
    private final Set<Diet> dietSel = EnumSet.noneOf(Diet.class);
    private final Set<Activity> activitySel = EnumSet.noneOf(Activity.class);
    private final Set<Alertness> alertnessSel = EnumSet.noneOf(Alertness.class);
    private final Set<FeedingSpeed> speedSel = EnumSet.noneOf(FeedingSpeed.class);
    private final Set<CaughtFilter> caughtSel = EnumSet.noneOf(CaughtFilter.class);

// 可点击的筛选选项片
    private static final int CAT_HABITAT = 0;
    private static final int CAT_DIET = 1;
    private static final int CAT_ACTIVITY = 2;
    private static final int CAT_ALERTNESS = 3;
    private static final int CAT_FEEDING = 4;
    private static final int CAT_CAUGHT = 5;

// 「钓获状态」筛选：不是条目自带的属性，而是拿条目的记账键去查本玩家的钓获记录
    private enum CaughtFilter { CAUGHT, UNCAUGHT }

    private record Chip(int x, int y, int w, int h, int category, Enum<?> value, String text) {}

    private final List<Chip> chips = new ArrayList<>();
// 每个筛选类别的标签文字（下标即 CAT_* 常量）
    private final String[] filterLabels = new String[6];

// 列表
    private int listTopY;
    private int listBottomY;
    private int listScroll;
    private FishingHandbookEntry selected;
    private List<FishingHandbookEntry> filtered = List.of();
    private List<FishingHandbookEntry> visible = List.of();
// 纯实体条目的预览实体缓存（避免每帧重复创建实体）
    private final Map<FishingHandbookEntry, Optional<Entity>> previewCache = new HashMap<>();

    public FishingHandbookScreen() {
        super(Component.translatable("book.flavor_immersed_daily_argo.title"));
    }

    @Override
    protected void init() {
        super.init();
        this.uiScale = computeScale();
        this.previewCache.clear();
        this.bookX = -BOOK_W / 2;
        this.bookY = -BOOK_H / 2;
        this.pageTop = bookY + 21;
        this.pageBottom = bookY + 271;
        this.leftX = bookX + 21;
        this.leftRight = bookX + 168;
        this.rightX = bookX + 217;
        this.rightRight = bookX + 362;
        this.rightCenterX = rightX + 73;

        buildChips();
        this.listTopY = pageTop + 18 + 6 * FILTER_ROW_H + 4;
        this.listBottomY = pageBottom - 2;
        rebuildList();
    }

// 计算整体缩放：书本最多占窗口宽度的 96%、高度的 92%，且不超过设计缩放
    private float computeScale() {
        float byWidth = this.width * 0.96F / BOOK_W;
        float byHeight = this.height * 0.92F / BOOK_H;
        float f = Math.min(byWidth, byHeight);
        return Math.max(0.5F, Math.min(f, DESIGN_SCALE));
    }

// 屏幕坐标 → 虚拟书坐标（渲染时先平移到屏幕中心再整体缩放）
    private double toBookX(double mx) {
        return (mx - this.width / 2.0) / this.uiScale;
    }

    private double toBookY(double my) {
        return (my - this.height / 2.0) / this.uiScale;
    }

// 筛选片布局

    private void buildChips() {
        chips.clear();
        int y = pageTop + 18;
        addFilterRow(y, "book.flavor_immersed_daily_argo.filter.habitat", CAT_HABITAT,
                new String[]{"book.flavor_immersed_daily_argo.habitat.fresh", "book.flavor_immersed_daily_argo.habitat.ocean"},
                new Habitat[]{Habitat.FRESH, Habitat.OCEAN});
        y += FILTER_ROW_H;
        addFilterRow(y, "book.flavor_immersed_daily_argo.filter.diet", CAT_DIET,
                new String[]{"book.flavor_immersed_daily_argo.diet.omnivore", "book.flavor_immersed_daily_argo.diet.herbivore",
                        "book.flavor_immersed_daily_argo.diet.carnivore", "book.flavor_immersed_daily_argo.diet.scavenger",
                        "book.flavor_immersed_daily_argo.diet.live"},
                Diet.values());
        y += FILTER_ROW_H;
        addFilterRow(y, "book.flavor_immersed_daily_argo.filter.activity", CAT_ACTIVITY,
                new String[]{"book.flavor_immersed_daily_argo.activity.all", "book.flavor_immersed_daily_argo.activity.day",
                        "book.flavor_immersed_daily_argo.activity.night", "book.flavor_immersed_daily_argo.activity.sunny",
                        "book.flavor_immersed_daily_argo.activity.rain"},
                Activity.values());
        y += FILTER_ROW_H;
        addFilterRow(y, "book.flavor_immersed_daily_argo.filter.alertness", CAT_ALERTNESS,
                new String[]{"book.flavor_immersed_daily_argo.alertness.low", "book.flavor_immersed_daily_argo.alertness.medium",
                        "book.flavor_immersed_daily_argo.alertness.high"},
                Alertness.values());
        y += FILTER_ROW_H;
        addFilterRow(y, "book.flavor_immersed_daily_argo.filter.feeding", CAT_FEEDING,
                new String[]{"book.flavor_immersed_daily_argo.feeding.slow", "book.flavor_immersed_daily_argo.feeding.medium",
                        "book.flavor_immersed_daily_argo.feeding.fast"},
                FeedingSpeed.values());
        y += FILTER_ROW_H;
        addFilterRow(y, "book.flavor_immersed_daily_argo.filter.caught", CAT_CAUGHT,
                new String[]{"book.flavor_immersed_daily_argo.caught.yes", "book.flavor_immersed_daily_argo.caught.no"},
                CaughtFilter.values());
    }

    private void addFilterRow(int y, String labelKey, int category, String[] optionKeys, Enum<?>[] values) {
        filterLabels[category] = Component.translatable(labelKey).getString();
        int x = leftX + font.width(Component.translatable(labelKey)) + 6;
        for (int i = 0; i < optionKeys.length; i++) {
            String text = Component.translatable(optionKeys[i]).getString();
            int w = font.width(text) + 4;
            chips.add(new Chip(x, y, w, 10, category, values[i], text));
            x += w + 2;
        }
    }

// 列表刷新

    private void rebuildList() {
        filtered = FishingHandbookData.getInstance()
                .filter(habitatSel, dietSel, activitySel, alertnessSel, speedSel)
                .stream().filter(this::matchesCaught).toList();
        if (!filtered.contains(selected)) {
            selected = filtered.isEmpty() ? null : filtered.get(0);
        }
        int rows = (listBottomY - listTopY) / LIST_ROW_H;
        listScroll = Math.min(listScroll, Math.max(0, filtered.size() - rows));
        updateVisible();
    }

// 「钓获状态」筛选：同类别多选取并集（已钓取 ∪ 未钓取 = 不限），不选即不限
    private boolean matchesCaught(FishingHandbookEntry entry) {
        if (caughtSel.isEmpty()) {
            return true;
        }
        CaughtFilter state = FishingCaughtRecords.isCaught(entry.caughtKey())
                ? CaughtFilter.CAUGHT
                : CaughtFilter.UNCAUGHT;
        return caughtSel.contains(state);
    }

    private void updateVisible() {
        int rows = (listBottomY - listTopY) / LIST_ROW_H;
        int from = listScroll;
        int to = Math.min(filtered.size(), from + rows);
        visible = from < to ? filtered.subList(from, to) : List.of();
    }

// 输入

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double bx = toBookX(mouseX);
            double by = toBookY(mouseY);
            for (Chip chip : chips) {
                if (bx >= chip.x && bx < chip.x + chip.w
                        && by >= chip.y && by < chip.y + chip.h) {
                    toggleChip(chip);
                    return true;
                }
            }
            if (bx >= leftX && bx < leftRight && by >= listTopY && by < listBottomY) {
                int idx = (int) ((by - listTopY) / LIST_ROW_H);
                if (idx >= 0 && idx < visible.size()) {
                    selected = visible.get(idx);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double bx = toBookX(mouseX);
        double by = toBookY(mouseY);
        if (bx >= leftX && bx < leftRight && by >= listTopY && by < listBottomY) {
            int rows = (listBottomY - listTopY) / LIST_ROW_H;
            int maxScroll = Math.max(0, filtered.size() - rows);
            listScroll = Math.max(0, Math.min(listScroll + (int) -verticalAmount, maxScroll));
            updateVisible();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void toggleChip(Chip chip) {
        switch (chip.category) {
            case CAT_HABITAT -> toggle(habitatSel, (Habitat) chip.value);
            case CAT_DIET -> toggle(dietSel, (Diet) chip.value);
            case CAT_ACTIVITY -> toggle(activitySel, (Activity) chip.value);
            case CAT_ALERTNESS -> toggle(alertnessSel, (Alertness) chip.value);
            case CAT_FEEDING -> toggle(speedSel, (FeedingSpeed) chip.value);
            case CAT_CAUGHT -> toggle(caughtSel, (CaughtFilter) chip.value);
            default -> {
            }
        }
        rebuildList();
    }

    private static <T extends Enum<T>> void toggle(Set<T> set, T value) {
        if (!set.add(value)) {
            set.remove(value);
        }
    }

    private boolean isSelected(Chip chip) {
        return switch (chip.category) {
            case CAT_HABITAT -> habitatSel.contains((Habitat) chip.value);
            case CAT_DIET -> dietSel.contains((Diet) chip.value);
            case CAT_ACTIVITY -> activitySel.contains((Activity) chip.value);
            case CAT_ALERTNESS -> alertnessSel.contains((Alertness) chip.value);
            case CAT_FEEDING -> speedSel.contains((FeedingSpeed) chip.value);
            case CAT_CAUGHT -> caughtSel.contains((CaughtFilter) chip.value);
            default -> false;
        };
    }

// 渲染

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        PoseStack pose = gui.pose();
        pose.pushPose();
// 平移到屏幕中心并整体缩放，书中的文字、图标随书一起等比缩放，
// 小窗口时整本书缩小适配屏幕，避免超出窗口导致内容被裁到左上角
        pose.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
        pose.scale(this.uiScale, this.uiScale, 1.0F);
// 自定义钓鱼手册背景（384×288，与虚拟布局 1:1，避免原版书纹拉伸模糊）
        gui.blit(BOOK_TEXTURE, bookX, bookY, BOOK_W, BOOK_H, 0.0F, 0.0F, BOOK_W, BOOK_H, BOOK_W, BOOK_H);
        renderLeftPage(gui);
        renderRightPage(gui);
        pose.popPose();
    }

    private void renderLeftPage(GuiGraphics gui) {
// 书名
        Component title = Component.translatable("book.flavor_immersed_daily_argo.title");
        gui.drawString(font, title, leftX + (leftRight - leftX - font.width(title)) / 2, pageTop + 4, 0x2F2F2F, false);

// 筛选行
        for (int cat = 0; cat < filterLabels.length; cat++) {
            if (filterLabels[cat] == null) {
                continue;
            }
            gui.drawString(font, Component.literal(filterLabels[cat]), leftX, pageTop + 18 + cat * FILTER_ROW_H, 0x2F2F2F, false);
        }
        for (Chip chip : chips) {
            gui.drawString(font, Component.literal(chip.text), chip.x, chip.y,
                    isSelected(chip) ? 0xC86F00 : 0x3F3F3F, false);
        }

// 战利品列表
        if (filtered.isEmpty()) {
            int total = FishingHandbookData.getInstance().all().size();
            Component empty = Component.translatable("book.flavor_immersed_daily_argo.empty", total);
            gui.drawString(font, empty, leftX, listTopY, 0x3F3F3F, false);
            return;
        }
        for (int i = 0; i < visible.size(); i++) {
            FishingHandbookEntry entry = visible.get(i);
            int rowY = listTopY + i * LIST_ROW_H;
            boolean isSelectedRow = entry == selected;
            if (isSelectedRow) {
                gui.fill(leftX, rowY, leftRight, rowY + LIST_ROW_H, 0x40D87F00);
            }
// 图标缩到 12px
            if (entry.usesEntityIcon()) {
                drawEntityIcon(gui, entry, leftX + 7, rowY + 7, 12.0F);
            } else {
                PoseStack pose = gui.pose();
                pose.pushPose();
                pose.translate(leftX + 1, rowY + 1, 0);
                pose.scale(0.75F, 0.75F, 1.0F);
                gui.renderItem(entry.icon(), 0, 0);
                pose.popPose();
            }
            gui.drawString(font, entry.displayNameComponent(), leftX + 18, rowY + 4,
                    isSelectedRow ? 0xC86F00 : 0x3F3F3F, false);
        }
// 还有更多条目时的滚动提示
        if (filtered.size() > visible.size()) {
            gui.drawString(font, Component.literal("▼"), rightCenterX - 8, listBottomY - 10, 0x808080, false);
        }
    }

    private void renderRightPage(GuiGraphics gui) {
        if (selected == null) {
            Component hint = Component.translatable("book.flavor_immersed_daily_argo.hint_none");
            gui.drawString(font, hint, rightCenterX - font.width(hint) / 2, pageTop + 100, 0x3F3F3F, false);
            return;
        }

// 大图标（32x32）
        if (selected.usesEntityIcon()) {
            drawEntityIcon(gui, selected, rightCenterX, pageTop + 32, 30.0F);
        } else {
            PoseStack pose = gui.pose();
            pose.pushPose();
            pose.scale(2.0F, 2.0F, 1.0F);
            gui.renderItem(selected.icon(), (rightCenterX - 16) / 2, (pageTop + 16) / 2);
            pose.popPose();
        }

// 名字
        Component name = selected.displayNameComponent();
        gui.drawString(font, name, rightCenterX - font.width(name) / 2, pageTop + 52, 0x2F2F2F, false);

// 属性行（首行为本玩家是否已钓获：标签加粗，已钓获绿色、未钓获红色）
        int y = pageTop + 70;
        boolean caught = FishingCaughtRecords.isCaught(selected.caughtKey());
        y = drawAttr(gui, y, "book.flavor_immersed_daily_argo.attr.caught",
                Component.translatable(caught
                                ? "book.flavor_immersed_daily_argo.caught.yes"
                                : "book.flavor_immersed_daily_argo.caught.no")
                        .withStyle(caught ? ChatFormatting.GREEN : ChatFormatting.RED),
                true);
        y = drawAttr(gui, y, "book.flavor_immersed_daily_argo.attr.habitat", habitatValue());
        y = drawAttr(gui, y, "book.flavor_immersed_daily_argo.attr.depth", depthValue());
        y = drawAttr(gui, y, "book.flavor_immersed_daily_argo.attr.diet",
                Component.translatable(dietKey(selected.diet)));
        y = drawAttr(gui, y, "book.flavor_immersed_daily_argo.attr.tension",
                Component.literal(String.valueOf(selected.struggleTension)));
        y = drawAttr(gui, y, "book.flavor_immersed_daily_argo.attr.alertness",
                Component.translatable(alertnessKey(selected.alertness)));
        y = drawAttr(gui, y, "book.flavor_immersed_daily_argo.attr.feeding",
                Component.translatable(feedingKey(selected.feedingSpeed)));
        y = drawAttr(gui, y, "book.flavor_immersed_daily_argo.attr.activity", activityValue());

// 简介（自动换行，支持本地化键）
        Component desc = selected.descriptionComponent();
        if (desc != null) {
            y += 6;
            for (FormattedCharSequence line : font.split(desc, rightRight - rightX)) {
                gui.drawString(font, line, rightX, y, 0x3F3F3F, false);
                y += 9;
                if (y > pageBottom - 9) {
                    break;
                }
            }
        }
    }

// 绘制一行属性（标签 + 可换行的值），返回下一行 y
    private int drawAttr(GuiGraphics gui, int y, String labelKey, Component value) {
        return drawAttr(gui, y, labelKey, value, false);
    }

// 绘制一行属性；boldLabel 为 true 时标签加粗
    private int drawAttr(GuiGraphics gui, int y, String labelKey, Component value, boolean boldLabel) {
        Component label = boldLabel
                ? Component.translatable(labelKey).withStyle(ChatFormatting.BOLD)
                : Component.translatable(labelKey);
        gui.drawString(font, label, rightX, y, 0x3F3F3F, false);
        int labelW = font.width(label) + 4;
        int maxW = rightRight - rightX - labelW;
        List<FormattedCharSequence> lines = font.split(value, Math.max(30, maxW));
        for (int i = 0; i < lines.size(); i++) {
            gui.drawString(font, lines.get(i), i == 0 ? rightX + labelW : rightX, y, 0x3F3F3F, false);
            y += 9;
        }
        return y;
    }

// 实体图标

// 取（并缓存）条目的预览实体；无实体或客户端世界未就绪时为空
    private Optional<Entity> previewEntity(FishingHandbookEntry entry) {
        return previewCache.computeIfAbsent(entry, e -> {
            if (e.entityId == null) {
                return Optional.empty();
            }
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(e.entityId).orElse(null);
            Level level = Minecraft.getInstance().level;
            if (type == null || level == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(type.create(level));
        });
    }

// 在 (cx, cy) 居中绘制生物实体图标，size 为目标高度（虚拟像素）。
// 用于没有物品形态的条目。
    private void drawEntityIcon(GuiGraphics gui, FishingHandbookEntry entry, int cx, int cy, float size) {
        Entity entity = previewEntity(entry).orElse(null);
        if (entity == null) {
            return;
        }
        float yaw = 20.0F;
        entity.setYRot(yaw);
        entity.setXRot(0.0F);
        entity.yRotO = yaw;
        entity.xRotO = 0.0F;
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = yaw - 10.0F;
            living.yBodyRotO = yaw - 10.0F;
            living.yHeadRot = yaw;
            living.yHeadRotO = yaw;
        }

// 统一以溺尸身高为基准缩放，使各生物的图标保持真实相对大小
        float scale = size / ENTITY_ICON_REF_HEIGHT;
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        PoseStack pose = gui.pose();
        pose.pushPose();
// z=50 与原版 InventoryScreen 一致；先整体下移半个身高使实体垂直居中
        pose.translate(cx, cy + size / 2.0F, 50.0F);
// 负 X 缩放配合绕 Z 翻转 180°，抵消镜像并让模型正向朝向观察者
        pose.scale(-scale, scale, scale);
        Quaternionf rot = new Quaternionf().rotateZ((float) Math.PI);
        rot.mul(new Quaternionf().rotateX(-18.0F * ((float) Math.PI / 180.0F)));
        pose.mulPose(rot);
        dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, pose, gui.bufferSource(), 0xF000F0);
        pose.popPose();
        dispatcher.setRenderShadow(true);
        gui.flush();
    }

// 属性值

    private Component habitatValue() {
        String base = Component.translatable(habitatKey(selected.habitat)).getString();
        if (selected.biomes.isEmpty()) {
            return Component.literal(base);
        }
        StringBuilder sb = new StringBuilder(base).append("（");
        for (int i = 0; i < selected.biomes.size(); i++) {
            String rl = selected.biomes.get(i);
// 原版群系键是 biome.minecraft.warm_ocean（点分隔），需把冒号替换成点
            String key = "biome." + rl.replace(':', '.');
            sb.append(Component.translatableWithFallback(key, rl).getString());
            if (i < selected.biomes.size() - 1) {
                sb.append("、");
            }
        }
        sb.append("）");
        return Component.literal(sb.toString());
    }

    private Component depthValue() {
        if (selected.depth.isEmpty()) {
            return Component.translatable("book.flavor_immersed_daily_argo.depth.unlimited");
        }
        return Component.literal(selected.depth);
    }

    private Component activityValue() {
        if (selected.activity.isEmpty() || selected.activity.contains(Activity.ALL)) {
            return Component.translatable("book.flavor_immersed_daily_argo.activity.all_time");
        }
        StringBuilder sb = new StringBuilder();
        for (Activity a : selected.activity) {
            if (sb.length() > 0) {
                sb.append("、");
            }
            sb.append(Component.translatable(activityKey(a)).getString());
        }
        return Component.literal(sb.toString());
    }

    private static String habitatKey(Habitat h) {
        return switch (h) {
            case FRESH -> "book.flavor_immersed_daily_argo.habitat.fresh";
            case OCEAN -> "book.flavor_immersed_daily_argo.habitat.ocean";
            case BOTH -> "book.flavor_immersed_daily_argo.habitat.both";
        };
    }

    private static String dietKey(Diet d) {
        return switch (d) {
            case OMNIVORE -> "book.flavor_immersed_daily_argo.diet.omnivore";
            case HERBIVORE -> "book.flavor_immersed_daily_argo.diet.herbivore";
            case CARNIVORE -> "book.flavor_immersed_daily_argo.diet.carnivore";
            case SCAVENGER -> "book.flavor_immersed_daily_argo.diet.scavenger";
            case LIVE -> "book.flavor_immersed_daily_argo.diet.live";
        };
    }

    private static String activityKey(Activity a) {
        return switch (a) {
            case ALL -> "book.flavor_immersed_daily_argo.activity.all";
            case DAY -> "book.flavor_immersed_daily_argo.activity.day";
            case NIGHT -> "book.flavor_immersed_daily_argo.activity.night";
            case SUNNY -> "book.flavor_immersed_daily_argo.activity.sunny";
            case RAIN -> "book.flavor_immersed_daily_argo.activity.rain";
        };
    }

    private static String alertnessKey(Alertness a) {
        return switch (a) {
            case LOW -> "book.flavor_immersed_daily_argo.alertness.low";
            case MEDIUM -> "book.flavor_immersed_daily_argo.alertness.medium";
            case HIGH -> "book.flavor_immersed_daily_argo.alertness.high";
        };
    }

    private static String feedingKey(FeedingSpeed s) {
        return switch (s) {
            case SLOW -> "book.flavor_immersed_daily_argo.feeding.slow";
            case MEDIUM -> "book.flavor_immersed_daily_argo.feeding.medium";
            case FAST -> "book.flavor_immersed_daily_argo.feeding.fast";
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
