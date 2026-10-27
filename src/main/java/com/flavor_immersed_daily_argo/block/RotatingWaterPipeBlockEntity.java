package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 旋转水管方块实体
//
// 本身没有数据要存，只为挂上 RotatingWaterPipeRenderer——旋转臂要播放 Blockbench
// 导出的 round 动画，超出单方块模型能烘焙的范围，必须由方块实体渲染器逐帧绘制。
//
// 服务端逻辑（管网接入、隔一格探测、灌溉）都在 RotatingWaterPipeBlock 的
// tick / randomTick 里，这里不注册 ticker。
public class RotatingWaterPipeBlockEntity extends BlockEntity {

    public RotatingWaterPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROTATING_WATER_PIPE_ENTITY.get(), pos, state);
    }
}