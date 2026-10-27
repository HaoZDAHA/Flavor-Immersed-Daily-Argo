package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.client.GuiTextures;
import com.flavor_immersed_daily_argo.all.ModDataComponents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

// 杆秤界面
//
// 与模组里其它容器一样是代码绘制的暗色底；秤盘下方一行字显示称出来的重量。
public class SteelyardScreen extends AbstractContainerScreen<SteelyardMenu> {

    private static final int LABEL_COLOR = 0xE0E0E0;

    public SteelyardScreen(SteelyardMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = SteelyardMenu.IMAGE_WIDTH;
        this.imageHeight = SteelyardMenu.IMAGE_HEIGHT;
        this.inventoryLabelY = SteelyardMenu.INVENTORY_LABEL_Y;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        GuiTextures.drawBackground(gui, GuiTextures.STEELYARD, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderLabels(gui, mouseX, mouseY);
        ItemStack stack = this.menu.panItem();
        Component text;
        if (stack.isEmpty()) {
            text = Component.translatable("gui.flavor_immersed_daily_argo.steelyard_empty");
        } else {
            Double weight = stack.get(ModDataComponents.WEIGHED_WEIGHT.get());
            text = weight == null
                    ? Component.translatable("gui.flavor_immersed_daily_argo.steelyard_unweighable")
                    : Component.translatable("gui.flavor_immersed_daily_argo.steelyard_weight",
                            SteelyardItem.format(weight));
        }
        int centerX = SteelyardMenu.PAN_X + 8;
        gui.drawString(this.font, text, centerX - this.font.width(text) / 2, SteelyardMenu.PAN_Y + 26, LABEL_COLOR);
    }
}
