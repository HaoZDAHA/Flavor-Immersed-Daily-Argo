package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

// 服务端数据包重载：把钓鱼手册与配件参数注册进服务端资源管理器，并在同步时下发给客户端。
//
// 手册与配件数据都在 data/ 下，客户端资源管理器读不到；玩法计算（水深 / 警惕性 /
// 进食速度 / 活动时段、配件系数）又需要这份数据，因此只在服务端加载一份，登录 / 重载时
// 通过 FishingDataSyncPacket 全量同步给客户端，保证多端内容一致。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class FishingDataEvents {

    private FishingDataEvents() {
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(FishingHandbookData.getInstance());
        event.addListener(FishingRodPartData.getInstance());
        event.addListener(WeighingTypeData.getInstance());
    }

// 玩家登录 / 执行 /reload 时，把服务端已加载的原始 JSON 全量下发给相关客户端
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        List<String> handbook = FishingHandbookData.getInstance().rawJson();
        List<String> parts = FishingRodPartData.getInstance().rawJson();
        List<String> weighing = WeighingTypeData.getInstance().rawJson();
        FishingDataSyncPacket packet = new FishingDataSyncPacket(handbook, parts, weighing);
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, packet));
    }
}
