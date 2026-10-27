package com.flavor_immersed_daily_argo.herding;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

// 鸡窝认蛋用的工具类
//
// “蛋”的判定与“蛋 → 禽类”的对应关系都不写死，而是按注册名推导：
// 物品注册名去掉 _egg 后缀就是同名实体（duck_egg → duck），
// 原版鸡蛋单独对应原版鸡。这样附属模组往自己命名空间加禽类和对应的蛋，
// 不用改这里的代码就能被鸡窝识别。
//
// 刷怪蛋（SpawnEggItem 或其 _spawn_egg 注册名）不算蛋，
// 否则会被当成可孵化的蛋塞进孵化槽。
public final class CoopEggs {

// 蛋类物品注册名的后缀
    private static final String EGG_SUFFIX = "_egg";

    private CoopEggs() {
    }

// 是否是鸡窝认可的蛋：原版鸡蛋，或注册名以 _egg 结尾的物品
    public static boolean isEgg(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof SpawnEggItem) {
            return false;
        }
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        return path.equals("egg") || path.endsWith(EGG_SUFFIX);
    }

// 蛋对应的禽类实体；认不出来（没有同名实体）时返回 null。
//
// 返回 null 既表示“不是蛋”，也表示“这个蛋没有对应的禽类”，
// 调用方按“不能孵化”处理即可。
    @Nullable
    public static EntityType<?> entityFor(ItemStack stack) {
        if (!isEgg(stack)) {
            return null;
        }
        Item item = stack.getItem();
        if (item == Items.EGG) {
            return EntityType.CHICKEN;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();
        if (!path.endsWith(EGG_SUFFIX)) {
            return null;
        }
        String entityName = path.substring(0, path.length() - EGG_SUFFIX.length());
        return BuiltInRegistries.ENTITY_TYPE.get(
                ResourceLocation.fromNamespaceAndPath(id.getNamespace(), entityName));
    }
}
