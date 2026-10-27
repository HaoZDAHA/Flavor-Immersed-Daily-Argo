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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        Holder<Enchantment> fortune = player.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.FORTUNE);
        EnchantmentHelper.updateEnchantments(boosted,
                enchantments -> enchantments.set(fortune, enchantments.getLevel(fortune) + 1));
        return boosted;
    }

// 直接打掉成熟作物时，草帽的时运加成在这里生效：命中概率就用「时运 +1」的工具把掉落重算一遍，
// 替换掉原本那批掉落物。
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        Entity breaker = event.getBreaker();
        if (!(breaker instanceof Player player) || !SickleItem.isMatureCrop(event.getState())) {
            return;
        }
        ItemStack boosted = rollFortuneTool(player, event.getTool());
        if (boosted == null) {
            return;
        }
        ServerLevel level = event.getLevel();
        BlockPos pos = event.getPos();
        List<ItemStack> drops = Block.getDrops(event.getState(), level, pos, event.getBlockEntity(), player, boosted);
        event.getDrops().clear();
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                event.getDrops().add(new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
            }
        }
    }
}
