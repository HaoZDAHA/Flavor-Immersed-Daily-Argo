package com.flavor_immersed_daily_argo.herding;

import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.client.event.RenderGuiEvent;

import java.util.Locale;

// 空手右键查看动物状态栏时，屏幕中央上方浮出的小面板。
//
// 面板只渲染、不下判断：数值由 AnimalStatsClient 在右键那一刻抓一份快照，
// 之后显示 5 秒。绘制全部走纯色块与文字，不带任何模糊效果。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public final class AnimalStatsHud {

// 面板停留时长（5 秒）
    private static final int SHOW_TICKS = 100;

    private static final int PANEL_WIDTH = 154;
    private static final int HEADER_HEIGHT = 16;
    private static final int ROW_HEIGHT = 11;
    private static final int LABEL_X = 6;
    private static final int VALUE_X = 42;
    private static final int BAR_X = 78;
    private static final int SEGMENT_WIDTH = 6;
    private static final int SEGMENT_STEP = 7;

    private static final int BACKGROUND = 0xC0101010;
    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int EMPTY_SEGMENT = 0xFF303030;

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
// 体重行里「标准」那一段的起始 x：给左侧的当前体重留出最多 3 位数加单位的宽度
    private static final int WEIGHT_STANDARD_X = VALUE_X + 50;

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
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
            if (ticksLeft > 0) {
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
        int left = (gui.guiWidth() - PANEL_WIDTH) / 2;
        int top = 8;
        int rows = LABEL_KEYS.length + (hasWeight ? 1 : 0);
        int height = HEADER_HEIGHT + rows * ROW_HEIGHT + 4;
        gui.fill(left, top, left + PANEL_WIDTH, top + height, BACKGROUND);
        gui.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 4, TITLE_COLOR);
        for (int row = 0; row < LABEL_KEYS.length; row++) {
            int y = top + HEADER_HEIGHT + row * ROW_HEIGHT;
            int value = values[row];
            gui.drawString(font, Component.translatable(LABEL_KEYS[row]), left + LABEL_X, y, TEXT_COLOR);
            gui.drawString(font, value + "/" + AnimalStats.MAX, left + VALUE_X, y, TEXT_COLOR);
            for (int segment = 0; segment < AnimalStats.MAX; segment++) {
                int x = left + BAR_X + segment * SEGMENT_STEP;
                gui.fill(x, y + 1, x + SEGMENT_WIDTH, y + 7,
                        segment < value ? SEGMENT_COLORS[row] : EMPTY_SEGMENT);
            }
        }
        if (hasWeight) {
            int y = top + HEADER_HEIGHT + LABEL_KEYS.length * ROW_HEIGHT;
            gui.drawString(font, Component.translatable(WEIGHT_LABEL_KEY), left + LABEL_X, y, TEXT_COLOR);
            gui.drawString(font, Component.translatable(WEIGHT_VALUE_KEY, formatWeight(weight)),
                    left + VALUE_X, y, TEXT_COLOR);
            gui.drawString(font, Component.translatable(WEIGHT_STANDARD_KEY, formatWeight(weightStandard)),
                    left + WEIGHT_STANDARD_X, y, TEXT_COLOR);
        }
    }
}
