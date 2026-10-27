package com.flavor_immersed_daily_argo.economy;

import java.util.List;

// 客户端持有的外卖订单只读缓存，供 HUD 指南针读取。
//
// 服务端每秒把「手持导航手机」的玩家手上的订单全量推来（open = false），接单 / 打开手机时
// 那几包带 open = true。这里只负责覆盖这一份；open 时顺带弹出 / 刷新接单界面。
public final class NavPhoneClientData {

    private static volatile List<PhoneOrderView> ORDERS = List.of();
// 玩家在接单界面选中的「追踪」订单 id；0 表示没选，HUD 默认追最近的那个点
    private static volatile long TRACKED = 0L;

    private NavPhoneClientData() {
    }

// 收到服务端同步：重建缓存，需要时弹出 / 刷新接单界面
    public static void apply(List<PhoneOrderView> orders, boolean open) {
        ORDERS = List.copyOf(orders);
        if (open) {
            PhoneOrdersScreen.open(orders);
        }
    }

// 当前手上的订单（含接单 / 取货状态）
    public static List<PhoneOrderView> orders() {
        return ORDERS;
    }

// 选中的追踪订单 id；0 表示没选
    public static long trackedId() {
        return TRACKED;
    }

// 选中 / 取消选中一个追踪订单
    public static void setTracked(long orderId) {
        TRACKED = orderId;
    }
}
