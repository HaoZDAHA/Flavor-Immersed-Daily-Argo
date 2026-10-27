package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.all.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.List;

// 手上拿着导航手机、又有在途订单时，在准星下方渲染一枚原版指南针，指向正在追踪的那个点，
// 并在指南针下方实时显示还有多少格。
//
// 指向哪个点：优先玩家在接单界面选中的那条；没选、或选中的那条已经结束（结算 / 取消），就退回
// 离玩家最近的那条已接单订单。每条订单的「下一个点」= 还没取货取出发地、已经取到货取目的地。
// 指针取哪一帧沿用原版 CompassItemPropertyFunction 的算法（并按原版物品模型的映射换算成
// 贴图编号），所以指向跟手持的原版指南针一致。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class DeliveryCompassHud {

// 指南针贴图边长（原版 16×16）
    private static final int SIZE = 16;
// 与准星之间的间距
    private static final int GAP_BELOW_CROSSHAIR = 12;
// 距离文字与指南针之间的间距
    private static final int GAP_BELOW_COMPASS = 2;
// 距离底衬相对文字的内边距
    private static final int PADDING_X = 3;
    private static final int PADDING_Y = 2;

    private static final int COLOR_TEXT = 0xFFF2F2F2;
    private static final int COLOR_TEXT_BACKGROUND = 0x80000000;

    private static final String TEXTURE_PREFIX = "textures/item/compass_";
    private static final String KEY_DISTANCE = "gui.flavor_immersed_daily_argo.nav_phone_compass_distance";

    private DeliveryCompassHud() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null
                || minecraft.screen != null || minecraft.options.hideGui) {
            return;
        }
        if (!holdsPhone(player)) {
            return;
        }
        int[] target = targetPoint(player, NavPhoneClientData.orders(), NavPhoneClientData.trackedId());
        if (target == null) {
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();
        Font font = minecraft.font;
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        int frame = compassFrame(player, partialTick, target[0], target[1]);
        ResourceLocation texture = ResourceLocation.withDefaultNamespace(
                TEXTURE_PREFIX + (frame < 10 ? "0" : "") + frame + ".png");
        int compassX = gui.guiWidth() / 2 - SIZE / 2;
        int compassY = gui.guiHeight() / 2 + GAP_BELOW_CROSSHAIR;
        gui.blit(texture, compassX, compassY, SIZE, SIZE, 0.0F, 0.0F, SIZE, SIZE, SIZE, SIZE);

// 指南针下方：还差多少格
        Component distance = Component.translatable(KEY_DISTANCE, blocksTo(player, target[0], target[1]));
        int textWidth = font.width(distance);
        int textX = (gui.guiWidth() - textWidth) / 2;
        int textY = compassY + SIZE + GAP_BELOW_COMPASS;
        gui.fill(textX - PADDING_X, textY - PADDING_Y, textX + textWidth + PADDING_X,
                textY + font.lineHeight + PADDING_Y, COLOR_TEXT_BACKGROUND);
        gui.drawString(font, distance, textX, textY, COLOR_TEXT, true);
    }

// 主手或副手拿着导航手机
    private static boolean holdsPhone(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main.is(ModItems.NAV_PHONE.get()) || off.is(ModItems.NAV_PHONE.get());
    }

// 该指向哪个点：优先选中的那条已接单订单，否则退回最近的。
//
// 返回：目标 xz，没有可指的目标时返回 null
    private static int[] targetPoint(Player player, List<PhoneOrderView> orders, long trackedId) {
        int[] tracked = null;
        int[] nearest = null;
        double best = Double.MAX_VALUE;
        for (PhoneOrderView view : orders) {
            if (!view.accepted()) {
                continue;
            }
            DeliveryOrder order = view.order();
            int x = view.pickedUp() ? order.destX() : order.originX();
            int z = view.pickedUp() ? order.destZ() : order.originZ();
            if (trackedId != 0L && order.id() == trackedId) {
                tracked = new int[]{x, z};
            }
            double dx = x + 0.5D - player.getX();
            double dz = z + 0.5D - player.getZ();
            double dist = dx * dx + dz * dz;
            if (dist < best) {
                best = dist;
                nearest = new int[]{x, z};
            }
        }
        return tracked != null ? tracked : nearest;
    }

// 玩家到某个方块的水平距离（格）
    private static int blocksTo(Player player, int x, int z) {
        double dx = x + 0.5D - player.getX();
        double dz = z + 0.5D - player.getZ();
        return (int) Math.round(Math.sqrt(dx * dx + dz * dz));
    }

// 指向目标时该用哪一帧指南针贴图。
//
// 沿用原版 CompassItemPropertyFunction 的算法算出 0~1 的旋转值，再按原版物品模型
// compass.json 的映射（每 1/32 一帧、整体偏移 16 帧）换算成 compass_XX 的编号。
    private static int compassFrame(Player player, float partialTick, int targetX, int targetZ) {
        double d0 = Math.atan2(targetZ + 0.5D - player.getZ(), targetX + 0.5D - player.getX())
                / (Math.PI * 2.0D);
        double d1 = Mth.positiveModulo(player.getViewYRot(partialTick) / 360.0F, 1.0D);
        double rotation = Mth.positiveModulo(0.75D - d1 + d0, 1.0D);
        return (Mth.floor(rotation * 32.0D + 0.5D) + 16) & 31;
    }
}
