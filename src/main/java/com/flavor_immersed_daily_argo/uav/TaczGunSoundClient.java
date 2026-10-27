package com.flavor_immersed_daily_argo.uav;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.sound.SoundManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

// 播无人机枪声的客户端部分。
//
// 这个类直接引用 TaCZ 的类，所以只会在 TaczGunCompat#isLoaded() 为真时被加载
// （见主类里的 UavGunSoundPacket 处理器），没装 TaCZ 时碰都不会碰到它。
//
// 做的事情跟 TaCZ 自己播第三人称枪声一模一样，只是把「射手」换成无人机这架实体：
// 由枪械展示资源里取出对应槽位的声音文件，再交给 TaCZ 的 SoundPlayManager 播。
public final class TaczGunSoundClient {

    private TaczGunSoundClient() {
    }

    public static void play(UavGunSoundPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity source = minecraft.level.getEntity(packet.entityId());
        if (source == null) {
            return;
        }
        TimelessAPI.getGunDisplay(packet.gunDisplayId(), packet.gunId()).ifPresent(display -> {
            ResourceLocation sound = display.getSounds(packet.soundName());
            if (sound == null) {
// 有的枪没做第三人称枪声，退回到第一人称那一份，总归要听见响
                sound = display.getSounds(SoundManager.SHOOT_SOUND);
            }
            if (sound != null) {
                SoundPlayManager.playClientSound(source, sound, packet.volume(), packet.pitch(), packet.distance());
            }
        });
    }
}
