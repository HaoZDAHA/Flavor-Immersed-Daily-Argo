package com.flavor_immersed_daily_argo.textile;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

// 丝绸
//
// 右键极短蓄力后消耗一份丝绸，随机修复身上一件「已经出现损坏」的护甲：
// 从四件护甲里挑出一件当前耐久有缺损的，回复其最大耐久的 1/10（至少 1 点）。
public class SilkItem extends Item {

// 蓄力时长：极短，6 tick ≈ 0.3 秒
    private static final int CHARGE_TICKS = 6;

// 每次修复的耐久上限比例（最大耐久的 1/10）
    private static final int REPAIR_FRACTION = 10;

    public SilkItem(Properties properties) {
        super(properties);
    }

// 身上没有破损护甲时不进入蓄力，直接当作普通右键
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (findDamagedArmor(player).isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return CHARGE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return stack;
        }
        List<ItemStack> damaged = findDamagedArmor(player);
        if (damaged.isEmpty()) {
            return stack;
        }
        if (!level.isClientSide) {
            ItemStack armor = damaged.get(level.random.nextInt(damaged.size()));
            int repair = Math.max(1, armor.getMaxDamage() / REPAIR_FRACTION);
            armor.setDamageValue(Math.max(0, armor.getDamageValue() - repair));
            stack.consume(1, player);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        return stack;
    }

// 收集玩家身上所有可修复、且已经出现损坏的护甲
    private static List<ItemStack> findDamagedArmor(Player player) {
        List<ItemStack> damaged = new ArrayList<>();
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!armor.isEmpty() && armor.isDamageableItem() && armor.getDamageValue() > 0) {
                damaged.add(armor);
            }
        }
        return damaged;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.flavor_immersed_daily_argo.silk"));
    }
}
