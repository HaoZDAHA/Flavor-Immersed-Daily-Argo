package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.economy.product.EconomicNews;
import com.flavor_immersed_daily_argo.economy.product.EconomicProductNetwork;
import com.flavor_immersed_daily_argo.economy.product.WeightPriceData;
import com.flavor_immersed_daily_argo.economy.product.WeightPriceNetwork;
import com.flavor_immersed_daily_argo.economy.shop.ShopManager;
import com.flavor_immersed_daily_argo.economy.shop.ShopNetwork;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// 商业电脑（shopping_computer）
//
// 一台家电方块，同时提供新闻 / 网购 / 开店三条业务线。有水平朝向，摆放后屏幕朝玩家；
// 粗碰撞箱随朝向旋转，与 blockstate 的旋转严格一致，所以永远贴合当前朝向的模型。
//
// 没有方块实体：模型是普通方块模型（models/block/shopping_computer.json），走后 cutout 渲染。
// 空手或手持任意物品右键都会打开市场界面（不用持卡）。
public class ShoppingComputerBlock extends Block {

    public static final MapCodec<ShoppingComputerBlock> CODEC = simpleCodec(ShoppingComputerBlock::new);

// 水平朝向：屏幕朝玩家，模型随之旋转
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 桌面 / 底座盒（所有朝向相同）：底面 2 像素厚的台面
    private static final VoxelShape BASE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);

// 屏幕盒：模型空间屏幕在 −x（west）侧，绕方块中心旋转后取外接盒；
// 与 blockstate 的 y 旋转（north→90 / east→180 / south→270 / west→0）出自同一套映射
    private static final VoxelShape NORTH = Shapes.or(BASE, Block.box(0.0, 2.0, 11.0, 16.0, 16.0, 13.0));
    private static final VoxelShape EAST = Shapes.or(BASE, Block.box(3.0, 2.0, 0.0, 5.0, 16.0, 16.0));
    private static final VoxelShape SOUTH = Shapes.or(BASE, Block.box(0.0, 2.0, 3.0, 16.0, 16.0, 5.0));
    private static final VoxelShape WEST = Shapes.or(BASE, Block.box(11.0, 2.0, 0.0, 13.0, 16.0, 16.0));

    public ShoppingComputerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

// 屏幕朝向玩家
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

// 按朝向返回对应的粗碰撞箱
    private static VoxelShape shapeFor(BlockState state) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

// 手持任意物品右键：同样打开市场界面（不禁卡）
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            open(serverPlayer);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

// 空手右键：打开市场界面
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            open(serverPlayer);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 打开市场界面，并把全量店铺列表、今日经济产物新闻与按重量收购的价目随登录包下发，客户端据此填充界面
    private static void open(ServerPlayer player) {
        long day = player.serverLevel().getDayTime() / 24000L;
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new MarketMenu(id, inv),
                Component.translatable("gui.flavor_immersed_daily_argo.market")
        ), buf -> {
            ShopNetwork.writeShops(buf, ShopManager.get(player.server).allShops(player.server));
            EconomicProductNetwork.writeNews(buf, EconomicNews.today(day));
            WeightPriceNetwork.writeAnimals(buf, WeightPriceData.animals(day));
            WeightPriceNetwork.writeFish(buf, WeightPriceData.fish(day));
        });
    }
}
