package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

// 灌溉水车方块实体
//
// 水轮本身没有数据要存，它只负责两件事：
//
// 服务端：水车还没通水时，每秒看一次脚下那格是不是水源；是就永久通水
// （通水状态决定本格对外是不是一个水源，见 WaterwheelBlock#getFluidState）；
// 客户端：下方或本格只要有水，就持续冒水花并偶尔来一声水响。
public class WaterwheelBlockEntity extends BlockEntity {

// 多久瞄一眼脚下水源（20 tick = 1 秒）；只是等水车通水用，通水后就不再看了
    private static final int CHECK_INTERVAL = 20;

    public WaterwheelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WATERWHEEL_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WaterwheelBlockEntity waterwheel) {
        if (level.isClientSide) {
            clientTick(level, pos);
        } else if (level.getGameTime() % CHECK_INTERVAL == 0L) {
            refreshWorking(level, pos, state);
        }
    }

// 脚下是水源就“通水”，通水之后就不再回头看外面那格水还在不在。
//
// 水车抽上来的是一股活水：通水后这三格的水就是无限的——别人拿桶来打、或者把脚下
// 那格水源整个抽走，都不会让水车停转，更不会让水车跟着消失。
    private static void refreshWorking(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(WaterwheelBlock.WORKING) || !isWaterSource(level.getFluidState(pos.below()))) {
            return;
        }
// 三格一起通水：上两格的流体状态跟着 bottom 走，桶对准任意一格都能打水
        syncPart(level, pos, true);
        syncPart(level, pos.above(), true);
        syncPart(level, pos.above(2), true);
    }

    private static void syncPart(Level level, BlockPos pos, boolean working) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof WaterwheelBlock && state.getValue(WaterwheelBlock.WORKING) != working) {
            level.setBlock(pos, state.setValue(WaterwheelBlock.WORKING, working), Block.UPDATE_ALL);
        }
    }

// 静水（水源）而不是流动的薄水层：水车得架在真正的水源上才转得动
    private static boolean isWaterSource(FluidState fluid) {
        return fluid.is(FluidTags.WATER) && fluid.isSource();
    }

// 客户端：脚下或本格有水就冒水花，偶尔来一声水响
    private static void clientTick(Level level, BlockPos pos) {
        if (!WaterwheelBlock.hasWater(level, pos)) {
            return;
        }
        if (level.random.nextFloat() < 0.4F) {
            double x = pos.getX() + 0.15D + level.random.nextDouble() * 0.7D;
            double z = pos.getZ() + 0.15D + level.random.nextDouble() * 0.7D;
            level.addParticle(ParticleTypes.SPLASH, x, surfaceY(level, pos), z, 0.0D, 0.12D, 0.0D);
        }
        if (level.random.nextInt(80) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS,
                    0.5F, 0.8F + level.random.nextFloat() * 0.4F, false);
        }
    }

// 水花溅起的高度：本格有水就按本格水面算，否则按下方那格的水面算
    private static double surfaceY(Level level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER) ? pos.getY() + 0.88D : pos.getY() + 0.05D;
    }
}
