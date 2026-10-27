package com.flavor_immersed_daily_argo.uav;

import com.flavor_immersed_daily_argo.client.GuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// 无人机物品栏界面。
//
// 底图见 GuiTextures#UAV，槽位凹槽已经画在图上。
public class UavScreen extends AbstractContainerScreen<UavMenu> {

// 标签颜色，与界面标题的默认观感接近
    private static final int LABEL_COLOR = 0xE0E0E0;

    public UavScreen(UavMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = UavMenu.IMAGE_WIDTH;
        this.imageHeight = UavMenu.IMAGE_HEIGHT;
        this.inventoryLabelY = UavMenu.INVENTORY_LABEL_Y;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        GuiTextures.drawBackground(gui, GuiTextures.UAV, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderLabels(gui, mouseX, mouseY);
        drawCentered(gui, "gui.flavor_immersed_daily_argo.uav.slot.module",
                UavMenu.MODULE_LABEL_X, UavMenu.TOP_LABEL_Y);
        drawCentered(gui, "gui.flavor_immersed_daily_argo.uav.slot.use",
                UavMenu.USE_LABEL_X, UavMenu.TOP_LABEL_Y);
        drawCentered(gui, "gui.flavor_immersed_daily_argo.uav.slot.backpack",
                UavMenu.BACKPACK_LABEL_X, UavMenu.BACKPACK_LABEL_Y);
    }

// 以槽位列中心为准，把标签水平居中画在指定高度（坐标相对界面左上角）
    private void drawCentered(GuiGraphics gui, String key, int centerX, int y) {
        Component text = Component.translatable(key);
        gui.drawString(this.font, text, centerX - this.font.width(text) / 2, y, LABEL_COLOR);
    }
}
