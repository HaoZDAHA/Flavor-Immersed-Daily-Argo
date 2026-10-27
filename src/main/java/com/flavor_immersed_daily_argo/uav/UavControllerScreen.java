package com.flavor_immersed_daily_argo.uav;

import com.flavor_immersed_daily_argo.entity.Uav;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import javax.annotation.Nullable;

// 无人机遥控器的操作界面：解绑 / 第一人称操作 / 电源 / 物品栏四个按钮。
//
// 按钮只是发一个 UavActionPacket，实体状态全在服务端改；界面上的「电源：开 / 关」按同步下来的
// 状态显示，按一下之后重建按钮就能立刻翻面。
public class UavControllerScreen extends Screen {

    private static final int BACKGROUND = 0x90000000;
    private static final int BUTTON_WIDTH = 170;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 6;

    private final int uavId;

// 刚点过电源、实体状态还没同步回来的本地预期；null 表示以实体为准。
//
// 电源是在服务端改的，包一来一回之前 Uav#isPowered() 还是旧值；不记下这一下，
// 按钮会一直显示旧文字，得关掉界面再打开才看得到变化。
    @Nullable
    private Boolean pendingPower;

    public UavControllerScreen(Uav uav) {
        super(Component.translatable("gui.flavor_immersed_daily_argo.uav.title"));
        this.uavId = uav.getId();
    }

    @Nullable
    private Uav uav() {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        return Minecraft.getInstance().level.getEntity(this.uavId) instanceof Uav uav ? uav : null;
    }

    private void send(byte action) {
        PacketDistributor.sendToServer(new UavActionPacket(this.uavId, action, 0, 0, 0, 0, 0, 0));
    }

    @Override
    protected void init() {
        int left = (this.width - BUTTON_WIDTH) / 2;
        int top = this.height / 2 - 49;
        int step = BUTTON_HEIGHT + GAP;

// 解绑无人机
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.flavor_immersed_daily_argo.uav.unbind"),
                        button -> {
                            this.send(UavActionPacket.UNBIND);
                            this.onClose();
                        })
                .bounds(left, top, BUTTON_WIDTH, BUTTON_HEIGHT).build());

// 第一人称操作：相机切到无人机身上
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.flavor_immersed_daily_argo.uav.operate"),
                        button -> {
                            Uav uav = this.uav();
                            if (uav != null) {
                                this.send(UavActionPacket.OPERATE);
                                UavClient.beginOperating(uav);
                            }
                            this.onClose();
                        })
                .bounds(left, top + step, BUTTON_WIDTH, BUTTON_HEIGHT).build());

// 电源：按一下开关，按钮文字跟着当前状态翻面。
// 服务端还没把新状态同步回来时先按刚才那一下的预期显示，不必关掉界面再打开。
        Uav uav = this.uav();
        boolean actual = uav != null && uav.isPowered();
        if (this.pendingPower != null && this.pendingPower == actual) {
            this.pendingPower = null;
        }
        boolean powered = this.pendingPower != null ? this.pendingPower : actual;
        this.addRenderableWidget(Button.builder(
                        Component.translatable(powered
                                ? "gui.flavor_immersed_daily_argo.uav.power_on"
                                : "gui.flavor_immersed_daily_argo.uav.power_off"),
                        button -> {
                            this.pendingPower = !powered;
                            this.send(UavActionPacket.TOGGLE_POWER);
                            this.rebuildWidgets();
                        })
                .bounds(left, top + step * 2, BUTTON_WIDTH, BUTTON_HEIGHT).build());

// 物品栏：模组槽 + 使用槽 + 背包槽，界面由服务端开
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.flavor_immersed_daily_argo.uav.inventory"),
                        button -> {
                            this.send(UavActionPacket.OPEN_INVENTORY);
                            this.onClose();
                        })
                .bounds(left, top + step * 3, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
// 用纯色遮罩代替原版的模糊背景
        guiGraphics.fill(0, 0, this.width, this.height, BACKGROUND);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 67, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
