package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.economy.DeliveryManager;
import com.flavor_immersed_daily_argo.economy.FoodOrder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

// 外卖小哥：小票打印机进入出单态时在机器附近刷出来的被动生物。
//
// 它守在打印机附近，偶尔溜达两步；准星指着它就能看到要交的食物与报酬。手里拿着种类数量
// 都对的物品右键即可交付：响一声、按下报酬掉出纸币、小哥随即消失。生成满 5 分钟会自行离开，
// 拿错东西右键它会告诉你还剩多久。
//
// 要交的食物 / 报酬随实体同步给客户端，供准星提示读取；归属的打印机坐标只服务端用，
// 用来让打印机统计与清理自己身边的小哥。
public class DeliveryRider extends Animal implements FoodOrder {

// 生成后多久自动消失（5 分钟）
    public static final int LIFETIME_TICKS = 6000;

// 要交的食物（含数量），会同步给客户端
    private static final EntityDataAccessor<ItemStack> DATA_ORDER =
            SynchedEntityData.defineId(DeliveryRider.class, EntityDataSerializers.ITEM_STACK);
// 报酬（元），会同步给客户端
    private static final EntityDataAccessor<Long> DATA_REWARD =
            SynchedEntityData.defineId(DeliveryRider.class, EntityDataSerializers.LONG);

// 走路 / 站立两套循环动画，交由客户端模型读取
    public final AnimationState goAnimationState = new AnimationState();
    public final AnimationState noAnimationState = new AnimationState();

// 归属的小票打印机坐标（服务端用）
    @Nullable
    private BlockPos home;

    private int age;

    public DeliveryRider(EntityType<? extends DeliveryRider> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ORDER, ItemStack.EMPTY);
        builder.define(DATA_REWARD, 0L);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
// 溜达出界就往打印机那边走回去，保证始终待在「附近」
        this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.7));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

// 单子

// 需要交付的食物（含数量）；空表示这个单子还没派下来
    @Override
    public ItemStack getRequiredFood() {
        return this.entityData.get(DATA_ORDER);
    }

// 报酬（元）
    @Override
    public long getReward() {
        return this.entityData.get(DATA_REWARD);
    }

// 打印机派单：要交的食物与报酬一起写进去
    public void setOrder(ItemStack food, long reward) {
        this.entityData.set(DATA_ORDER, food.copy());
        this.entityData.set(DATA_REWARD, reward);
    }

    @Nullable
    public BlockPos getHome() {
        return this.home;
    }

// 由打印机生成时登记归属，并限制活动范围（超出就会自己走回打印机身边）
    public void setHome(BlockPos home) {
        this.home = home;
        this.restrictTo(home, 8);
    }

// 寿命

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (++this.age > LIFETIME_TICKS) {
            this.discard();
        }
    }

// 还剩多少 tick 就消失
    public int getRemainingTicks() {
        return Math.max(0, LIFETIME_TICKS - this.age);
    }

// 交付

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack required = this.getRequiredFood();
        if (required.isEmpty()) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(required.getItem()) || held.getCount() < required.getCount()) {
// 东西不对：只报一下还剩多久，不消耗交互里的物品
            if (!this.level().isClientSide) {
                player.displayClientMessage(Component.translatable(
                        "message.flavor_immersed_daily_argo.rider.remaining", formatRemaining()), true);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel level = (ServerLevel) this.level();
        long reward = this.getReward();
        if (!player.getAbilities().instabuild) {
            held.shrink(required.getCount());
        }
        DeliveryManager.spawnMoney(level, this.position(), reward);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.4D, 0.4D, 0.4D, 0.0D);
        player.displayClientMessage(Component.translatable(
                "message.flavor_immersed_daily_argo.rider.done", reward), true);
        this.discard();
        return InteractionResult.SUCCESS;
    }

// 「3 分 20 秒」这样的剩余时间文案
    private String formatRemaining() {
        int seconds = (this.getRemainingTicks() + 19) / 20;
        int minutes = seconds / 60;
        int rest = seconds % 60;
        return minutes > 0 ? minutes + " 分 " + rest + " 秒" : rest + " 秒";
    }

// 生物属性

// 不是可繁殖的动物，也不吃任何东西
    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

// 到点自己消失，不走原版「远离玩家就刷掉」那套
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

// 存读

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Age", this.age);
        if (this.home != null) {
            tag.putLong("Home", this.home.asLong());
        }
        ItemStack order = this.getRequiredFood();
        if (!order.isEmpty()) {
            CompoundTag orderTag = new CompoundTag();
            order.save(this.registryAccess(), orderTag);
            tag.put("Order", orderTag);
        }
        tag.putLong("Reward", this.getReward());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.age = tag.getInt("Age");
        if (tag.contains("Home")) {
            this.home = BlockPos.of(tag.getLong("Home"));
        }
        if (tag.contains("Order")) {
            ItemStack.parse(this.registryAccess(), tag.getCompound("Order"))
                    .ifPresent(stack -> this.entityData.set(DATA_ORDER, stack));
        }
        this.entityData.set(DATA_REWARD, tag.getLong("Reward"));
    }
}
