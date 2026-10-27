package com.flavor_immersed_daily_argo.forestry;

import java.util.ArrayList;
import java.util.List;

// 客户端持有的林业网点只读缓存。
//
// 服务端每次变更都会全量下发，客户端只负责覆盖这一份，供护林员写字板界面读取。
public final class ForestryClientData {

    private static volatile List<ForestryPoint> POINTS = List.of();

    private ForestryClientData() {
    }

// 收到服务端同步：重建本地缓存
    public static void apply(List<String> encoded) {
        List<ForestryPoint> parsed = new ArrayList<>(encoded.size());
        for (String text : encoded) {
            ForestryPoint point = ForestryPoint.decode(text);
            if (point != null) {
                parsed.add(point);
            }
        }
        POINTS = List.copyOf(parsed);
    }

// 全部网点（按维度与坐标排序，界面显示更稳定）
    public static List<ForestryPoint> points() {
        return POINTS;
    }
}
