package com.flavor_immersed_daily_argo.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 生剑鱼方块
//
// 剑鱼模型在 z 轴跨度约 -40~+39 像素（约 4.9 格），超出原版方块模型 JSON 允许的坐标范围
// （-16~32），无法导出为普通方块模型，所以它不参与区块网格烘焙
// （RenderShape#ENTITYBLOCK_ANIMATED），改由方块实体渲染器
// RawSwordfishRenderer 按原始尺寸绘制。
//
// 其余行为（徒手瞬间破坏、无碰撞、水平朝向 state、放置时朝向玩家）完全继承
// RawSeafoodBlock，与其它 8 种立体生鲜保持一致。
public class RawSwordfishBlock extends RawSeafoodBlock implements EntityBlock {

    public RawSwordfishBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RawSwordfishBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
