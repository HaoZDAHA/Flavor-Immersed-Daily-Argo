package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.client.GuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// 鸡窝界面
//
// 底图见 GuiTextures#CHICKEN_COOP，槽位凹槽已经画在图上，标签仍由代码绘制。
public class ChickenCoopScreen extends AbstractContainerScreen<ChickenCoopMenu> {

// 槽位列中心，标签按它居中
    private static final int CENTER_X = ChickenCoopMenu.IMAGE_WIDTH / 2;

// 标签颜色，与界面标题的默认观感接近
    private static final int LABEL_COLOR = 0xE0E0E0;

    public ChickenCoopScreen(ChickenCoopMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = ChickenCoopMenu.IMAGE_WIDTH;
        this.imageHeight = ChickenCoopMenu.IMAGE_HEIGHT;
        this.inventoryLabelY = ChickenCoopMenu.INVENTORY_LABEL_Y;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        GuiTextures.drawBackground(gui, GuiTextures.CHICKEN_COOP, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderLabels(gui, mouseX, mouseY);
        drawCentered(gui, "gui.flavor_immersed_daily_argo.coop_hatch", 8);
        drawCentered(gui, "gui.flavor_immersed_daily_argo.coop_collect", 34);
    }

// 以槽位列中心为准，把标签水平居中画在指定高度（坐标相对界面左上角）
    private void drawCentered(GuiGraphics gui, String key, int y) {
        Component text = Component.translatable(key);
        gui.drawString(this.font, text, CENTER_X - this.font.width(text) / 2, y, LABEL_COLOR);
    }
}
