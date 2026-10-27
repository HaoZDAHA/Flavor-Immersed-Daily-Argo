package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.level.BlockEvent;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 草帽：戴在头上的帽子。
//
// 为什么不是 ArmorItem：原版 CustomHeadLayer 会跳过装备在头部槽位的
// ArmorItem，改由 HumanoidArmorLayer 用固定的盔甲贴图层去画，物品模型里调好的
// display.head 根本用不上。做成普通物品、只把装备槽位指到
// EquipmentSlot#HEAD，头部渲染就回到 CustomHeadLayer 的正轨，模型 JSON 的
// display.head 才会生效。
//
// 时运加成：佩戴时收获农作物有 #FORTUNE_CHANCE 的概率让时运 +1。实现上不去改玩家
// 手里的工具，而是按概率复制一份「时运 +1」的工具来重新结算掉落，所以两条收获路径都吃得到：
// 直接打掉成熟作物走 #onBlockDrops，用镰刀这类右键收割走
// SickleItem 里的掉落计算。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public class StrawHatItem extends TooltipItem {

// 收获农作物时触发「时运 +1」的概率
    public static final float FORTUNE_CHANCE = 0.25F;

    public StrawHatItem(Item.Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

// 占住头部槽位，让原版把草帽当头盔穿戴
    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

// 玩家头上是否戴着草帽
    public static boolean isWornBy(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.STRAW_HAT.get());
    }

// 摇一次「时运 +1」：玩家戴着草帽且命中概率时，返回一份时运 +1 的工具副本（只用来算掉落），
// 否则返回 null。
//
// 空手收获时用一根木棍承载这级时运：作物战利品表里对工具的判断基本都是「不是剪刀」，
// 木棍与空手等价，但它能挂上附魔组件，好让 apply_bonus 按时运结算。
    @Nullable
    public static ItemStack rollFortuneTool(Player player, ItemStack tool) {
        if (!isWornBy(player) || player.getRandom().nextFloat() >= FORTUNE_CHANCE) {
            return null;
        }
        ItemStack boosted = tool.isEmpty() ? new ItemStack(Items.STICK) : tool.copy();
        Enchantment fortune = Enchantments.BLOCK_FORTUNE;
        Map<Enchantment, Integer> enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(boosted));
        enchantments.put(fortune, enchantments.getOrDefault(fortune, 0) + 1);
        EnchantmentHelper.setEnchantments(enchantments, boosted);
        return boosted;
    }

// 直接打掉成熟作物时，草帽的时运加成在这里生效：命中概率就用「时运 +1」的工具把掉落重算一遍，
// 把多出来的那部分补出去（原版掉落照旧，这里只补差额）。
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Entity breaker = event.getPlayer();
        if (!(breaker instanceof Player player)
                || !(event.getLevel() instanceof ServerLevel level)
                || !SickleItem.isMatureCrop(event.getState())) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        ItemStack boosted = rollFortuneTool(player, tool);
        if (boosted == null) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Map<Item, Integer> base = countDrops(Block.getDrops(event.getState(), level, pos, blockEntity, player, tool));
        Map<Item, Integer> extra = countDrops(Block.getDrops(event.getState(), level, pos, blockEntity, player, boosted));
        for (Map.Entry<Item, Integer> entry : extra.entrySet()) {
            int diff = entry.getValue() - base.getOrDefault(entry.getKey(), 0);
            if (diff > 0) {
                Block.popResource(level, pos, new ItemStack(entry.getKey(), diff));
            }
        }
    }

    private static Map<Item, Integer> countDrops(List<ItemStack> drops) {
        Map<Item, Integer> counts = new HashMap<>();
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                counts.merge(drop.getItem(), drop.getCount(), Integer::sum);
            }
        }
        return counts;
    }
}
