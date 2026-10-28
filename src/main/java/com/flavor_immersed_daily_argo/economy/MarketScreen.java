package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.client.GuiStyle;
import com.flavor_immersed_daily_argo.client.RoundedButton;
import com.flavor_immersed_daily_argo.economy.product.EconomicNews;
import com.flavor_immersed_daily_argo.economy.product.WeightPriceData;
import com.flavor_immersed_daily_argo.economy.shop.CartLine;
import com.flavor_immersed_daily_argo.economy.shop.Shop;
import com.flavor_immersed_daily_argo.economy.shop.ShopCheckoutPacket;
import com.flavor_immersed_daily_argo.economy.shop.ShopClientData;
import com.flavor_immersed_daily_argo.economy.shop.ShopEntry;
import com.flavor_immersed_daily_argo.economy.shop.ShopManagePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// 商业电脑界面。
//
// 顶栏四个页签：新闻 / 网购 / 开店 / 行情。窗口做成又宽又矮的 340×200，
// 不再画玩家背包格：
//
// 网购页：左列店铺 → 点进某店（右上出现「← 返回」）→ 右列条目行（图标 + 数量 + 库存 + 价格 +
// 购物车 - [n] +）→ 底部「合计 / 清空购物车 / 结账」；点「结账」弹出银行卡选择面板；
// 开店页：左列店铺列表，右上输入店名创建；点自己的店进管理视图，可上架 / 下架 / 改价 / 补货 /
// 提现 / 删除。上架与补货直接用手上拿着的整叠物品，提现也在弹出的面板里选银行卡；
// 行情页：按重量收购的价目。左栏能称重的生鱼（每斤价），右栏畜类（标准价 + 超出每公斤加价）。
//
// 无贴图背景，直接 gui.fill 一块暗底；购物车状态存 ShopClientData，界面关闭不清空。
public class MarketScreen extends AbstractContainerScreen<MarketMenu> {

    private static final int IMAGE_WIDTH = MarketMenu.IMAGE_WIDTH;
    private static final int IMAGE_HEIGHT = MarketMenu.IMAGE_HEIGHT;

// 左列（店铺列表 / 管理视图的条目列表）
    private static final int LIST_X = 8;
    private static final int LIST_W = 130;
// 右列（条目列表 / 管理按钮）
    private static final int RIGHT_X = 146;
    private static final int RIGHT_W = 186;

    private static final int LIST_TOP = 26;
    private static final int ROW_H = 20;
    private static final int LIST_ROWS = 6;

// 行情页：左栏生鱼（贴合背景左面板）、右栏畜类（贴合背景右面板），表头一行、数据从 PRICE_TOP 开始
    private static final int FISH_X = LIST_X;
    private static final int FISH_W = LIST_W;
    private static final int ANIMAL_X = RIGHT_X;
    private static final int ANIMAL_W = RIGHT_W;
    private static final int PRICE_HEADER_Y = 26;
    private static final int PRICE_TOP = 42;
    private static final int PRICE_ROWS = 6;
// 页签右侧的标题（新闻页、行情页共用）
    private static final int TITLE_X = 204;

// 银行卡选择面板
    private static final int CARD_PANEL_W = 220;
    private static final int CARD_ROW_H = 20;

// 面板模式：-1 = 未打开，0 = 结账选卡，1 = 提现选卡
    private static final int PICKER_NONE = -1;
    private static final int PICKER_CHECKOUT = 0;
    private static final int PICKER_WITHDRAW = 1;

// 当前页签：0 = 新闻，1 = 网购，2 = 开店，3 = 行情
    private int page = 0;
// 网购页：已进入的子目录店铺 id；null = 还在店铺列表
    private String shopPageId;
// 开店页：正在管理的店铺 id；null = 还在店铺列表
    private String manageId;
// 开店页：管理视图中选中的条目下标
    private int selectedEntry = -1;
// 各列表的滚动量
    private int shopScroll;
    private int entryScroll;
    private int newsScroll;
    private int animalScroll;
    private int fishScroll;

    private EditBox shopNameField;
    private EditBox priceField;

// 鼠标悬停的商店条目，画完所有页面后统一弹出物品提示
    private ItemStack hoveredStack = ItemStack.EMPTY;

// 银行卡选择面板的状态
    private int pickerMode = PICKER_NONE;
    private final List<Integer> cardSlots = new ArrayList<>();

    public MarketScreen(MarketMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.pickerMode = PICKER_NONE;
        this.cardSlots.clear();
        rebuild();
    }

// 按当前页签 / 视图重建控件
    private void rebuild() {
        this.clearWidgets();
// 顶部四个页签：当前页走金色画风
        button("gui.flavor_immersed_daily_argo.market.news", 8, 4, 44, 16,
                RoundedButton.Style.NORMAL, this.page == 0, () -> switchPage(0));
        button("gui.flavor_immersed_daily_argo.market.shopping", 56, 4, 44, 16,
                RoundedButton.Style.NORMAL, this.page == 1, () -> switchPage(1));
        button("gui.flavor_immersed_daily_argo.market.shop", 104, 4, 44, 16,
                RoundedButton.Style.NORMAL, this.page == 2, () -> switchPage(2));
        button("gui.flavor_immersed_daily_argo.market.prices", 152, 4, 44, 16,
                RoundedButton.Style.NORMAL, this.page == 3, () -> switchPage(3));

        if (this.page == 1) {
// 网购页：进入子目录后给「← 返回」
            if (this.shopPageId != null) {
                button("gui.flavor_immersed_daily_argo.market.back", 276, 4, 56, 16, () -> {
                    this.shopPageId = null;
                    this.entryScroll = 0;
                });
            }
            button("gui.flavor_immersed_daily_argo.market.cart_clear", 146, 156, 90, 20,
                    ShopClientData::clearCart);
            button("gui.flavor_immersed_daily_argo.market.checkout", 242, 156, 90, 20,
                    RoundedButton.Style.ACCENT, () -> openCardPicker(PICKER_CHECKOUT));
        } else if (this.page == 2) {
            if (this.manageId == null) {
// 店铺列表视图：输入店名后创建
                this.shopNameField = field(this.shopNameField, RIGHT_X, 26, RIGHT_W, 18);
                button("gui.flavor_immersed_daily_argo.market.create_shop", RIGHT_X, 52, RIGHT_W, 18,
                        RoundedButton.Style.ACCENT, () -> {
                            String name = this.shopNameField == null ? "" : this.shopNameField.getValue();
                            PacketDistributor.sendToServer(new ShopManagePacket(
                                    ShopManagePacket.Action.CREATE, "", name, 0, 0));
                        });
            } else {
// 管理视图：上架 / 下架 / 改价 / 补货 / 提现 / 删除 + 「← 返回」
                button("gui.flavor_immersed_daily_argo.market.back", 276, 4, 56, 16, () -> {
                    this.manageId = null;
                    this.selectedEntry = -1;
                    this.entryScroll = 0;
                    rebuild();
                });
                this.priceField = field(this.priceField, RIGHT_X, 52, RIGHT_W, 18);
// 输入框里的数字直接作为上架价格，不用先上架再改价
                button("gui.flavor_immersed_daily_argo.market.list_item", RIGHT_X, 76, RIGHT_W, 16, () -> {
                    int listed = price();
                    if (listed <= 0) {
                        ShopClientData.applyResult(false,
                                Component.translatable("gui.flavor_immersed_daily_argo.market.price_required"));
                        return;
                    }
                    sendManage(ShopManagePacket.Action.ADD_ENTRY, listed);
                });
                button("gui.flavor_immersed_daily_argo.market.delist", RIGHT_X, 94, RIGHT_W, 16,
                        () -> sendManage(ShopManagePacket.Action.REMOVE_ENTRY, 0));
// 改价同样走输入框，空值或不合法时给出提示
                button("gui.flavor_immersed_daily_argo.market.set_price", RIGHT_X, 112, RIGHT_W, 16, () -> {
                    int repriced = price();
                    if (repriced <= 0) {
                        ShopClientData.applyResult(false,
                                Component.translatable("gui.flavor_immersed_daily_argo.market.price_required"));
                        return;
                    }
                    sendManage(ShopManagePacket.Action.SET_PRICE, repriced);
                });
                button("gui.flavor_immersed_daily_argo.market.restock", RIGHT_X, 130, RIGHT_W, 16,
                        () -> sendManage(ShopManagePacket.Action.RESTOCK, 0));
                button("gui.flavor_immersed_daily_argo.market.withdraw", RIGHT_X, 148, RIGHT_W, 16,
                        () -> openCardPicker(PICKER_WITHDRAW));
// 删除店铺：只有进入自己店铺的管理视图后可用，避免误删
                button("gui.flavor_immersed_daily_argo.market.delete_shop", RIGHT_X, 166, RIGHT_W, 16,
                        RoundedButton.Style.DANGER, () -> {
                            PacketDistributor.sendToServer(new ShopManagePacket(
                                    ShopManagePacket.Action.DELETE, this.manageId, "", 0, 0));
                            this.manageId = null;
                            this.selectedEntry = -1;
                            this.entryScroll = 0;
                            rebuild();
                        });
            }
        }
    }

    private void switchPage(int newPage) {
        this.page = newPage;
        rebuild();
    }

    private int price() {
        if (this.priceField == null) {
            return -1;
        }
        try {
            return Math.max(0, Integer.parseInt(this.priceField.getValue().trim()));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void sendManage(ShopManagePacket.Action action, int price) {
        if (this.manageId == null) {
            return;
        }
        int index = action == ShopManagePacket.Action.SET_PRICE || action == ShopManagePacket.Action.REMOVE_ENTRY
                || action == ShopManagePacket.Action.RESTOCK ? this.selectedEntry : 0;
        PacketDistributor.sendToServer(new ShopManagePacket(action, this.manageId, "", index, price));
    }

// 加一个普通按钮（坐标相对界面左上角）
    private void button(String key, int x, int y, int w, int h, Runnable onClick) {
        button(key, x, y, w, h, RoundedButton.Style.NORMAL, false, onClick);
    }

// 加一个指定画风的按钮
    private void button(String key, int x, int y, int w, int h, RoundedButton.Style style, Runnable onClick) {
        button(key, x, y, w, h, style, false, onClick);
    }

// 加一个按钮；selected 用于页签的选中态
    private void button(String key, int x, int y, int w, int h,
                        RoundedButton.Style style, boolean selected, Runnable onClick) {
        this.addRenderableWidget(RoundedButton.of(this.leftPos + x, this.topPos + y, w, h,
                Component.translatable(key), style, selected, onClick));
    }

// 取用（或新建）一个输入框并挂到界面上
    private EditBox field(EditBox existing, int x, int y, int w, int h) {
        EditBox box = existing;
        if (box == null) {
            box = new EditBox(this.font, this.leftPos + x, this.topPos + y, w, h,
                    Component.translatable("gui.flavor_immersed_daily_argo.market.price_hint"));
// 去掉原版九宫格底，圆角底色由 renderBg 自己画
            box.setBordered(false);
            box.setTextColor(GuiStyle.TEXT);
        }
        box.setX(this.leftPos + x);
        box.setY(this.topPos + y);
        box.setWidth(w);
        box.setHeight(h);
        this.addRenderableWidget(box);
        return box;
    }

// 银行卡选择面板

// 打开选卡面板：清掉页面控件，改由面板接管点击
    private void openCardPicker(int mode) {
        this.pickerMode = mode;
        this.scanCards();
        this.clearWidgets();
    }

    private void closeCardPicker() {
        this.pickerMode = PICKER_NONE;
        this.cardSlots.clear();
        rebuild();
    }

// 扫一遍背包，记下所有银行卡所在的格（0–35 主背包 / 40 副手）
    private void scanCards() {
        this.cardSlots.clear();
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        Inventory inv = this.minecraft.player.getInventory();
        for (int i = 0; i < inv.items.size(); i++) {
            if (inv.items.get(i).getItem() instanceof CreditCardItem) {
                this.cardSlots.add(i);
            }
        }
        if (!inv.offhand.get(0).isEmpty() && inv.offhand.get(0).getItem() instanceof CreditCardItem) {
            this.cardSlots.add(Inventory.SLOT_OFFHAND);
        }
    }

// 面板几何：{x, y, height}，居中显示，最多 6 行
    private int[] cardPanel() {
        int rows = Math.max(1, Math.min(6, this.cardSlots.size()));
        int height = 26 + rows * CARD_ROW_H + 26;
        int x = this.leftPos + (IMAGE_WIDTH - CARD_PANEL_W) / 2;
        int y = this.topPos + (IMAGE_HEIGHT - height) / 2;
        return new int[]{x, y, height};
    }

    private int cardRowAt(double mouseX, double mouseY) {
        int[] panel = cardPanel();
        int rows = Math.min(6, this.cardSlots.size());
        for (int i = 0; i < rows; i++) {
            int y = panel[1] + 26 + i * CARD_ROW_H;
            if (mouseX >= panel[0] + 8 && mouseX < panel[0] + CARD_PANEL_W - 8
                    && mouseY >= y && mouseY < y + CARD_ROW_H - 2) {
                return i;
            }
        }
        return -1;
    }

    private boolean inCancelButton(double mouseX, double mouseY) {
        int[] panel = cardPanel();
        int x = panel[0] + 10;
        int y = panel[1] + panel[2] - 24;
        return mouseX >= x && mouseX < x + CARD_PANEL_W - 20 && mouseY >= y && mouseY < y + 18;
    }

// 绘制

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
// 无贴图：半透明黑圆角底板 + 页签分隔线
        GuiStyle.panel(gui, leftPos, topPos, IMAGE_WIDTH, IMAGE_HEIGHT);
        GuiStyle.divider(gui, leftPos + 8, topPos + 23, IMAGE_WIDTH - 16);

// 开店页的输入框底（输入框已去掉原版边框）
        if (this.page == 2) {
            if (this.manageId == null) {
                boolean focused = this.shopNameField != null && this.shopNameField.isFocused();
                GuiStyle.field(gui, leftPos + RIGHT_X - 3, topPos + 25, RIGHT_W + 6, 20, focused);
            } else {
                boolean focused = this.priceField != null && this.priceField.isFocused();
                GuiStyle.field(gui, leftPos + RIGHT_X - 3, topPos + 51, RIGHT_W + 6, 20, focused);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
// 不画默认标题与背包名，顶部位置留给页签
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        this.hoveredStack = ItemStack.EMPTY;
        switch (this.page) {
            case 1 -> renderShopping(gui, mouseX, mouseY);
            case 2 -> renderShop(gui, mouseX, mouseY);
            case 3 -> renderPrices(gui, mouseX, mouseY);
            default -> renderNews(gui, mouseX, mouseY);
        }
        if (this.pickerMode != PICKER_NONE) {
            renderCardPicker(gui, mouseX, mouseY);
        } else if (!this.hoveredStack.isEmpty()) {
// 悬停在商店条目上时，弹出该物品的原版提示（名字、附魔、食物属性等）
            gui.renderTooltip(font, this.hoveredStack, mouseX, mouseY);
        }
// 服务端回执：结账成功 / 失败原因直接画在窗口里（容器界面打开时看不见 actionBar）
        Component note = ShopClientData.feedback();
        if (!note.getString().isEmpty()) {
            gui.drawString(font, note, leftPos + LIST_X, topPos + 182,
                    ShopClientData.feedbackSuccess() ? 0x80FF80 : 0xFF8080);
        }
        this.renderTooltip(gui, mouseX, mouseY);
    }

// 画选卡面板：整屏压暗 + 居中面板 + 卡片行 + 取消
    private void renderCardPicker(GuiGraphics gui, int mouseX, int mouseY) {
        gui.fill(0, 0, this.width, this.height, 0x90000000);
        int[] panel = cardPanel();
        int px = panel[0];
        int py = panel[1];
        int pw = CARD_PANEL_W;
        int ph = panel[2];
        GuiStyle.panel(gui, px, py, pw, ph);

        gui.drawString(font, Component.translatable(this.pickerMode == PICKER_WITHDRAW
                        ? "gui.flavor_immersed_daily_argo.market.choose_card_withdraw"
                        : "gui.flavor_immersed_daily_argo.market.choose_card"),
                px + 8, py + 8, GuiStyle.ACCENT);

        if (this.cardSlots.isEmpty()) {
            gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.no_card"),
                    px + 10, py + 30, GuiStyle.TEXT_DIM);
        }
        int rows = Math.min(6, this.cardSlots.size());
        for (int i = 0; i < rows; i++) {
            int slot = this.cardSlots.get(i);
            ItemStack stack = this.minecraft == null || this.minecraft.player == null
                    ? ItemStack.EMPTY : this.minecraft.player.getInventory().getItem(slot);
            int y = py + 26 + i * CARD_ROW_H;
            boolean hovered = cardRowAt(mouseX, mouseY) == i;
            GuiStyle.row(gui, px + 8, y, pw - 16, CARD_ROW_H - 2, hovered, false);
            gui.renderItem(stack, px + 10, y + 1);
            String owner = CreditCardItem.getOwner(stack);
            gui.drawString(font, owner == null || owner.isBlank()
                            ? Component.translatable("gui.flavor_immersed_daily_argo.market.card_anonymous")
                            : Component.literal(owner),
                    px + 30, y + 5, GuiStyle.TEXT);
            String money = "¥" + CreditCardItem.getAccount(stack);
            gui.drawString(font, money, px + pw - 12 - font.width(money), y + 5, GuiStyle.ACCENT);
        }

        int cx = px + 10;
        int cy = py + ph - 24;
        boolean cancelHovered = inCancelButton(mouseX, mouseY);
        GuiStyle.button(gui, cx, cy, pw - 20, 18, RoundedButton.Style.NORMAL, false, cancelHovered, true);
        gui.drawCenteredString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.cancel"),
                cx + (pw - 20) / 2, cy + 5, GuiStyle.TEXT);
    }

// 新闻页

// 新闻页：今日经济产物涨跌，从长得最高排到跌得最狠。
//
// 一行一样产物：图标 + 名称 + 今日收购价 + 涨跌（元/组）。数据由服务端算好下发，
// 同一天全服一致。
    private void renderNews(GuiGraphics gui, int mouseX, int mouseY) {
        List<EconomicNews> news = ShopClientData.news();
        gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.news_title"),
                leftPos + TITLE_X, topPos + 10, GuiStyle.ACCENT);
        if (news.isEmpty()) {
            gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.news_placeholder"),
                    leftPos + LIST_X, topPos + LIST_TOP + 4, GuiStyle.TEXT_DIM);
            return;
        }
        int x = LIST_X;
        int w = IMAGE_WIDTH - LIST_X * 2;
        this.newsScroll = Mth.clamp(this.newsScroll, 0, Math.max(0, news.size() - LIST_ROWS));
        gui.enableScissor(leftPos, topPos + LIST_TOP, leftPos + x + w, topPos + LIST_TOP + LIST_ROWS * ROW_H);
        for (int row = 0; row < LIST_ROWS; row++) {
            int index = this.newsScroll + row;
            if (index >= news.size()) {
                break;
            }
            EconomicNews line = news.get(index);
            int y = topPos + LIST_TOP + row * ROW_H;
            boolean hovered = inRow(mouseX, mouseY, x, w, row);
            GuiStyle.row(gui, leftPos + x, y, w, ROW_H - 2, hovered, false);

            ItemStack icon = new ItemStack(line.item());
            gui.renderItem(icon, leftPos + x + 2, y + 2);
            gui.drawString(font, icon.getHoverName(), leftPos + x + 22, y + 6, GuiStyle.TEXT);

            String price = "¥" + line.price();
            gui.drawString(font, price, leftPos + x + w - 150, y + 6, GuiStyle.ACCENT);

            int delta = line.delta();
            Component deltaText = Component.translatable(delta >= 0
                            ? "gui.flavor_immersed_daily_argo.market.news_up"
                            : "gui.flavor_immersed_daily_argo.market.news_down",
                    Math.abs(delta));
            gui.drawString(font, deltaText, leftPos + x + w - 6 - font.width(deltaText), y + 6,
                    delta > 0 ? 0x80FF80 : delta < 0 ? 0xFF8080 : 0xC0C0C0);
        }
        gui.disableScissor();
    }

// 行情页

// 行情页：按重量收购的价目。
//
// 左栏生鱼——上过秤就按秤上的斤数、没上秤按分布均值，乘每斤价；右栏畜类——把动物装进编制麻袋
// 丢进收购箱，标准体重给「标准价」，体重超出标准的部分再按每公斤加价累加。价格一律两位小数
// （含当日浮动，不会正好是整元），最终售价向上取整。
    private void renderPrices(GuiGraphics gui, int mouseX, int mouseY) {
        List<WeightPriceData.AnimalPrice> animals = ShopClientData.animalPrices();
        List<WeightPriceData.FishPrice> fish = ShopClientData.fishPrices();

        gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.prices_title"),
                leftPos + TITLE_X, topPos + 10, GuiStyle.ACCENT);

// 表头：左栏生鱼、右栏畜类
        gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.price_fish"),
                leftPos + FISH_X + 2, topPos + PRICE_HEADER_Y, GuiStyle.TEXT);
        drawRight(gui, Component.translatable("gui.flavor_immersed_daily_argo.market.price_per_jin"),
                leftPos + FISH_X + FISH_W - 3, topPos + PRICE_HEADER_Y, GuiStyle.TEXT_DIM);
        gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.price_animals"),
                leftPos + ANIMAL_X + 2, topPos + PRICE_HEADER_Y, GuiStyle.TEXT);
        drawRight(gui, Component.translatable("gui.flavor_immersed_daily_argo.market.price_base"),
                leftPos + ANIMAL_X + 124, topPos + PRICE_HEADER_Y, GuiStyle.TEXT_DIM);
        drawRight(gui, Component.translatable("gui.flavor_immersed_daily_argo.market.price_per_kg"),
                leftPos + ANIMAL_X + ANIMAL_W - 3, topPos + PRICE_HEADER_Y, GuiStyle.TEXT_DIM);

// 左栏：生鱼
        if (fish.isEmpty()) {
            gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.price_empty"),
                    leftPos + FISH_X + 2, topPos + PRICE_TOP + 4, GuiStyle.TEXT_DIM);
        } else {
            this.fishScroll = Mth.clamp(this.fishScroll, 0, Math.max(0, fish.size() - PRICE_ROWS));
            gui.enableScissor(leftPos + FISH_X, topPos + PRICE_TOP,
                    leftPos + FISH_X + FISH_W, topPos + PRICE_TOP + PRICE_ROWS * ROW_H);
            for (int row = 0; row < PRICE_ROWS; row++) {
                int index = this.fishScroll + row;
                if (index >= fish.size()) {
                    break;
                }
                WeightPriceData.FishPrice price = fish.get(index);
                int y = topPos + PRICE_TOP + row * ROW_H;
                boolean hovered = inPriceRow(mouseX, mouseY, FISH_X, FISH_W, row);
                GuiStyle.row(gui, leftPos + FISH_X, y, FISH_W, ROW_H - 2, hovered, false);
                ItemStack icon = new ItemStack(price.item());
                gui.renderItem(icon, leftPos + FISH_X + 2, y + 2);
// 兼容模组的鱼名可能很长，按栏宽截断，免得压到右边的价格上
                String priceText = "¥" + money(price.perJin());
                int nameRoom = FISH_W - 22 - 3 - font.width(priceText) - 4;
                gui.drawString(font, fit(icon.getHoverName().getString(), nameRoom),
                        leftPos + FISH_X + 22, y + 6, GuiStyle.TEXT);
                drawRight(gui, Component.literal(priceText),
                        leftPos + FISH_X + FISH_W - 3, y + 6, GuiStyle.ACCENT);
            }
            gui.disableScissor();
        }

// 右栏：畜类
        if (animals.isEmpty()) {
            gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.price_empty"),
                    leftPos + ANIMAL_X + 2, topPos + PRICE_TOP + 4, GuiStyle.TEXT_DIM);
        } else {
            this.animalScroll = Mth.clamp(this.animalScroll, 0, Math.max(0, animals.size() - PRICE_ROWS));
            gui.enableScissor(leftPos + ANIMAL_X, topPos + PRICE_TOP,
                    leftPos + ANIMAL_X + ANIMAL_W, topPos + PRICE_TOP + PRICE_ROWS * ROW_H);
            for (int row = 0; row < PRICE_ROWS; row++) {
                int index = this.animalScroll + row;
                if (index >= animals.size()) {
                    break;
                }
                WeightPriceData.AnimalPrice price = animals.get(index);
                int y = topPos + PRICE_TOP + row * ROW_H;
                boolean hovered = inPriceRow(mouseX, mouseY, ANIMAL_X, ANIMAL_W, row);
                GuiStyle.row(gui, leftPos + ANIMAL_X, y, ANIMAL_W, ROW_H - 2, hovered, false);
                gui.drawString(font, Component.translatable(price.type().getDescriptionId()),
                        leftPos + ANIMAL_X + 2, y + 6, GuiStyle.TEXT);
                drawRight(gui, Component.literal("¥" + money(price.basePrice())),
                        leftPos + ANIMAL_X + 124, y + 6, GuiStyle.ACCENT);
                drawRight(gui, Component.literal("¥" + money(price.extraPerKg())),
                        leftPos + ANIMAL_X + ANIMAL_W - 3, y + 6, 0x9FE0FF);
            }
            gui.disableScissor();
        }
    }

// 鼠标是否落在行情页某栏的某一可见行上
    private boolean inPriceRow(double mouseX, double mouseY, int x, int w, int row) {
        if (mouseX < leftPos + x || mouseX >= leftPos + x + w) {
            return false;
        }
        int y = topPos + PRICE_TOP + row * ROW_H;
        return mouseY >= y && mouseY < y + ROW_H - 2;
    }

// 价格文本：固定两位小数
    private static String money(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

// 文字放不进 width 就截断并补省略号
    private String fit(String text, int width) {
        if (font.width(text) <= width) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(0, width - font.width("…"))) + "…";
    }

// 右对齐画一段文字（right 是右边缘的绝对 x 坐标）
    private void drawRight(GuiGraphics gui, Component text, int right, int y, int color) {
        gui.drawString(font, text, right - font.width(text), y, color);
    }

// 网购页

    private void renderShopping(GuiGraphics gui, int mouseX, int mouseY) {
        List<Shop> shops = ShopClientData.shops();
        renderShopList(gui, mouseX, mouseY, shops, false);
        if (this.shopPageId == null) {
            gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.pick_shop_hint"),
                    leftPos + RIGHT_X + 2, topPos + LIST_TOP + 4, GuiStyle.TEXT_DIM);
        } else {
            Shop shop = ShopClientData.findShop(this.shopPageId);
            if (shop != null) {
                renderEntryList(gui, mouseX, mouseY, shop, true);
            }
        }
// 购物车合计
        long total = cartTotal();
        gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.cart_total", total),
                leftPos + LIST_X, topPos + 162, GuiStyle.ACCENT);
    }

    private long cartTotal() {
        long total = 0L;
        for (CartLine line : ShopClientData.cart()) {
            Shop shop = ShopClientData.findShop(line.shopId());
            if (shop == null || line.entryIndex() < 0 || line.entryIndex() >= shop.entries().size()) {
                continue;
            }
            total += (long) shop.entries().get(line.entryIndex()).price() * line.qty();
        }
        return total;
    }

// 开店页

    private void renderShop(GuiGraphics gui, int mouseX, int mouseY) {
        List<Shop> shops = ShopClientData.shops();
        if (this.manageId == null) {
            renderShopList(gui, mouseX, mouseY, shops, true);
            gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.shop_name_hint"),
                    leftPos + RIGHT_X, topPos + 78, GuiStyle.TEXT_DIM);
        } else {
            Shop shop = ShopClientData.findShop(this.manageId);
            if (shop == null) {
                this.manageId = null;
                return;
            }
            renderManageEntries(gui, mouseX, mouseY, shop);
            gui.drawString(font, Component.translatable(shop.title()),
                    leftPos + RIGHT_X, topPos + 26, GuiStyle.TEXT);
            gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.market.balance", shop.balance()),
                    leftPos + RIGHT_X, topPos + 38, GuiStyle.ACCENT);
        }
    }

// 列表绘制

// 画店铺列表（左列）；own = true 时用于开店页，自己的店高亮
    private void renderShopList(GuiGraphics gui, int mouseX, int mouseY, List<Shop> shops, boolean own) {
        if (shops.isEmpty()) {
            gui.drawString(font, Component.literal("-"), leftPos + LIST_X + 4, topPos + LIST_TOP + 4, GuiStyle.TEXT_DIM);
            return;
        }
        this.shopScroll = Mth.clamp(this.shopScroll, 0, Math.max(0, shops.size() - LIST_ROWS));
        gui.enableScissor(leftPos, topPos + LIST_TOP, leftPos + LIST_X + LIST_W, topPos + LIST_TOP + LIST_ROWS * ROW_H);
        for (int row = 0; row < LIST_ROWS; row++) {
            int index = this.shopScroll + row;
            if (index >= shops.size()) {
                break;
            }
            Shop shop = shops.get(index);
            int y = topPos + LIST_TOP + row * ROW_H;
            boolean selected = own
                    ? shop.id().equals(this.manageId)
                    : shop.id().equals(this.shopPageId);
            boolean hovered = inRow(mouseX, mouseY, LIST_X, LIST_W, row);
            GuiStyle.row(gui, leftPos + LIST_X, y, LIST_W, ROW_H - 2, hovered, selected);
            gui.drawString(font, Component.translatable(shop.title()),
                    leftPos + LIST_X + 4, y + 6, shop.isSystem() ? 0xE0E0E0 : 0xFFD24A);
        }
        gui.disableScissor();
    }

// 画条目列表（右列，网购用）；buy = true 时带购物车 +/-
    private void renderEntryList(GuiGraphics gui, int mouseX, int mouseY, Shop shop, boolean buy) {
        List<ShopEntry> entries = shop.entries();
        if (entries.isEmpty()) {
            gui.drawString(font, Component.literal("-"), leftPos + RIGHT_X + 4, topPos + LIST_TOP + 4, GuiStyle.TEXT_DIM);
            return;
        }
        this.entryScroll = Mth.clamp(this.entryScroll, 0, Math.max(0, entries.size() - LIST_ROWS));
        gui.enableScissor(leftPos, topPos + LIST_TOP, leftPos + RIGHT_X + RIGHT_W, topPos + LIST_TOP + LIST_ROWS * ROW_H);
        for (int row = 0; row < LIST_ROWS; row++) {
            int index = this.entryScroll + row;
            if (index >= entries.size()) {
                break;
            }
            ShopEntry entry = entries.get(index);
            int y = topPos + LIST_TOP + row * ROW_H;
            boolean hovered = inRow(mouseX, mouseY, RIGHT_X, RIGHT_W, row);
            GuiStyle.row(gui, leftPos + RIGHT_X, y, RIGHT_W, ROW_H - 2, hovered, false);
            if (hovered) {
                this.hoveredStack = new ItemStack(entry.item(), Math.max(1, entry.count()));
            }

            gui.renderItem(new ItemStack(entry.item()), leftPos + RIGHT_X + 2, y + 2);
            gui.drawString(font, "x" + entry.count(), leftPos + RIGHT_X + 21, y + 3, GuiStyle.TEXT);
            gui.drawString(font, entry.infinite()
                            ? Component.translatable("gui.flavor_immersed_daily_argo.market.infinite_stock")
                            : Component.translatable("gui.flavor_immersed_daily_argo.market.in_stock", entry.stock()),
                    leftPos + RIGHT_X + 44, y + 3,
                    entry.infinite() || entry.stock() > 0 ? 0x80A080 : 0xA08080);
            String price = "¥" + entry.price();
            gui.drawString(font, price, leftPos + RIGHT_X + RIGHT_W - 4 - font.width(price), y + 3, GuiStyle.ACCENT);

            if (buy) {
                int qty = ShopClientData.qty(shop.id(), index);
                gui.drawString(font, "-", leftPos + RIGHT_X + 118, y + 12, 0xFF8080);
                gui.drawString(font, "[" + qty + "]", leftPos + RIGHT_X + 130, y + 12, 0x9FE0FF);
                gui.drawString(font, "+", leftPos + RIGHT_X + 156, y + 12, 0x80FF80);
            }
        }
        gui.disableScissor();
    }

// 开店页管理视图：左列条目列表（可选中）
    private void renderManageEntries(GuiGraphics gui, int mouseX, int mouseY, Shop shop) {
        List<ShopEntry> entries = shop.entries();
        if (entries.isEmpty()) {
// 按左列宽度折行，避免长提示压到右侧的按钮上
            var hint = font.split(Component.translatable("gui.flavor_immersed_daily_argo.market.drag_item_hint"), LIST_W - 4);
            for (int i = 0; i < hint.size(); i++) {
                gui.drawString(font, hint.get(i), leftPos + LIST_X + 2, topPos + LIST_TOP + 4 + i * 10, GuiStyle.TEXT_DIM);
            }
            return;
        }
        this.entryScroll = Mth.clamp(this.entryScroll, 0, Math.max(0, entries.size() - LIST_ROWS));
        gui.enableScissor(leftPos, topPos + LIST_TOP, leftPos + LIST_X + LIST_W, topPos + LIST_TOP + LIST_ROWS * ROW_H);
        for (int row = 0; row < LIST_ROWS; row++) {
            int index = this.entryScroll + row;
            if (index >= entries.size()) {
                break;
            }
            ShopEntry entry = entries.get(index);
            int y = topPos + LIST_TOP + row * ROW_H;
            boolean selected = index == this.selectedEntry;
            boolean hovered = inRow(mouseX, mouseY, LIST_X, LIST_W, row);
            GuiStyle.row(gui, leftPos + LIST_X, y, LIST_W, ROW_H - 2, hovered, selected);
            if (hovered) {
                this.hoveredStack = new ItemStack(entry.item(), Math.max(1, entry.count()));
            }
            gui.renderItem(new ItemStack(entry.item()), leftPos + LIST_X + 2, y + 2);
            gui.drawString(font, "x" + entry.count(), leftPos + LIST_X + 21, y + 3, GuiStyle.TEXT);
            String price = "¥" + entry.price();
            gui.drawString(font, price, leftPos + LIST_X + LIST_W - 4 - font.width(price), y + 3, GuiStyle.ACCENT);
            gui.drawString(font, entry.infinite() ? "∞" : String.valueOf(entry.stock()),
                    leftPos + LIST_X + 2, y + 12, 0xA0C0A0);
        }
        gui.disableScissor();
    }

// 鼠标是否落在左/右列的某一可见行上
    private boolean inRow(double mouseX, double mouseY, int x, int w, int row) {
        if (mouseX < leftPos + x || mouseX >= leftPos + x + w) {
            return false;
        }
        int y = topPos + LIST_TOP + row * ROW_H;
        return mouseY >= y && mouseY < y + ROW_H - 2;
    }

// 鼠标所在的行下标（相对列区域）；不在列区域或没有内容返回 -1
    private int rowIndexAt(double mouseX, double mouseY, int x, int w, int scroll, int count) {
        if (mouseX < leftPos + x || mouseX >= leftPos + x + w) {
            return -1;
        }
        double top = topPos + LIST_TOP;
        if (mouseY < top || mouseY >= top + LIST_ROWS * ROW_H) {
            return -1;
        }
        int row = (int) ((mouseY - top) / ROW_H);
        int index = scroll + row;
        return index >= 0 && index < count ? index : -1;
    }

// 交互

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
// 选卡面板是模态的：先处理面板，点别处一律吞掉
        if (this.pickerMode != PICKER_NONE) {
            if (button == 0) {
                if (inCancelButton(mouseX, mouseY)) {
                    closeCardPicker();
                    return true;
                }
                int row = cardRowAt(mouseX, mouseY);
                if (row >= 0 && row < this.cardSlots.size()) {
                    int slot = this.cardSlots.get(row);
                    if (this.pickerMode == PICKER_CHECKOUT) {
                        PacketDistributor.sendToServer(
                                new ShopCheckoutPacket(List.copyOf(ShopClientData.cart()), slot));
                    } else {
                        PacketDistributor.sendToServer(new ShopManagePacket(
                                ShopManagePacket.Action.WITHDRAW, this.manageId, "", slot, 0));
                    }
                    closeCardPicker();
                }
            }
            return true;
        }

        if (button == 0) {
            if (this.page == 1) {
                List<Shop> shops = ShopClientData.shops();
                int shopIndex = rowIndexAt(mouseX, mouseY, LIST_X, LIST_W, this.shopScroll, shops.size());
                if (shopIndex >= 0) {
                    this.shopPageId = shops.get(shopIndex).id();
                    this.entryScroll = 0;
                    rebuild();
                    return true;
                }
                if (this.shopPageId != null) {
                    Shop shop = ShopClientData.findShop(this.shopPageId);
                    if (shop != null) {
                        int entryIndex = rowIndexAt(mouseX, mouseY, RIGHT_X, RIGHT_W, this.entryScroll, shop.entries().size());
                        if (entryIndex >= 0) {
// 行左侧加购；右侧的 - / + 增减数量
                            double rel = mouseX - (leftPos + RIGHT_X);
                            if (rel >= 114 && rel < 128) {
                                ShopClientData.removeLine(shop.id(), entryIndex);
                            } else if (rel >= 152 && rel < 166) {
                                ShopClientData.addLine(shop.id(), entryIndex);
                            } else {
                                ShopClientData.addLine(shop.id(), entryIndex);
                            }
                            return true;
                        }
                    }
                }
            } else if (this.page == 2 && this.manageId == null) {
                List<Shop> shops = ShopClientData.shops();
                int shopIndex = rowIndexAt(mouseX, mouseY, LIST_X, LIST_W, this.shopScroll, shops.size());
                if (shopIndex >= 0) {
                    Shop shop = shops.get(shopIndex);
                    String me = this.minecraft != null && this.minecraft.player != null
                            ? this.minecraft.player.getName().getString() : "";
                    if (shop.playerShop() && shop.ownerName().equals(me)) {
                        this.manageId = shop.id();
                        this.selectedEntry = -1;
                        this.entryScroll = 0;
                        rebuild();
                    }
                    return true;
                }
            } else if (this.page == 2) {
                Shop shop = ShopClientData.findShop(this.manageId);
                if (shop != null) {
                    int entryIndex = rowIndexAt(mouseX, mouseY, LIST_X, LIST_W, this.entryScroll, shop.entries().size());
                    if (entryIndex >= 0) {
                        this.selectedEntry = entryIndex;
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (this.pickerMode != PICKER_NONE) {
            return true;
        }
        int step = (int) Math.signum(scrollY);
// 新闻页：整块区域都可滚动
        if (this.page == 0) {
            int count = ShopClientData.news().size();
            this.newsScroll = Mth.clamp(this.newsScroll - step, 0, Math.max(0, count - LIST_ROWS));
            return true;
        }
// 行情页：左栏生鱼、右栏畜类各自滚动
        if (this.page == 3) {
            if (mouseX < leftPos + ANIMAL_X) {
                int count = ShopClientData.fishPrices().size();
                this.fishScroll = Mth.clamp(this.fishScroll - step, 0, Math.max(0, count - PRICE_ROWS));
            } else {
                int count = ShopClientData.animalPrices().size();
                this.animalScroll = Mth.clamp(this.animalScroll - step, 0, Math.max(0, count - PRICE_ROWS));
            }
            return true;
        }
// 网购页：店铺列在左、条目列在右
        if (this.page == 1) {
            if (mouseX >= leftPos + LIST_X && mouseX < leftPos + LIST_X + LIST_W) {
                int count = ShopClientData.shops().size();
                this.shopScroll = Mth.clamp(this.shopScroll - step, 0, Math.max(0, count - LIST_ROWS));
                return true;
            }
            Shop shop = ShopClientData.findShop(this.shopPageId);
            if (shop != null && mouseX >= leftPos + RIGHT_X) {
                int count = shop.entries().size();
                this.entryScroll = Mth.clamp(this.entryScroll - step, 0, Math.max(0, count - LIST_ROWS));
                return true;
            }
        } else if (this.page == 2) {
// 开店页：列表视图店铺在左；管理视图条目在左
            Shop current;
            int x;
            if (this.manageId == null) {
                current = null;
                x = LIST_X;
            } else {
                current = ShopClientData.findShop(this.manageId);
                x = LIST_X;
            }
            if (mouseX >= leftPos + x && mouseX < leftPos + x + LIST_W) {
                if (current == null) {
                    int count = ShopClientData.shops().size();
                    this.shopScroll = Mth.clamp(this.shopScroll - step, 0, Math.max(0, count - LIST_ROWS));
                } else {
                    int count = current.entries().size();
                    this.entryScroll = Mth.clamp(this.entryScroll - step, 0, Math.max(0, count - LIST_ROWS));
                }
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
// 选卡面板打开时，ESC 只关面板，不关界面
        if (this.pickerMode != PICKER_NONE && keyCode == 256) {
            closeCardPicker();
            return true;
        }
        if (this.shopNameField != null && this.shopNameField.isFocused()
                && this.shopNameField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.priceField != null && this.priceField.isFocused()
                && this.priceField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.pickerMode != PICKER_NONE) {
            return true;
        }
        if (this.shopNameField != null && this.shopNameField.isFocused()
                && this.shopNameField.charTyped(codePoint, modifiers)) {
            return true;
        }
        if (this.priceField != null && this.priceField.isFocused()
                && this.priceField.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
}
