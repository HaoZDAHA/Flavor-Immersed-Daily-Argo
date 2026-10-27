package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

// 「兴盛」林业网点（评分 ≥ ForestryPoint#PROSPERITY_SCORE）带给所在区块的六项加成：
//
// 作物生长加速：#onCropGrow —— 随机刻生长时有概率直接长一阶；
// 动物心情提升：在 AnimalStatsManager 的每日结算里额外 +1（见该类注释）；
// 钓鱼多宝：#onItemFished —— 钓获时有概率附带一份原版宝藏战利品；
// 掉落翻倍自带抢夺：#onLivingDrops —— 击杀敌对生物掉落翻倍、并额外多掉；
// 村民交易降价：#onTradeWithVillager —— 交易时给一份固定折扣；
// 挖掘自带时运：#onBlockDrops —— 破坏方块时有概率多掉一份。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class ForestryEffects {

// 作物恰好走随机刻生长时，额外再长一阶的概率
    private static final float CROP_BONUS_CHANCE = 0.35F;
// 钓获时附带一份宝藏的概率
    private static final float FISH_TREASURE_CHANCE = 0.4F;
// 击杀敌对生物时，每件掉落额外多掉一份的概率（近似抢夺 I）
    private static final float LOOTING_BONUS_CHANCE = 0.25F;
// 挖掘时每件掉落额外多掉一份的概率（近似时运 I）
    private static final float FORTUNE_BONUS_CHANCE = 0.34F;
// 村民交易的固定折扣（绿宝石数）
    private static final int VILLAGER_DISCOUNT = 2;

    private ForestryEffects() {
    }

// 作物生长加速

    @SubscribeEvent
    public static void onCropGrow(CropGrowEvent.Pre event) {
        if (event.getResult() == CropGrowEvent.Pre.Result.GROW) {
            return;
        }
        if (!(event.getLevel() instanceof Level level)
                || !ForestryNetworkManager.isProsperous(level, event.getPos())) {
            return;
        }
        if (event.getLevel().getRandom().nextFloat() < CROP_BONUS_CHANCE) {
            event.setResult(CropGrowEvent.Pre.Result.GROW);
        }
    }

// 钓鱼多宝

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.getRandom().nextFloat() >= FISH_TREASURE_CHANCE) {
            return;
        }
        FishingHook hook = event.getHookEntity();
        if (hook == null || !ForestryNetworkManager.isProsperous(player.level(), hook.blockPosition())) {
            return;
        }
        ServerLevel level = player.serverLevel();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING_TREASURE);
        if (table == null || table == LootTable.EMPTY) {
            return;
        }
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, hook.position())
                .withParameter(LootContextParams.TOOL, player.getMainHandItem())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.FISHING);
        event.getDrops().addAll(table.getRandomItems(params));
    }

// 击杀敌对生物掉落翻倍

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Enemy)) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof ServerPlayer player)
                || !ForestryNetworkManager.isProsperous(player.level(), event.getEntity().blockPosition())) {
            return;
        }
        RandomSource random = player.getRandom();
        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            int doubled = Math.min(stack.getMaxStackSize(), stack.getCount() * 2);
// 自带抢夺：再按概率额外多掉一份
            if (random.nextFloat() < LOOTING_BONUS_CHANCE && doubled < stack.getMaxStackSize()) {
                doubled++;
            }
            stack.setCount(doubled);
        }
    }

// 村民交易降价

    @SubscribeEvent
    public static void onTradeWithVillager(TradeWithVillagerEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        AbstractVillager villager = event.getAbstractVillager();
        if (villager == null || !ForestryNetworkManager.isProsperous(villager.level(), villager.blockPosition())) {
            return;
        }
        MerchantOffer offer = event.getMerchantOffer();
        int current = offer.getSpecialPriceDiff();
// 折扣后第一格至少要剩 1 个，避免价格被压到 0 或复数
        int floor = 1 - offer.getCostA().getCount();
        int next = Math.max(floor, current - VILLAGER_DISCOUNT);
        if (next != current) {
            offer.setSpecialPriceDiff(next);
        }
    }

// 挖掘自带时运

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)
                || !ForestryNetworkManager.isProsperous(event.getLevel(), event.getPos())) {
            return;
        }
        RandomSource random = player.getRandom();
        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if (stack.getCount() < stack.getMaxStackSize() && random.nextFloat() < FORTUNE_BONUS_CHANCE) {
                stack.grow(1);
            }
        }
    }
}
