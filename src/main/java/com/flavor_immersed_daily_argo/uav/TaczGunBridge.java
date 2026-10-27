package com.flavor_immersed_daily_argo.uav;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.common.GunConfig;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.sound.SoundManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;

// 真正调用 TaCZ 接口的地方，只有 TaczGunCompat 确认 TaCZ 装着之后才会被它加载。
//
// 替身玩家（FakePlayer）不会走 TaCZ 的 tick，所以这里补上两件它自己做不了的事：
// 别把射速计时抹掉，以及替栓动枪上膛。
final class TaczGunBridge {

// 替身玩家不播切枪动画，draw 完把切枪冷却推到过去，第一发才打得出去
    private static final long SKIP_DRAW_COOLDOWN_MS = 60_000L;

// 枪声音量与音调，取值照抄 TaCZ 播第三人称枪声那一处
    private static final float FIRE_SOUND_VOLUME = 0.8F;
    private static final float FIRE_SOUND_PITCH = 0.9F;
    private static final float FIRE_SOUND_PITCH_JITTER = 0.125F;

    private TaczGunBridge() {
    }

// 替身玩家手里的枪打一发。
//
// draw 只在换枪时调一次，这不是图省事而是必须：TaCZ 的 draw 一进来就
// 调 ShooterDataHolder#initialData()，会把 shootTimestamp 抹成 -1，而射速
// 恰恰靠这个时间戳算（LivingEntityShoot#getShootCoolDown）。每发都 draw，就等于把
// 射速限制关掉，连点按鼠标速度出弹。
//
// soundSource：枪声挂在谁身上。TaCZ 的枪声由客户端按「开枪的实体」去找人再播，替身玩家
// 客户端上没有，所以传的是无人机
    static boolean fire(FakePlayer shooter, ItemStack gun, Entity soundSource) {
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return false;
        }
        IGunOperator operator = IGunOperator.fromLivingEntity(shooter);
        ShooterDataHolder data = operator.getDataHolder();

        if (isOtherGun(data, iGun, gun)) {
            operator.draw(() -> shooter.getMainHandItem());
            data.drawTimestamp = System.currentTimeMillis() - SKIP_DRAW_COOLDOWN_MS;
        }

        ShootResult result = operator.shoot(shooter::getXRot, shooter::getYRot);
        if (result == ShootResult.NEED_BOLT) {
// 拉栓靠 tick 推进，替身玩家没有 tick，直接把弹补进膛里，栓动枪才不至于卡死
            iGun.setBulletInBarrel(gun, true);
            return false;
        }
        if (result != ShootResult.SUCCESS) {
// COOL_DOWN 就是枪械自己的射速在拦，照实当作没打出去
            return false;
        }
        PacketDistributor.sendToPlayersTrackingEntity(soundSource, gunSound(soundSource, iGun, gun));
        return true;
    }

// 枪声包：TaCZ 自己那套「按射手实体播声」在无人机身上落空（替身玩家客户端上不存在），
// 改由无人机当声源、客户端自己取这杆枪的声音资源来放（见 TaczGunSoundClient）。
    private static UavGunSoundPacket gunSound(Entity source, IGun iGun, ItemStack gun) {
        return new UavGunSoundPacket(
                source.getId(),
                iGun.getGunId(gun),
                iGun.getGunDisplayId(gun),
                SoundManager.SHOOT_3P_SOUND,
                FIRE_SOUND_VOLUME,
                FIRE_SOUND_PITCH + source.getRandom().nextFloat() * FIRE_SOUND_PITCH_JITTER,
                GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get());
    }

// 替身玩家记着的那把枪跟手里这杆不是同一杆（它还没抽过枪时也算）
    private static boolean isOtherGun(ShooterDataHolder data, IGun iGun, ItemStack gun) {
        if (data.currentGunItem == null) {
            return true;
        }
        ItemStack current = data.currentGunItem.get();
        IGun currentGun = IGun.getIGunOrNull(current);
        return currentGun == null || !currentGun.getGunId(current).equals(iGun.getGunId(gun));
    }
}
