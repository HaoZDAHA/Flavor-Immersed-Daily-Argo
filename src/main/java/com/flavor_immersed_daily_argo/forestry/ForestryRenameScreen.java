package com.flavor_immersed_daily_argo.forestry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

// 给林业网点命名的界面：一个输入框加一个确认按钮，回车也能确认。
//
// 与家养动物的取名界面同款。确认后把新名字打包发给服务端写入，本类不直接改网点数据。
// 名字留空即恢复默认名字（界面上按坐标生成）。关闭后回到打开它的写字板界面。
public class ForestryRenameScreen extends Screen {

    private static final int FIELD_WIDTH = 200;
    private static final int BACKGROUND = 0x90000000;

    private final ForestryPoint point;
    private final Screen parent;
    private EditBox nameField;

    public ForestryRenameScreen(ForestryPoint point, @Nullable Screen parent) {
        super(Component.translatable("gui.flavor_immersed_daily_argo.forestry.rename.title"));
        this.point = point;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = (this.width - FIELD_WIDTH) / 2;
        int top = this.height / 2;
        this.nameField = new EditBox(this.font, left, top - 10, FIELD_WIDTH, 20,
                Component.translatable("gui.flavor_immersed_daily_argo.rename.hint"));
        this.nameField.setMaxLength(ForestryPoint.MAX_NAME_LENGTH);
        this.nameField.setValue(this.point.name());
        this.addRenderableWidget(this.nameField);
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.flavor_immersed_daily_argo.rename.confirm"), button -> this.submit())
                .bounds(left, top + 16, FIELD_WIDTH, 20)
                .build());
        this.setInitialFocus(this.nameField);
    }

    private void submit() {
        PacketDistributor.sendToServer(
                new ForestryRenamePacket(this.point.dimension(), this.point.pos(), this.nameField.getValue().trim()));
        this.onClose();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
// 用纯色遮罩代替原版的模糊背景
        guiGraphics.fill(0, 0, this.width, this.height, BACKGROUND);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
