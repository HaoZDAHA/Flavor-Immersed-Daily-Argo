package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModFluidTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidType;

// 灌溉水车的“水”。
//
// 它没有对应的水方块，也永远不会流动、不会 tick，只作为水车三格 getFluidState
// 返回的流体状态存在。之所以不用原版水，是因为原版水一定会被客户端画出来（水柱），
// 而自定义流体可以在客户端注册 IClientFluidTypeExtensions.renderFluid 里直接跳过绘制。
//
// 功能上它跟水源完全一致：isSource 为 true、com.flavor_immersed_daily_argo.all.ModFluidTypes
// 里 canHydrate(true) 让耕地/甘蔗/珊瑚等判定把这三格当成水源，
// 数据包里又把它加进了 minecraft:water 流体标签，其它模组查 FluidTags.WATER
// 也会认。取水靠 WaterwheelBlock 自己实现的 BucketPickup，与该流体无关。
public class WaterwheelWaterFluid extends Fluid {

// 液面高度，与水源一致（8/9 格）：只影响流体形状与射线，看不见任何贴图
    private static final float HEIGHT = 8.0F / 9.0F;

    private static final VoxelShape SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, HEIGHT, 1.0D);

    @Override
    public FluidType getFluidType() {
        return ModFluidTypes.WATERWHEEL_WATER_TYPE.get();
    }

// 用桶能装起来的是原版水桶
    @Override
    public Item getBucket() {
        return Items.WATER_BUCKET;
    }

// 谁也别想替换掉这格水
    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

// 静止不动
    @Override
    protected Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
        return Vec3.ZERO;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
        return HEIGHT;
    }

    @Override
    public float getOwnHeight(FluidState state) {
        return HEIGHT;
    }

// 挖掉水车时这三格应该变空气，而不是留下真正的水
    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSource(FluidState state) {
        return true;
    }

    @Override
    public int getAmount(FluidState state) {
        return 8;
    }

// 流体形状给到 8/9 格高：空桶对准任意一格的射线才能命中
    @Override
    public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }
}
