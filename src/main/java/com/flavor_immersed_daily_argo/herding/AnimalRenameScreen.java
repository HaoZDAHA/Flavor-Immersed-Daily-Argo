package com.flavor_immersed_daily_argo.herding;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

// 右键实体时打开的取名界面：一个输入框加一个确认按钮，回车也能确认。
//
// 家养动物（潜行右键）和稻草人（直接右键）共用这一个界面。确认后把名字打包发给服务端写入，
// 本类不直接改实体。名字留空即恢复默认名字。
public class AnimalRenameScreen extends Screen {

    private static final int FIELD_WIDTH = 200;
    private static final int MAX_NAME_LENGTH = 32;
    private static final int BACKGROUND = 0x90000000;

    private final Entity target;
    private EditBox nameField;

    public AnimalRenameScreen(Entity target) {
        super(Component.translatable("gui.flavor_immersed_daily_argo.rename.title"));
        this.target = target;
    }

    @Override
    protected void init() {
        int left = (this.width - FIELD_WIDTH) / 2;
        int top = this.height / 2;
        this.nameField = new EditBox(this.font, left, top - 10, FIELD_WIDTH, 20,
                Component.translatable("gui.flavor_immersed_daily_argo.rename.hint"));
        this.nameField.setMaxLength(MAX_NAME_LENGTH);
        if (this.target.hasCustomName()) {
            this.nameField.setValue(this.target.getCustomName().getString());
        }
        this.addRenderableWidget(this.nameField);
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.flavor_immersed_daily_argo.rename.confirm"), button -> this.submit())
                .bounds(left, top + 16, FIELD_WIDTH, 20)
                .build());
        this.setInitialFocus(this.nameField);
    }

    private void submit() {
        PacketDistributor.sendToServer(
                new AnimalRenamePacket(this.target.getId(), this.nameField.getValue().trim()));
        this.onClose();
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
