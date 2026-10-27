package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.economy.product.EconomicProductLoader;
import com.flavor_immersed_daily_argo.economy.shop.ShopDataLoader;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;

// 服务端数据包重载：把系统店铺与经济产物数据注册进服务端资源管理器。
//
// 这些 JSON 在 data/ 下，客户端资源管理器读不到；只在服务端加载一份，再随登录包 /
// 同步包全量下发，保证多端内容一致。
//
// 物品标签是在资源重载「跑完监听器之后」才绑定的，监听器里读标签只会拿到空集，所以标签条目的展开要等
// TagsUpdatedEvent。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = EventBusSubscriber.Bus.FORGE)
public final class ShopDataEvents {

    private ShopDataEvents() {
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ShopDataLoader());
        event.addListener(new EconomicProductLoader());
    }

// 物品标签绑定完成后，用已绑定的物品注册表把两个数据包里的标签条目重新展开一次。
//
// 只处理 SERVER_DATA_LOAD（单人存档同样会触发）；多人客户端收到的是
// CLIENT_PACKET_RECEIVED，这里跳过，避免用客户端空数据覆盖服务端下发的店铺 / 新闻。
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            return;
        }
        ShopDataLoader.rebuild();
        EconomicProductLoader.rebuild();
    }
}
