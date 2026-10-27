package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.item.TooltipItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Locale;

// 杆秤：右键打开称重界面，把东西放上秤盘就抽一个重量记在物品上。
//
// 重量只在「上过秤」之后才写进物品的重量数据组件，所以没称过的东西照常堆叠；称过之后每件
// 重量都不同，自然就分开了。重量特别大的东西还会在全服通报一声。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public class SteelyardItem extends TooltipItem {

// 超过这个重量（斤）就在全服通报
    public static final double BROADCAST_WEIGHT = 30.0D;

    public SteelyardItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new SteelyardMenu(id, inv),
                    Component.translatable("gui.flavor_immersed_daily_argo.steelyard")));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

// 上秤：给还没称过的物品抽一个重量写进组件。
//
// 物品不在任何称重分类里（weighing_types 数据包没收录）就原样不动——
// 称不了的东西不会凭空多个重量出来。
    public static void weigh(ItemStack stack, Player player) {
        if (stack.isEmpty() || stack.has(ModDataComponents.WEIGHED_WEIGHT.get())) {
            return;
        }
        WeighingTypeData.Distribution distribution = WeighingTypeData.getInstance().forItem(stack);
        if (distribution == null) {
            return;
        }
        double weight = distribution.sample(player.level().random);
        stack.set(ModDataComponents.WEIGHED_WEIGHT.get(), weight);

// 大货：全服通报一声
        if (weight >= BROADCAST_WEIGHT && player.level() instanceof ServerLevel serverLevel) {
            Component message = Component.translatable("msg.flavor_immersed_daily_argo.heavy_catch",
                    player.getDisplayName(), stack.getHoverName(), format(weight));
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(message, false);
        }
    }

// 重量文本：整数不带小数尾巴
    public static String format(double weight) {
        return weight == Math.floor(weight) && !Double.isInfinite(weight)
                ? String.valueOf((long) weight)
                : String.format(Locale.ROOT, "%.2f", weight);
    }

// 称过的物品都在 tooltip 里带上重量
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Double weight = event.getItemStack().get(ModDataComponents.WEIGHED_WEIGHT.get());
        if (weight != null) {
            event.getToolTip().add(Component
                    .translatable("tooltip.flavor_immersed_daily_argo.weight", format(weight))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
