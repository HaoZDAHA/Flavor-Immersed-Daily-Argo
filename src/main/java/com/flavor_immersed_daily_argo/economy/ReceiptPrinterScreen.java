package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.client.GuiStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

// 小票打印机界面：8×5 共 40 个只展示的槽位，右上角一个出单开关。
//
// 底图与其他经济类界面一致，由 GuiStyle 现画：半透明黑圆角底板 + 自绘物品格；
// 出单开关也改成同一套圆角画风。开关拨开之后机器才会进入出单态。
public class ReceiptPrinterScreen extends AbstractContainerScreen<ReceiptPrinterMenu> {

// 出单开关：位置（相对界面左上角）与尺寸，独占菜单与背包之间的一行
    private static final int SWITCH_X = 8;
    private static final int SWITCH_Y = 116;
    private static final int SWITCH_W = 30;
    private static final int SWITCH_H = 12;
// 开关拨动滑块与轨道、文字的颜色
    private static final int SWITCH_ON_COLOR = 0xFF3AA655;
    private static final int SWITCH_OFF_COLOR = 0xFF3A3F46;
    private static final int SWITCH_KNOB_COLOR = 0xFFEDEDED;
    private static final int SWITCH_TEXT_COLOR = GuiStyle.TEXT;
// 开关状态文字与轨道之间的间距
    private static final int SWITCH_TEXT_GAP = 6;

    private static final String KEY_SWITCH_ON = "gui.flavor_immersed_daily_argo.receipt_printer.switch_on";
    private static final String KEY_SWITCH_OFF = "gui.flavor_immersed_daily_argo.receipt_printer.switch_off";

    public ReceiptPrinterScreen(ReceiptPrinterMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = ReceiptPrinterMenu.IMAGE_WIDTH;
        this.imageHeight = ReceiptPrinterMenu.IMAGE_HEIGHT;
        this.inventoryLabelY = ReceiptPrinterMenu.INVENTORY_LABEL_Y;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        GuiStyle.panel(gui, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
// 40 个展示格 + 玩家背包格，全部用统一的圆角凹槽
        for (Slot slot : this.menu.slots) {
            GuiStyle.slot(gui, this.leftPos + slot.x, this.topPos + slot.y);
        }
        drawSwitch(gui, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
// 黑底上原版的深灰字看不见，标题与背包名统一换成新画风的颜色
        gui.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, GuiStyle.TEXT);
        gui.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                GuiStyle.TEXT_DIM);
    }

// 出单开关：圆角胶囊轨道 + 圆形滑块，右边配一行状态文字
    private void drawSwitch(GuiGraphics gui, int mouseX, int mouseY) {
        boolean on = this.menu.isEnabled();
        int trackX = this.leftPos + SWITCH_X;
        int trackY = this.topPos + SWITCH_Y;
        boolean hover = isOverSwitch(mouseX, mouseY);

        GuiStyle.roundedRect(gui, trackX, trackY, SWITCH_W, SWITCH_H, SWITCH_H / 2,
                on ? SWITCH_ON_COLOR : SWITCH_OFF_COLOR);
        if (hover) {
            GuiStyle.roundedRect(gui, trackX, trackY, SWITCH_W, SWITCH_H, SWITCH_H / 2, 0x30FFFFFF);
        }

        int knobW = SWITCH_H - 4;
        int knobX = on ? trackX + SWITCH_W - knobW - 2 : trackX + 2;
        GuiStyle.roundedRect(gui, knobX, trackY + 2, knobW, knobW, knobW / 2, SWITCH_KNOB_COLOR);

        Component label = Component.translatable(on ? KEY_SWITCH_ON : KEY_SWITCH_OFF);
        gui.drawString(this.font, label, trackX + SWITCH_W + SWITCH_TEXT_GAP, trackY + 2,
                SWITCH_TEXT_COLOR, true);
    }

// 鼠标是不是在开关轨道上
    private boolean isOverSwitch(double mouseX, double mouseY) {
        return mouseX >= this.leftPos + SWITCH_X && mouseX < this.leftPos + SWITCH_X + SWITCH_W
                && mouseY >= this.topPos + SWITCH_Y && mouseY < this.topPos + SWITCH_Y + SWITCH_H;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isOverSwitch(mouseX, mouseY)) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ReceiptPrinterMenu.BUTTON_TOGGLE);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
