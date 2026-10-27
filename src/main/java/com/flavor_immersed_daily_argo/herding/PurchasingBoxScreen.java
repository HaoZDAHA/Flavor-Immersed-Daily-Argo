package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.client.GuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

// 收购箱界面
//
// 底图见 GuiTextures#PURCHASING_BOX：上面是 3 行箱格，中间是玩家背包，
// 最下面一条是出售栏，出售按钮画在栏里。
public class PurchasingBoxScreen extends AbstractContainerScreen<PurchasingBoxMenu> {

    public PurchasingBoxScreen(PurchasingBoxMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = PurchasingBoxMenu.IMAGE_WIDTH;
        this.imageHeight = PurchasingBoxMenu.IMAGE_HEIGHT;
        this.inventoryLabelY = PurchasingBoxMenu.INVENTORY_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.flavor_immersed_daily_argo.purchasing_box.sell"),
                        button -> PacketDistributor.sendToServer(new PurchasingBoxSellPacket()))
                .bounds(this.leftPos + (this.imageWidth - PurchasingBoxMenu.SELL_BUTTON_WIDTH) / 2,
                        this.topPos + PurchasingBoxMenu.SELL_BUTTON_Y,
                        PurchasingBoxMenu.SELL_BUTTON_WIDTH, 18)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        GuiTextures.drawBackground(gui, GuiTextures.PURCHASING_BOX, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }
}
