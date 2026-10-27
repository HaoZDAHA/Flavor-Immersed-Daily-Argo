package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

// 竿架方块实体：把南山钓竿架在上面替玩家守竿。
//
// 用法是「先抛竿，再把竿子架上去」：玩家正常抛出鱼钩后手持钓竿右键竿架，竿架接过这一竿
// （落点一并记下），之后只要附近有玩家，竿架就自己等鱼咬钩；咬钩时提醒最近的玩家，
// 玩家右键即可取回竿子，竿架会在原落点把鱼钩放回去并直接进入遛鱼阶段（见 #takeRod）。
// 一直没人来管的话，竿子会自动掉出来，免得一根空架子常年占着世界资源。
public class RodPodBlockEntity extends BlockEntity {

    private static final String ROD_TAG = "Rod";
// 架竿时那一竿抛出的位置，取竿时在原处把鱼钩放回去
    private static final String CAST_TAG = "CastPos";

// 提醒半径：这个范围内的玩家才会被叫来收竿
    public static final double REMIND_RADIUS = 16.0D;
// 咬钩等待时间（tick）：10~30 秒
    private static final int MIN_BITE_TICKS = 10 * 20;
    private static final int MAX_BITE_TICKS = 30 * 20;
// 连续没人在附近多久就把竿子掉出来（tick）：5 分钟
    private static final int AUTO_DROP_TICKS = 5 * 60 * 20;
// 最近玩家多久重扫一次（tick）：咬钩提醒和自动掉竿都不需要逐 tick 的精度
    private static final int PLAYER_RESCAN_TICKS = 20;

    private ItemStack rod = ItemStack.EMPTY;
// 架竿时那一竿的落点；没有记录时取竿会用竿架正前方兜底
    private double castX;
    private double castY;
    private double castZ;
    private boolean hasCastPos;
// 距离咬钩还剩多少 tick；-1 表示还没开始计时
    private int biteTicks = -1;
// 是否已经咬钩（在等玩家来取竿）
    private boolean biting;
// 连续多少 tick 附近没有玩家
    private int idleTicks;
// 缓存下来的「最近玩家」，两次重扫之间直接复用，省掉每 tick 遍历玩家列表
    @Nullable
    private UUID cachedNearestId;
    private int playerRescan;

    public RodPodBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROD_POD_ENTITY.get(), pos, state);
    }

// 架上那根竿子；空架返回空栈
    public ItemStack getRod() {
        return this.rod;
    }

    public boolean hasRod() {
        return !this.rod.isEmpty();
    }

// 是否正有鱼咬钩（客户端渲染 / 提示用）
    public boolean isBiting() {
        return this.biting;
    }

// 是否记着这一竿的落点（客户端据此从竿梢画鱼线）
    public boolean hasCastPos() {
        return this.hasCastPos;
    }

// 这一竿的落点（浮漂停靠的位置），只有 #hasCastPos() 为 true 时有意义
    public Vec3 getCastPos() {
        return new Vec3(this.castX, this.castY, this.castZ);
    }

// 把竿子架上竿架；已经有竿子了返回 false。
//
// 竿架是接过玩家手上正在守的那一竿，所以落点也一并记下来——取竿时要在同一个位置
// 把鱼钩放回水里，而不是挪到竿架跟前。
    public boolean placeRod(ItemStack stack, double castX, double castY, double castZ) {
        if (this.hasRod() || stack.isEmpty()) {
            return false;
        }
        this.rod = stack.copyWithCount(1);
        this.castX = castX;
        this.castY = castY;
        this.castZ = castZ;
        this.hasCastPos = true;
        reset();
        this.setChanged();
        return true;
    }

// 取下竿子：竿子交到玩家手上，若正好有鱼咬钩就顺手把这一竿开成遛鱼。
//
// 先给竿子再开钓——遛鱼阶段的参数是从玩家手上的竿子里读的，顺序反了就只剩默认值。
    public void takeRod(Player player) {
        if (!this.hasRod()) {
            return;
        }
        ItemStack taken = this.rod;
        boolean wasBiting = this.biting;
        this.rod = ItemStack.EMPTY;
        this.reset();

        if (player.level().isClientSide) {
            this.setChanged();
            return;
        }
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, taken);
        } else if (!player.getInventory().add(taken)) {
            player.drop(taken, false);
        }
        if (wasBiting) {
            startFight(player);
        } else {
// 没咬钩就不是收竿：在落点把这一竿放回水里，玩家拿回竿子还能接着钓
            spawnHook(player);
        }
        this.setChanged();
    }

// 拆掉竿架时把竿子掉出来
    public void dropRod(Level level) {
        if (this.hasRod()) {
            Block.popResource(level, this.worldPosition, this.rod);
            this.rod = ItemStack.EMPTY;
        }
        this.hasCastPos = false;
        this.reset();
        this.setChanged();
    }

// 守竿

    public static void serverTick(Level level, BlockPos pos, BlockState state, RodPodBlockEntity pod) {
        pod.tick(level, pos);
    }

    private void tick(Level level, BlockPos pos) {
        if (!this.hasRod()) {
            if (this.biting || this.biteTicks != -1 || this.idleTicks != 0) {
                reset();
            }
            return;
        }

        Player nearest = nearestPlayerCached(level, pos);
        if (nearest == null) {
// 没人托管：倒计时把竿子掉出来，别一直空耗
            this.idleTicks++;
            if (this.biting || this.biteTicks != -1) {
                reset();
            }
            if (this.idleTicks >= AUTO_DROP_TICKS) {
                dropRod(level);
            }
            return;
        }

        this.idleTicks = 0;
        if (this.biting) {
            return;
        }
        if (this.biteTicks < 0) {
            this.biteTicks = MIN_BITE_TICKS + level.random.nextInt(MAX_BITE_TICKS - MIN_BITE_TICKS + 1);
            return;
        }
        if (--this.biteTicks <= 0) {
            this.biting = true;
            this.biteTicks = -1;
            notifyBite(level, pos, nearest);
        }
    }

// 咬钩提醒：给最近的玩家打个招呼，旁边也来一声水花
    private void notifyBite(Level level, BlockPos pos, Player player) {
        level.playSound(null, pos, SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.BLOCKS, 1.0F, 0.8F);
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("msg.flavor_immersed_daily_argo.rod_pod_bite"), false);
    }

// 取最近的玩家，带 1 秒缓存：两次重扫之间只按 UUID 反查（一次哈希查找），
// 不再每 tick 遍历本维度的玩家列表。
    @Nullable
    private Player nearestPlayerCached(Level level, BlockPos pos) {
        if (this.playerRescan > 0) {
            this.playerRescan--;
            return this.cachedNearest(level, pos);
        }
        this.playerRescan = PLAYER_RESCAN_TICKS;
        Player scanned = nearestPlayer(level, pos);
        this.cachedNearestId = scanned == null ? null : scanned.getUUID();
        return scanned;
    }

// 缓存里的玩家还在线、还在这个维度附近就继续用
    @Nullable
    private Player cachedNearest(Level level, BlockPos pos) {
        if (this.cachedNearestId == null) {
            return null;
        }
        MinecraftServer server = level.getServer();
        if (server == null) {
            return null;
        }
        Player player = server.getPlayerList().getPlayer(this.cachedNearestId);
        if (player == null || player.level() != level || player.isSpectator()) {
            return null;
        }
        return player.distanceToSqr(Vec3.atCenterOf(pos)) <= REMIND_RADIUS * REMIND_RADIUS ? player : null;
    }

    @Nullable
    private static Player nearestPlayer(Level level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        double limit = REMIND_RADIUS * REMIND_RADIUS;
        Player best = null;
        double bestSqr = limit;
        List<? extends Player> players = level.players();
        for (Player player : players) {
            if (player.isSpectator()) {
                continue;
            }
            double distSqr = player.distanceToSqr(center);
            if (distSqr <= bestSqr) {
                bestSqr = distSqr;
                best = player;
            }
        }
        return best;
    }

// 把竿架上等到的这一竿交给玩家：在当初抛竿的落点重新放出鱼钩，主人设为该玩家，直接进入遛鱼阶段。
    private void startFight(Player player) {
        ModFishingHook hook = spawnHook(player);
        if (hook == null) {
            return;
        }
        if (!hook.startFightFromPod(player)) {
            hook.discard();
            player.fishing = null;
        }
    }

// 在落点放出一枚鱼钩，主人设为该玩家。
//
// 不是服务端就没有鱼钩可放，返回 null。
    @Nullable
    private ModFishingHook spawnHook(Player player) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return null;
        }
        Vec3 cast = this.castPosition();
        this.hasCastPos = false;
        ModFishingHook hook = new ModFishingHook(EntityType.FISHING_BOBBER, serverLevel);
        hook.setPos(cast.x, cast.y, cast.z);
        serverLevel.addFreshEntity(hook);
        hook.setOwner(player);
        player.fishing = hook;
        return hook;
    }

// 这一竿的落点；没有记录时用竿架正前方兜底
    private Vec3 castPosition() {
        if (this.hasCastPos) {
            return new Vec3(this.castX, this.castY, this.castZ);
        }
        Direction facing = this.getBlockState().hasProperty(RodPodBlock.FACING)
                ? this.getBlockState().getValue(RodPodBlock.FACING)
                : Direction.NORTH;
        return Vec3.atCenterOf(this.worldPosition).add(facing.getStepX(), 0.4D, facing.getStepZ());
    }

    private void reset() {
        this.biting = false;
        this.biteTicks = -1;
        this.idleTicks = 0;
    }

// 存档与同步

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.rod.isEmpty()) {
            tag.put(ROD_TAG, this.rod.save(registries));
        }
        if (this.hasCastPos) {
            tag.putDouble(CAST_TAG + "X", this.castX);
            tag.putDouble(CAST_TAG + "Y", this.castY);
            tag.putDouble(CAST_TAG + "Z", this.castZ);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.rod = tag.contains(ROD_TAG)
                ? ItemStack.parse(registries, tag.getCompound(ROD_TAG)).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        this.hasCastPos = tag.contains(CAST_TAG + "X") && tag.contains(CAST_TAG + "Y") && tag.contains(CAST_TAG + "Z");
        if (this.hasCastPos) {
            this.castX = tag.getDouble(CAST_TAG + "X");
            this.castY = tag.getDouble(CAST_TAG + "Y");
            this.castZ = tag.getDouble(CAST_TAG + "Z");
        }
        reset();
    }

// 竿子与咬钩状态随区块下发，客户端才能画出架上的竿子
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
