package com.flavor_immersed_daily_argo.economy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
// 与护林员写字板一致，界面覆写 #renderBackground 改画纯色遮罩，
// 因此世界不会糊成一片（不触发原版的模糊后处理）。订单多了可以滚轮翻页。
public class PhoneOrdersScreen extends Screen {

    private static final int PANEL_WIDTH = 320;
    private static final int ROW_HEIGHT = 46;
    private static final int HEADER_HEIGHT = 34;
    private static final int FOOTER_HEIGHT = 20;
// 面板上下各留出的边距
    private static final int PANEL_VERTICAL_MARGIN = 40;

    private static final int BUTTON_WIDTH = 52;
    private static final int BUTTON_HEIGHT = 18;
    private static final int SIDE_MARGIN = 10;

// 行内文字 / 「下一个目标」箭头相对面板左边缘的缩进
    private static final int TEXT_INDENT = 16;
    private static final int ARROW_INDENT = 5;

    private static final int COLOR_SCRIM = 0x66000000;
    private static final int COLOR_PANEL = 0xF0151515;
    private static final int COLOR_BORDER = 0xFF3D3D3D;
    private static final int COLOR_DIVIDER = 0xFF2A2A2A;
    private static final int COLOR_ROW_HOVER = 0x30FFFFFF;
    private static final int COLOR_TITLE = 0xFFE8C36A;
    private static final int COLOR_TEXT = 0xFFDDDDDD;
    private static final int COLOR_DIM = 0xFF9A9A9A;
    private static final int COLOR_MONEY = 0xFF6FD06F;
    private static final int COLOR_ACCEPT = 0xFF2E7D32;
    private static final int COLOR_ACCEPT_TEXT = 0xFFFFFFFF;
    private static final int COLOR_ACCEPTED = 0x40000000;
    private static final int COLOR_ACCEPTED_TEXT = 0xFF6E6E6E;
    private static final int COLOR_CANCEL = 0xFF9E3226;
    private static final int COLOR_CANCEL_HOVER = 0xFFBD4535;
    private static final int COLOR_CANCEL_TEXT = 0xFFFFFFFF;
// 已接单里「下一个该去的点」那一行的高亮色
    private static final int COLOR_TARGET = 0xFFE8C36A;
// 正在追踪的那一行的底衬
    private static final int COLOR_TRACKED_BG = 0x18E8C36A;

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

// 屏幕放得下的行数（至少一行）
    private int maxRows() {
        int available = this.height - PANEL_VERTICAL_MARGIN - HEADER_HEIGHT - FOOTER_HEIGHT;
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
        return HEADER_HEIGHT + Math.max(1, visibleRows()) * ROW_HEIGHT + FOOTER_HEIGHT;
    }

    private int panelLeft() {
        return (this.width - PANEL_WIDTH) / 2;
    }

    private int panelTop() {
        return (this.height - panelHeight()) / 2;
    }

    private int listTop() {
        return panelTop() + HEADER_HEIGHT;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
// 用纯色遮罩代替原版的模糊背景
        graphics.fill(0, 0, this.width, this.height, COLOR_SCRIM);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
// super.render 会调用上面覆写的 renderBackground（纯色遮罩），不会糊
        super.render(graphics, mouseX, mouseY, partialTick);

        int left = panelLeft();
        int top = panelTop();
        int height = panelHeight();
        graphics.fill(left, top, left + PANEL_WIDTH, top + height, COLOR_PANEL);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 1, COLOR_BORDER);
        graphics.fill(left, top + height - 1, left + PANEL_WIDTH, top + height, COLOR_BORDER);
        graphics.fill(left, top, left + 1, top + height, COLOR_BORDER);
        graphics.fill(left + PANEL_WIDTH - 1, top, left + PANEL_WIDTH, top + height, COLOR_BORDER);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 12, COLOR_TITLE);

        long gameTime = this.minecraft == null || this.minecraft.level == null
                ? 0L : this.minecraft.level.getGameTime();
        Player player = this.minecraft == null ? null : this.minecraft.player;
        double playerX = player == null ? 0.0D : player.getX();
        double playerZ = player == null ? 0.0D : player.getZ();

        if (this.orders.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable(KEY_EMPTY),
                    this.width / 2, listTop() + 14, COLOR_DIM);
        } else {
            int first = this.scroll;
            int last = Math.min(this.orders.size(), first + visibleRows());
            for (int i = first; i < last; i++) {
                renderOrder(graphics, this.orders.get(i), left, listTop() + (i - first) * ROW_HEIGHT,
                        gameTime, i, mouseX, mouseY, playerX, playerZ);
            }
            if (this.orders.size() > visibleRows()) {
                graphics.drawString(this.font, "▲", left + PANEL_WIDTH - 11, top + HEADER_HEIGHT - 9, COLOR_DIM, false);
                graphics.drawString(this.font, "▼", left + PANEL_WIDTH - 11, top + height - FOOTER_HEIGHT - 1, COLOR_DIM, false);
            }
        }

        graphics.drawCenteredString(this.font, Component.translatable(KEY_HINT),
                this.width / 2, top + height - 15, COLOR_DIM);
    }

    private void renderOrder(GuiGraphics graphics, PhoneOrderView view, int left, int rowTop,
                             long gameTime, int index, int mouseX, int mouseY,
                             double playerX, double playerZ) {
        DeliveryOrder order = view.order();
        boolean tracked = view.accepted() && order.id() == NavPhoneClientData.trackedId();
        if (index > this.scroll) {
            graphics.fill(left + 8, rowTop, left + PANEL_WIDTH - 8, rowTop + 1, COLOR_DIVIDER);
        }
        if (tracked) {
// 正在追踪：整行淡金底衬 + 左侧一道金条
            graphics.fill(left + 2, rowTop + 1, left + PANEL_WIDTH - 2, rowTop + ROW_HEIGHT - 1, COLOR_TRACKED_BG);
            graphics.fill(left + 2, rowTop + 1, left + 4, rowTop + ROW_HEIGHT - 1, COLOR_TARGET);
        } else if (!view.accepted() && isInsideRow(mouseX, mouseY, left, rowTop)) {
            graphics.fill(left + 2, rowTop + 1, left + PANEL_WIDTH - 2, rowTop + ROW_HEIGHT - 1, COLOR_ROW_HOVER);
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

// 已接单：下一个该去的点高亮（还没取货是出发地，已取货是目的地）
        drawStop(graphics, left, rowTop + 8, origin, originDist, view.accepted() && !view.pickedUp());
        drawStop(graphics, left, rowTop + 26, dest, destDist, view.accepted() && view.pickedUp());

        int buttonLeft = left + PANEL_WIDTH - SIDE_MARGIN - BUTTON_WIDTH;
        String reward = Component.translatable(KEY_REWARD, order.money()).getString();
        graphics.drawString(this.font, reward, buttonLeft - 10 - this.font.width(reward), rowTop + 8,
                COLOR_MONEY, false);
        String time = formatTime(order.remaining(gameTime));
        graphics.drawString(this.font, time, buttonLeft - 10 - this.font.width(time), rowTop + 26,
                COLOR_DIM, false);

        int buttonTop = rowTop + (ROW_HEIGHT - BUTTON_HEIGHT) / 2;
        if (view.accepted()) {
// 已接的单：上面一小块「已接单」标，下面一颗「取消」按钮
            int chipTop = acceptedChipTop(rowTop);
            graphics.fill(buttonLeft, chipTop, buttonLeft + BUTTON_WIDTH, chipTop + ACCEPTED_CHIP_HEIGHT,
                    COLOR_ACCEPTED);
            graphics.drawCenteredString(this.font, Component.translatable(KEY_ACCEPTED),
                    buttonLeft + BUTTON_WIDTH / 2, chipTop + 4, COLOR_ACCEPTED_TEXT);

            int cancelTop = acceptedCancelTop(rowTop);
            boolean hovered = isInsideButton(mouseX, mouseY, buttonLeft, cancelTop,
                    BUTTON_WIDTH, BUTTON_HEIGHT);
            graphics.fill(buttonLeft, cancelTop, buttonLeft + BUTTON_WIDTH, cancelTop + BUTTON_HEIGHT,
                    hovered ? COLOR_CANCEL_HOVER : COLOR_CANCEL);
            graphics.drawCenteredString(this.font, Component.translatable(KEY_CANCEL),
                    buttonLeft + BUTTON_WIDTH / 2, cancelTop + 5, COLOR_CANCEL_TEXT);
        } else {
            graphics.fill(buttonLeft, buttonTop, buttonLeft + BUTTON_WIDTH, buttonTop + BUTTON_HEIGHT, COLOR_ACCEPT);
            graphics.drawCenteredString(this.font, Component.translatable(KEY_ACCEPT),
                    buttonLeft + BUTTON_WIDTH / 2, buttonTop + 5, COLOR_ACCEPT_TEXT);
        }
    }

// 画一行「地点 + 距离」；是「下一个目标」就用高亮色、并在最左加个箭头
    private void drawStop(GuiGraphics graphics, int left, int y, Component coord, Component dist,
                          boolean nextTarget) {
        if (nextTarget) {
            graphics.drawString(this.font, "▶", left + ARROW_INDENT, y, COLOR_TARGET, false);
        }
        int x = left + TEXT_INDENT;
        graphics.drawString(this.font, coord, x, y, nextTarget ? COLOR_TARGET : COLOR_TEXT, false);
        graphics.drawString(this.font, dist, x + this.font.width(coord) + 6, y,
                nextTarget ? COLOR_TARGET : COLOR_DIM, false);
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

    private boolean isInsideRow(double mouseX, double mouseY, int left, int rowTop) {
        return mouseX >= left && mouseX < left + PANEL_WIDTH
                && mouseY >= rowTop && mouseY < rowTop + ROW_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = panelLeft();
            int buttonLeft = left + PANEL_WIDTH - SIDE_MARGIN - BUTTON_WIDTH;
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
                    if (isInsideRow(mouseX, mouseY, left, rowTop)) {
                        long id = view.order().id();
                        NavPhoneClientData.setTracked(NavPhoneClientData.trackedId() == id ? 0L : id);
                        return true;
                    }
                } else if (isInsideRow(mouseX, mouseY, left, rowTop)) {
                    PacketDistributor.sendToServer(new PhoneAcceptOrderPacket(view.order().id()));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int max = Math.max(0, this.orders.size() - maxRows());
        if (max > 0) {
            this.scroll = Math.max(0, Math.min(this.scroll + (int) -verticalAmount, max));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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
