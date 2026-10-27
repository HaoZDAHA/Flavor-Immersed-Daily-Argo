package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.fishing.SteelyardItem;
import com.flavor_immersed_daily_argo.herding.AnimalStats;
import com.flavor_immersed_daily_argo.herding.AnimalStatsManager;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Optional;

// 编制麻袋：把任何动物（不分幼崽与成年）装进袋子，再次右键放出。
//
// 袋子装了什么存在 ModDataComponents#CAPTURED_ENTITY 里（整份实体 NBT），
// 因此放出来时自定义名称、驯服状态、血量等都会原样保留；客户端靠这份组件把贴图
// 切换成 poultry_catching_net_full。
public class PoultryCatchingNetItem extends Item {

// 放出时与玩家之间留出的水平距离
    private static final double RELEASE_DISTANCE = 1.5D;

    public PoultryCatchingNetItem(Properties properties) {
        super(properties);
    }

// 袋子里那只生物的数据；空袋返回 null
    @Nullable
    public static CompoundTag getStored(ItemStack stack) {
        return ModDataComponents.CAPTURED_ENTITY.get().get(stack);
    }

// 袋子是否已经装了东西
    public static boolean isFull(ItemStack stack) {
        return getStored(stack) != null;
    }

// 能不能装：所有动物类实体，不分幼崽与成年
    public static boolean canCapture(LivingEntity entity) {
        return entity instanceof Animal;
    }

// 把生物存成 NBT。
//
// saveWithoutId 不写 id（那是 saveAsPassenger 才做的），
// 少了它就认不出类型、放不出来，所以这里补上。
    private static CompoundTag saveEntity(LivingEntity entity) {
        CompoundTag data = new CompoundTag();
        entity.saveWithoutId(data);
        data.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
        return data;
    }

// 把袋中那只动物的体重（kg）记到麻袋上。
//
// 只有登记过体重标准的物种才写：已养成状态栏的按它的实际体重，还没养过的按标准体重。
// 猫狗这类没有体重标准的，袋子上不留体重组件。
    private static void saveWeight(ItemStack sack, LivingEntity entity) {
        Float standard = AnimalStatsManager.weightStandard(entity.getType());
        if (standard == null) {
            ModDataComponents.CAPTURED_WEIGHT.get().remove(sack);
            return;
        }
        AnimalStats stats = AnimalStatsManager.getStats(entity);
        float weight = stats != null && stats.weight() > 0.0F ? stats.weight() : standard;
        ModDataComponents.CAPTURED_WEIGHT.get().set(sack, (double) weight);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (isFull(stack) || !canCapture(target)) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide) {
// 注意：创造模式下 Player#interactOn 传进来的是手持物品的「副本」，改 stack 只会改到副本上，
// 玩家手里那把麻袋依然是空的；所以这里必须改真正的手持物品。
            ItemStack held = player.getItemInHand(hand);
            ModDataComponents.CAPTURED_ENTITY.get().set(held, saveEntity(target));
            saveWeight(held, target);
            target.discard();
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag data = getStored(stack);
        if (data == null) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            Optional<Entity> spawned = EntityType.create(data, level);
            if (spawned.isPresent()) {
                Entity entity = spawned.get();
                Vec3 look = player.getLookAngle();
                entity.moveTo(player.getX() + look.x * RELEASE_DISTANCE,
                        player.getY(),
                        player.getZ() + look.z * RELEASE_DISTANCE,
                        player.getYRot() + 180.0F, 0.0F);
                if (entity instanceof Mob mob) {
                    mob.setPersistenceRequired();
                }
                level.addFreshEntity(entity);
                ModDataComponents.CAPTURED_ENTITY.get().remove(stack);
                ModDataComponents.CAPTURED_WEIGHT.get().remove(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.flavor_immersed_daily_argo.poultry_catching_net"));
        CompoundTag data = getStored(stack);
        if (data != null) {
            EntityType.byString(data.getString("id")).ifPresent(type ->
                    tooltipComponents.add(Component.translatable(
                            "tooltip.flavor_immersed_daily_argo.poultry_catching_net.stored",
                            Component.translatable(type.getDescriptionId()))));
            Double weight = ModDataComponents.CAPTURED_WEIGHT.get().get(stack);
            if (weight != null) {
                tooltipComponents.add(Component.translatable(
                        "tooltip.flavor_immersed_daily_argo.poultry_catching_net.weight",
                        SteelyardItem.format(weight)));
            }
        }
    }
}
