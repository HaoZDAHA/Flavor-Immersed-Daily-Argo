package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 铁锅炖大鹅的方块实体
//
// 本身不存任何数据，只是为了让 PotStewGooseRenderer 挂到该方块上
// （模型带多段嵌套旋转、体积也超过单个方块，必须由方块实体渲染器绘制。
// 吃到第几口记在方块状态的 stage 里）。
public class PotStewGooseBlockEntity extends BlockEntity {

    public PotStewGooseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POT_STEW_GOOSE_ENTITY.get(), pos, state);
    }
}
