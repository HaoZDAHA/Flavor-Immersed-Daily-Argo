package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.fishing.FishingRodModMenu;
import com.flavor_immersed_daily_argo.fishing.FishingRodStats;
import com.flavor_immersed_daily_argo.fishing.ModFishingHook;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Locale;

public class ModFishingRodItem extends FishingRodItem {

    public ModFishingRodItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        FishingHook active = player.fishing;

        if (active != null) {
// 收杆 / 遛鱼阶段
            if (!level.isClientSide) {
// 南山钓竿的遛鱼阶段：右键可能被阶段接管（进入阶段 / 阶段中主动放弃）
                if (active instanceof ModFishingHook hook && hook.handleRightClick(player)) {
                    player.swing(hand);
                    return InteractionResultHolder.sidedSuccess(itemstack, false);
                }
                int damage = active.retrieve(itemstack);
                itemstack.hurtAndBreak(damage, player, p -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL,
                        1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            }
            player.swing(hand);
        } else {
// 抛竿 —— 投掷自定义鱼钩
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL,
                    0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!level.isClientSide) {
                ModFishingHook hook = new ModFishingHook(player, level, 0, 0);
                level.addFreshEntity(hook);
            }
            player.swing(hand);
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
// 打开改装页的提示
        tooltipComponents.add(Component.translatable("tooltip.flavor_immersed_daily_argo.rod_hint")
                .withStyle(ChatFormatting.DARK_GRAY));

        List<ItemStack> mods = FishingRodModMenu.getData(stack).mods();

// 配件一览
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(section("tooltip.flavor_immersed_daily_argo.rod_section_parts"));
        tooltipComponents.add(part("gui.flavor_immersed_daily_argo.line", partName(mods, 0)));
        tooltipComponents.add(part("gui.flavor_immersed_daily_argo.body", partName(mods, 1)));
        tooltipComponents.add(part("gui.flavor_immersed_daily_argo.reel", partName(mods, 2)));
        tooltipComponents.add(part("gui.flavor_immersed_daily_argo.bait", partName(mods, 3)));
        tooltipComponents.add(part("gui.flavor_immersed_daily_argo.floater", partName(mods, 4)));
        tooltipComponents.add(part("gui.flavor_immersed_daily_argo.sinker", partName(mods, 5)));

// 六个槽位实时推算出的钓竿参数
        FishingRodStats stats = FishingRodStats.of(mods);
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(section("tooltip.flavor_immersed_daily_argo.rod_section_stats"));
// 临界耐久：竿体与鱼线中最低的耐久值（某一侧未装时该侧取基准耐久 60）
        tooltipComponents.add(stat("tooltip.flavor_immersed_daily_argo.rod_critical",
                trim(stats.criticalDurability)));
// 收杆容错时长：遛鱼阶段最长允许时间
        tooltipComponents.add(stat("tooltip.flavor_immersed_daily_argo.rod_reel_tolerance",
                trim(stats.toleranceSeconds) + unit("rod_unit_seconds")));
// 收杆动作次数：完成几次收杆动作可钓上鱼
        tooltipComponents.add(stat("tooltip.flavor_immersed_daily_argo.rod_reel_actions",
                stats.reelActions + unit("rod_unit_times")));
// 抗流能力
        tooltipComponents.add(stat("tooltip.flavor_immersed_daily_argo.rod_current_resistance",
                trim(stats.currentResistance)));
// 深度区间
        tooltipComponents.add(stat("tooltip.flavor_immersed_daily_argo.rod_depth_range",
                trim(stats.depthMin) + " ~ " + trim(stats.depthMax)));
// 饵料类型：鱼饵槽里装的鱼饵名（空槽显示「无」）
        tooltipComponents.add(stat("tooltip.flavor_immersed_daily_argo.rod_bait_type",
                partName(mods, FishingRodStats.SLOT_BAIT).getString()));
// 最大拉力：竿体与鱼线中最低的拉力（某一侧未装时该侧取默认拉力 10）
        tooltipComponents.add(stat("tooltip.flavor_immersed_daily_argo.rod_max_tension",
                trim(stats.maxTension)));
// 钓组显眼度：影响咬钩率与遛鱼时的屏幕抖动
        tooltipComponents.add(stat("tooltip.flavor_immersed_daily_argo.rod_conspicuousness",
                trim(stats.conspicuousness)));
    }

// 分组标题：金色竖条 + 金色标题
    private static Component section(String key) {
        return Component.literal("▎").withStyle(ChatFormatting.GOLD)
                .append(Component.translatable(key).withStyle(ChatFormatting.GOLD));
    }

// 参数行：灰色标签（补全角空格对齐）+ 白色数值
    private static Component stat(String labelKey, String value) {
        Component label = Component.translatable(labelKey);
        return Component.literal(" ").withStyle(ChatFormatting.RESET)
                .append(Component.literal(pad(label.getString(), 6)).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
    }

// 配件行：灰色槽位名 + 白色配件名
    private static Component part(String labelKey, Component value) {
        Component label = Component.translatable(labelKey);
        return Component.literal(" ").withStyle(ChatFormatting.RESET)
                .append(Component.literal(pad(label.getString(), 3)).withStyle(ChatFormatting.GRAY))
                .append(value.copy().withStyle(ChatFormatting.WHITE));
    }

// 槽位里配件的显示名；空槽位显示「无」
    private static Component partName(List<ItemStack> mods, int slot) {
        if (slot >= mods.size() || mods.get(slot).isEmpty()) {
            return none();
        }
        return mods.get(slot).getHoverName();
    }

// 用全角空格补齐到指定全角字数，让后面的数值对齐
    private static String pad(String text, int width) {
        return text.length() >= width ? text + " " : text + "\u3000".repeat(width - text.length());
    }

// 「无」文案
    private static Component none() {
        return Component.translatable("tooltip.flavor_immersed_daily_argo.rod_none");
    }

// 数值单位（前面加一个空格）
    private static String unit(String key) {
        return " " + Component.translatable("tooltip.flavor_immersed_daily_argo." + key).getString();
    }

// 去掉整数的小数尾巴：30.0 → 30，30.5 → 30.5
    private static String trim(double value) {
        return value == Math.floor(value) && !Double.isInfinite(value)
                ? String.valueOf((long) value)
                : String.format(Locale.ROOT, "%.1f", value);
    }
}
