package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.client.GuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// 昆虫培育箱界面
//
// 底图见 GuiTextures#INSECT_INCUBATOR，四个分区与槽位凹槽都画在图上，
// 只有进度条是运行时按数据填色的。
public class InsectIncubatorScreen extends AbstractContainerScreen<InsectIncubatorMenu> {

// 进度条：轨道 + 已培育部分
    private static final int PROGRESS_TRACK = 0xFF3A3A3A;
    private static final int PROGRESS_FILL = 0xFF4CAF50;

// 标签颜色，与界面标题的默认观感接近
    private static final int LABEL_COLOR = 0xE0E0E0;

    public InsectIncubatorScreen(InsectIncubatorMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = InsectIncubatorMenu.IMAGE_WIDTH;
        this.imageHeight = InsectIncubatorMenu.IMAGE_HEIGHT;
        this.inventoryLabelY = InsectIncubatorMenu.INVENTORY_LABEL_Y;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        GuiTextures.drawBackground(gui, GuiTextures.INSECT_INCUBATOR, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        drawProgress(gui, this.leftPos, this.topPos);
    }

// 培育进度条：跑满一次就转化一次，表现上类似作物生长
    private void drawProgress(GuiGraphics gui, int left, int top) {
        int barX = left + InsectIncubatorMenu.PROGRESS_X;
        int barY = top + InsectIncubatorMenu.PROGRESS_Y;
        int width = InsectIncubatorMenu.PROGRESS_WIDTH;
        int height = InsectIncubatorMenu.PROGRESS_HEIGHT;
        gui.fill(barX, barY, barX + width, barY + height, PROGRESS_TRACK);

        int max = this.menu.getMaxProgress();
        if (max <= 0) {
            return;
        }
        int filled = Math.min(width, width * this.menu.getProgress() / max);
        gui.fill(barX, barY, barX + filled, barY + height, PROGRESS_FILL);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderLabels(gui, mouseX, mouseY);
        drawCentered(gui, "gui.flavor_immersed_daily_argo.insect_incubator_feed",
                InsectIncubatorMenu.FEED_LABEL_X, InsectIncubatorMenu.FEED_LABEL_Y);
        drawCentered(gui, "gui.flavor_immersed_daily_argo.insect_incubator_carrier",
                InsectIncubatorMenu.CARRIER_LABEL_X, InsectIncubatorMenu.CARRIER_LABEL_Y);
        drawCentered(gui, "gui.flavor_immersed_daily_argo.insect_incubator_parents",
                InsectIncubatorMenu.PARENT_LABEL_X, InsectIncubatorMenu.PARENT_LABEL_Y);
        drawCentered(gui, "gui.flavor_immersed_daily_argo.insect_incubator_offspring",
                InsectIncubatorMenu.OFFSPRING_LABEL_X, InsectIncubatorMenu.OFFSPRING_LABEL_Y);
    }

// 以指定列中心为准，把标签水平居中画在指定高度（坐标相对界面左上角）
    private void drawCentered(GuiGraphics gui, String key, int centerX, int y) {
        Component text = Component.translatable(key);
        gui.drawString(this.font, text, centerX - this.font.width(text) / 2, y, LABEL_COLOR);
    }
}
