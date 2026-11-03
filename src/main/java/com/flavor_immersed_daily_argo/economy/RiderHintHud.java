package com.flavor_immersed_daily_argo.economy;

import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RenderGuiEvent;

// 准星落在外卖小哥或顾客身上时，在准星下方浮出一块提示：左边是所需食物的图标，右边写着
// 要交的数量、报酬，以及按 R 查看这道菜制作配方的按键提示。
//
// 数据来自实体同步过来的订单，纯客户端读取。底衬用纯色块，不带模糊。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public final class RiderHintHud {

    private static final int BACKGROUND = 0x80000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
// 配方提示那一行用暗一点的灰，和订单信息区分开
    private static final int HINT_COLOR = 0xFFB0B0B0;
// 提示与准星之间的间距
    private static final int GAP_BELOW_CROSSHAIR = 14;
// 底衬相对文字的内边距
    private static final int PADDING_X = 4;
    private static final int PADDING_TOP = 3;
    private static final int PADDING_BOTTOM = 2;
// 食物图标尺寸与图标到文字的间距
    private static final int ICON_SIZE = 16;
    private static final int ICON_GAP = 4;

    private static final String KEY_ORDER = "gui.flavor_immersed_daily_argo.rider.order";
    private static final String KEY_REWARD = "gui.flavor_immersed_daily_argo.rider.reward";
    private static final String KEY_RECIPE_HINT = "gui.flavor_immersed_daily_argo.rider.recipe_hint";

    private RiderHintHud() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null
                || minecraft.screen != null || minecraft.options.hideGui) {
            return;
        }
        FoodOrder order = OrderRecipeKeyMapping.aimedOrder(minecraft);
        if (order == null) {
            return;
        }
        ItemStack orderStack = order.getRequiredFood();
        if (orderStack.isEmpty()) {
            return;
        }
        Font font = minecraft.font;
        Component orderLine = Component.translatable(KEY_ORDER, orderStack.getCount(), orderStack.getHoverName());
        Component rewardLine = Component.translatable(KEY_REWARD, order.getReward());
// 装了 JEI 或 EMI 才提示按键；都没装时按 R 只会提示查看不了配方
        Component recipeLine = hasRecipeViewer()
                ? Component.translatable(KEY_RECIPE_HINT,
                        OrderRecipeKeyMapping.SHOW_ORDER_RECIPE.getTranslatedKeyMessage())
                : null;

// 图标 + 两 / 三行文字并排，整体在准星下方居中
        int textWidth = Math.max(font.width(orderLine), font.width(rewardLine));
        if (recipeLine != null) {
            textWidth = Math.max(textWidth, font.width(recipeLine));
        }
        int lineCount = recipeLine == null ? 2 : 3;
        int contentWidth = ICON_SIZE + ICON_GAP + textWidth;
        int contentHeight = Math.max(ICON_SIZE, font.lineHeight * lineCount);
        int x = (event.getGuiGraphics().guiWidth() - contentWidth) / 2;
        int y = event.getGuiGraphics().guiHeight() / 2 + GAP_BELOW_CROSSHAIR;

        GuiGraphics gui = event.getGuiGraphics();
        gui.fill(x - PADDING_X, y - PADDING_TOP,
                x + contentWidth + PADDING_X, y + contentHeight + PADDING_BOTTOM, BACKGROUND);

// 食物图标与文字都在这块底衬里纵向居中
        gui.renderItem(orderStack, x, y + (contentHeight - ICON_SIZE) / 2);
        int textX = x + ICON_SIZE + ICON_GAP;
        int textY = y + (contentHeight - font.lineHeight * lineCount) / 2;
        gui.drawString(font, orderLine, textX, textY, TEXT_COLOR, true);
        gui.drawString(font, rewardLine, textX, textY + font.lineHeight, TEXT_COLOR, true);
        if (recipeLine != null) {
            gui.drawString(font, recipeLine, textX, textY + font.lineHeight * 2, HINT_COLOR, true);
        }
    }

    private static boolean hasRecipeViewer() {
        return ModList.get().isLoaded("jei") || ModList.get().isLoaded("emi");
    }
}
