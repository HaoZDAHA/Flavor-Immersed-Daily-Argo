package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 油焖大虾的方块实体
//
// 本身不存任何数据，只是为了让 BraisedPrawnsRenderer 挂到该方块上
// （模型带多段嵌套旋转，必须由方块实体渲染器绘制。吃到第几口记在方块状态的 stage 里）。
public class BraisedPrawnsBlockEntity extends BlockEntity {

    public BraisedPrawnsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRAISED_PRAWNS_ENTITY.get(), pos, state);
    }
}
