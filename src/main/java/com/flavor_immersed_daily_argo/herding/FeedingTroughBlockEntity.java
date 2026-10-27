package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

import java.util.UUID;

// 牧场食槽方块实体
//
// 只有一格槽位，装饲料或纯净水。平时什么都不做，只在方块的随机刻到了的时候
// 扫一遍周围大范围内的动物，挑一只「饿 / 渴到一定程度」的招过来；动物走路期间逐 tick 盯着它有没有
// 到位，到了就吃掉一份、补上对应的数值；槽被吃空后模型回到空的阶段。
//
// 能来吃的前提是这只动物有状态栏（也就是被喂过、或是在有状态栏的同类身边长大的），
// 因为饱食度 / 口渴度这两个数值本来就只存在于有状态栏的动物身上。
public class FeedingTroughBlockEntity extends BlockEntity {

// 唯一的槽位下标
    public static final int SLOT = 0;

    private static final String ITEM_TAG = "Item";

// 招揽范围：水平 16 格、上下 8 格
    private static final double ATTRACT_RADIUS = 16.0D;
    private static final double ATTRACT_RADIUS_Y = 8.0D;
// 走到这么近就算到槽边，可以开吃了
    private static final double EAT_DISTANCE = 1.5D;
// 动物走完一段路之后重新指路的间隔（2 秒）
    private static final int REPATH_INTERVAL = 40;
// 招来之后最多等这么久，还没走到就放弃（30 秒）
    private static final int PATIENCE = 600;

// 饱食度 / 口渴度掉到这个线以下，动物才会过来吃
    private static final int NEED_THRESHOLD = 5;
// 吃一份补多少饱食度 / 口渴度
    private static final int FEED_RESTORE = 5;
    private static final int WATER_RESTORE = 5;

// 槽位。内容变了要写回方块实体，存档才能拿走里面的东西
    private final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            FeedingTroughBlockEntity.this.setChanged();
        }
    };

// 当前招来的动物，吃到了、走开了或等太久了就清掉重新挑
    @Nullable
    private UUID eater;

    private int repathCooldown;
    private int patience;

    public FeedingTroughBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FEEDING_TROUGH_ENTITY.get(), pos, state);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

// 槽里还剩多少份，准星提示用
    public int count() {
        return this.inventory.getItem(SLOT).getCount();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FeedingTroughBlockEntity trough) {
        trough.tick();
    }

// 与玩家交互

// 把手里的一摞倒进槽里；槽里有别的东西或装满了就原样不动
    public void insert(ItemStack hand) {
        ItemStack slot = this.inventory.getItem(SLOT);
        if (!slot.isEmpty() && !ItemStack.isSameItemSameTags(slot, hand)) {
            return;
        }
        int space = hand.getMaxStackSize() - slot.getCount();
        if (space <= 0) {
            return;
        }
        int moved = Math.min(space, hand.getCount());
        if (slot.isEmpty()) {
            this.inventory.setItem(SLOT, hand.copyWithCount(moved));
        } else {
            slot.grow(moved);
            this.inventory.setChanged();
        }
        hand.shrink(moved);
        this.updateContent();
    }

// 把槽里的东西取回给玩家
    public void extractTo(Player player) {
        ItemStack slot = this.inventory.getItem(SLOT);
        if (slot.isEmpty()) {
            return;
        }
        ItemStack taken = slot.copy();
        this.inventory.setItem(SLOT, ItemStack.EMPTY);
        if (!player.getInventory().add(taken)) {
            player.drop(taken, false);
        }
        this.updateContent();
    }

// 按槽里的东西把方块状态换成对应的阶段
    private void updateContent() {
        if (this.level == null) {
            return;
        }
        FeedingTroughBlock.TroughContent target = FeedingTroughBlock.contentFor(this.inventory.getItem(SLOT));
        if (target == null) {
            target = FeedingTroughBlock.TroughContent.EMPTY;
        }
        BlockState state = this.getBlockState();
        if (state.getValue(FeedingTroughBlock.CONTENT) != target) {
            this.level.setBlock(this.worldPosition, state.setValue(FeedingTroughBlock.CONTENT, target), Block.UPDATE_ALL);
        }
    }

// 扫描与进食

// 由方块的随机刻调用：只有这一刻才去扫一遍周围的动物。
//
// 扫的范围大（16×8×16），但触发得极少，所以总体开销远低于「每 tick 小范围扫」。
// 已经有一只动物在赶来的路上时就直接返回，不再招惹第二只。
    public void serverRandomTick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        FeedingTroughBlock.TroughContent content = FeedingTroughBlock.contentFor(this.inventory.getItem(SLOT));
        if (content == null) {
            this.eater = null;
            return;
        }
        Mob current = this.eaterIn(this.eater);
        if (current != null && this.needs(current, content)) {
            return;
        }
        this.eater = null;
        Mob found = this.findNearestNeedy(content);
        if (found == null) {
            return;
        }
        this.eater = found.getUUID();
        this.patience = PATIENCE;
        this.repathCooldown = 0;
        Vec3 center = Vec3.atCenterOf(this.worldPosition);
        found.getNavigation().moveTo(center.x, center.y, center.z, 1.0D);
    }

// 每 tick 只做一件事：盯着已经招来的那只动物有没有走到。
//
// 没招到动物时（绝大多数时间）这里就是一个空指针判断，成本可以忽略；
// 真正耗时的「扫周围实体」已经完全搬到随机刻里了。
    private void tick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        Mob mob = this.eaterIn(this.eater);
        if (mob == null) {
            this.eater = null;
            return;
        }
        FeedingTroughBlock.TroughContent content = FeedingTroughBlock.contentFor(this.inventory.getItem(SLOT));
        if (content == null || !this.needs(mob, content)) {
            this.eater = null;
            return;
        }
        if (this.reachTrough(mob)) {
            this.feed(mob, content);
        } else if (--this.patience <= 0) {
            this.eater = null;
        }
    }

    @Nullable
    private Mob eaterIn(@Nullable UUID id) {
        if (id == null || !(this.level instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(id);
        return entity instanceof Mob mob && mob.isAlive() && !mob.isRemoved() ? mob : null;
    }

    @Nullable
    private Mob findNearestNeedy(FeedingTroughBlock.TroughContent content) {
        if (this.level == null) {
            return null;
        }
        AABB box = new AABB(this.worldPosition).inflate(ATTRACT_RADIUS, ATTRACT_RADIUS_Y, ATTRACT_RADIUS);
        Vec3 center = Vec3.atCenterOf(this.worldPosition);
        Mob best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Mob mob : this.level.getEntitiesOfClass(Mob.class, box)) {
            if (!this.needs(mob, content)) {
                continue;
            }
            double distance = mob.distanceToSqr(center);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = mob;
            }
        }
        return best;
    }

// 这只动物饿 / 渴到该来吃了，而且槽里的东西正好对得上
    private boolean needs(Mob mob, FeedingTroughBlock.TroughContent content) {
        if (!AnimalStatsManager.isTracked(mob)) {
            return false;
        }
        AnimalStats stats = AnimalStatsManager.getStats(mob);
        if (stats == null) {
            return false;
        }
        return content == FeedingTroughBlock.TroughContent.WATER
                ? stats.thirst() <= NEED_THRESHOLD
                : stats.satiety() <= NEED_THRESHOLD;
    }

// 还没到就偶尔再指一次路（只在上一段路走完时才重指）；到了返回 true
    private boolean reachTrough(Mob mob) {
        Vec3 center = Vec3.atCenterOf(this.worldPosition);
        if (mob.distanceToSqr(center) <= EAT_DISTANCE * EAT_DISTANCE) {
            mob.getNavigation().stop();
            return true;
        }
        if (--this.repathCooldown <= 0) {
            this.repathCooldown = REPATH_INTERVAL;
            if (mob.getNavigation().isDone()) {
                mob.getNavigation().moveTo(center.x, center.y, center.z, 1.0D);
            }
        }
        return false;
    }

// 吃掉一份，补上对应的数值
    private void feed(Mob mob, FeedingTroughBlock.TroughContent content) {
        ItemStack slot = this.inventory.getItem(SLOT);
        AnimalStats stats = AnimalStatsManager.getStats(mob);
        if (slot.isEmpty() || stats == null) {
            return;
        }
        slot.shrink(1);
        this.inventory.setChanged();
        AnimalStatsManager.setStats(mob, content == FeedingTroughBlock.TroughContent.WATER
                ? stats.withThirst(stats.thirst() + WATER_RESTORE)
                : stats.withSatiety(stats.satiety() + FEED_RESTORE));
        this.eater = null;
        this.updateContent();

        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.8F, 1.0F);
            if (this.level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        mob.getX(), mob.getY() + mob.getBbHeight() * 0.6D, mob.getZ(),
                        4, 0.3D, 0.2D, 0.3D, 0.0D);
            }
        }
    }

// 存档

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ItemStack slot = this.inventory.getItem(SLOT);
        if (!slot.isEmpty()) {
            tag.put(ITEM_TAG, slot.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
// 方块状态是随区块一起存的，这里只负责把槽里的东西读回来
        this.inventory.setItem(SLOT, tag.contains(ITEM_TAG)
                ? ItemStack.of(tag.getCompound(ITEM_TAG))
                : ItemStack.EMPTY);
    }

// 客户端同步

// 内容随区块一起下发，客户端拿到才能显示数量
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

// 内容一变就推给客户端；方块实体还没接上世界时（读档途中）不发
    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
