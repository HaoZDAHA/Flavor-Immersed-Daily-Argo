package com.flavor_immersed_daily_argo.block;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.forestry.ForestryNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 气象仪器方块实体
//
// 本身不存数据，只负责挂载渲染器，并每 20 tick 查询一次该区块网点是否有非常规天气事件，
// 据此更新 WeatherInstrumentBlock#HEAVY 方块状态（客户端渲染器读它选动画）。
public class WeatherInstrumentBlockEntity extends BlockEntity {

    public WeatherInstrumentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WEATHER_INSTRUMENT_ENTITY.get(), pos, state);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (level.isClientSide || level.getGameTime() % 20L != 0L) {
            return;
        }
        boolean heavy = ForestryNetworkManager.hasWeatherEvent(level, pos);
        if (state.getValue(WeatherInstrumentBlock.HEAVY) != heavy) {
            level.setBlock(pos, state.setValue(WeatherInstrumentBlock.HEAVY, heavy), Block.UPDATE_ALL);
        }
    }
}
