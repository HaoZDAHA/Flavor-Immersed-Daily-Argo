package com.flavor_immersed_daily_argo.compat;

import com.flavor_immersed_daily_argo.all.ModItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.lang.reflect.Method;
import java.util.function.Consumer;

// Thirst Was Taken（口渴）兼容。
//
// 口渴模组用关键字猜物品能不能补水，柚子 / 番石榴这类名字它认不出来，
// 所以这里在它开放的事件里直接登记：果汁按饮品、水果按食物、汤按食物。
//
// 不硬依赖它：没装 thirst 时整个类不会被加载，事件类和方法都用反射拿，
// 这样其他版本的口渴模组即使 API 有变也只会打一条警告，不会拖垮模组加载。
public final class ThirstCompat {

    private static final String THIRST_MOD_ID = "thirst";
    private static final String REGISTER_EVENT =
            "dev.ghen.thirst.foundation.common.event.RegisterThirstValueEvent";

// 果汁：对照 Farmer's Delight 的西瓜汁（8 / 13）
    private static final int JUICE_THIRST = 8;
    private static final int JUICE_QUENCHED = 13;
// 水果：对照原版苹果（2 / 3）
    private static final int FRUIT_THIRST = 2;
    private static final int FRUIT_QUENCHED = 3;
// 汤：对照 Farmer's Delight 的鸡汤（4 / 5）
    private static final int SOUP_THIRST = 4;
    private static final int SOUP_QUENCHED = 5;

    private static final Logger LOGGER = LogUtils.getLogger();

    private ThirstCompat() {
    }

    public static void register() {
        if (!ModList.get().isLoaded(THIRST_MOD_ID)) {
            return;
        }
        try {
            Class<?> eventType = Class.forName(REGISTER_EVENT);
            Method addDrink = eventType.getMethod("addDrink", Item.class, int.class, int.class);
            Method addFood = eventType.getMethod("addFood", Item.class, int.class, int.class);

            Consumer<Object> listener = event -> {
                try {
                    registerDrinks(event, addDrink);
                    registerFoods(event, addFood);
                } catch (ReflectiveOperationException exception) {
                    LOGGER.warn("[FIDA] 口渴模组兼容登记失败", exception);
                }
            };

            IEventBus.class
                    .getMethod("addListener", EventPriority.class, boolean.class, Class.class, Consumer.class)
                    .invoke(MinecraftForge.EVENT_BUS, EventPriority.NORMAL, false, eventType, listener);
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.warn("[FIDA] 口渴模组事件不可用，跳过饮品 / 水果 / 汤的补水兼容", exception);
        }
    }

    private static void registerDrinks(Object event, Method addDrink) throws ReflectiveOperationException {
        addDrink.invoke(event, ModItems.POMELO_JUICE.get(), JUICE_THIRST, JUICE_QUENCHED);
        addDrink.invoke(event, ModItems.GUAVA_JUICE.get(), JUICE_THIRST, JUICE_QUENCHED);
    }

    private static void registerFoods(Object event, Method addFood) throws ReflectiveOperationException {
        addFood.invoke(event, ModItems.POMELO.get(), FRUIT_THIRST, FRUIT_QUENCHED);
        addFood.invoke(event, ModItems.GUAVA.get(), FRUIT_THIRST, FRUIT_QUENCHED);

        addFood.invoke(event, ModItems.DUCK_BLOOD_VERMICELLI_SOUP.get(), SOUP_THIRST, SOUP_QUENCHED);
        addFood.invoke(event, ModItems.PIGEON_SOUP.get(), SOUP_THIRST, SOUP_QUENCHED);
        addFood.invoke(event, ModItems.TURTLE_PIGEON_SOUP.get(), SOUP_THIRST, SOUP_QUENCHED);
        addFood.invoke(event, ModItems.CLAM_CHIVE_DOUGH_DROP_SOUP.get(), SOUP_THIRST, SOUP_QUENCHED);
        addFood.invoke(event, ModItems.SEA_URCHIN_LUFFA_SOUP.get(), SOUP_THIRST, SOUP_QUENCHED);
        addFood.invoke(event, ModItems.FISH_MAW_TOFU_SOUP.get(), SOUP_THIRST, SOUP_QUENCHED);
    }
}
