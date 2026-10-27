package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

// 洒水壶：以「耐久」充当水量——最高 128，最低 1（永不损毁）。
//
// 右键水源即可把水量打满；对着农作物长按右键会摆出类似拉弓的蓄力姿势，拉满后松开即浇灌
// 准星所在格及其周围的农作物，每一格都有概率附加骨粉效果（催熟），每成功催熟一株掉 1 点耐久。
// 水量只剩 1 时拉不动，会提示去打水。
//
// 右键优先级：作物大多自带右键处理（收获 / 骨粉），且方块自身的右键会排在物品之前。
// 因此打水与蓄力的入口放在 PlayerInteractEvent.RightClickBlock 里，在方块交互之前接管；
// 只在点到农作物 / 水源 / 耕地时才抢占，避免影响箱子等方块的正常右键。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public class WateringCanItem extends TooltipItem {

// 蓄力时长（tick），拉满后才能浇水
    private static final int CHARGE_TICKS = 20;
// 每株作物附加骨粉效果的概率
    private static final float BONEMEAL_CHANCE = 0.35F;
// 浇灌范围：以准星所指方块为中心的水平半径（1 → 3×3）
    private static final int RADIUS = 1;
// 水量耗尽时的提示 key
    private static final String EMPTY_KEY = "message.flavor_immersed_daily_argo.watering_can.empty";

    public WateringCanItem(Item.Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

// 合成出来的洒水壶默认是空的（只剩 1 点耐久），需要先去打水
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        stack.setDamageValue(stack.getMaxDamage() - 1);
    }

// 右键优先：先于方块自身的右键处理

// 在方块自身的右键之前接管：
//
// 点到农作物 → 抢过来开始蓄力（否则会被作物自带的右键收获 / 骨粉吃掉）；
// 点到水源 → 直接打满；
// 点到耕地 → 也允许在这一格上蓄力，方便对着土浇周围作物。
//
// 点到其它方块（箱子等）一律放行，不影响正常交互。
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof WateringCanItem)) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();

// 作物优先于水源判断：水生作物本身带 waterlogged，若先判水源会被误当成打水
        if (state.getBlock() instanceof BonemealableBlock) {
            event.setCanceled(true);
            startUse(level, player, stack, event.getHand());
            return;
        }
        if (isWaterAt(level, pos, event.getFace())) {
            event.setCanceled(true);
            if (!level.isClientSide) {
                fill(level, player, stack);
            }
            return;
        }
        if (state.is(Blocks.FARMLAND)) {
            event.setCanceled(true);
            startUse(level, player, stack, event.getHand());
        }
    }

// 兜底：事件未触发时，右键空气也能打水 / 蓄力
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (fillFromSight(level, player, stack)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (!canDraw(stack)) {
            warnEmpty(level, player);
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

// 事件路径上的兜底：不属于上面的方块时，右键仍可蓄力
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        startUse(level, player, context.getItemInHand(), context.getHand());
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 蓄力（拉弓姿势）

    @Override
    public int getUseDuration(ItemStack stack) {
        return CHARGE_TICKS;
    }

// 用拉弓的姿势来表达蓄力
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

// 蓄力途中不断滴水，作为视觉反馈
    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (level instanceof ServerLevel serverLevel && remainingUseDuration % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.DRIPPING_WATER,
                    living.getX(), living.getY() + 1.2, living.getZ(),
                    1, 0.15, 0.1, 0.15, 0.0);
        }
    }

// 拉满松开：浇灌准星所在格及周围的农作物
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (timeLeft > 0) {
            return; // 没拉满就不浇
        }
        if (level instanceof ServerLevel serverLevel) {
            water(serverLevel, player, stack);
        }
    }

// 打水 / 浇水

// 把水打满，并播出装水音效（仅服务端调用）
    private static void fill(Level level, Player player, ItemStack stack) {
        stack.setDamageValue(0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

// 视线里（含流体）能打到水就打满，返回是否打到了水
    private static boolean fillFromSight(Level level, Player player, ItemStack stack) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos pos = hit.getBlockPos();
        if (!isWaterAt(level, pos, hit.getDirection())) {
            return false;
        }
        if (!level.isClientSide) {
            fill(level, player, stack);
        }
        return true;
    }

// 浇灌准星所指方块 3×3 内的农作物，每株有概率附加骨粉效果（催熟），成功一次扣 1 点耐久
    private static void water(ServerLevel level, Player player, ItemStack stack) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos center = hit.getBlockPos();
        level.playSound(null, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);

        RandomSource random = level.getRandom();
        for (BlockPos pos : area(center)) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BonemealableBlock crop) || !crop.isValidBonemealTarget(level, pos, state, false)) {
                continue;
            }
// 先来点水花，表示这一格被浇到了
            level.sendParticles(ParticleTypes.SPLASH,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    4, 0.3, 0.3, 0.3, 0.0);
// 有概率附加骨粉效果；水量见底（只剩 1）就不再消耗
            if (canDraw(stack) && random.nextFloat() < BONEMEAL_CHANCE
                    && crop.isBonemealSuccess(level, random, pos, state)) {
                crop.performBonemeal(level, random, pos, state);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        8, 0.3, 0.3, 0.3, 0.0);
                stack.setDamageValue(stack.getDamageValue() + 1);
            }
        }
    }

// 工具方法

// 开始蓄力；水量只剩 1 时拉不动，改为提示去打水
    private static void startUse(Level level, Player player, ItemStack stack, InteractionHand hand) {
        if (!canDraw(stack)) {
            warnEmpty(level, player);
            return;
        }
        player.startUsingItem(hand);
    }

// 水量是否还够拉（耐久 > 1）
    private static boolean canDraw(ItemStack stack) {
        return water(stack) > 1;
    }

// 当前水量 = 最大耐久 − 已损耗耐久
    private static int water(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    private static void warnEmpty(Level level, Player player) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable(EMPTY_KEY), true);
        }
    }

// 目标格本身或其被点击的那一面相邻格是水
    private static boolean isWaterAt(Level level, BlockPos pos, Direction face) {
        return level.getFluidState(pos).is(FluidTags.WATER)
                || level.getFluidState(pos.relative(face)).is(FluidTags.WATER);
    }

// 以准星所指方块为中心的水平 3×3，并向上多扫一层，兼顾高一点的作物
    private static Iterable<BlockPos> area(BlockPos center) {
        return BlockPos.betweenClosed(
                center.offset(-RADIUS, 0, -RADIUS),
                center.offset(RADIUS, 1, RADIUS));
    }
}