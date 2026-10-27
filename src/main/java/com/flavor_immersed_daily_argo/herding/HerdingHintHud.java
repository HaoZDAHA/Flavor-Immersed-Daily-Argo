package com.flavor_immersed_daily_argo.herding;

import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RenderGuiEvent;

// 准星落在畜禽饲养的几个方块上时，在准星下方浮出的一行小提示，说明指着的是什么、里面还剩多少。
//
// 覆盖三种方块：化粪发酵桶（区分左右半，各自显示粪便 / 肥料数量）、牧场食槽（饲料 / 水的数量）、
// 沼气罐（加压沼气数量）。数量来自方块实体，方块实体已经把内容随区块同步到客户端。
// 底衬用纯色块，不带模糊。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public final class HerdingHintHud {

    private static final int BACKGROUND = 0x80000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
// 提示与准星之间的间距
    private static final int GAP_BELOW_CROSSHAIR = 14;
// 底衬相对文字的内边距
    private static final int PADDING_X = 4;
    private static final int PADDING_TOP = 3;
    private static final int PADDING_BOTTOM = 2;

    private static final String KEY_SEPTIC_POOP = "gui.flavor_immersed_daily_argo.septic_tank.poop";
    private static final String KEY_SEPTIC_FERTILIZER = "gui.flavor_immersed_daily_argo.septic_tank.fertilizer";
    private static final String KEY_TROUGH_EMPTY = "gui.flavor_immersed_daily_argo.trough.empty";
    private static final String KEY_TROUGH_FOOD = "gui.flavor_immersed_daily_argo.trough.food";
    private static final String KEY_TROUGH_WATER = "gui.flavor_immersed_daily_argo.trough.water";
    private static final String KEY_METHANE_TANK = "gui.flavor_immersed_daily_argo.methane_tank.gas";

    private HerdingHintHud() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null
                || minecraft.screen != null || minecraft.options.hideGui) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit)) {
            return;
        }
        BlockState state = minecraft.level.getBlockState(hit.getBlockPos());
        Component hint = hintFor(minecraft, state, hit.getBlockPos());
        if (hint == null) {
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();
        Font font = minecraft.font;
        int width = font.width(hint);
        int x = (gui.guiWidth() - width) / 2;
        int y = gui.guiHeight() / 2 + GAP_BELOW_CROSSHAIR;
        gui.fill(x - PADDING_X, y - PADDING_TOP, x + width + PADDING_X, y + font.lineHeight + PADDING_BOTTOM,
                BACKGROUND);
        gui.drawString(font, hint, x, y, TEXT_COLOR, true);
    }

// 准星指着哪个方块就给哪一行提示；不是这几种方块就返回 null
    private static Component hintFor(Minecraft minecraft, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof SepticTankBlock) {
// 两半只有左半有方块实体，右半统一转回左半读数量
            if (!(minecraft.level.getBlockEntity(SepticTankBlock.mainPos(state, pos))
                    instanceof SepticTankBlockEntity tank)) {
                return null;
            }
            return SepticTankBlock.isPoopSide(state)
                    ? Component.translatable(KEY_SEPTIC_POOP, tank.countPoop())
                    : Component.translatable(KEY_SEPTIC_FERTILIZER, tank.countFertilizer());
        }
        if (state.getBlock() instanceof FeedingTroughBlock) {
            if (!(minecraft.level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough)) {
                return null;
            }
            FeedingTroughBlock.TroughContent content = state.getValue(FeedingTroughBlock.CONTENT);
            return switch (content) {
                case FOOD -> Component.translatable(KEY_TROUGH_FOOD, trough.count());
                case WATER -> Component.translatable(KEY_TROUGH_WATER, trough.count());
                case EMPTY -> Component.translatable(KEY_TROUGH_EMPTY);
            };
        }
        if (state.getBlock() instanceof MethaneTankBlock) {
            if (!(minecraft.level.getBlockEntity(pos) instanceof MethaneTankBlockEntity tank)) {
                return null;
            }
            return Component.translatable(KEY_METHANE_TANK, tank.gas());
        }
        return null;
    }
}