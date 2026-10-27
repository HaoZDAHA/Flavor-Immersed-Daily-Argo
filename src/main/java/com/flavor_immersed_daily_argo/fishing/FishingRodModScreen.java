package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.client.GuiTextures;
import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;

public class FishingRodModScreen extends AbstractContainerScreen<FishingRodModMenu> {

    private static final int IMG_W = 300;
    private static final int IMG_H = 260;
// 文字颜色：原版 §b 的天蓝
    private static final int TEXT_COLOR = 0x55FFFF;

    private String customName = "";
    private float rotation;

    public FishingRodModScreen(FishingRodModMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = IMG_W;
        this.imageHeight = IMG_H;
    }

    @Override
    protected void init() {
        super.init();

// 名称输入框已去掉，打开时把钓竿现有名称原样带回来，关闭时再原样交还给服务端
        this.customName = this.menu.getCustomName();

// 保存确定按钮：把名称交给服务端保存后关闭界面
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.flavor_immersed_daily_argo.rod_save"),
                        b -> {
                            PacketDistributor.sendToServer(new FishingRodModSavePacket(customName));
                            this.onClose();
                        })
                .bounds(leftPos + 215, topPos + 128, 80, 18)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        GuiTextures.drawBackground(gui, GuiTextures.ROD_MOD, leftPos, topPos, IMG_W, IMG_H);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);

// 界面标题（左上）
        gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.rod_mod"),
                leftPos + 8, topPos + 12, TEXT_COLOR);

// 旋转的钓鱼竿模型（两排槽位之间的空隙展示，比原来缩小避免遮挡槽位）
        rotation += partialTick * 2.0f;
        if (rotation >= 360f) rotation -= 360f;

        PoseStack pose = gui.pose();
        pose.pushPose();
        int cx = leftPos + 160;
        int cy = topPos + 78;
        pose.translate(cx, cy, 150);
        pose.scale(35f, -35f, 35f);
        pose.mulPose(new Quaternionf().rotateY((float) Math.toRadians(rotation)));
        pose.mulPose(new Quaternionf().rotateX((float) Math.toRadians(20)));
// 展示用钓竿带上当前槽位数据，贴图才会跟物品栏同步（装了竿体即换成竿体贴图）
        ItemStack rodDisplay = new ItemStack(ModItems.MOD_FISHING_ROD.get());
        ModDataComponents.ROD_MOD_DATA.get().set(rodDisplay,
                new FishingRodModData(customName, this.menu.currentMods()));
        Minecraft.getInstance().getItemRenderer().renderStatic(
                rodDisplay, ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT, LightTexture.FULL_SKY,
                pose, gui.bufferSource(), null, 0);
        pose.popPose();

// 槽位标签（居中对齐在各槽位上方）
        drawCenteredLabel(gui, "gui.flavor_immersed_daily_argo.line", 105, 33);
        drawCenteredLabel(gui, "gui.flavor_immersed_daily_argo.body", 160, 33);
        drawCenteredLabel(gui, "gui.flavor_immersed_daily_argo.reel", 215, 33);
        drawCenteredLabel(gui, "gui.flavor_immersed_daily_argo.bait", 105, 93);
        drawCenteredLabel(gui, "gui.flavor_immersed_daily_argo.floater", 160, 93);
        drawCenteredLabel(gui, "gui.flavor_immersed_daily_argo.sinker", 215, 93);

        gui.drawString(font, playerInventoryTitle, leftPos + 69, topPos + 160, TEXT_COLOR);

        this.renderTooltip(gui, mouseX, mouseY);
    }

// 以槽位中心 x 为准，把标签水平居中画在其上方
    private void drawCenteredLabel(GuiGraphics gui, String key, int slotX, int y) {
        Component text = Component.translatable(key);
        gui.drawString(font, text, leftPos + slotX - font.width(text) / 2, topPos + y, TEXT_COLOR);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
// 标题不画，交给 render() 处理
    }

    @Override
    public void onClose() {
// 配件在放入槽位时就已由服务端写回钓竿，这里只需再同步一次名称
        PacketDistributor.sendToServer(new FishingRodModSavePacket(customName));
        super.onClose();
    }
}
