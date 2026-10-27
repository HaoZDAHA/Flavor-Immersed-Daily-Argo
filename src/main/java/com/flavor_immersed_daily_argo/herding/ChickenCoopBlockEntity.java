package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.util.InventoryNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// 鸡窝方块实体
//
// 三件事：
//
// 吸引——把附近的家禽招到鸡窝上并定住；一个鸡窝同一时间只留一只，
// 占用关系记在禽类的持久化数据里，别的鸡窝不会来抢。
// 孵化——孵化槽里放蛋，随机计时到点后孵出一只对应的幼崽。
// 蛋与禽类的对应关系见 CoopEggs。
// 收集——把周围的蛋类掉落物收进收集槽；收集槽只存蛋，永远不孵化。
public class ChickenCoopBlockEntity extends BlockEntity implements MenuProvider {

// 孵化槽下标
    public static final int HATCH_SLOT = 0;
// 收集槽数量，紧跟在孵化槽之后
    public static final int COLLECT_SLOTS = 9;
// 槽位总数
    public static final int SLOT_COUNT = 1 + COLLECT_SLOTS;

// 禽类占用标记的键，值为鸡窝方块坐标的 long 形式
    private static final String CLAIM_KEY = "FlavorImmersedDailyCoop";
    private static final String INVENTORY_TAG = "Inventory";
    private static final String PERCHED_TAG = "Perched";
    private static final String PERCH_NO_AI_TAG = "PerchNoAi";
    private static final String HATCH_TIMER_TAG = "HatchTimer";

// 吸引范围：水平 8 格、上下 4 格
    private static final double ATTRACT_RADIUS = 8.0D;
    private static final double ATTRACT_RADIUS_Y = 4.0D;
// 已经走到这个水平距离以内，就把它定住并吸附到方块正中心
    private static final double PERCH_DISTANCE = 0.5D;
// 找候选禽类的间隔，避免每 tick 都扫一遍实体
    private static final int SEARCH_INTERVAL = 20;
// 往鸡窝重新指路的间隔
    private static final int REPATH_INTERVAL = 20;
// 收集蛋的间隔
    private static final int COLLECT_INTERVAL = 20;
    private static final double COLLECT_RADIUS = 4.0D;
// 孵化耗时：1200~2400 tick（约 1~2 分钟）随机一次
    private static final int HATCH_MIN_TICKS = 1200;
    private static final int HATCH_RANDOM_TICKS = 1200;

// 孵化槽与收集槽。玩家/管道改动后要写回方块实体，存档才能拿走内容
    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            ChickenCoopBlockEntity.this.setChanged();
        }
    };

// 当前占用的禽类
    @Nullable
    private UUID perched;
// 定住禽类时是否由本鸡窝打开过 NoAI，释放时按需还原
    private boolean perchNoAiSet;

    private int hatchTimer;
// 下一次孵化所需的 tick 数，开始孵化时抽一次并固定下来
    private int hatchTarget;
    private int collectCooldown;
    private int repathCooldown;
    private int searchCooldown;

    public ChickenCoopBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHICKEN_COOP_ENTITY.get(), pos, state);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChickenCoopBlockEntity coop) {
        coop.tick();
    }

// 每 tick 主循环

    private void tick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (--this.collectCooldown <= 0) {
            this.collectCooldown = COLLECT_INTERVAL;
            this.collectNearbyEggs();
        }
        this.tickHatching();
        this.tickPerch();
    }

// 孵化

    private void tickHatching() {
        EntityType<?> type = CoopEggs.entityFor(this.inventory.getItem(HATCH_SLOT));
        if (type == null) {
            this.hatchTimer = 0;
            this.hatchTarget = 0;
            return;
        }
        if (this.hatchTarget <= 0) {
            this.hatchTarget = HATCH_MIN_TICKS + this.level.random.nextInt(HATCH_RANDOM_TICKS);
        }
        if (++this.hatchTimer < this.hatchTarget) {
            return;
        }
        this.hatchTimer = 0;
        this.hatchTarget = 0;
        this.hatch(type);
    }

// 消耗孵化槽里的一个蛋，并在鸡窝中央放出对应的幼崽
    private void hatch(EntityType<?> type) {
        if (this.level == null) {
            return;
        }
        ItemStack egg = this.inventory.getItem(HATCH_SLOT);
        egg.shrink(1);
        this.inventory.setChanged();

        Entity baby = type.create(this.level);
        if (baby == null) {
            return;
        }
        if (baby instanceof AgeableMob ageable) {
            ageable.setBaby(true);
        }
        if (baby instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        Vec3 center = Vec3.atCenterOf(this.worldPosition);
        baby.moveTo(center.x, center.y, center.z, this.level.random.nextFloat() * 360.0F, 0.0F);
        this.level.addFreshEntity(baby);
    }

// 收集

    private void collectNearbyEggs() {
        if (this.level == null) {
            return;
        }
        AABB box = new AABB(this.worldPosition).inflate(COLLECT_RADIUS);
        for (ItemEntity drop : this.level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (!drop.isAlive() || !CoopEggs.isEgg(drop.getItem())) {
                continue;
            }
            ItemStack remainder = this.insert(drop.getItem());
            if (remainder.isEmpty()) {
                drop.discard();
            } else {
                drop.setItem(remainder);
            }
        }
    }

// 先并入同类收集槽，再找空槽；返回塞不下的部分
    private ItemStack insert(ItemStack stack) {
        for (int i = 1; i < SLOT_COUNT; i++) {
            ItemStack slot = this.inventory.getItem(i);
            if (!ItemStack.isSameItemSameComponents(slot, stack)) {
                continue;
            }
            int space = slot.getMaxStackSize() - slot.getCount();
            if (space <= 0) {
                continue;
            }
            int moved = Math.min(space, stack.getCount());
            slot.grow(moved);
            this.inventory.setChanged();
            stack.shrink(moved);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        for (int i = 1; i < SLOT_COUNT; i++) {
            if (!this.inventory.getItem(i).isEmpty()) {
                continue;
            }
            this.inventory.setItem(i, stack.copy());
            return ItemStack.EMPTY;
        }
        return stack;
    }

// 吸引

    private void tickPerch() {
        Chicken chicken = this.findPerched();
        if (chicken != null) {
            this.updatePerch(chicken);
            return;
        }
        this.releasePerched();
        if (--this.searchCooldown > 0) {
            return;
        }
        this.searchCooldown = SEARCH_INTERVAL;
        Chicken candidate = this.findCandidate();
        if (candidate == null) {
            return;
        }
        this.perched = candidate.getUUID();
        candidate.getPersistentData().putLong(CLAIM_KEY, this.worldPosition.asLong());
        this.setChanged();
        this.updatePerch(candidate);
    }

    @Nullable
    private Chicken findPerched() {
        if (this.perched == null || !(this.level instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(this.perched);
        return entity instanceof Chicken chicken && chicken.isAlive() && !chicken.isRemoved() ? chicken : null;
    }

// 还没到就指路让它走上来，到了就定在方块正中心
    private void updatePerch(Chicken chicken) {
        double centerX = this.worldPosition.getX() + 0.5D;
        double centerZ = this.worldPosition.getZ() + 0.5D;
        double dx = chicken.getX() - centerX;
        double dz = chicken.getZ() - centerZ;
        if (dx * dx + dz * dz > PERCH_DISTANCE * PERCH_DISTANCE) {
            this.holdStill(chicken, false);
            if (--this.repathCooldown <= 0) {
                this.repathCooldown = REPATH_INTERVAL;
                Vec3 center = Vec3.atCenterOf(this.worldPosition);
                chicken.getNavigation().moveTo(center.x, center.y, center.z, 1.0D);
            }
            return;
        }
// 到位：定住并吸附到方块正中心，否则会停在离中心还有半格的位置
        this.holdStill(chicken, true);
        chicken.getNavigation().stop();
        chicken.setPos(centerX, chicken.getY(), centerZ);
        Vec3 motion = chicken.getDeltaMovement();
        chicken.setDeltaMovement(0.0D, motion.y, 0.0D);
    }

    private void holdStill(Chicken chicken, boolean still) {
        if (still) {
            if (!chicken.isNoAi()) {
                this.perchNoAiSet = true;
                chicken.setNoAi(true);
            }
        } else if (this.perchNoAiSet) {
            chicken.setNoAi(false);
            this.perchNoAiSet = false;
        }
    }

    @Nullable
    private Chicken findCandidate() {
        if (this.level == null) {
            return null;
        }
        AABB box = new AABB(this.worldPosition).inflate(ATTRACT_RADIUS, ATTRACT_RADIUS_Y, ATTRACT_RADIUS);
        Chicken best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Chicken chicken : this.level.getEntitiesOfClass(Chicken.class, box)) {
            if (!chicken.isAlive() || chicken.isRemoved() || this.claimedByOther(chicken)) {
                continue;
            }
            double distance = chicken.distanceToSqr(Vec3.atCenterOf(this.worldPosition));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = chicken;
            }
        }
        return best;
    }

    private boolean claimedByOther(Chicken chicken) {
        CompoundTag data = chicken.getPersistentData();
        return data.contains(CLAIM_KEY) && data.getLong(CLAIM_KEY) != this.worldPosition.asLong();
    }

// 放开当前占用的禽类：解除它的停留状态与占用标记
    public void releasePerched() {
        if (this.perched == null) {
            return;
        }
        if (this.level instanceof ServerLevel serverLevel) {
            if (serverLevel.getEntity(this.perched) instanceof Chicken chicken) {
                if (this.perchNoAiSet) {
                    chicken.setNoAi(false);
                }
                CompoundTag data = chicken.getPersistentData();
                if (data.contains(CLAIM_KEY) && data.getLong(CLAIM_KEY) == this.worldPosition.asLong()) {
                    data.remove(CLAIM_KEY);
                }
            }
        }
        this.perched = null;
        this.perchNoAiSet = false;
        this.setChanged();
    }

// 存档

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(INVENTORY_TAG, InventoryNbt.save(this.inventory, registries));
        if (this.perched != null) {
            tag.putUUID(PERCHED_TAG, this.perched);
// NoAI 记录也要存：否则读档后 perched 里那只被冻住的禽类再也放不开
            tag.putBoolean(PERCH_NO_AI_TAG, this.perchNoAiSet);
        }
        tag.putInt(HATCH_TIMER_TAG, this.hatchTimer);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        InventoryNbt.load(this.inventory, tag, INVENTORY_TAG, registries);
        this.perched = tag.hasUUID(PERCHED_TAG) ? tag.getUUID(PERCHED_TAG) : null;
        this.perchNoAiSet = this.perched != null && tag.getBoolean(PERCH_NO_AI_TAG);
        this.hatchTimer = tag.getInt(HATCH_TIMER_TAG);
    }

// 界面

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.flavor_immersed_daily_argo.chicken_coop");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ChickenCoopMenu(containerId, playerInventory, this);
    }
}
