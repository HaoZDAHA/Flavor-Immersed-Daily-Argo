package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.recipe.RiverSiltFarmingRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// 河道淤泥
//
// 整格高的水底淤泥。平时是干燥的纹理（texture=0）；铺在水底、拿着水产饲料右键之后
// 变成含水纹理（texture=1），这时才能播种养殖。
//
// 养殖内容全部由数据包里的 river_silt_farming 配方决定：手持配方登记的苗种右键，
// 淤泥记下苗种与产物并开始生长；成熟后右键（空手也行）收获，淤泥退回干燥状态。
// 苗种与成熟后的三份产物都由方块实体渲染器平铺在淤泥表面。
public class RiverSiltBlock extends BaseEntityBlock {

    public static final MapCodec<RiverSiltBlock> CODEC = simpleCodec(RiverSiltBlock::new);

// 纹理状态：0 干燥，1 含水（被水产饲料养过）
    public static final IntegerProperty TEXTURE = IntegerProperty.create("texture", 0, 1);

    public RiverSiltBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TEXTURE, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TEXTURE);
    }

// 方块本体照常画模型，苗种由方块实体渲染器叠加
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RiverSiltBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.RIVER_SILT_ENTITY.get(),
                        RiverSiltBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof RiverSiltBlockEntity silt)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
// 干燥的淤泥：只认水产饲料，而且必须泡在水下
        if (state.getValue(TEXTURE) == 0) {
            if (!stack.is(ModItems.FEED_AQUATIC.get()) || !isUnderWater(level, pos)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(TEXTURE, 1), Block.UPDATE_ALL);
                consume(player, hand);
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8F, 1.1F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
// 含水淤泥：已经养着东西就等成熟收获，空着就按手里的苗种找配方
        if (silt.hasCrop()) {
// 还没熟时可以用水产饲料催熟，每次推进四分之一的生长时间
            if (!silt.isMature() && stack.is(ModItems.FEED_AQUATIC.get())) {
                if (!level.isClientSide) {
                    silt.fertilize(Math.max(1, silt.getGrowTime() / 4));
                    consume(player, hand);
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                                8, 0.3D, 0.1D, 0.3D, 0.0D);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            return tryHarvest(silt, state, level, pos, player)
                    ? ItemInteractionResult.sidedSuccess(level.isClientSide)
                    : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Optional<RiverSiltFarmingRecipe> recipe = silt.findRecipe(stack);
        if (recipe.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            RiverSiltFarmingRecipe found = recipe.get();
            silt.plant(new ItemStack(stack.getItem()), found.getResult(), found.getTime());
            consume(player, hand);
            level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

// 空手右键：成熟时收货
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof RiverSiltBlockEntity silt) || !silt.hasCrop()) {
            return InteractionResult.PASS;
        }
        return tryHarvest(silt, state, level, pos, player)
                ? InteractionResult.sidedSuccess(level.isClientSide)
                : InteractionResult.PASS;
    }

// 成熟就收获并把淤泥退回干燥状态，返回是否真的收到了东西
    private static boolean tryHarvest(RiverSiltBlockEntity silt, BlockState state,
                                      Level level, BlockPos pos, Player player) {
        if (!silt.isMature()) {
            return false;
        }
        if (!level.isClientSide) {
            silt.harvest(player);
            level.setBlock(pos, state.setValue(TEXTURE, 0), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.1F);
        }
        return true;
    }

// 淤泥正上方要有水，才算「铺在水下」
    private static boolean isUnderWater(Level level, BlockPos pos) {
        return level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

// 扣掉手里的一份；创造模式不扣
    private static void consume(Player player, InteractionHand hand) {
        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
        }
    }
}
