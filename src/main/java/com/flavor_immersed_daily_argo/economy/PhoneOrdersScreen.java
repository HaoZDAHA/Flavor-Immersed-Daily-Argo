package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.client.GuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

// 导航手机展开后弹出的接单界面。
//
// 一条条列出手上的外卖单（起点 / 终点 xz、配送费、剩余时间、距离），每行右侧有个「接单」按钮：
// 点下之后服务端才在出发地摆出外卖袋、在目的地放好配送点。没接单的行显示「玩家到出发地」「出发地
// 到目的地」两段距离；已接单的行显示玩家到两端的距离，并把「下一个该去的点」高亮出来，多出一颗
// 「取消」按钮（点它把这单整个作废）。点已接单的行内别处可选中 / 取消选中追踪——HUD 指南针优先指
// 向被追踪的那条。
//
// 画风模仿外卖软件：明黄顶栏 + 白卡片 + 橙色金额，面板与卡片都是圆角；底部提示按面板宽度自动折行，
// 不会撑出界面。界面覆写 #renderBackground 改画纯色遮罩，因此世界不会糊成一片。
public class PhoneOrdersScreen extends Screen {

    private static final int PANEL_WIDTH = 320;
    private static final int ROW_HEIGHT = 48;
    private static final int HEADER_HEIGHT = 38;
// 顶栏实际显示的黄色高度（下面一段被白色盖掉，只留上面圆角）
    private static final int HEADER_BAND = 28;
    private static final int PANEL_VERTICAL_MARGIN = 36;
    private static final int PANEL_SIDE_MARGIN = 12;
// 面板圆角
    private static final int PANEL_CORNER = 12;

    private static final int BUTTON_WIDTH = 56;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SIDE_MARGIN = 12;

// 行内文字的缩进
    private static final int TEXT_INDENT = 7;
    private static final int ARROW_INDENT = 6;
// 右侧留给「配送费 / 时间」的横向宽度
    private static final int RIGHT_TEXT_ROOM = 78;

    private static final int COLOR_SCRIM = 0x5A000000;
    private static final int COLOR_PANEL = 0xFCFFFFFF;
    private static final int COLOR_SHADOW = 0x33000000;
// 美团黄
    private static final int COLOR_HEADER = 0xFFFFD100;
    private static final int COLOR_TITLE = 0xFF2B2B2B;
    private static final int COLOR_TEXT = 0xFF2F2F2F;
    private static final int COLOR_DIM = 0xFF8C8C8C;
    private static final int COLOR_MONEY = 0xFFFF6A00;
    private static final int COLOR_DIVIDER = 0x11000000;

// 白卡片
    private static final int COLOR_CARD = 0xFFFFFFFF;
    private static final int COLOR_CARD_BORDER = 0x14000000;
    private static final int COLOR_CARD_HOVER = 0xFFFFF8DE;
    private static final int COLOR_CARD_HOVER_BORDER = 0x4DF5A623;

// 正在追踪的那一整行：淡黄底 + 左侧金条
    private static final int COLOR_TRACKED_BG = 0xFFFFF5C7;
    private static final int COLOR_TARGET = 0xFFE0A200;

// 接单按钮：黄到橙黄的渐变
    private static final int COLOR_ACCEPT_TOP = 0xFFFFE066;
    private static final int COLOR_ACCEPT_BOTTOM = 0xFFFFC400;
    private static final int COLOR_ACCEPT_BORDER = 0x40C99900;
    private static final int COLOR_ACCEPT_TEXT = 0xFF2B2B2B;

// 已接单标记
    private static final int COLOR_ACCEPTED_BG = 0x1FF5A623;
    private static final int COLOR_ACCEPTED_TEXT = 0xFF9A7B2E;

// 取消按钮：白底细边，文字红
    private static final int COLOR_CANCEL_FILL = 0xFFFFFAF8;
    private static final int COLOR_CANCEL_FILL_HOVER = 0xFFFFEDE8;
    private static final int COLOR_CANCEL_BORDER = 0x66D9482F;
    private static final int COLOR_CANCEL_TEXT = 0xFFD93025;

// 已接单那行右上：上面一小块「已接单」标、下面一颗「取消」按钮，叠着放
    private static final int ACCEPTED_CHIP_HEIGHT = 16;
    private static final int ACCEPTED_CHIP_GAP = 2;

    private static final String KEY_ORIGIN = "gui.flavor_immersed_daily_argo.nav_phone_origin";
    private static final String KEY_DEST = "gui.flavor_immersed_daily_argo.nav_phone_dest";
    private static final String KEY_REWARD = "gui.flavor_immersed_daily_argo.nav_phone_reward";
    private static final String KEY_ACCEPT = "gui.flavor_immersed_daily_argo.nav_phone_accept";
    private static final String KEY_ACCEPTED = "gui.flavor_immersed_daily_argo.nav_phone_accepted";
    private static final String KEY_CANCEL = "gui.flavor_immersed_daily_argo.nav_phone_cancel";
    private static final String KEY_DIST_YOU = "gui.flavor_immersed_daily_argo.nav_phone_dist_you";
    private static final String KEY_DIST_ROUTE = "gui.flavor_immersed_daily_argo.nav_phone_dist_route";
    private static final String KEY_EMPTY = "gui.flavor_immersed_daily_argo.nav_phone_empty";
    private static final String KEY_HINT = "gui.flavor_immersed_daily_argo.nav_phone_hint";

    private List<PhoneOrderView> orders;
// 列表首行的下标（订单太多时靠滚轮翻）
    private int scroll;

    public PhoneOrdersScreen(List<PhoneOrderView> orders) {
        super(Component.translatable("gui.flavor_immersed_daily_argo.nav_phone_title"));
        this.orders = orders;
    }

// 收到服务端下发的订单后调用：界面已开着就地刷新，否则新开一个
    public static void open(List<PhoneOrderView> orders) {
        if (Minecraft.getInstance().screen instanceof PhoneOrdersScreen screen) {
            screen.setOrders(orders);
        } else {
            Minecraft.getInstance().setScreen(new PhoneOrdersScreen(orders));
        }
    }

    public void setOrders(List<PhoneOrderView> orders) {
        this.orders = orders;
        clampScroll();
    }

    @Override
    protected void init() {
        super.init();
        clampScroll();
    }

// 版式

// 面板宽度：窄屏时收一点，别贴边
    private int panelWidth() {
        return Math.min(PANEL_WIDTH, this.width - PANEL_SIDE_MARGIN * 2);
    }

// 提示文字按面板宽度折行，行数决定底栏高度
    private List<FormattedCharSequence> hintLines() {
        return this.font.split(Component.translatable(KEY_HINT), panelWidth() - 28);
    }

    private int footerHeight() {
        return 8 + Math.max(1, hintLines().size()) * 10;
    }

// 屏幕放得下的行数（至少一行）
    private int maxRows() {
        int available = this.height - PANEL_VERTICAL_MARGIN - HEADER_HEIGHT - footerHeight();
        return Math.max(1, available / ROW_HEIGHT);
    }

    private int visibleRows() {
        return Math.min(this.orders.size(), maxRows());
    }

    private void clampScroll() {
        int max = Math.max(0, this.orders.size() - maxRows());
        this.scroll = Math.max(0, Math.min(this.scroll, max));
    }

    private int panelHeight() {
        return HEADER_HEIGHT + Math.max(1, visibleRows()) * ROW_HEIGHT + footerHeight();
    }

    private int panelLeft() {
        return (this.width - panelWidth()) / 2;
    }

    private int panelTop() {
        return (this.height - panelHeight()) / 2;
    }

    private int listTop() {
        return panelTop() + HEADER_HEIGHT;
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
// 用纯色遮罩代替原版的模糊背景
        graphics.fill(0, 0, this.width, this.height, COLOR_SCRIM);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
// super.render 会调用上面覆写的 renderBackground（纯色遮罩），不会糊
        super.render(graphics, mouseX, mouseY, partialTick);

        int left = panelLeft();
        int top = panelTop();
        int width = panelWidth();
        int height = panelHeight();

// 投影 + 白色圆角面板
        GuiStyle.roundedRect(graphics, left - 2, top + 1, width + 4, height + 4, PANEL_CORNER + 2, COLOR_SHADOW);
        GuiStyle.roundedRect(graphics, left - 1, top - 1, width + 2, height + 2, PANEL_CORNER + 1, 0x1A000000);
        GuiStyle.roundedRect(graphics, left, top, width, height, PANEL_CORNER, COLOR_PANEL);
        drawHeader(graphics, left, top, width);

        long gameTime = this.minecraft == null || this.minecraft.level == null
                ? 0L : this.minecraft.level.getGameTime();
        Player player = this.minecraft == null ? null : this.minecraft.player;
        double playerX = player == null ? 0.0D : player.getX();
        double playerZ = player == null ? 0.0D : player.getZ();

        if (this.orders.isEmpty()) {
            drawCentered(graphics, Component.translatable(KEY_EMPTY),
                    left + width / 2, listTop() + 18, COLOR_DIM);
        } else {
            int first = this.scroll;
            int last = Math.min(this.orders.size(), first + visibleRows());
            for (int i = first; i < last; i++) {
                renderOrder(graphics, this.orders.get(i), left, width,
                        listTop() + (i - first) * ROW_HEIGHT, gameTime, i, mouseX, mouseY, playerX, playerZ);
            }
            if (this.orders.size() > visibleRows()) {
                graphics.drawString(this.font, "▲", left + width - 13, top + HEADER_HEIGHT - 10, COLOR_DIM, false);
                graphics.drawString(this.font, "▼", left + width - 13,
                        top + height - footerHeight() - 2, COLOR_DIM, false);
            }
        }

        drawHint(graphics, left, top, width, height);
    }

// 明黄顶栏：只保留上方两个圆角，下沿压平接白色内容区
    private void drawHeader(GuiGraphics graphics, int left, int top, int width) {
        GuiStyle.roundedRect(graphics, left, top, width, HEADER_BAND + PANEL_CORNER, PANEL_CORNER, COLOR_HEADER);
        graphics.fill(left, top + HEADER_BAND, left + width, top + HEADER_BAND + PANEL_CORNER, COLOR_PANEL);
        drawCentered(graphics, this.title, left + width / 2, top + 9, COLOR_TITLE);
// 顶栏下沿一道淡分隔
        graphics.fill(left + 10, top + HEADER_BAND, left + width - 10, top + HEADER_BAND + 1, COLOR_DIVIDER);
    }

// 底部提示：按宽度折行后逐行居中，绝不溢出面板
    private void drawHint(GuiGraphics graphics, int left, int top, int width, int height) {
        List<FormattedCharSequence> lines = hintLines();
        int y = top + height - footerHeight() + 5;
        for (FormattedCharSequence line : lines) {
            int x = left + (width - this.font.width(line)) / 2;
            graphics.drawString(this.font, line, x, y, COLOR_DIM, false);
            y += 10;
        }
    }

    private void renderOrder(GuiGraphics graphics, PhoneOrderView view, int left, int width, int rowTop,
                             long gameTime, int index, int mouseX, int mouseY,
                             double playerX, double playerZ) {
        DeliveryOrder order = view.order();
        boolean tracked = view.accepted() && order.id() == NavPhoneClientData.trackedId();
        boolean hovered = isInsideRow(mouseX, mouseY, left, width, rowTop);

// 每单一张白卡片；追踪中的换成淡黄底 + 左侧金条，没接单的悬停时泛黄
        int cardX = left + 6;
        int cardW = width - 12;
        int cardY = rowTop + 2;
        int cardH = ROW_HEIGHT - 4;
        if (tracked) {
            GuiStyle.roundedRect(graphics, cardX, cardY, cardW, cardH, 8, COLOR_TRACKED_BG);
            GuiStyle.roundedRect(graphics, cardX + 1, cardY + 6, 3, cardH - 12, 2, COLOR_TARGET);
        } else if (hovered && !view.accepted()) {
            GuiStyle.roundedRect(graphics, cardX, cardY, cardW, cardH, 8, COLOR_CARD_HOVER_BORDER);
            GuiStyle.roundedRect(graphics, cardX + 1, cardY + 1, cardW - 2, cardH - 2, 7, COLOR_CARD_HOVER);
        } else {
            GuiStyle.roundedRect(graphics, cardX, cardY, cardW, cardH, 8, COLOR_CARD_BORDER);
            GuiStyle.roundedRect(graphics, cardX + 1, cardY + 1, cardW - 2, cardH - 2, 7, COLOR_CARD);
        }

// 出发地：都显示玩家到它的距离
        Component origin = Component.translatable(KEY_ORIGIN, order.originX(), order.originZ());
        Component originDist = Component.translatable(KEY_DIST_YOU,
                blocksBetween(playerX, playerZ, order.originX(), order.originZ()));
// 目的地：没接单显示「出发地到目的地」的全程；已接单显示玩家到它的距离
        Component dest = Component.translatable(KEY_DEST, order.destX(), order.destZ());
        Component destDist = view.accepted()
                ? Component.translatable(KEY_DIST_YOU,
                        blocksBetween(playerX, playerZ, order.destX(), order.destZ()))
                : Component.translatable(KEY_DIST_ROUTE,
                        blocksBetween(order.originX(), order.originZ(), order.destX(), order.destZ()));

        int buttonLeft = left + width - SIDE_MARGIN - BUTTON_WIDTH;
        int textLeft = cardX + TEXT_INDENT + (tracked ? 4 : 0);
        int textLimit = Math.max(40, buttonLeft - RIGHT_TEXT_ROOM - textLeft);

// 已接单：下一个该去的点高亮（还没取货是出发地，已取货是目的地）
        drawStop(graphics, textLeft, textLimit, rowTop + 9, origin, originDist,
                view.accepted() && !view.pickedUp());
        drawStop(graphics, textLeft, textLimit, rowTop + 26, dest, destDist,
                view.accepted() && view.pickedUp());

        String reward = Component.translatable(KEY_REWARD, order.money()).getString();
        int rightEdge = buttonLeft - 8;
        graphics.drawString(this.font, reward, rightEdge - this.font.width(reward), rowTop + 9,
                COLOR_MONEY, false);
        String time = formatTime(order.remaining(gameTime));
        graphics.drawString(this.font, time, rightEdge - this.font.width(time), rowTop + 26,
                COLOR_DIM, false);

        if (view.accepted()) {
// 已接的单：上面一小块「已接单」标，下面一颗「取消」按钮
            int chipTop = acceptedChipTop(rowTop);
            GuiStyle.roundedRect(graphics, buttonLeft, chipTop, BUTTON_WIDTH, ACCEPTED_CHIP_HEIGHT, 8,
                    COLOR_ACCEPTED_BG);
            drawCentered(graphics, Component.translatable(KEY_ACCEPTED),
                    buttonLeft + BUTTON_WIDTH / 2, chipTop + 4, COLOR_ACCEPTED_TEXT);

            int cancelTop = acceptedCancelTop(rowTop);
            boolean cancelHover = isInsideButton(mouseX, mouseY, buttonLeft, cancelTop,
                    BUTTON_WIDTH, BUTTON_HEIGHT);
            GuiStyle.roundedRect(graphics, buttonLeft, cancelTop, BUTTON_WIDTH, BUTTON_HEIGHT, 9,
                    COLOR_CANCEL_BORDER);
            GuiStyle.roundedRect(graphics, buttonLeft + 1, cancelTop + 1, BUTTON_WIDTH - 2,
                    BUTTON_HEIGHT - 2, 8, cancelHover ? COLOR_CANCEL_FILL_HOVER : COLOR_CANCEL_FILL);
            drawCentered(graphics, Component.translatable(KEY_CANCEL),
                    buttonLeft + BUTTON_WIDTH / 2, cancelTop + 6, COLOR_CANCEL_TEXT);
        } else {
            int buttonTop = rowTop + (ROW_HEIGHT - BUTTON_HEIGHT) / 2;
            GuiStyle.roundedRect(graphics, buttonLeft, buttonTop, BUTTON_WIDTH, BUTTON_HEIGHT, 10,
                    COLOR_ACCEPT_BORDER);
            GuiStyle.roundedGradient(graphics, buttonLeft + 1, buttonTop + 1, BUTTON_WIDTH - 2,
                    BUTTON_HEIGHT - 2, 9, COLOR_ACCEPT_TOP, COLOR_ACCEPT_BOTTOM);
            drawCentered(graphics, Component.translatable(KEY_ACCEPT),
                    buttonLeft + BUTTON_WIDTH / 2, buttonTop + 6, COLOR_ACCEPT_TEXT);
        }
    }

// 居中文字：不带阴影，避免出现重影
    private void drawCentered(GuiGraphics graphics, Component text, int centerX, int y, int color) {
        graphics.drawString(this.font, text, centerX - this.font.width(text) / 2, y, color, false);
    }

// 画一行「地点 · 距离」；是「下一个目标」就用深金色，并在最左加个箭头
    private void drawStop(GuiGraphics graphics, int x, int limit, int y, Component coord, Component dist,
                          boolean nextTarget) {
        if (nextTarget) {
            graphics.drawString(this.font, "▶", x - ARROW_INDENT - 1, y, COLOR_TARGET, false);
        }
        String text = coord.getString() + "  " + dist.getString();
        Component line = Component.literal(this.font.plainSubstrByWidth(text, limit));
        graphics.drawString(this.font, line, x, y, nextTarget ? COLOR_TARGET : COLOR_TEXT, false);
    }

// 两点之间的水平距离（格）
    private static int blocksBetween(double x1, double z1, double x2, double z2) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        return (int) Math.round(Math.sqrt(dx * dx + dz * dz));
    }

// 已接单那行：小标 + 间距 + 按钮 整体在行内居中，返回小标的顶
    private static int acceptedChipTop(int rowTop) {
        int stack = ACCEPTED_CHIP_HEIGHT + ACCEPTED_CHIP_GAP + BUTTON_HEIGHT;
        return rowTop + (ROW_HEIGHT - stack) / 2;
    }

// 已接单那行：「取消」按钮的顶
    private static int acceptedCancelTop(int rowTop) {
        return acceptedChipTop(rowTop) + ACCEPTED_CHIP_HEIGHT + ACCEPTED_CHIP_GAP;
    }

    private static boolean isInsideButton(double mouseX, double mouseY, int left, int top,
                                          int width, int height) {
        return mouseX >= left && mouseX < left + width
                && mouseY >= top && mouseY < top + height;
    }

    private boolean isInsideRow(double mouseX, double mouseY, int left, int width, int rowTop) {
        return mouseX >= left + 6 && mouseX < left + width - 6
                && mouseY >= rowTop + 2 && mouseY < rowTop + ROW_HEIGHT - 2;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = panelLeft();
            int width = panelWidth();
            int buttonLeft = left + width - SIDE_MARGIN - BUTTON_WIDTH;
            int first = this.scroll;
            int last = Math.min(this.orders.size(), first + visibleRows());
            for (int i = first; i < last; i++) {
                PhoneOrderView view = this.orders.get(i);
                int rowTop = listTop() + (i - first) * ROW_HEIGHT;
                if (view.accepted()) {
// 已接的单：点在「取消」按钮上作废；点行内别处则选中 / 取消选中追踪
                    if (isInsideButton(mouseX, mouseY, buttonLeft, acceptedCancelTop(rowTop),
                            BUTTON_WIDTH, BUTTON_HEIGHT)) {
                        PacketDistributor.sendToServer(new PhoneCancelOrderPacket(view.order().id()));
                        return true;
                    }
                    if (isInsideRow(mouseX, mouseY, left, width, rowTop)) {
                        long id = view.order().id();
                        NavPhoneClientData.setTracked(NavPhoneClientData.trackedId() == id ? 0L : id);
                        return true;
                    }
                } else if (isInsideRow(mouseX, mouseY, left, width, rowTop)) {
                    PacketDistributor.sendToServer(new PhoneAcceptOrderPacket(view.order().id()));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        int max = Math.max(0, this.orders.size() - maxRows());
        if (max > 0) {
            this.scroll = Math.max(0, Math.min(this.scroll + (int) -verticalAmount, max));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, verticalAmount);
    }

    private static String formatTime(long ticks) {
        long seconds = Math.max(0L, ticks / 20L);
        return String.format("%d:%02d", seconds / 60L, seconds % 60L);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
