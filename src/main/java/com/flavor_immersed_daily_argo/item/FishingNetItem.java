package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 鱼抄网：手动把活鱼抄进网兜里，再倒进鱼护或者放回水里。
//
// 网兜最多装 #CAPACITY 条活鱼，每条都是一份完整的实体 NBT（存在
// ModDataComponents#CAPTURED_ENTITIES 里），所以放出来时自定义名称、状态都原样保留。
// 钓上来的鱼不会自动进网：有鱼护就进鱼护，没有则以实体飞到玩家身边，需要玩家自己拿网右键捞。
// 主手副手拿着都能对着鱼右键把它抄进来；装着鱼时对鱼护右键可以倒进去，对着方块右键则放出来。
public class FishingNetItem extends Item {

// 网兜最多装几条鱼
    public static final int CAPACITY = 3;

// 放出时鱼与玩家之间留出的水平距离
    private static final double RELEASE_DISTANCE = 1.5D;

    public FishingNetItem(Properties properties) {
        super(properties);
    }

// 网里的鱼；空网返回空表
    public static List<CompoundTag> getStored(ItemStack stack) {
        List<CompoundTag> stored = stack.get(ModDataComponents.CAPTURED_ENTITIES.get());
        return stored == null ? List.of() : stored;
    }

// 网里有没有鱼
    public static boolean isEmpty(ItemStack stack) {
        return getStored(stack).isEmpty();
    }

// 网兜是否已经装满了
    public static boolean isFull(ItemStack stack) {
        return getStored(stack).size() >= CAPACITY;
    }

// 网兜里现有几条鱼
    public static int count(ItemStack stack) {
        return getStored(stack).size();
    }

// 把一条活鱼抄进网里；网兜满了就装不下，返回 false（此时不动这条鱼）。
    public static boolean store(ItemStack stack, Entity entity) {
        if (stack.isEmpty() || isFull(stack)) {
            return false;
        }
        List<CompoundTag> stored = new ArrayList<>(getStored(stack));
        stored.add(snapshot(entity));
        stack.set(ModDataComponents.CAPTURED_ENTITIES.get(), stored);
        entity.discard();
        return true;
    }

// 从网里拿走最先兜住的 amount 条鱼（倒进鱼护后用）
    public static void removeFirst(ItemStack stack, int amount) {
        List<CompoundTag> stored = new ArrayList<>(getStored(stack));
        int removed = 0;
        while (removed < amount && !stored.isEmpty()) {
            stored.remove(0);
            removed++;
        }
        if (stored.isEmpty()) {
            stack.remove(ModDataComponents.CAPTURED_ENTITIES.get());
        } else {
            stack.set(ModDataComponents.CAPTURED_ENTITIES.get(), stored);
        }
    }

// 能抄进网里的只有鱼
    public static boolean canCapture(Entity entity) {
        return entity instanceof AbstractFish;
    }

// 实体快照：saveWithoutId 不写 id，这里补上才能重建
    private static CompoundTag snapshot(Entity entity) {
        CompoundTag data = new CompoundTag();
        entity.saveWithoutId(data);
        data.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
        return data;
    }

// 对着鱼右键把它抄进网里（主手副手都行）
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (!canCapture(target)) {
            return InteractionResult.PASS;
        }
// 创造模式下传进来的是手持物品的副本，改 stack 改不到玩家手里那把，所以统一改真正的手持物品
        ItemStack held = player.getItemInHand(hand);
        if (isFull(held)) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide) {
            store(held, target);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

// 对着方块右键：把网里的一条鱼放在点击的那一面
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (isEmpty(stack)) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            Vec3 spawn = Vec3.atCenterOf(context.getClickedPos().relative(context.getClickedFace()));
            releaseOne(level, player, stack, spawn);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 右键空气：把网里的一条鱼放到玩家面前
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isEmpty(stack)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            Vec3 look = player.getLookAngle();
            Vec3 spawn = new Vec3(player.getX() + look.x * RELEASE_DISTANCE,
                    player.getY(),
                    player.getZ() + look.z * RELEASE_DISTANCE);
            releaseOne(level, player, stack, spawn);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

// 把网里所有鱼都放到指定位置（倒进鱼护用），返回放出的条数
    public static int releaseAll(Level level, Player player, ItemStack stack, Vec3 pos) {
        int released = 0;
        while (!isEmpty(stack)) {
            releaseOne(level, player, stack, pos);
            released++;
        }
        return released;
    }

// 放出网里最先兜住的那条鱼
    private static void releaseOne(Level level, Player player, ItemStack stack, Vec3 pos) {
        List<CompoundTag> stored = getStored(stack);
        if (stored.isEmpty()) {
            return;
        }
        Optional<Entity> spawned = EntityType.create(stored.get(0), level);
        if (spawned.isEmpty()) {
// 认不出的实体（比如卸载了对应模组）：直接丢掉这一条，别一直堵在网里
            removeFirst(stack, 1);
            return;
        }
        Entity entity = spawned.get();
        entity.moveTo(pos.x, pos.y, pos.z, player.getYRot() + 180.0F, 0.0F);
// 放出来的鱼不该因为玩家走远被刷掉
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        level.addFreshEntity(entity);
        removeFirst(stack, 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.flavor_immersed_daily_argo.fishing_net"));
        if (!isEmpty(stack)) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.flavor_immersed_daily_argo.fishing_net.stored", count(stack)));
        }
    }
}
