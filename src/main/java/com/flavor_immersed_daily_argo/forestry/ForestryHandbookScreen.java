package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.client.GuiStyle;
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

// 配色：深松绿面板 + 金色强调，保留林场的清新感但整体压深
    private static final int PANEL_TOP = 0xFF26362A;
    private static final int PANEL_BOTTOM = 0xFF131B15;
    private static final int PANEL_BORDER = 0xFF4C6E55;
    private static final int PANEL_HIGHLIGHT = 0x26FFFFFF;
    private static final int ACCENT = 0xFFE7C277;
    private static final int ACCENT_DEEP = 0xFFB8913F;
    private static final int ACCENT_LIGHT = 0xFFF4DCA0;

// 列表行：深绿渐变卡片，悬停稍亮，选中转琥珀
    private static final int ROW_BORDER = 0xFF354B3C;
    private static final int ROW_TOP = 0xFF212E24;
    private static final int ROW_BOTTOM = 0xFF182119;
    private static final int ROW_HOVER_TOP = 0xFF2A3A2D;
    private static final int ROW_HOVER_BOTTOM = 0xFF202C23;
    private static final int ROW_SELECTED_TOP = 0xFF3C4D2E;
    private static final int ROW_SELECTED_BOTTOM = 0xFF2B3823;

// 右栏详情卡：比面板再深一层的凹陷
    private static final int CARD_TOP = 0xFF1B251D;
    private static final int CARD_BOTTOM = 0xFF141C16;
    private static final int CARD_BORDER = 0xFF33493A;

    private static final int TEXT_TITLE = 0xFFEDE6D2;
    private static final int TEXT_MAIN = 0xFFD6DCCF;
    private static final int TEXT_DIM = 0xFF8C9A8B;
    private static final int TEXT_GOOD = 0xFF8FD48F;
    private static final int TEXT_WARN = 0xFFE88A6E;

// 面板与顶栏的圆角（小圆角更接近像素风）
    private static final int PANEL_CORNER = 6;
    private static final int HEADER_H = 18;

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
// 外投影 + 深绿描边 + 深绿渐变 + 顶端一道内高光，做出像素边框的层次
        GuiStyle.roundedRect(gui, x - 3, y - 2, w + 6, h + 7, PANEL_CORNER + 3, 0x40000000);
        GuiStyle.roundedRect(gui, x - 1, y - 1, w + 2, h + 2, PANEL_CORNER + 1, PANEL_BORDER);
        GuiStyle.roundedGradient(gui, x, y, w, h, PANEL_CORNER, PANEL_TOP, PANEL_BOTTOM);
        GuiStyle.roundedRect(gui, x + 2, y + 2, w - 4, 1, 0, PANEL_HIGHLIGHT);
    }

// 顶栏沿用经济机器的版式：左侧金色渐变竖条 + 金色标题，下面一条细分隔线
    private void drawHeader(GuiGraphics gui, int x, int y, int w) {
        GuiStyle.roundedGradient(gui, x + 9, y + 5, 3, 11, 1, ACCENT_LIGHT, ACCENT_DEEP);
        Component title = Component.translatable("gui.flavor_immersed_daily_argo.forestry.title");
        gui.drawString(font, title, x + 16, y + 7, ACCENT, false);
        GuiStyle.roundedRect(gui, x + 10, y + HEADER_H + 5, w - 20, 1, 0, 0x40E7C277);
    }

    private void renderList(GuiGraphics gui, double mouseX, double mouseY) {
        int left = panelX + LIST_LEFT;
        int right = panelX + LIST_RIGHT;
// 分栏标题
        gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.forestry.points"),
                left, panelY + TOP + 2, ACCENT, false);

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
            int rowW = right - left;
            int rowH = LIST_ROW_H - 2;
            if (isSelected) {
// 选中：琥珀描边 + 深绿渐变 + 左侧金条
                GuiStyle.roundedRect(gui, left, rowY, rowW, rowH, 4, ACCENT_DEEP);
                GuiStyle.roundedGradient(gui, left + 1, rowY + 1, rowW - 2, rowH - 2, 3,
                        ROW_SELECTED_TOP, ROW_SELECTED_BOTTOM);
                GuiStyle.roundedRect(gui, left + 2, rowY + 3, 3, rowH - 6, 1, ACCENT);
            } else {
                GuiStyle.roundedRect(gui, left, rowY, rowW, rowH, 4, ROW_BORDER);
                GuiStyle.roundedGradient(gui, left + 1, rowY + 1, rowW - 2, rowH - 2, 3,
                        hovered ? ROW_HOVER_TOP : ROW_TOP, hovered ? ROW_HOVER_BOTTOM : ROW_BOTTOM);
            }
            gui.drawString(font, truncate(nameOf(point), right - left - 12), left + 7, rowY + 2,
                    point.prosperous() ? TEXT_GOOD : TEXT_TITLE, false);
            Component sub = Component.literal(shortDimension(point.dimension()) + " · "
                    + Component.translatable("gui.flavor_immersed_daily_argo.forestry.score").getString()
                    + " " + point.score() + " · " + eventText(point));
            gui.drawString(font, sub, left + 7, rowY + 12, point.prosperous() ? TEXT_GOOD : TEXT_DIM, false);
        }
        if (all.size() > rows.size()) {
            gui.drawString(font, Component.literal("▼"), right - 10, listBottom - 10, TEXT_DIM, false);
        }
    }

    private void renderDetail(GuiGraphics gui, double mouseX, double mouseY) {
        int left = panelX + DETAIL_LEFT;
// 右栏垫一张白色圆角卡片，把详情与纸面分开
        int cardX = left - 6;
        int cardY = panelY + TOP - 3;
        int cardW = DETAIL_RIGHT - DETAIL_LEFT + 12;
        int cardH = PANEL_H - TOP - 5;
        GuiStyle.roundedRect(gui, cardX, cardY, cardW, cardH, 5, CARD_BORDER);
        GuiStyle.roundedGradient(gui, cardX + 1, cardY + 1, cardW - 2, cardH - 2, 4,
                CARD_TOP, CARD_BOTTOM);
        GuiStyle.roundedRect(gui, cardX + 3, cardY + 3, cardW - 6, 1, 0, 0x14FFFFFF);
        gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.forestry.detail"),
                left, panelY + TOP + 2, ACCENT, false);
        GuiStyle.roundedRect(gui, left - 2, panelY + TOP + 13, DETAIL_RIGHT - DETAIL_LEFT - 2, 1, 0,
                0x33E7C277);
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
        drawButton(gui, mouseX, mouseY, RENAME_BTN_X, false,
                Component.translatable("gui.flavor_immersed_daily_argo.forestry.rename"));
        drawButton(gui, mouseX, mouseY, REMOVE_BTN_X, true,
                Component.translatable("gui.flavor_immersed_daily_argo.forestry.remove"));
    }

    private void drawButton(GuiGraphics gui, double mouseX, double mouseY, int localX, boolean danger,
                            Component label) {
        boolean hovered = isOnButton(mouseX, mouseY, localX);
        int bx = panelX + localX;
        int by = panelY + BTN_Y;
// 金色（重命名）/ 暗红（解除）两种渐变，都带一圈描边
        int top = danger ? (hovered ? 0xFFC4675A : 0xFFAE5447) : (hovered ? 0xFFF4D08A : 0xFFE5BE72);
        int bottom = danger ? (hovered ? 0xFF9E4438 : 0xFF883A30) : (hovered ? 0xFFD9A94C : 0xFFC4953F);
        int border = danger ? 0xFFD98C7D : 0xFFF3DDB0;
        GuiStyle.roundedRect(gui, bx, by, BTN_W, BTN_H, 4, border);
        GuiStyle.roundedGradient(gui, bx + 1, by + 1, BTN_W - 2, BTN_H - 2, 3, top, bottom);
        int textColor = danger ? 0xFFFFEDE5 : 0xFF2A1F0C;
        gui.drawString(font, label, bx + (BTN_W - font.width(label)) / 2, by + 4, textColor, false);
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
