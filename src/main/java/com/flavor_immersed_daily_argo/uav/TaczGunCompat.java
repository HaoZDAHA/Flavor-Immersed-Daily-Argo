package com.flavor_immersed_daily_argo.uav;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;

// 对 TaCZ 枪械的可选兼容入口。
//
// TaCZ 的枪既不是 net.minecraft.world.item.ProjectileItem，也不重写 use
// （它的 onEntitySwing 只是用来抑制挥臂的），开火只能走它自己的
// com.tacz.guns.api.entity.IGunOperator，真正干活的是 TaczGunBridge。
//
// 这个类本身不引用任何 TaCZ 的类：先确认 TaCZ 装着，再转交。没装 TaCZ 时
// TaczGunBridge 永远不会被加载，也就不会 NoClassDefFoundError。
public final class TaczGunCompat {

    private static final String MOD_ID = "tacz";
// TaCZ 的物品都在这个包下，用它判断比加载 TaCZ 的类安全
    private static final String PACKAGE_PREFIX = "com.tacz.";

    private static Boolean loaded;

    private TaczGunCompat() {
    }

    private static boolean loaded() {
        if (loaded == null) {
            loaded = ModList.get().isLoaded(MOD_ID);
        }
        return loaded;
    }

// TaCZ 装了没有。用来在碰 TaCZ 的类之前先挡一道
    public static boolean isLoaded() {
        return loaded();
    }

// 这件物品是不是 TaCZ 的枪：只看物品类所在的包
    public static boolean isGun(ItemStack stack) {
        return loaded() && stack.getItem().getClass().getName().startsWith(PACKAGE_PREFIX);
    }

// 让替身玩家开一枪。
//
// soundSource：枪声挂在谁身上。TaCZ 的枪声由客户端按「开枪的实体」播放，替身玩家客户端没有，
// 所以要传无人机
// 返回：是否真的打了出去
    public static boolean fire(FakePlayer shooter, ItemStack gun, Entity soundSource) {
        return loaded() && TaczGunBridge.fire(shooter, gun, soundSource);
    }
}
