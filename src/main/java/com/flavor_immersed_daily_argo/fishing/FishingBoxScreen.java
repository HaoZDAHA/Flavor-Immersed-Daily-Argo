package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.client.GuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// 钓鱼箱界面
//
// 底图见 GuiTextures#FISHING_BOX。
public class FishingBoxScreen extends AbstractContainerScreen<FishingBoxMenu> {

    public FishingBoxScreen(FishingBoxMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = FishingBoxMenu.IMAGE_WIDTH;
        this.imageHeight = FishingBoxMenu.IMAGE_HEIGHT;
        this.inventoryLabelX = FishingBoxMenu.INVENTORY_LABEL_X;
        this.inventoryLabelY = FishingBoxMenu.INVENTORY_LABEL_Y;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        GuiTextures.drawBackground(gui, GuiTextures.FISHING_BOX, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }
}
