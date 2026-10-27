package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 宠物窝方块实体
//
// 三件事：
//
// 安家——把附近已驯服的猫狗招过来绑定，一个窝只住一只。
// 守候——住进来的宠物每 20 刻被指一次路，直到走进窝里。
// 复活——宠物过世时留下快照，过了当天就在窝上把它重新放出来。
public class PetNestBlockEntity extends BlockEntity {

    private static final String PET_TAG = "Pet";
    private static final String PENDING_PET_TAG = "PendingPet";
    private static final String DEATH_DAY_TAG = "DeathDay";

// 招揽范围：水平 2 区块、上下 8 格
    private static final double SCAN_RADIUS = 32.0D;
    private static final double SCAN_RADIUS_Y = 8.0D;
// 找宠物的间隔；相位随机错开，免得所有宠物窝挤在同一刻一起扫
    private static final int SEARCH_INTERVAL = 40;
// 往窝里指路的间隔
    private static final int REPATH_INTERVAL = 20;
// 走到这个水平距离以内就算到家了
    private static final double ARRIVE_DISTANCE = 1.5D;
// 一天的刻数，与 ForestryNetworkManager 的日计数口径一致
    private static final long TICKS_PER_DAY = 24000L;

// 住在这里的宠物
    @Nullable
    private UUID pet;
// 过世宠物的快照，等着次日复活；有快照期间这个窝不再收新宠物
    @Nullable
    private CompoundTag pendingPet;
// 宠物过世时的日计数
    private long deathDay;

// -1 表示还没定过扫描相位
    private int searchCooldown = -1;
    private int repathCooldown;

    public PetNestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PET_NEST_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PetNestBlockEntity nest) {
        nest.tick();
    }

    private void tick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (this.tickRevive()) {
            return;
        }
        if (this.pendingPet != null) {
// 快照还在占位，先不收新宠物，免得多出一只
            return;
        }
        this.tickAdopt();
    }

// 招揽与守候

    private void tickAdopt() {
        TamableAnimal current = this.findPet();
        if (current != null) {
// 只在游荡模式下指路：主人把它切回跟随后，别去抢原版 FollowOwnerGoal 的方向盘
            if (PetNestFolk.getMode(current) == PetNestFolk.PetMode.WANDER) {
                this.tickApproach(current);
            }
            return;
        }
        this.forgetPet();
        if (this.searchCooldown < 0) {
// 首次扫描相位随机错开；-1 不落盘，每次读档都会重新错开
            this.searchCooldown = this.level.random.nextInt(SEARCH_INTERVAL) + 1;
        }
        if (--this.searchCooldown > 0) {
            return;
        }
        this.searchCooldown = SEARCH_INTERVAL;
        TamableAnimal candidate = this.findCandidate();
        if (candidate != null) {
            this.adopt(candidate);
        }
    }

// 还没到家就每 20 刻指一次路；进了窝就不再指，让它自己在附近游荡
    private void tickApproach(TamableAnimal pet) {
        double dx = pet.getX() - (this.worldPosition.getX() + 0.5D);
        double dz = pet.getZ() - (this.worldPosition.getZ() + 0.5D);
        if (dx * dx + dz * dz <= ARRIVE_DISTANCE * ARRIVE_DISTANCE) {
            return;
        }
        if (--this.repathCooldown > 0) {
            return;
        }
        this.repathCooldown = REPATH_INTERVAL;
        Vec3 center = Vec3.atCenterOf(this.worldPosition);
        pet.getNavigation().moveTo(center.x, center.y, center.z, 1.0D);
    }

    @Nullable
    private TamableAnimal findPet() {
        if (this.pet == null || !(this.level instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(this.pet);
        return entity instanceof TamableAnimal pet && pet.isAlive() && !pet.isRemoved() ? pet : null;
    }

// 住客已经不在这个窝里了（走远没加载、过世、被转移）。
//
// 只清掉窝侧的引用：宠物自己身上的绑定键留着，万一只是区块暂时卸载，
// 回来之后还能重新被这个窝收下。
    private void forgetPet() {
        if (this.pet == null) {
            return;
        }
        this.pet = null;
        this.setChanged();
    }

    @Nullable
    private TamableAnimal findCandidate() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return null;
        }
        AABB box = new AABB(this.worldPosition).inflate(SCAN_RADIUS, SCAN_RADIUS_Y, SCAN_RADIUS);
        List<TamableAnimal> candidates = new ArrayList<>();
        candidates.addAll(serverLevel.getEntitiesOfClass(Cat.class, box));
        candidates.addAll(serverLevel.getEntitiesOfClass(Wolf.class, box));

        TamableAnimal best = null;
        double bestDistance = Double.MAX_VALUE;
        for (TamableAnimal pet : candidates) {
            if (!pet.isAlive() || pet.isRemoved() || !pet.isTame()) {
                continue;
            }
            if (PetNestFolk.settledElsewhere(pet, this.worldPosition, serverLevel.dimension())) {
                continue;
            }
            double distance = pet.distanceToSqr(Vec3.atCenterOf(this.worldPosition));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pet;
            }
        }
        return best;
    }

    private void adopt(TamableAnimal pet) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        PetNestFolk.bind(pet, this.worldPosition, serverLevel.dimension());
// 直接进游荡模式：摘掉跟随目标，宠物才会老老实实按上面的指路走进窝里
        PetNestFolk.setMode(pet, PetNestFolk.PetMode.WANDER);
        this.pet = pet.getUUID();
        this.repathCooldown = 0;
        this.setChanged();

        if (pet.getOwner() instanceof ServerPlayer owner) {
            owner.displayClientMessage(
                    Component.translatable("message.flavor_immersed_daily_argo.pet_nest.bound"), true);
        }
    }

// 过世与复活

// 宠物过世：把快照与当天日计数记下来，次日在窝上复活
    public void onPetDied(CompoundTag snapshot) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        this.pet = null;
        this.pendingPet = snapshot;
        this.deathDay = serverLevel.getDayTime() / TICKS_PER_DAY;
        this.setChanged();
    }

// 到了第二天就把快照里的宠物放回窝上；返回是否真的复活了
    private boolean tickRevive() {
        if (this.pendingPet == null || !(this.level instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (serverLevel.getDayTime() / TICKS_PER_DAY <= this.deathDay) {
            return false;
        }
        CompoundTag snapshot = this.pendingPet;
        this.pendingPet = null;
        this.deathDay = 0L;

        Entity revived = EntityType.create(snapshot, serverLevel).orElse(null);
        if (!(revived instanceof TamableAnimal pet)) {
            this.setChanged();
            return false;
        }
        pet.setPersistenceRequired();
        Vec3 center = Vec3.atCenterOf(this.worldPosition);
        pet.moveTo(center.x, center.y, center.z, serverLevel.random.nextFloat() * 360.0F, 0.0F);
        pet.setDeltaMovement(Vec3.ZERO);
        serverLevel.addFreshEntity(pet);

        PetNestFolk.bind(pet, this.worldPosition, serverLevel.dimension());
        PetNestFolk.setMode(pet, PetNestFolk.PetMode.WANDER);
        this.pet = pet.getUUID();
        this.repathCooldown = 0;
        this.setChanged();
        return true;
    }

// 窝被拆掉：解绑住客，丢掉还没到期的快照
    public void onBroken() {
        if (this.level instanceof ServerLevel serverLevel && this.pet != null
                && serverLevel.getEntity(this.pet) instanceof TamableAnimal pet) {
            PetNestFolk.unbind(pet);
        }
        this.pet = null;
        this.pendingPet = null;
        this.deathDay = 0L;
    }

// 存档

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.pet != null) {
            tag.putUUID(PET_TAG, this.pet);
        }
        if (this.pendingPet != null) {
            tag.put(PENDING_PET_TAG, this.pendingPet.copy());
            tag.putLong(DEATH_DAY_TAG, this.deathDay);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.pet = tag.hasUUID(PET_TAG) ? tag.getUUID(PET_TAG) : null;
        this.pendingPet = tag.contains(PENDING_PET_TAG, CompoundTag.TAG_COMPOUND)
                ? tag.getCompound(PENDING_PET_TAG).copy() : null;
        this.deathDay = tag.getLong(DEATH_DAY_TAG);
    }
}