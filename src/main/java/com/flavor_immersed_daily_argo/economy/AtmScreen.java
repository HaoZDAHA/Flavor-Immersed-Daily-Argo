package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.client.GuiStyle;
import com.flavor_immersed_daily_argo.client.RoundedButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

// 自助取款机 / 银行卡界面。
//
// 自助取款机模式分两页：
//
// 存取页：持卡主人 / 卡面账户读数，改主人、存入、按面额取出；
// 外汇兑换页：按配置里的条目列出来，点一条就用卡面账户的钱换别的模组物品。
//
// 直接右键银行卡打开的模式只有主人 / 账户读数与 9 格存储，不显示任何页签或按钮。
public class AtmScreen extends AbstractContainerScreen<AtmMenu> {

    private static final int IMG_W = 176;
// 自助取款机：顶部有主人 / 账户 / 按钮区（含外汇页），卡槽只有 1 行
    private static final int ATM_IMG_H = 236;
// 直接右键银行卡：只有读数 + 槽位，界面矮一些
    private static final int CARD_IMG_H = 164;

// 外汇兑换列表区域（相对 topPos）：起始 y / 行高 / 一屏行数
    private static final int EX_LIST_TOP = 40;
    private static final int EX_ROW_H = 18;
    private static final int EX_ROWS = 4;

    private EditBox ownerField;
// 顶部两枚页签（只有取款机模式才有）
    private RoundedButton bankTab;
    private RoundedButton exchangeTab;
// 存取页的控件；切到兑换页时整体摘掉
    private final List<AbstractWidget> bankingWidgets = new ArrayList<>();
    private boolean exchangePage;
// 兑换列表向上滚了多少行
    private int exchangeScroll;

    public AtmScreen(AtmMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = IMG_W;
        this.imageHeight = menu.isAtmMode() ? ATM_IMG_H : CARD_IMG_H;
// 玩家物品栏标题的位置（压在背包槽位上方）
        this.inventoryLabelY = menu.isAtmMode() ? 142 : 70;
    }

    @Override
    protected void init() {
        super.init();
        this.bankingWidgets.clear();

// 直接右键银行卡打开的存储界面没有页签 / 按钮，只有 9 格
        if (!this.menu.isAtmMode()) {
            return;
        }

// 顶部两枚页签（存取 / 外汇），始终可见；选中的一枚走金色画风
        this.bankTab = RoundedButton.of(leftPos + 92, topPos + 3, 38, 14,
                Component.translatable("gui.flavor_immersed_daily_argo.atm_tab_bank"),
                RoundedButton.Style.NORMAL, () -> showPage(false));
        this.exchangeTab = RoundedButton.of(leftPos + 132, topPos + 3, 38, 14,
                Component.translatable("gui.flavor_immersed_daily_argo.atm_tab_exchange"),
                RoundedButton.Style.NORMAL, () -> showPage(true));
        this.addRenderableWidget(this.bankTab);
        this.addRenderableWidget(this.exchangeTab);

// 存取页控件
        this.ownerField = new EditBox(this.font, leftPos + 8, topPos + 42, 120, 16,
                Component.translatable("gui.flavor_immersed_daily_argo.atm_owner_hint"));
        this.ownerField.setMaxLength(32);
// 去掉原版输入框的九宫格底，改用自己画的圆角框（见 renderBg）
        this.ownerField.setBordered(false);
        this.ownerField.setTextColor(GuiStyle.TEXT);
        this.ownerField.setHint(Component.translatable("gui.flavor_immersed_daily_argo.atm_owner_hint"));
        this.ownerField.setValue(this.menu.getOwnerName());
        this.bankingWidgets.add(this.ownerField);

// 改主人：把输入框里的 ID 交给服务端，只有现任主人改得动
        this.bankingWidgets.add(RoundedButton.of(leftPos + 132, topPos + 42, 38, 16,
                Component.translatable("gui.flavor_immersed_daily_argo.atm_set_owner"), () ->
                        PacketDistributor.sendToServer(new AtmSetOwnerPacket(this.ownerField.getValue()))));

// 存入：把卡内纸币折算进卡面账户
        this.bankingWidgets.add(RoundedButton.of(leftPos + 8, topPos + 62, 160, 16,
                Component.translatable("gui.flavor_immersed_daily_argo.atm_deposit"),
                RoundedButton.Style.ACCENT, () -> clickButton(AtmMenu.BUTTON_DEPOSIT)));

// 取出：六种面额各一个按钮，点哪个取哪个，单次最多一组，避免一口气塞爆卡
        int[] denominations = CreditCardItem.DENOMINATIONS;
        for (int i = 0; i < denominations.length; i++) {
            int denom = denominations[i];
            this.bankingWidgets.add(RoundedButton.of(leftPos + 8 + i * 27, topPos + 96, 25, 16,
                    Component.literal(String.valueOf(denom)), () -> clickButton(denom)));
        }

        showPage(this.exchangePage);
    }

// 切换存取页 / 外汇页：把不属于当前页的控件摘掉，属于的挂上
    private void showPage(boolean exchange) {
        this.exchangePage = exchange;
        if (this.bankTab != null && this.exchangeTab != null) {
            this.bankTab.setSelected(!exchange);
            this.exchangeTab.setSelected(exchange);
        }
        for (AbstractWidget widget : this.bankingWidgets) {
            if (exchange) {
                this.removeWidget(widget);
            } else {
                this.addRenderableWidget(widget);
            }
        }
        if (exchange && this.ownerField != null) {
            this.ownerField.setFocused(false);
        }
    }

    private void clickButton(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
// 无贴图：半透明黑圆角底板 + 自绘物品格
        GuiStyle.panel(gui, leftPos, topPos, IMG_W, this.imageHeight);
        GuiStyle.divider(gui, leftPos + 8, topPos + 37, IMG_W - 16);

// 输入框的圆角底（输入框本身已去掉原版边框，只画文字与光标）
        if (this.menu.isAtmMode() && !this.exchangePage) {
            boolean focused = this.ownerField != null && this.ownerField.isFocused();
            GuiStyle.field(gui, leftPos + 5, topPos + 41, 126, 18, focused);
        }

// 卡内 9 格与玩家背包格
        for (Slot slot : this.menu.slots) {
            GuiStyle.slot(gui, leftPos + slot.x, topPos + slot.y);
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);

        gui.drawString(font, this.title, leftPos + 8, topPos + 6, GuiStyle.TEXT);

        String owner = this.menu.getOwnerName();
        gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.atm_owner",
                        owner.isEmpty()
                                ? Component.translatable("tooltip.flavor_immersed_daily_argo.card_owner_none")
                                : Component.literal(owner)),
                leftPos + 8, topPos + 18, GuiStyle.TEXT_DIM);

        gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.atm_value", this.menu.getAccount()),
                leftPos + 8, topPos + 30, GuiStyle.ACCENT);

        if (this.menu.isAtmMode()) {
            if (this.exchangePage) {
                renderExchangeList(gui, mouseX, mouseY);
            } else {
                gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.atm_withdraw_denom"),
                        leftPos + 8, topPos + 84, GuiStyle.TEXT_DIM);
            }
        }

        this.renderTooltip(gui, mouseX, mouseY);
    }

// 画外汇兑换列表：每条一行，点一下就用卡面账户的钱换目标物品；行数超出可滚轮翻看
    private void renderExchangeList(GuiGraphics gui, int mouseX, int mouseY) {
        List<ExchangeEntry> entries = this.menu.getExchanges();
        int top = topPos + EX_LIST_TOP;
        int bottom = top + EX_ROWS * EX_ROW_H;

        if (entries.isEmpty()) {
            gui.drawString(font, Component.translatable("gui.flavor_immersed_daily_argo.atm_exchange_empty"),
                    leftPos + 8, top + 4, GuiStyle.TEXT_DIM);
            return;
        }

        long account = this.menu.getAccount();
        gui.enableScissor(leftPos, top, leftPos + IMG_W, bottom);
        for (int row = 0; row < EX_ROWS; row++) {
            int index = this.exchangeScroll + row;
            if (index >= entries.size()) {
                break;
            }
            ExchangeEntry entry = entries.get(index);
            int y = top + row * EX_ROW_H;
            boolean hovered = mouseX >= leftPos + 4 && mouseX < leftPos + 172
                    && mouseY >= y && mouseY < y + EX_ROW_H - 1;
            GuiStyle.row(gui, leftPos + 4, y, 168, EX_ROW_H - 1, hovered, false);

            gui.renderItem(new ItemStack(entry.source()), leftPos + 6, y + 1);
            gui.drawString(font, "x" + entry.sourceCount(), leftPos + 26, y + 5, GuiStyle.TEXT);
            gui.drawString(font, "→", leftPos + 46, y + 5, GuiStyle.TEXT_DIM);
            gui.renderItem(new ItemStack(entry.target()), leftPos + 58, y + 1);
            gui.drawString(font, "x" + entry.targetCount(), leftPos + 78, y + 5, GuiStyle.TEXT);

            String price = "¥" + entry.price();
            int priceColor = account >= entry.price() ? GuiStyle.ACCENT : GuiStyle.TEXT_DIM;
            gui.drawString(font, price, leftPos + 168 - font.width(price), y + 5, priceColor);
        }
        gui.disableScissor();

// 悬停某一条时给个物品名提示
        int hovered = exchangeIndexAt(mouseX, mouseY);
        if (hovered >= 0) {
            ExchangeEntry entry = entries.get(hovered);
            List<Component> lines = new ArrayList<>(3);
            lines.add(new ItemStack(entry.source()).getHoverName());
            lines.add(Component.literal("→ ").append(new ItemStack(entry.target()).getHoverName()));
            lines.add(Component.translatable("gui.flavor_immersed_daily_argo.atm_exchange_price", entry.price()));
            gui.renderComponentTooltip(font, lines, mouseX, mouseY);
        }
    }

// 鼠标下的那条兑换条目下标；不在列表区域内或没内容就返回 -1
    private int exchangeIndexAt(double mouseX, double mouseY) {
        List<ExchangeEntry> entries = this.menu.getExchanges();
        if (mouseX < leftPos + 4 || mouseX >= leftPos + 172) {
            return -1;
        }
        double top = topPos + EX_LIST_TOP;
        if (mouseY < top || mouseY >= top + EX_ROWS * EX_ROW_H) {
            return -1;
        }
        int row = (int) ((mouseY - top) / EX_ROW_H);
        int index = this.exchangeScroll + row;
        return index < entries.size() ? index : -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.exchangePage && button == 0) {
            int index = exchangeIndexAt(mouseX, mouseY);
            if (index >= 0) {
                PacketDistributor.sendToServer(new AtmExchangePacket(index));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (this.exchangePage) {
            int maxScroll = Math.max(0, this.menu.getExchanges().size() - EX_ROWS);
            this.exchangeScroll = Mth.clamp(this.exchangeScroll - (int) Math.signum(scrollY), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
// 玩家物品栏标题照常（位置由 inventoryLabelY 控制），其余文字交给 render() 里画
        gui.drawString(font, this.playerInventoryTitle, 8, this.inventoryLabelY, GuiStyle.TEXT_DIM);
    }

// 输入框吃键：焦点在输入框时，键盘事件全部转交给它
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.ownerField != null && this.ownerField.isFocused()
                && this.ownerField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.ownerField != null && this.ownerField.isFocused()
                && this.ownerField.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
}
