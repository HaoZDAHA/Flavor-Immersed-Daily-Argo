package com.flavor_immersed_daily_argo.uav;

// 无人机开火时在客户端补一份枪声。
//
// TaCZ 的枪声由客户端按开枪实体播放，而无人机借用的替身玩家只存在于服务端，
// 那份声音会落空；这个类就是替代入口。1.20.1 下 TaCZ 的声音接口尚未接入，
// 这里先留空实现，收到 UavGunSoundPacket 时不会报错。
public final class TaczGunSoundClient {

    private TaczGunSoundClient() {
    }

    public static void play(UavGunSoundPacket packet) {
// TODO: 接入 TaCZ 枪械声音接口后，在这里按 packet 的位置与音量播放
    }
}
