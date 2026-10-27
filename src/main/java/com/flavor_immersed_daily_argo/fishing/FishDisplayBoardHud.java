package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

// 准星落在钓鱼展示架上时，在准星下方浮出的一行小提示：挂着的是哪条、称过的话多重。
//
// 挂着的那条随方块实体同步到客户端，所以重量直接读物品栈上的 WEIGHED_WEIGHT 组件；
// 没上过秤就只显示名字。底衬用纯色块，不带模糊。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class FishDisplayBoardHud {

    private static final int BACKGROUND = 0x80000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
// 提示与准星之间的间距
    private static final int GAP_BELOW_CROSSHAIR = 14;
// 底衬相对文字的内边距
    private static final int PADDING_X = 4;
    private static final int PADDING_TOP = 3;
    private static final int PADDING_BOTTOM = 2;

// 称过重时：%1$s 鱼名、%2$s 重量
    private static final String KEY_WEIGHTED = "gui.flavor_immersed_daily_argo.fish_display_board.weight";

    private FishDisplayBoardHud() {
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
        BlockPos pos = hit.getBlockPos();
        if (!(minecraft.level.getBlockState(pos).getBlock() instanceof FishDisplayBoardBlock)
                || !(minecraft.level.getBlockEntity(pos) instanceof FishDisplayBoardBlockEntity board)) {
            return;
        }
        ItemStack stack = board.displayed();
        if (stack.isEmpty()) {
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();
        Font font = minecraft.font;
        Component hint = hint(stack);
        int width = font.width(hint);
        int x = (gui.guiWidth() - width) / 2;
        int y = gui.guiHeight() / 2 + GAP_BELOW_CROSSHAIR;
        gui.fill(x - PADDING_X, y - PADDING_TOP, x + width + PADDING_X, y + font.lineHeight + PADDING_BOTTOM,
                BACKGROUND);
        gui.drawString(font, hint, x, y, TEXT_COLOR, true);
    }

// 称过重就带上重量，没称过只报名字
    private static Component hint(ItemStack stack) {
        Double weight = stack.get(ModDataComponents.WEIGHED_WEIGHT.get());
        if (weight == null) {
            return stack.getHoverName();
        }
        return Component.translatable(KEY_WEIGHTED, stack.getHoverName(), SteelyardItem.format(weight));
    }
}
