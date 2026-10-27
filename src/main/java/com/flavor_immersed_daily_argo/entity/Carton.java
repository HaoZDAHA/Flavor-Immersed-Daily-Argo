package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.util.InventoryNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// 无人机投放的纸箱。
//
// 运输模式（模组槽装运输模块、使用槽装纸箱）下，无人机飞到自动范围的 XZ 中心后就甩下这么一只箱子，
// 把机舱里的货物全打包进去，然后自己返航（见 Uav#dropCarton）。箱子会自己慢慢落到地面，
// 落地前准星也点得到它。
//
// 收货有两种方式，都不退箱子（一次性）：
//
// 右键——把箱子里的东西塞进玩家背包，塞不下的掉在他脚边；
// 左键——把箱子砸开，里面的东西散落一地。
public class Carton extends Entity {

// 箱子里能塞几件东西：跟无人机的槽位数一样，整架无人机的货都装得下
    private static final int SLOT_COUNT = Uav.SLOT_COUNT;

// 下落速度（格 / tick）：慢悠悠地落，不是自由落体
    private static final double FALL_SPEED = 0.05D;

// 箱子里的东西
    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT);

// 已经踩到地面上没有。落地之后就不用再往下探了
    private boolean landed;

    public Carton(EntityType<? extends Carton> type, Level level) {
        super(type, level);
    }

// 往箱子里塞一件东西（无人机投放时逐格递进来）
    public void pack(ItemStack stack) {
        if (!stack.isEmpty()) {
            this.inventory.addItem(stack);
        }
    }

// 必须覆写：Entity#isPickable() 默认 false，不改成 true 准星选不中，
// 右键收货与左键砸开都传不到这里。
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

// 别让箭矢、投掷物把箱子当成靶子 —— Entity#canBeHitByProjectile() 默认是
// isAlive() && isPickable()，不拦住的话上面那个覆写会顺带把这一条也改成 true。
    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

// 慢慢往下落，踩到地面就停住
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || this.landed) {
            return;
        }
        double before = this.getY();
        this.move(MoverType.SELF, new Vec3(0.0D, -FALL_SPEED, 0.0D));
        if (this.getY() >= before - 1.0E-4D) {
            this.landed = true;
        }
    }

// 右键收货：东西进玩家背包，然后箱子当场散掉
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        for (int slot = 0; slot < this.inventory.getContainerSize(); slot++) {
            ItemStack stack = this.inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            this.inventory.setItem(slot, ItemStack.EMPTY);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, 1.2F);
        this.discard();
        return InteractionResult.SUCCESS;
    }

// 左键砸开：里面的东西散落一地，箱子碎掉（同样不可回收）
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) {
            return false;
        }
        for (int slot = 0; slot < this.inventory.getContainerSize(); slot++) {
            ItemStack stack = this.inventory.getItem(slot);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack);
                this.inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.7F, 1.3F);
        this.discard();
        return true;
    }

// 存读

    @Override
    protected void defineSynchedData() {
    }

// 箱子里装的东西跟无人机、木车用的是同一套「Slot + 物品」写法
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        InventoryNbt.load(this.inventory, tag, "Items");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("Items", InventoryNbt.save(this.inventory));
    }
}
