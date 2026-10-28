package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.client.GuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Locale;

// 空手右键查看动物状态栏时，准星上方浮出的小面板。
//
// 面板只渲染、不下判断：数值由 AnimalStatsClient 在右键那一刻抓一份快照，
// 之后显示 5 秒，按潜行键可立即收起。画风跟经济类机器界面一致：GuiStyle 的深色圆角底板 +
// 金色标题 + 像素格子进度条，文字一律不带阴影。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public final class AnimalStatsHud {

// 面板停留时长（5 秒）
    private static final int SHOW_TICKS = 100;

// 版式
    private static final int PANEL_WIDTH = 158;
    private static final int HEADER_HEIGHT = 18;
    private static final int ROW_HEIGHT = 12;
    private static final int PADDING = 9;
    private static final int BAR_X = 88;
    private static final int SEGMENT_WIDTH = 5;
    private static final int SEGMENT_STEP = 6;
    private static final int SEGMENT_HEIGHT = 6;
// 面板与准星之间的空隙
    private static final int CROSSHAIR_GAP = 14;
// 底部“潜行关闭”提示占的高度
    private static final int HINT_HEIGHT = 11;

    private static final int TITLE_COLOR = GuiStyle.ACCENT;
    private static final int TEXT_COLOR = GuiStyle.TEXT_DIM;
    private static final int VALUE_COLOR = GuiStyle.TEXT;
    private static final int EMPTY_SEGMENT = 0xFF2A2E36;

    private static final String[] LABEL_KEYS = {
            "gui.flavor_immersed_daily_argo.stat.satiety",
            "gui.flavor_immersed_daily_argo.stat.thirst",
            "gui.flavor_immersed_daily_argo.stat.cleanliness",
            "gui.flavor_immersed_daily_argo.stat.mood"
    };

// 四条状态栏各自的填充色：饱食橙、口渴蓝、清洁青、心情粉
    private static final int[] SEGMENT_COLORS = {
            0xFFFFA028, 0xFF38A0FF, 0xFF4FE0C8, 0xFFFF6FA8
    };

// 体重那一行（只对有权重标准的动物显示，猫狗没有这一行）
    private static final String WEIGHT_LABEL_KEY = "gui.flavor_immersed_daily_argo.stat.weight";
    private static final String WEIGHT_VALUE_KEY = "gui.flavor_immersed_daily_argo.stat.weight.value";
    private static final String WEIGHT_STANDARD_KEY = "gui.flavor_immersed_daily_argo.stat.weight.standard";
// 面板底部的操作提示
    private static final String HINT_KEY = "gui.flavor_immersed_daily_argo.stat.hint";
// 体重行里当前体重的起始 x
    private static final int WEIGHT_VALUE_X = 42;

    private static int ticksLeft;
    private static Component title = Component.empty();
    private static int[] values = new int[LABEL_KEYS.length];
    private static boolean hasWeight;
    private static float weight;
    private static float weightStandard;

    private AnimalStatsHud() {
    }

// 抓一份当次快照并开始倒计时；动物没有状态栏时什么也不显示
    public static void show(LivingEntity entity) {
        AnimalStats stats = AnimalStatsManager.getStats(entity);
        if (stats == null) {
            ticksLeft = 0;
            return;
        }
        title = entity.getDisplayName();
        values = new int[]{stats.satiety(), stats.thirst(), stats.cleanliness(), stats.mood()};
        Float standard = AnimalStatsManager.weightStandard(entity.getType());
        hasWeight = standard != null;
        weightStandard = standard == null ? 0.0F : standard;
// 旧存档里的动物体重还没初始化，面板上直接按标准显示，免得先亮一个 0
        weight = stats.weight() > 0.0F ? stats.weight() : weightStandard;
        ticksLeft = SHOW_TICKS;
    }

// 体重显示成一位小数，整数则省掉小数点（450.0 → 450）
    private static String formatWeight(float value) {
        String text = String.format(Locale.ROOT, "%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (ticksLeft <= 0) {
            return;
        }
// 潜行立刻收起，不用等倒计时
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.isShiftKeyDown()) {
            ticksLeft = 0;
        } else {
            ticksLeft--;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (ticksLeft <= 0) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        GuiGraphics gui = event.getGuiGraphics();

        int rows = LABEL_KEYS.length + (hasWeight ? 1 : 0);
        int height = HEADER_HEIGHT + rows * ROW_HEIGHT + HINT_HEIGHT + 4;
// 贴在准星正右方，竖直居中；右侧放不下时往左收
        int left = gui.guiWidth() / 2 + CROSSHAIR_GAP;
        left = Math.max(4, Math.min(left, gui.guiWidth() - PANEL_WIDTH - 4));
        int top = (gui.guiHeight() - height) / 2;
        top = Math.max(4, Math.min(top, gui.guiHeight() - height - 4));

        GuiStyle.panel(gui, left, top, PANEL_WIDTH, height);

        drawCentered(gui, title, left + PANEL_WIDTH / 2, top + 5, TITLE_COLOR);
        GuiStyle.divider(gui, left + PADDING, top + HEADER_HEIGHT - 1, PANEL_WIDTH - PADDING * 2);

        for (int row = 0; row < LABEL_KEYS.length; row++) {
            int y = top + HEADER_HEIGHT + 1 + row * ROW_HEIGHT;
            gui.drawString(font, Component.translatable(LABEL_KEYS[row]), left + PADDING, y + 1,
                    TEXT_COLOR, false);
            String value = values[row] + "/" + AnimalStats.MAX;
            gui.drawString(font, value, left + BAR_X - 5 - font.width(value), y + 1, VALUE_COLOR, false);
            drawBar(gui, left + BAR_X, y, values[row], SEGMENT_COLORS[row]);
        }

        if (hasWeight) {
            int y = top + HEADER_HEIGHT + 1 + LABEL_KEYS.length * ROW_HEIGHT;
            gui.drawString(font, Component.translatable(WEIGHT_LABEL_KEY), left + PADDING, y + 1,
                    TEXT_COLOR, false);
            String value = Component.translatable(WEIGHT_VALUE_KEY, formatWeight(weight)).getString();
            gui.drawString(font, value, left + WEIGHT_VALUE_X, y + 1, VALUE_COLOR, false);
            String standard = Component.translatable(WEIGHT_STANDARD_KEY, formatWeight(weightStandard)).getString();
            gui.drawString(font, standard, left + PANEL_WIDTH - PADDING - font.width(standard), y + 1,
                    TEXT_COLOR, false);
        }

// 底部操作提示
        int hintY = top + height - HINT_HEIGHT + 1;
        GuiStyle.divider(gui, left + PADDING, hintY - 3, PANEL_WIDTH - PADDING * 2);
        drawCentered(gui, Component.translatable(HINT_KEY), left + PANEL_WIDTH / 2, hintY, TEXT_COLOR);
    }

// 像素格子进度条：十个小方块，亮起来的是该条目的颜色
    private static void drawBar(GuiGraphics gui, int x, int y, int value, int color) {
        int clamped = Math.max(0, Math.min(value, AnimalStats.MAX));
        for (int segment = 0; segment < AnimalStats.MAX; segment++) {
            int sx = x + segment * SEGMENT_STEP;
            GuiStyle.roundedRect(gui, sx, y, SEGMENT_WIDTH, SEGMENT_HEIGHT, 1,
                    segment < clamped ? color : EMPTY_SEGMENT);
        }
    }

// 居中文字：不带阴影，避免出现重影
    private static void drawCentered(GuiGraphics gui, Component text, int centerX, int y, int color) {
        Font font = Minecraft.getInstance().font;
        gui.drawString(font, text, centerX - font.width(text) / 2, y, color, false);
    }
}
