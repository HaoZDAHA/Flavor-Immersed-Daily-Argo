package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 生剑鱼方块实体
//
// 本身不存任何数据，只是为了让 RawSwordfishRenderer 挂到该方块上
// （剑鱼模型远大于单个方块，必须由方块实体渲染器绘制）。
public class RawSwordfishBlockEntity extends BlockEntity {

    public RawSwordfishBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RAW_SWORDFISH_ENTITY.get(), pos, state);
    }
}
