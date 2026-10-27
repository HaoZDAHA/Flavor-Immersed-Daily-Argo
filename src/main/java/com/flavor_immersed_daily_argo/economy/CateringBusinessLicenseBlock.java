package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.entity.Customer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

// 餐饮营业执照。
//
// 挂在墙上的一块牌匾，只能贴在结实方块的侧面。空手右键切换开业 / 打烊：开业立刻在附近招一个
// 顾客出来，之后每个随机刻再补一个，直到店里同时有 Customer#MAX_CUSTOMERS 个顾客为止；
// 打烊或牌子被拆掉时，属于这块牌子的顾客一并清掉。
//
// 顾客只在玩家用「座垫标记」标过的方块上落座（见 SeatManager），牌子这边不关心座位的
// 有无，只负责往店里送人。牌子没有方块实体：开业状态本身就是方块状态 #OPEN，随存档一起
// 保存；顾客的维持全靠 #randomTick。
public class CateringBusinessLicenseBlock extends Block {

    public static final MapCodec<CateringBusinessLicenseBlock> CODEC = simpleCodec(CateringBusinessLicenseBlock::new);

// 水平朝向：挂牌后正面朝玩家
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

// 营业状态：false = 打烊，true = 开业（开业后会在附近维持最多 6 个顾客）
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

// 挂牌那一层的薄板：离地 3px 起、高 10px、厚 1px，贴着方块一侧（与展示架同一套朝向约定）
    private static final VoxelShape SHAPE_NORTH = Block.box(1.0D, 3.0D, 15.0D, 15.0D, 13.0D, 16.0D);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0D, 3.0D, 1.0D, 1.0D, 13.0D, 15.0D);
    private static final VoxelShape SHAPE_SOUTH = Block.box(1.0D, 3.0D, 0.0D, 15.0D, 13.0D, 1.0D);
    private static final VoxelShape SHAPE_WEST = Block.box(15.0D, 3.0D, 1.0D, 16.0D, 13.0D, 15.0D);

// 统计 / 清理顾客时的搜索半径
    private static final double CUSTOMER_RANGE = 16.0D;

// 派顾客的间隔（tick）。开业后由预定刻按这个节奏补人：约 3 秒一个，六位顾客不到 20 秒就坐满，
// 客人走了腾出空位也还会继续补——不能指望随机刻，随机刻平均要一分钟才轮到这块牌子一次。
    private static final int SPAWN_INTERVAL = 60;

    public CateringBusinessLicenseBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

// 正面朝向玩家
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(OPEN, false);
    }

// 只能贴在背面那格结实方块的侧面上
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos wall = pos.relative(facing.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
    }

// 背后那块墙没了就自己掉下来
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.getValue(FACING).getOpposite() && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

// 指示箱取牌子本身，避免整格指示箱把薄板挡在中间点时摸不着
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeOf(state);
    }

// 碰撞箱：那一块薄板，随朝向转
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeOf(state);
    }

    private static VoxelShape shapeOf(BlockState state) {
        return switch (state.getValue(FACING)) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

// 开业 / 打烊

// 手里拿着东西时一律放行给物品自己用（放方块、贴东西都不该顺手把店开了），
// 只有空手才落到下面的开业 / 打烊逻辑上。
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        return stack.isEmpty()
                ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

// 空手右键：开业就立刻招一个顾客，打烊就把店里的顾客都赶走
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            boolean open = !state.getValue(OPEN);
            serverLevel.setBlock(pos, state.setValue(OPEN, open), 3);
            if (open) {
                serverLevel.playSound(null, pos, SoundEvents.VILLAGER_YES, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.displayClientMessage(
                        Component.translatable("message.flavor_immersed_daily_argo.license.open"), true);
// 先立刻来一位，再把「持续派单」的预定刻挂上
                spawnCustomer(serverLevel, pos);
                serverLevel.scheduleTick(pos, this, SPAWN_INTERVAL);
            } else {
                serverLevel.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.displayClientMessage(
                        Component.translatable("message.flavor_immersed_daily_argo.license.closed"), true);
                discardCustomers(serverLevel, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 开业期间持续派单：每 #SPAWN_INTERVAL tick 检查一次，店里没坐满就再招一位，
// 然后接着把下一次预定刻挂上——只要还开着门这个循环就不停，客人 5 分钟到期走掉后也会补上新人。
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(OPEN)) {
            return;
        }
        if (countCustomers(level, pos) < Customer.MAX_CUSTOMERS) {
            spawnCustomer(level, pos);
        }
        level.scheduleTick(pos, this, SPAWN_INTERVAL);
    }

// 随机刻只当保险：正常由 #tick 自我续期，万一预定刻丢了（读档异常等），
// 这里把派单循环重新挂上——已经在跑就不重复挂，免得变成双倍速度。
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(OPEN) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, SPAWN_INTERVAL);
        }
    }

// 牌子被拆掉时把属于这家店的顾客一并清掉
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
            discardCustomers(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

// 顾客

// 归属这块牌子、且还在附近的顾客
    private static List<Customer> findCustomers(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(Customer.class, new AABB(pos).inflate(CUSTOMER_RANGE),
                customer -> pos.equals(customer.getHome()));
    }

    private static int countCustomers(ServerLevel level, BlockPos pos) {
        return findCustomers(level, pos).size();
    }

// 把属于这块牌子的顾客全都清掉（打烊 / 拆牌子时用）
    private static void discardCustomers(ServerLevel level, BlockPos pos) {
        for (Customer customer : findCustomers(level, pos)) {
            customer.discard();
        }
    }

// 从今天的菜单里随手点一样当订单，在牌子附近挑个落脚点放一个顾客出来
    private static void spawnCustomer(ServerLevel level, BlockPos pos) {
        List<Item> foods = DeliveryFoodPool.dailyFoods(level.getDayTime() / 24000L);
        if (foods.isEmpty()) {
            return;
        }
        BlockPos spot = findSpawnSpot(level, pos);
        if (spot == null) {
            return;
        }
        Customer customer = ModEntities.CUSTOMER.get().create(level);
        if (customer == null) {
            return;
        }
        ItemStack order = new ItemStack(foods.get(level.random.nextInt(foods.size())));
        order.setCount(1 + level.random.nextInt(3));
        long reward = (long) order.getCount() * (3 + level.random.nextInt(4));
        customer.setHome(pos);
        customer.setOrder(order, reward);
        customer.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(customer);
    }

// 在牌子周围找一格能站人的地面（可以比牌子低几格，牌匾挂在高处也能落到地面）；找不到返回 null
    private static BlockPos findSpawnSpot(ServerLevel level, BlockPos pos) {
        for (int attempt = 0; attempt < 32; attempt++) {
            int dx = level.random.nextInt(9) - 4;
            int dz = level.random.nextInt(9) - 4;
            int dy = -level.random.nextInt(4);
            BlockPos candidate = pos.offset(dx, dy, dz);
            if (canStand(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

// 该格上方两格空着、脚下是结实地面就能站人
    private static boolean canStand(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
            return false;
        }
        if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
            return false;
        }
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }
}
