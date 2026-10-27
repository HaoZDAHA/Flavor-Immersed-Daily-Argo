package com.flavor_immersed_daily_argo.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collections;

// 肥沃耕地
//
// 本体就是原版耕地（FarmBlock），只多一条：随机刻时顺手把正上方的作物催一个生长阶段。
// 湿度衰减与下雨回满、上方被挡就变回泥土、被踩踏成泥土（fallOn）、放置时贴不上去就给泥土 ——
// 这些原版耕地行为全部原样继承，一行都不重写，避免以后版本更新时行为跑偏。
//
// 开销：多出来的成本只有一次 getBlockState(pos.above())，而且只在原版本来就排好的
// 那一次随机刻里做（默认 randomTickSpeed=3 时一格平均约 68 秒才轮到一次），铺满一整片田也几乎
// 不花钱；作物已成熟时直接返回，不做任何写入。
//
// 为什么能种上去：CropBlock 与瓜藤（StemBlock）判断能否种植时用的是
// state.getBlock() instanceof FarmBlock（NeoForge 打的补丁），本方块继承自 FarmBlock，
// 天然就通过 —— 原版作物和 flavor_immersed_daily 的作物都能直接种在它上面，不需要额外挂任何标签。
// （这也是这里刻意不把自己塞进 #minecraft:dirt 的原因：那会把本方块变成幽匿可替换方块、
// 被洞穴雕刻器当泥土处理，原版耕地并不这样，属于没必要的副作用。）
//
// 挖掘手感同原版耕地：挂进 #minecraft:mineable/shovel（见
// data/minecraft/tags/block/mineable/shovel.json），铲子挖得快、徒手慢。
public class FertileFarmlandBlock extends FarmBlock {

// FarmBlock 把 codec() 的返回类型收窄成了 MapCodec<FarmBlock>，而 MapCodec 是不变的，
// 所以这里必须用同样的 MapCodec<FarmBlock>（工厂仍是本类，反序列化出来的才是肥沃耕地）
    public static final MapCodec<FarmBlock> CODEC = simpleCodec(FertileFarmlandBlock::new);

    public FertileFarmlandBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FarmBlock> codec() {
        return CODEC;
    }

// 先把原版耕地那一套跑完（湿度衰减 / 下雨回满），再给上方的作物催一阶
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        boostCropAbove(level, pos);
    }

// 把正上方的作物推进恰好一个生长阶段；到顶、或上方压根不是作物，就什么都不做。
//
// 兼容性判据与镰刀（SickleItem）保持一致，不写死任何模组的类：
// 1. 原版及继承 CropBlock 的作物（含 flavor_immersed_daily 的 FIDCropBlock），
// 直接走 isMaxAge / getAge / getStateForAge 这套公开接口；
// 2. 其余没有统一基类的植物（瓜藤等 BushBlock、带 waterlogged 的水生作物），
// 以「拥有名为 age 的整型生长属性」为通用判据，用 setValue 推进，其余属性原样保留。
    private static void boostCropAbove(ServerLevel level, BlockPos pos) {
        BlockPos cropPos = pos.above();
        BlockState cropState = level.getBlockState(cropPos);
        Block crop = cropState.getBlock();

        if (crop instanceof CropBlock cropBlock) {
            if (!cropBlock.isMaxAge(cropState)) {
                BlockState next = cropBlock.getStateForAge(cropBlock.getAge(cropState) + 1);
                level.setBlock(cropPos, next, Block.UPDATE_CLIENTS);
            }
            return;
        }

        IntegerProperty age = ageProperty(cropState);
        if (age == null) {
            return;
        }
        if (!(crop instanceof BushBlock) && !cropState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return;
        }
        int value = cropState.getValue(age);
        if (value < maxOf(age)) {
            level.setBlock(cropPos, cropState.setValue(age, value + 1), Block.UPDATE_CLIENTS);
        }
    }

// 取方块状态里名为 age 的整型生长属性（原版与模组作物的通用命名），没有则返回 null
    private static IntegerProperty ageProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integer && property.getName().equals("age")) {
                return integer;
            }
        }
        return null;
    }

// 该生长属性的最大取值，即作物的成熟阶段
    private static int maxOf(IntegerProperty property) {
        return Collections.max(property.getPossibleValues());
    }
}