package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.economy.DeliveryManager;
import com.flavor_immersed_daily_argo.economy.FoodOrder;
import com.flavor_immersed_daily_argo.economy.SeatManager;
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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// 顾客：餐饮营业执照开业后招来的堂食客人。
//
// 它只在玩家用「座垫标记」标记过的方块上落座（见 SeatManager），像外卖小哥一样带着
// 一张单子：准星指着它能看到要交的食物与报酬，手里拿着种类数量都对的物品右键即可结单——
// 响一声、掉出纸币、顾客随即满足离场。落到座垫上之前它会走到座位附近自己坐下；坐着的过程中
// 如果座垫被拆掉标记（超出 #isSeatValid 判定）会站起来重新找座位。生成满 5 分钟会自行离开。
//
// 要交的食物 / 报酬随实体同步给客户端，供准星提示与模型坐姿读取；归属的营业执照坐标（home）
// 只服务端用，用来限定找座位的范围、以及在坐下时让顾客面朝店门。顾客不轮询营业执照——
// 执照打烊 / 被拆时由执照方块自己清理归属的顾客。
public class Customer extends Animal implements FoodOrder {

// 生成后多久自动消失（5 分钟）
    public static final int LIFETIME_TICKS = 6000;

// 单家店同一时间最多接待的顾客数（供营业执照统计用）
    public static final int MAX_CUSTOMERS = 6;

// 找座位时以营业执照为中心的最大半径（格）
    private static final double SEAT_RANGE = 16.0D;

// 是否已经落座（客户端渲染坐姿要用，必须同步）
    private static final EntityDataAccessor<Boolean> DATA_SEATED =
            SynchedEntityData.defineId(Customer.class, EntityDataSerializers.BOOLEAN);
// 要交的食物（含数量），会同步给客户端
    private static final EntityDataAccessor<ItemStack> DATA_ORDER =
            SynchedEntityData.defineId(Customer.class, EntityDataSerializers.ITEM_STACK);
// 报酬（元），会同步给客户端
    private static final EntityDataAccessor<Long> DATA_REWARD =
            SynchedEntityData.defineId(Customer.class, EntityDataSerializers.LONG);

// 归属的营业执照坐标（服务端用）
    @Nullable
    private BlockPos home;
// 当前坐的座位坐标（服务端用）
    @Nullable
    private BlockPos seat;

    private int age;

    public Customer(EntityType<? extends Customer> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SEATED, false);
        builder.define(DATA_ORDER, ItemStack.EMPTY);
        builder.define(DATA_REWARD, 0L);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
// 找座就座：进店后去最近的空座位坐下
        this.goalSelector.addGoal(1, new SitGoal(this));
// 溜达出界就往营业执照那边走回去，保证始终待在「店里」
        this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.7));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

// 座位

// 是否已经落座
    public boolean isSeated() {
        return this.entityData.get(DATA_SEATED);
    }

// 当前坐的座位坐标；没坐下时为 null
    @Nullable
    public BlockPos getSeat() {
        return this.seat;
    }

    @Nullable
    public BlockPos getHome() {
        return this.home;
    }

// 由营业执照生成时登记归属，并限制活动范围（超出就会自己走回店门口）
    public void setHome(BlockPos home) {
        this.home = home;
        this.restrictTo(home, 16);
    }

// 这次的坐位是否仍然可用：座垫标记还在、座垫方块没被拆、头顶那一格不挡碰撞。
    private boolean isSeatValid(BlockPos pos) {
        if (!(this.level() instanceof ServerLevel sl)) {
            return false;
        }
        return SeatManager.get(sl).isSeat(pos)
                && !sl.getBlockState(pos).isAir()
                && sl.getBlockState(pos.above()).getCollisionShape(sl, pos.above()).isEmpty();
    }

// 在自己附近找一个还没被别人占的空座位，取距离最近的那个。
//
// 只在服务端使用；没登记归属（home == null）、附近没有合法座位、或候选都被人占了时
// 返回 null。
    @Nullable
    private BlockPos findFreeSeat() {
        if (!(this.level() instanceof ServerLevel sl) || this.home == null) {
            return null;
        }
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : SeatManager.get(sl).seats()) {
            if (!this.home.closerThan(pos, SEAT_RANGE)) {
                continue;
            }
            if (!this.isSeatValid(pos)) {
                continue;
            }
            boolean occupied = !sl.getEntitiesOfClass(Customer.class,
                    new AABB(pos).inflate(0.5D), c -> pos.equals(c.getSeat())).isEmpty();
            if (occupied) {
                continue;
            }
            double dist = pos.distSqr(this.blockPosition());
            if (dist < bestDist) {
                bestDist = dist;
                best = pos;
            }
        }
        return best;
    }

// 坐到指定座位上：钉住位置、停掉移动 / AI，并面朝营业执照坐着看店。
//
// 由 SitGoal 在走到座位附近时调用，供后续执照逻辑复用。
    public void sitOn(BlockPos pos) {
        this.seat = pos;
        this.entityData.set(DATA_SEATED, true);
        this.getNavigation().stop();
        this.setNoAi(true);
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        if (this.home != null) {
// 面朝营业执照，坐着看店
            double dx = (this.home.getX() + 0.5D) - this.getX();
            double dz = (this.home.getZ() + 0.5D) - this.getZ();
            float yaw = (float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
            this.setYRot(yaw);
            this.yHeadRot = yaw;
        }
    }

// 站起来：解开会话，恢复 AI（座垫失效 / 拆掉时用）
    public void standUp() {
        this.seat = null;
        this.entityData.set(DATA_SEATED, false);
        this.setNoAi(false);
    }

// 找座位的 AI：节流扫描座位，走到最近空座位后坐下
    private static class SitGoal extends Goal {

        private final Customer customer;
        @Nullable
        private BlockPos target;

        SitGoal(Customer customer) {
            this.customer = customer;
        }

        @Override
        public boolean canUse() {
            if (this.customer.isSeated()) {
                return false;
            }
// 每 20 tick 才扫一次座位表，省得每 tick 遍历
            if (this.customer.tickCount % 20 != 0) {
                return false;
            }
            this.target = this.customer.findFreeSeat();
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.customer.isSeated() && this.target != null;
        }

        @Override
        public void start() {
            this.customer.getNavigation().moveTo(
                    this.target.getX() + 0.5D, this.target.getY() + 1.0D, this.target.getZ() + 0.5D, 0.6D);
        }

        @Override
        public void tick() {
            if (this.customer.blockPosition().distSqr(this.target) <= 4.0D
                    || this.customer.getNavigation().isDone()) {
                this.customer.sitOn(this.target);
                this.target = null;
            }
        }

        @Override
        public void stop() {
            this.customer.getNavigation().stop();
            this.target = null;
        }
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

// 营业执照派单：要交的食物与报酬一起写进去
    public void setOrder(ItemStack food, long reward) {
        this.entityData.set(DATA_ORDER, food.copy());
        this.entityData.set(DATA_REWARD, reward);
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
            return;
        }
// 坐着的过程中座位失效（座垫被拆 / 标记被取消）就站起来重新找座。
// 顾客不检查营业执照：执照打烊 / 被拆由执照方块自己清理归属的顾客。
        if (this.isSeated() && (this.seat == null || !this.isSeatValid(this.seat))) {
            this.standUp();
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
                        "message.flavor_immersed_daily_argo.customer.remaining", formatRemaining()), true);
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
                "message.flavor_immersed_daily_argo.customer.done", reward), true);
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
        tag.putBoolean("Seated", this.isSeated());
        if (this.home != null) {
            tag.putLong("Home", this.home.asLong());
        }
        if (this.seat != null) {
            tag.putLong("Seat", this.seat.asLong());
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
        if (tag.contains("Seat")) {
            this.seat = BlockPos.of(tag.getLong("Seat"));
        }
// 落座状态一并恢复：坐姿、钉住的位置（NoAI）都还在，站起来会原地卡住
        this.entityData.set(DATA_SEATED, tag.getBoolean("Seated"));
        if (tag.contains("Order")) {
            ItemStack.parse(this.registryAccess(), tag.getCompound("Order"))
                    .ifPresent(stack -> this.entityData.set(DATA_ORDER, stack));
        }
        this.entityData.set(DATA_REWARD, tag.getLong("Reward"));
    }
}
