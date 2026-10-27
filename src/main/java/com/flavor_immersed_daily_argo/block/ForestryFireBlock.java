package com.flavor_immersed_daily_argo.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

// 林业火灾：林业网点闹火灾时冒出的火。
//
// 和原版火不同，它既不蔓延也不会自然熄灭，更不会烧毁任何方块：
// 不排随机刻（因此不 tick、不扩散、不熄灭），#canBurn 恒为 false。
// 玩家只能拿灭火器把它扑灭（见 ForestryNetworkManager#extinguish）。
public class ForestryFireBlock extends BaseFireBlock {

    public static final MapCodec<ForestryFireBlock> CODEC = simpleCodec(ForestryFireBlock::new);

    public ForestryFireBlock(BlockBehaviour.Properties properties) {
        super(properties, 1.0F);
    }

    @Override
    protected MapCodec<? extends BaseFireBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return false;
    }
}
