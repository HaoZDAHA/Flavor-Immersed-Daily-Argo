package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.item.SickleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

// 老鼠。
//
// 田里的害兽：平时在作物附近溜达（go 动画），一旦发现成熟的作物就凑过去啃上一口，
// 令其生长阶段倒退一级——啃食时会播 eat 动画、吃食的粒子与音效，爪子里还叼着对应战利品的物品图标
// （见 MouseRenderer）。啃够 #CROPS_BEFORE_LEAVING 株后这只老鼠会自己溜走消失，
// 不会把整片田啃光，也从不毁掉作物方块。
//
// 跑得飞快（比玩家快、比猫略慢），看见威胁就溜（AvoidEntityGoal），但视野只有
// #VISION_RANGE 格，且对放轻脚步逼近的猫毫无察觉；猫则由
// MouseSpawning 在入场时挂上「主动猎鼠」的目标选择器。生成完全由
// MouseSpawning 控制，可用 /mouse spawn <true|false> 开关。
public class Mouse extends Animal {

// 一次啃食持续多久（tick）
    private static final int EAT_TICKS = 60;

// 啃够这么多株作物就溜走消失，免得一只老鼠把整片田啃光
    private static final int CROPS_BEFORE_LEAVING = 3;

// 已啃作物数在存档里的键
    private static final String CROPS_EATEN_TAG = "CropsEaten";

// 找成熟作物的水平搜索半径（格）
    private static final double SEARCH_RADIUS = 6.0D;

// 能看见威胁的距离（格）：比原来短，威胁得凑近了老鼠才察觉
    private static final float VISION_RANGE = 4.0F;

// 啃食中：客户端据此播 eat 动画、在爪子里叼出物品图标
    private static final EntityDataAccessor<Boolean> DATA_EATING =
            SynchedEntityData.defineId(Mouse.class, EntityDataSerializers.BOOLEAN);

// 正在啃的那份战利品：仅用于客户端渲染，不落回世界
    private static final EntityDataAccessor<ItemStack> DATA_EATEN_ITEM =
            SynchedEntityData.defineId(Mouse.class, EntityDataSerializers.ITEM_STACK);

// 运动时循环播放的 go 动画
    public final AnimationState goAnimationState = new AnimationState();

// 啃食时循环播放的 eat 动画
    public final AnimationState eatAnimationState = new AnimationState();

    private int eatingTicks;

// 已经啃过几株作物：啃满 #CROPS_BEFORE_LEAVING 株后这只老鼠就消失
    private int cropsEaten;

    public Mouse(EntityType<? extends Mouse> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
// 又怕猫又怕人：看见就窜；不过正放轻脚步逼近的猫不算数（见 AvoidStalkingCatGoal）
        this.goalSelector.addGoal(2, new AvoidStalkingCatGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, VISION_RANGE, 1.4D, 1.8D));
        this.goalSelector.addGoal(3, new EatCropGoal());
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.2D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

// 同步数据

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_EATING, false);
        builder.define(DATA_EATEN_ITEM, ItemStack.EMPTY);
    }

    public boolean isEating() {
        return this.entityData.get(DATA_EATING);
    }

    public ItemStack getEatenItem() {
        return this.entityData.get(DATA_EATEN_ITEM);
    }

// 啃食

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!this.isEating()) {
            return;
        }
// 啃食是原地动作：站着啃，别一边啃一边溜达
        this.getNavigation().stop();
        if (--this.eatingTicks % 8 == 0) {
            this.playEatEffects();
        }
        if (this.eatingTicks <= 0) {
            this.entityData.set(DATA_EATING, false);
            this.entityData.set(DATA_EATEN_ITEM, ItemStack.EMPTY);
// 啃够了就叼着这一口溜走：不破坏方块，也不再回来
            if (this.cropsEaten >= CROPS_BEFORE_LEAVING) {
                this.discard();
            }
        }
    }

// 啃食一株成熟作物并开始吃它的掉落物：令作物倒退一个生长阶段
    private void startEating(BlockPos pos) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        BlockState state = server.getBlockState(pos);
        if (!SickleItem.isMatureCrop(state)) {
            return;
        }
// 先按战利品表算出会被啃掉的东西，取第一件当「爪子里叼着的那份」
        List<ItemStack> drops = Block.getDrops(state, server, pos, server.getBlockEntity(pos), this, ItemStack.EMPTY);
        ItemStack food = ItemStack.EMPTY;
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                food = drop.copyWithCount(1);
                break;
            }
        }
        if (food.isEmpty()) {
            food = new ItemStack(state.getBlock().asItem());
        }
// 啃掉一茬：作物退回上一个生长阶段，并不被毁掉
        server.setBlock(pos, SickleItem.regressedState(state), 3);
        this.cropsEaten++;
        this.entityData.set(DATA_EATEN_ITEM, food);
        this.entityData.set(DATA_EATING, true);
        this.eatingTicks = EAT_TICKS;
        this.getNavigation().stop();
        this.playEatEffects();
    }

// 啃食的观感：吃食音效 + 食物碎屑粒子
    private void playEatEffects() {
        ItemStack food = this.getEatenItem();
        if (food.isEmpty() || !(this.level() instanceof ServerLevel server)) {
            return;
        }
        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EAT, this.getSoundSource(), 0.6F,
                1.0F + (this.random.nextFloat() - 0.5F) * 0.4F);
        server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, food),
                this.getX(), this.getY() + 0.25D, this.getZ(),
                3, 0.1D, 0.1D, 0.1D, 0.02D);
    }

// 就近找一株成熟作物
    @Nullable
    private BlockPos findMatureCrop() {
        BlockPos origin = this.blockPosition();
        int radius = (int) SEARCH_RADIUS;
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -1, -radius), origin.offset(radius, 1, radius))) {
            if (SickleItem.isMatureCrop(this.level().getBlockState(pos))) {
                found.add(pos.immutable());
            }
        }
        return found.isEmpty() ? null : found.get(this.getRandom().nextInt(found.size()));
    }

// 看见成熟作物就凑过去啃掉
    private class EatCropGoal extends Goal {

        @Nullable
        private BlockPos crop;

        EatCropGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (Mouse.this.isEating() || Mouse.this.getRandom().nextInt(15) != 0) {
                return false;
            }
            this.crop = Mouse.this.findMatureCrop();
            return this.crop != null;
        }

        @Override
        public boolean canContinueToUse() {
            return !Mouse.this.isEating() && this.crop != null
                    && SickleItem.isMatureCrop(Mouse.this.level().getBlockState(this.crop));
        }

        @Override
        public void start() {
            if (this.crop != null) {
                Mouse.this.getNavigation().moveTo(this.crop.getX() + 0.5D, this.crop.getY(), this.crop.getZ() + 0.5D, 1.3D);
            }
        }

        @Override
        public void tick() {
            if (this.crop == null) {
                return;
            }
            Mouse.this.getLookControl().setLookAt(this.crop.getX() + 0.5D, this.crop.getY() + 0.5D, this.crop.getZ() + 0.5D);
            double dist = Mouse.this.distanceToSqr(this.crop.getX() + 0.5D, this.crop.getY() + 0.5D, this.crop.getZ() + 0.5D);
            if (dist <= 2.25D) {
                Mouse.this.startEating(this.crop);
            } else if (Mouse.this.getNavigation().isDone()) {
                Mouse.this.getNavigation().moveTo(this.crop.getX() + 0.5D, this.crop.getY(), this.crop.getZ() + 0.5D, 1.3D);
            }
        }

        @Override
        public void stop() {
            this.crop = null;
            Mouse.this.getNavigation().stop();
        }
    }

// 躲猫

// 躲猫：正放轻脚步逼近的猫，老鼠察觉不到。
//
// 猫猎食走原版 OcelotAttackGoal——猫看见老鼠后是一小步一小步挪过去的。
// 该目标处于运行中时就把猫从「要躲开的对象」里划掉，好让猫能摸到跟前再扑。
    private static class AvoidStalkingCatGoal extends AvoidEntityGoal<Cat> {

        AvoidStalkingCatGoal(Mouse mouse) {
            super(mouse, Cat.class, VISION_RANGE, 1.3D, 1.6D);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !isStalking(this.toAvoid);
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !isStalking(this.toAvoid);
        }
    }

// 猫是否正在放轻脚步逼近猎物（原版 OcelotAttackGoal 运行中）
    private static boolean isStalking(@Nullable Cat cat) {
        if (cat == null) {
            return false;
        }
        for (WrappedGoal wrapped : cat.goalSelector.getAvailableGoals()) {
            if (wrapped.isRunning() && wrapped.getGoal() instanceof OcelotAttackGoal) {
                return true;
            }
        }
        return false;
    }

// 动物杂项

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(CROPS_EATEN_TAG, this.cropsEaten);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.cropsEaten = tag.getInt(CROPS_EATEN_TAG);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT_SEEDS);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.MOUSE.get().create(level);
    }

// 原版没有老鼠音效；蝙蝠的吱吱声最接近，别用兔子那套（字幕会显示成 Rabbit）
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }
}
