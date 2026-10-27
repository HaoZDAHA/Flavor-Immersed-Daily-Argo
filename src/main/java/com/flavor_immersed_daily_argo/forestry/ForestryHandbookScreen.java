package com.flavor_immersed_daily_argo.forestry;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

// 护林员写字板界面。
//
// 左栏是全部林业网点的列表（名字 + 评分 + 状况，滚轮滚动），点击某一行后右栏显示该网点的
// 详细信息，以及「重命名」「解除网点」两个按钮。界面数据来自服务端全量同步的
// ForestryClientData。
public class ForestryHandbookScreen extends Screen {

// 版式（虚拟坐标，渲染时整体居中缩放）
    private static final int PANEL_W = 340;
    private static final int PANEL_H = 210;
    private static final float DESIGN_SCALE = 1.6F;
// 左栏列表范围
    private static final int LIST_LEFT = 8;
    private static final int LIST_RIGHT = 132;
    private static final int LIST_ROW_H = 22;
// 右栏详情范围
    private static final int DETAIL_LEFT = 142;
    private static final int DETAIL_RIGHT = PANEL_W - 10;
    private static final int TOP = 24;
    private static final int BOTTOM = PANEL_H - 10;
// 底部两个按钮：「重命名」「解除网点」
    private static final int BTN_W = 76;
    private static final int BTN_H = 16;
    private static final int BTN_Y = BOTTOM - BTN_H;
    private static final int RENAME_BTN_X = DETAIL_LEFT;
    private static final int REMOVE_BTN_X = DETAIL_LEFT + 88;

// 配色
    private static final int PANEL_FILL = 0xFFF2E6C8;
    private static final int PANEL_BORDER = 0xFF6B4F2A;
    private static final int HEADER_FILL = 0xFF7A5A32;
    private static final int ROW_SELECTED = 0x60D89A2B;
    private static final int ROW_HOVER = 0x30D89A2B;
    private static final int TEXT_TITLE = 0xFF3F2A12;
    private static final int TEXT_MAIN = 0xFF2F2F2F;
    private static final int TEXT_DIM = 0xFF7A6B52;
    private static final int TEXT_GOOD = 0xFF2C7A2C;
    private static final int TEXT_WARN = 0xFFB23A2A;
    private static final int BTN_FILL = 0xFF9C3B2E;
    private static final int BTN_HOVER = 0xFFB84A38;
    private static final int BTN_TEXT = 0xFFFFF3E0;

    private int panelX;
    private int panelY;
    private float uiScale;
    private int listTop;
    private int listBottom;
    private int listScroll;
    private ForestryPoint selected;

    public ForestryHandbookScreen() {
        super(Component.translatable("gui.flavor_immersed_daily_argo.forestry.title"));
    }

    @Override
    protected void init() {
        super.init();
        this.uiScale = computeScale();
        this.panelX = -PANEL_W / 2;
        this.panelY = -PANEL_H / 2;
        this.listTop = panelY + TOP + 14;
        this.listBottom = panelY + BOTTOM - 2;
        resolveSelection();
        clampScroll();
    }

    private List<ForestryPoint> points() {
        return ForestryClientData.points();
    }

// 保证选中项仍然存在于最新数据里（解除后会重新落到第一处）
    private void resolveSelection() {
        List<ForestryPoint> points = points();
        if (selected != null) {
            for (ForestryPoint point : points) {
                if (point.dimension().equals(selected.dimension()) && point.pos().equals(selected.pos())) {
                    selected = point;
                    return;
                }
            }
        }
        selected = points.isEmpty() ? null : points.get(0);
    }

    private void clampScroll() {
        int rows = (listBottom - listTop) / LIST_ROW_H;
        int maxScroll = Math.max(0, points().size() - rows);
        listScroll = Math.max(0, Math.min(listScroll, maxScroll));
    }

// 计算整体缩放：面板最多占窗口宽度的 96%、高度的 92%，且不超过设计缩放
    private float computeScale() {
        float byWidth = this.width * 0.96F / PANEL_W;
        float byHeight = this.height * 0.92F / PANEL_H;
        return Math.max(0.5F, Math.min(Math.min(byWidth, byHeight), DESIGN_SCALE));
    }

    private double toPanelX(double mx) {
        return (mx - this.width / 2.0) / this.uiScale;
    }

    private double toPanelY(double my) {
        return (my - this.height / 2.0) / this.uiScale;
    }

// 输入

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double bx = toPanelX(mouseX);
            double by = toPanelY(mouseY);
// 列表行
            if (bx >= panelX + LIST_LEFT && bx < panelX + LIST_RIGHT && by >= listTop && by < listBottom) {
                int idx = (int) ((by - listTop) / LIST_ROW_H);
                List<ForestryPoint> points = points();
                int absolute = listScroll + idx;
                if (idx >= 0 && absolute < points.size()) {
                    List<ForestryPoint> visible = visibleRows();
                    if (idx < visible.size()) {
                        selected = visible.get(idx);
                        return true;
                    }
                }
            }
// 重命名 / 解除按钮
            if (selected != null) {
                if (isOnButton(bx, by, RENAME_BTN_X)) {
                    openRename();
                    return true;
                }
                if (isOnButton(bx, by, REMOVE_BTN_X)) {
                    sendRemove();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isOnButton(double bx, double by, int localX) {
        return bx >= panelX + localX && bx < panelX + localX + BTN_W
                && by >= panelY + BTN_Y && by < panelY + BTN_Y + BTN_H;
    }

    private void openRename() {
        if (selected == null) {
            return;
        }
        Minecraft.getInstance().setScreen(new ForestryRenameScreen(selected, this));
    }

    private void sendRemove() {
        if (selected == null) {
            return;
        }
        PacketDistributor.sendToServer(new ForestryRemovePacket(selected.dimension(), selected.pos()));
        selected = null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        double bx = toPanelX(mouseX);
        double by = toPanelY(mouseY);
        if (bx >= panelX + LIST_LEFT && bx < panelX + LIST_RIGHT && by >= listTop && by < listBottom) {
            int rows = (listBottom - listTop) / LIST_ROW_H;
            int maxScroll = Math.max(0, points().size() - rows);
            listScroll = Math.max(0, Math.min(listScroll + (int) -verticalAmount, maxScroll));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, verticalAmount);
    }

    private List<ForestryPoint> visibleRows() {
        List<ForestryPoint> all = points();
        int rows = (listBottom - listTop) / LIST_ROW_H;
        int from = Math.min(listScroll, all.size());
        int to = Math.min(all.size(), from + rows);
        return from < to ? all.subList(from, to) : List.of();
    }

// 渲染

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        resolveSelection();
        clampScroll();
        double mx = toPanelX(mouseX);
        double my = toPanelY(mouseY);

        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
        pose.scale(this.uiScale, this.uiScale, 1.0F);

        drawPanel(gui, panelX, panelY, PANEL_W, PANEL_H);
        drawHeader(gui, panelX, panelY, PANEL_W);
        renderList(gui, mx, my);
        renderDetail(gui, mx, my);
        pose.popPose();
    }

    private void drawPanel(GuiGraphics gui, int x, int y, int w, int h) {
        gui.fill(x, y, x + w, y + h, PANEL_FILL);
        gui.fill(x, y, x + w, y + 1, PANEL_BORDER);
        gui.fill(x, y + h - 1, x + w, y + h, PANEL_BORDER);
        gui.fill(x, y, x + 1, y + h, PANEL_BORDER);
        gui.fill(x + w - 1, y, x + w, y + h, PANEL_BORDER);
    }

    private void drawHeader(GuiGraphics gui, int x, int y, int w) {
        gui.fill(x + 1, y + 1, x + w - 1, y + 15, HEADER_FILL);
        Component title = Component.translatable("gui.flavor_immersed_daily_argo.forestry.title");
        gui.drawString(font, title, x + (w - font.width(title)) / 2, y + 4, 0xFFF2E6C8, false);
    }

    private void renderList(GuiGraphics gui, double mouseX, double mouseY) {
        int left = panelX + LIST_LEFT;
        int right = panelX + LIST_RIGHT;
// 分栏标题
        gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.forestry.points"),
                left, panelY + TOP + 2, TEXT_TITLE, false);

        List<ForestryPoint> all = points();
        if (all.isEmpty()) {
            gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.forestry.empty"),
                    left, listTop + 2, TEXT_DIM, false);
            Component hint = Component.translatable("gui.flavor_immersed_daily_argo.forestry.hint");
            int hy = listTop + 14;
            for (var line : font.split(hint, LIST_RIGHT - LIST_LEFT)) {
                gui.drawString(font, line, left, hy, TEXT_DIM, false);
                hy += 10;
            }
            return;
        }

        List<ForestryPoint> rows = visibleRows();
        for (int i = 0; i < rows.size(); i++) {
            ForestryPoint point = rows.get(i);
            int rowY = listTop + i * LIST_ROW_H;
            boolean isSelected = point == selected;
            boolean hovered = mouseX >= left && mouseX < right && mouseY >= rowY && mouseY < rowY + LIST_ROW_H;
            if (isSelected) {
                gui.fill(left, rowY, right, rowY + LIST_ROW_H - 1, ROW_SELECTED);
            } else if (hovered) {
                gui.fill(left, rowY, right, rowY + LIST_ROW_H - 1, ROW_HOVER);
            }
            gui.drawString(font, truncate(nameOf(point), right - left - 6), left + 3, rowY + 2,
                    point.prosperous() ? TEXT_GOOD : TEXT_TITLE, false);
            Component sub = Component.literal(shortDimension(point.dimension()) + " · "
                    + Component.translatable("gui.flavor_immersed_daily_argo.forestry.score").getString()
                    + " " + point.score() + " · " + eventText(point));
            gui.drawString(font, sub, left + 3, rowY + 12, point.prosperous() ? TEXT_GOOD : TEXT_DIM, false);
        }
        if (all.size() > rows.size()) {
            gui.drawString(font, Component.literal("▼"), right - 10, listBottom - 10, TEXT_DIM, false);
        }
    }

    private void renderDetail(GuiGraphics gui, double mouseX, double mouseY) {
        int left = panelX + DETAIL_LEFT;
        gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.forestry.detail"),
                left, panelY + TOP + 2, TEXT_TITLE, false);
        if (selected == null) {
            gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.forestry.none"),
                    left, panelY + TOP + 20, TEXT_DIM, false);
            return;
        }

        int y = panelY + TOP + 20;
        y = drawAttr(gui, left, y, "gui.flavor_immersed_daily_argo.forestry.name",
                truncate(nameOf(selected), DETAIL_RIGHT - DETAIL_LEFT - 52).getString(), TEXT_MAIN);
        y = drawAttr(gui, left, y, "gui.flavor_immersed_daily_argo.forestry.dimension",
                shortDimension(selected.dimension()), TEXT_MAIN);
        y = drawAttr(gui, left, y, "gui.flavor_immersed_daily_argo.forestry.pos",
                selected.pos().getX() + ", " + selected.pos().getY() + ", " + selected.pos().getZ(), TEXT_MAIN);
        y = drawAttr(gui, left, y, "gui.flavor_immersed_daily_argo.forestry.score",
                selected.score() + " / " + ForestryPoint.MAX_SCORE,
                selected.prosperous() ? TEXT_GOOD : TEXT_WARN);
        y = drawAttr(gui, left, y, "gui.flavor_immersed_daily_argo.forestry.status",
                eventText(selected),
                selected.event() == ForestryEventType.NONE ? TEXT_MAIN : TEXT_WARN);
// 水体情况：水质灾难不直接体现在事件位，由玩家用水质检测器测量后上报到这里
        y = drawAttr(gui, left, y, "gui.flavor_immersed_daily_argo.forestry.water_status",
                waterText(selected),
                selected.waterReport() == ForestryEventType.NONE ? TEXT_MAIN : TEXT_WARN);

// 兴盛加成
        if (selected.prosperous()) {
            gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.forestry.bonus"),
                    left, y + 4, TEXT_GOOD, false);
            int by = y + 14;
            for (var line : font.split(
                    Component.translatable("gui.flavor_immersed_daily_argo.forestry.bonus_text"),
                    DETAIL_RIGHT - DETAIL_LEFT)) {
                gui.drawString(font, line, left, by, TEXT_MAIN, false);
                by += 10;
            }
        }

// 底部按钮：重命名 / 解除
        drawButton(gui, mouseX, mouseY, RENAME_BTN_X,
                Component.translatable("gui.flavor_immersed_daily_argo.forestry.rename"));
        drawButton(gui, mouseX, mouseY, REMOVE_BTN_X,
                Component.translatable("gui.flavor_immersed_daily_argo.forestry.remove"));
    }

    private void drawButton(GuiGraphics gui, double mouseX, double mouseY, int localX, Component label) {
        boolean hovered = isOnButton(mouseX, mouseY, localX);
        int bx = panelX + localX;
        int by = panelY + BTN_Y;
        gui.fill(bx, by, bx + BTN_W, by + BTN_H, hovered ? BTN_HOVER : BTN_FILL);
        gui.drawString(font, label, bx + (BTN_W - font.width(label)) / 2, by + 4, BTN_TEXT, false);
    }

// 网点名字：玩家起过名字就用名字，否则用按坐标生成的默认名
    private Component nameOf(ForestryPoint point) {
        String name = point.name();
        if (name != null && !name.isEmpty()) {
            return Component.literal(name);
        }
        return Component.translatable("gui.flavor_immersed_daily_argo.forestry.default_name",
                point.pos().getX(), point.pos().getZ());
    }

// 按像素宽度截断，避免过长的名字撑出面板
    private Component truncate(Component text, int maxWidth) {
        return Component.literal(font.plainSubstrByWidth(text.getString(), maxWidth));
    }

    private int drawAttr(GuiGraphics gui, int x, int y, String labelKey, String value, int valueColor) {
        Component label = Component.translatable(labelKey);
        gui.drawString(font, label, x, y, TEXT_DIM, false);
        gui.drawString(font, Component.literal(value), x + 52, y, valueColor, false);
        return y + 12;
    }

    private static String eventText(ForestryPoint point) {
        return Component.translatable(point.event().translationKey()).getString();
    }

// 水体情况：玩家用水质检测器上报的水体灾难；未上报时显示「正常」
    private static String waterText(ForestryPoint point) {
        ForestryEventType report = point.waterReport();
        if (report == ForestryEventType.NONE) {
            return Component.translatable("gui.flavor_immersed_daily_argo.forestry.water.normal").getString();
        }
        return Component.translatable(report.translationKey()).getString();
    }

// 维度名只显示路径部分，避免「minecraft:overworld」把行撑满
    private static String shortDimension(ResourceLocation dimension) {
        return dimension.getPath();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
