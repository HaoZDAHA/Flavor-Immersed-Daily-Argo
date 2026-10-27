package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.fishing.FishingHandbookData;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// 捕捞渔网
//
// 放进水里当渔网用：只要这一格正上方是水、且没有被另一张渔网压住，随机刻就会在这格上方
// 的水里刷出一条水产活体。刷哪条由钓鱼手册数据（data/<命名空间>/fishing_handbook/*.json）决定——
// 海洋群系刷海产、其余群系刷淡水，联动的三个水产模组写进手册的鱼也会一并参与抽取。
// 上方是空气（干放或露出水面）时不生效。
//
// 实现 SimpleWaterloggedBlock：放进水里会照样保住那格水源，干放就是干的。
//
// 网墙按相邻渔网动态隐藏（与鱼护同款连接逻辑）——相邻格子也是渔网时，那一侧不立网墙，
// 多张网连成一片。连接状态用 north/east/south/west 四个布尔属性表达
// （true 表示该侧有相邻渔网），blockstate 用精确组合各挑一个模型（含旋转）。
// 压在另一张渔网上时，这四向连接还会跟着下面那张网走，见 #refresh。
//
// 与鱼护的差别：一是碰撞箱——这里有整格的体积碰撞箱，玩家可以自由穿过、其他生物被挡住；
// 二是网墙会顺着下面那张网往上续——墙上的网只在外侧立网墙，内侧与下方连着，所以往上摞高
// 做墙壁只要摆外圈（不用把整层填满），内侧也不会多出一层网面。
//
// 底网（#BOTTOM）：最底层，以及下方那张网只是一层四面相连的纯底面时，这一层照旧铺底；
// 下方那张网是网墙（侧面 state 或 侧+底 state）时，这一层跟着当墙、不再铺底。
public class FishingNetBlock extends Block implements SimpleWaterloggedBlock {

    public static final MapCodec<FishingNetBlock> CODEC = simpleCodec(FishingNetBlock::new);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
// 该侧是否有相邻渔网（有则隐藏这一面的网墙）
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
// 这一层是否带底网：下方没网（最底层）或下方只是一层不带侧墙的纯底面时带底，下方是网墙时跟着当墙
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

// 每个随机刻刷出水产的概率
    private static final float SPAWN_CHANCE = 0.3F;

// 完整体积：非玩家的生物一律挡在外面
    private static final VoxelShape BLOCK_SHAPE = Shapes.block();

    public FishingNetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, false)
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(BOTTOM, true));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, NORTH, EAST, SOUTH, WEST, BOTTOM);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
// 只是「能含水」：放在水里才跟着变水方块，干地放置就是干的
        return refresh(this.defaultBlockState()
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER), level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
// 含水的话得让水照常结算，不然水会一直挂在这一格不动
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return refresh(state, level, pos);
    }

// 按周围环境刷新：四向邻接 + 下方那张网的连接形态，决定哪几面立网墙、这一层带不带底网
    private static BlockState refresh(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        boolean onNet = below.getBlock() instanceof FishingNetBlock;
// 压在另一张渔网上时，网墙形态跟着下面那张网走：下面连着的那几面上面也连着（不立墙），
// 于是"外侧立墙、内侧连着"的墙形会一层层往上续，往上摞高只摆外圈就够了，内侧不会多出网面
        return state.setValue(NORTH, isNet(level, pos.north()) || (onNet && below.getValue(NORTH)))
                .setValue(EAST, isNet(level, pos.east()) || (onNet && below.getValue(EAST)))
                .setValue(SOUTH, isNet(level, pos.south()) || (onNet && below.getValue(SOUTH)))
                .setValue(WEST, isNet(level, pos.west()) || (onNet && below.getValue(WEST)))
                .setValue(BOTTOM, !onNet || !hasSideWall(below));
    }

// 这张网上有没有立着侧面网墙——只要有任意一面没和相邻渔网连上（侧面 state 或 侧+底 state）就算有。
//
// 下方那张网是网墙时，压在上面的这一层跟着当墙、不再铺底；下方没网（最底层）
// 或下方只是一层四面全相连的纯底面时，这一层照旧铺底网。
    private static boolean hasSideWall(BlockState state) {
        return !(state.getValue(NORTH) && state.getValue(EAST)
                && state.getValue(SOUTH) && state.getValue(WEST));
    }

// 含水时才对外是一个满格水源，干放时就是普通方块
    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    private static boolean isNet(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof FishingNetBlock;
    }

// 鼠标指示箱取整格，方便对准右键
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BLOCK_SHAPE;
    }

// 碰撞箱跟着实体走：玩家返回空形状（可以自由穿过），其他生物返回整格（被网挡住）。
// 这是「只有玩家能穿过」的唯一实现方式——原版方块碰撞对所有实体一视同仁。
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player) {
            return Shapes.empty();
        }
        return BLOCK_SHAPE;
    }

// 只要本格正上方是水（本格不必自己含水），且上方没被别的渔网占住，就在那格水里刷一条水产。
// 刷出的活体直接落在网面正上方的那一格。
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (!level.getFluidState(above).is(Fluids.WATER) || isNet(level, above)) {
            return;
        }
        if (random.nextFloat() >= SPAWN_CHANCE) {
            return;
        }
        FishingHandbookEntry entry = pickEntry(level, pos, random);
        if (entry == null) {
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entry.entityId).orElse(null);
        if (type == null) {
            return;
        }
        Entity entity = type.create(level);
        if (entity == null) {
            return;
        }
        entity.moveTo(above.getX() + 0.5D, above.getY() + 0.2D, above.getZ() + 0.5D,
                random.nextFloat() * 360.0F, 0.0F);
        bindFishVariant(entity, entry);
        level.addFreshEntity(entity);
    }

// 从钓鱼手册里抽一条「当前水情 + 有活体形态」的条目。
//
// 优先在群系也相符的条目里抽；群系把候选滤空时退回只按水域筛选的结果，避免完全刷不出东西。
    @Nullable
    private static FishingHandbookEntry pickEntry(ServerLevel level, BlockPos pos, RandomSource random) {
        ResourceLocation biome = level.getBiome(pos).getKey().location();
        boolean isOcean = isOceanPath(biome.getPath());
        List<FishingHandbookEntry> preferred = new ArrayList<>();
        List<FishingHandbookEntry> fallback = new ArrayList<>();
        for (FishingHandbookEntry entry : FishingHandbookData.getInstance().all()) {
            if (!entry.available() || !entry.hasEntity() || !entry.habitatMatches(isOcean)) {
                continue;
            }
            fallback.add(entry);
            if (entry.biomesMatch(biome)) {
                preferred.add(entry);
            }
        }
        List<FishingHandbookEntry> pool = preferred.isEmpty() ? fallback : preferred;
        return pool.isEmpty() ? null : pool.get(random.nextInt(pool.size()));
    }

// 部分模组把所有鱼共用一个实体，靠实体里携带的鱼物品来区分种类（如捕星者的 starcatcher:fish），
// 不写入鱼物品的话实体渲染为空白。这里尝试把鱼物品写进实体，实体没有该方法时保持原样。
    private static void bindFishVariant(Entity entity, FishingHandbookEntry entry) {
        if (entry.itemId == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(entry.itemId).orElse(null);
        if (item == null) {
            return;
        }
        if (entity instanceof AbstractFish fish) {
            fish.setFromBucket(false);
        }
        try {
            entity.getClass().getMethod("setFish", ItemStack.class).invoke(entity, item.getDefaultInstance());
        } catch (ReflectiveOperationException ignored) {
// 该实体不是靠物品区分种类的通用鱼，无需处理
        }
    }

// 判断生物群系是不是海洋（海水的判据），与钓竿里那套保持一致
    private static boolean isOceanPath(String path) {
        return path.equals("ocean") || path.equals("deep_ocean")
                || path.equals("cold_ocean") || path.equals("deep_cold_ocean")
                || path.equals("frozen_ocean") || path.equals("deep_frozen_ocean")
                || path.equals("lukewarm_ocean") || path.equals("deep_lukewarm_ocean")
                || path.equals("warm_ocean");
    }
}
