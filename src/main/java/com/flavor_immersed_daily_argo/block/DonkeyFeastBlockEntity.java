package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 全驴宴的方块实体
//
// 本身不存任何数据，只是为了让 DonkeyFeastRenderer 挂到该方块上
// （圆桌模型 3 格长、带大量嵌套旋转，必须由方块实体渲染器绘制。
// 吃了几口记在方块状态的 stage 里）。
public class DonkeyFeastBlockEntity extends BlockEntity {

    public DonkeyFeastBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DONKEY_FEAST_ENTITY.get(), pos, state);
    }
}
