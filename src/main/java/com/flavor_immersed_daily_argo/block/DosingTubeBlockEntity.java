package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 加料阀方块实体：只存一件事——罐里那颗金坷垃还剩多久生效。
//
// 投一颗加 DosingTubeBlock#DURATION_PER_JINKELA 刻（5 分钟），可以叠加；
// 生效期间阀门方块状态上的 active 为 true，管网刷新时据此把整张网的
// fertilized 刷成 true，于是所有喷口浇出去的水都带上一点随机催熟。
public class DosingTubeBlockEntity extends BlockEntity {

    private static final String TICKS_TAG = "JinkelaTicks";

// 剩余生效时长（tick），0 表示罐里没有正在生效的金坷垃
    private int ticks;

    public DosingTubeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DOSING_TUBE_ENTITY.get(), pos, state);
    }

// 投一颗金坷垃进去：直接给生效时长续上 5 分钟
    public void insertJinkela() {
        this.ticks += DosingTubeBlock.DURATION_PER_JINKELA;
        this.setChanged();
        this.syncActive();
    }

    public static void tick(Level level, BlockPos pos, DosingTubeBlockEntity tube) {
        if (tube.ticks <= 0) {
            return;
        }
        tube.ticks--;
        if (tube.ticks == 0) {
// 效果结束：先撤掉管网上的催熟加成，再存一次
            tube.syncActive();
            tube.setChanged();
        } else if (tube.ticks % 20 == 0) {
            tube.setChanged();
        }
    }

// 把「罐里有没有正在生效的金坷垃」写回方块状态，并让管网重新算一遍催熟加成
    private void syncActive() {
        if (this.level == null) {
            return;
        }
        BlockState state = this.getBlockState();
        boolean active = this.ticks > 0;
        if (state.getValue(DosingTubeBlock.ACTIVE) != active) {
            this.level.setBlock(this.worldPosition,
                    state.setValue(DosingTubeBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
            WaterPipeBlock.refreshNetwork(this.level, this.worldPosition);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.ticks > 0) {
            tag.putInt(TICKS_TAG, this.ticks);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.ticks = tag.getInt(TICKS_TAG);
    }
}
