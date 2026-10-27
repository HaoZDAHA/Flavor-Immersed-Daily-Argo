package com.flavor_immersed_daily_argo.butchering;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.util.Map;
import java.util.function.Supplier;

// 死动物的获取方式：手持 flavor_immersed_daily 的 斩骨刀攻击对应的活体动物，
// 动物会直接变成一具尸体（死动物物品），不掉原版战利品。
//
// 原理照搬父模组 FlavorImmersedDaily.onAttackEntity：在目标坐标生成一个
// ItemEntity、把目标 discard() 掉，并取消这次攻击。父模组自己处理
// 牛 / 羊 / 猪 / 鸡，这里负责本模组的五种禽类（鸭 / 鹅 / 鹌鹑 / 鸽子 / 火鸡）
// 以及狗 / 驴 / 骆驼 / 兔子 / 羊驼 / 鱿鱼。
//
// 斩骨刀按注册名在运行时查找，因此不依赖 flavor_immersed_daily 的编译期；没装该模组时
// 拿不到刀具，这条链路自然不生效。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class DeadAnimalEvents {

// 斩骨刀的注册名（来自 flavor_immersed_daily 模组）
    private static final ResourceLocation BONE_CUTTER_KNIFE =
            new ResourceLocation("flavor_immersed_daily", "bonecutterknife");

// 被击杀的动物 → 掉落的死动物物品。
//
// 延迟到首次用到时再构建：本类的静态初始化在事件总线扫描 @SubscribeEvent
// 时就可能触发，那时 ModEntities 的注册表还没绑定，直接 .get() 会抛异常。
    private static Map<EntityType<?>, Supplier<Item>> deadByAnimal;

    private static Map<EntityType<?>, Supplier<Item>> deadByAnimal() {
        if (deadByAnimal == null) {
            deadByAnimal = Map.ofEntries(
// 本模组的五种禽类
                    Map.entry(ModEntities.DUCK.get(), ModItems.DEAD_DUCK),
                    Map.entry(ModEntities.GOOSE.get(), ModItems.DEAD_GOOSE),
                    Map.entry(ModEntities.QUAIL.get(), ModItems.DEAD_QUAIL),
                    Map.entry(ModEntities.PIGEON.get(), ModItems.DEAD_PIGEON),
                    Map.entry(ModEntities.TURKEY.get(), ModItems.DEAD_TURKEY),
// 原版动物
                    Map.entry(EntityType.WOLF, ModItems.DEAD_DOG),
                    Map.entry(EntityType.DONKEY, ModItems.DEAD_DONKEY),
                    Map.entry(EntityType.CAMEL, ModItems.DEAD_CAMEL),
                    Map.entry(EntityType.RABBIT, ModItems.DEAD_RABBIT),
                    Map.entry(EntityType.LLAMA, ModItems.DEAD_LLAMA),
// 鱿鱼：一刀下去整只变成生鱿鱼，再放上砧板剁开
                    Map.entry(EntityType.SQUID, ModItems.RAW_SQUID));
        }
        return deadByAnimal;
    }

    private DeadAnimalEvents() {
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Supplier<Item> dead = deadByAnimal().get(event.getTarget().getType());
        if (dead == null) {
            return;
        }
        Player player = event.getEntity();
        Item knife = BuiltInRegistries.ITEM.getOptional(BONE_CUTTER_KNIFE).orElse(null);
        if (knife == null || !player.getMainHandItem().is(knife)) {
            return;
        }
        Entity target = event.getTarget();
        if (!target.level().isClientSide) {
            target.level().addFreshEntity(new ItemEntity(
                    target.level(),
                    target.getX(), target.getY(), target.getZ(),
                    new ItemStack(dead.get())));
            target.discard();
        }
        event.setCanceled(true);
    }
}
