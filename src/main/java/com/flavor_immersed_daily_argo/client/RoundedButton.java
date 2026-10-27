package com.flavor_immersed_daily_argo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

// 圆角、无原版贴图的按钮，画风见 GuiStyle#button。
//
// 三种样式：普通（深灰玻璃）、主操作（金色描边，用于结账 / 存入）、危险（红色描边，用于删除）。
// selected 用于页签：选中时整颗按钮变成金色描边 + 金色文字。
public class RoundedButton extends Button {

    public enum Style {
// 普通次级按钮
        NORMAL,
// 主操作按钮（金色）
        ACCENT,
// 危险操作按钮（红色）
        DANGER
    }

    private final Style style;
    private boolean selected;

    private RoundedButton(int x, int y, int width, int height, Component message,
                          Style style, boolean selected, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.style = style;
        this.selected = selected;
    }

    public static RoundedButton of(int x, int y, int width, int height, Component message, Runnable onClick) {
        return of(x, y, width, height, message, Style.NORMAL, false, onClick);
    }

    public static RoundedButton of(int x, int y, int width, int height, Component message,
                                   Style style, Runnable onClick) {
        return of(x, y, width, height, message, style, false, onClick);
    }

    public static RoundedButton of(int x, int y, int width, int height, Component message,
                                   Style style, boolean selected, Runnable onClick) {
        return new RoundedButton(x, y, width, height, message, style, selected, b -> onClick.run());
    }

// 页签用：切换选中态后下一帧即换画风
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHoveredOrFocused() && this.active;
        GuiStyle.button(gui, getX(), getY(), getWidth(), getHeight(), this.style, this.selected, hovered, this.active);
        int color = GuiStyle.buttonText(this.style, this.selected, this.active);
        gui.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, color);
    }
}
