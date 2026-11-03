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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 洒水壶：以「耐久」充当水量——最高 128，最低 1（永不损毁）。
//
// 右键水源即可把水量打满；对着农作物按住右键浇上 3 秒，准星所指那一格才能催熟一阶，
// 成功一次掉 1 点耐久。水量只剩 1 时浇不动，会提示去打水。
//
// 浇水进度没有借用原版的「使用物品」状态：那个状态在方块右键里手动 startUsingItem
// 起不来（服务端收不到开始使用的包，松手时 releaseUsing 不触发），之前长按完全没反应
// 就是这个原因。这里改用「按住右键时客户端每 4 tick 会补发一次方块右键事件」来计进度：
// 每次事件 +4 tick，中间断开超过 #CHARGE_TIMEOUT 就从头浇。
//
// 右键优先级：作物大多自带右键处理（收获 / 骨粉），且方块自身的右键会排在物品之前。
// 因此打水与浇水的入口放在 PlayerInteractEvent.RightClickBlock 里，在方块交互之前接管；
// 只在点到农作物 / 水源 / 耕地时才抢占，避免影响箱子等方块的正常右键。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public class WateringCanItem extends TooltipItem {

// 浇灌范围：以准星所指方块为中心的水平半径（0 → 只浇准星那一格）
    private static final int RADIUS = 0;
// 浇透一株要按住多久（tick）：3 秒
    private static final int CHARGE_TICKS = 60;
// 按住右键时，客户端大约每 4 tick 补发一次右键事件
    private static final int TICKS_PER_EVENT = 4;
// 两次事件隔这么久就当玩家松手了，重新计时
    private static final int CHARGE_TIMEOUT = 10;
// 水量耗尽时的提示 key
    private static final String EMPTY_KEY = "message.flavor_immersed_daily_argo.watering_can.empty";

// 每个玩家当前的浇水进度（只在服务端用，天然是主线程访问）
    private static final Map<UUID, Charge> CHARGES = new HashMap<>();

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
// 点到农作物 → 抢过来开始浇水（否则会被作物自带的右键收获 / 骨粉吃掉）；
// 点到水源 → 直接打满；
// 点到耕地 → 也允许在这一格上浇，方便对着土浇上面的作物。
//
// 点到其它方块（箱子等）一律放行，不影响正常交互。
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        Level level = event.getLevel();
        Player player = event.getEntity();

        if (!(stack.getItem() instanceof WateringCanItem)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

// 作物优先于水源判断：水生作物本身带 waterlogged，若先判水源会被误当成打水
        if (state.getBlock() instanceof BonemealableBlock) {
            event.setCanceled(true);
            charge(level, player, stack, pos);
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
            charge(level, player, stack, pos);
        }
    }

// 事件路径之外的兜底：同样按蓄水时长浇
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        charge(level, player, context.getItemInHand(), context.getClickedPos());
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 打水 / 浇水

// 把水打满，并播出装水音效（仅服务端调用）
    private static void fill(Level level, Player player, ItemStack stack) {
        stack.setDamageValue(0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

// 累计浇水时间：按住右键时一帧帧攒，攒满 #CHARGE_TICKS 才真正浇下去
    private static void charge(Level level, Player player, ItemStack stack, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        if (!canDraw(stack)) {
            warnEmpty(level, player);
            return;
        }
        long now = level.getGameTime();
        Charge charge = CHARGES.computeIfAbsent(player.getUUID(), id -> new Charge());
// 换了目标、或者中间松过手，就从这一下重新计时
        if (charge.pos == null || !charge.pos.equals(pos) || now - charge.lastEventTick > CHARGE_TIMEOUT) {
            charge.pos = pos.immutable();
            charge.ticks = 0;
        }
        charge.lastEventTick = now;
        charge.ticks += TICKS_PER_EVENT;

// 滴水：让玩家看得出正在浇
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DRIPPING_WATER,
                    pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D,
                    2, 0.25D, 0.1D, 0.25D, 0.0D);
        }

        if (charge.ticks >= CHARGE_TICKS) {
            charge.ticks = 0;
            ripen(level, player, stack, pos);
        }
    }

// 把准星所指这一格（耕地则连带上面那格）的农作物催熟一阶，成功一次扣 1 点耐久
    private static void ripen(Level level, Player player, ItemStack stack, BlockPos center) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!canDraw(stack)) {
            warnEmpty(serverLevel, player);
            return;
        }
        serverLevel.playSound(null, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);

        RandomSource random = serverLevel.getRandom();
        for (BlockPos pos : area(center)) {
            if (!canDraw(stack)) {
                break;
            }
            BlockState state = serverLevel.getBlockState(pos);
            if (!(state.getBlock() instanceof BonemealableBlock crop) || !crop.isValidBonemealTarget(serverLevel, pos, state)) {
                continue;
            }
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    4, 0.3, 0.3, 0.3, 0.0);
            crop.performBonemeal(serverLevel, random, pos, state);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    8, 0.3, 0.3, 0.3, 0.0);
            stack.setDamageValue(stack.getDamageValue() + 1);
        }
    }

// 工具方法

// 水量是否还够浇（耐久 > 1）
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

// 以准星所指方块为中心的水平范围（#RADIUS = 0 时只有这一格），并向上多扫一层，
// 兼顾高一点的作物、以及点在耕地上时上面那一株
    private static Iterable<BlockPos> area(BlockPos center) {
        return BlockPos.betweenClosed(
                center.offset(-RADIUS, 0, -RADIUS),
                center.offset(RADIUS, 1, RADIUS));
    }

// 单个玩家的浇水进度
    private static final class Charge {
        private BlockPos pos;
        private int ticks;
        private long lastEventTick;
    }
}
