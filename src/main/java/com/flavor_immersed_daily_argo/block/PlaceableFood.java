package com.flavor_immersed_daily_argo.block;

import net.minecraft.network.chat.Component;

import java.util.List;

// 可放置食物方块的统一标记。
//
// 这些方块的物品摆下之后就是一份能分几口吃的菜（生鲜海产、整鸭菜、各种水产菜），
// 所以方块物品的 tooltip 末尾统一追加一行灰色的“可放置”提示。
//
// 方块本身的 appendHoverText 只在“方块物品”上生效（原版 BlockItem
// 会把 tooltip 转发给方块），摆在世界里的方块不会显示这行字。实现方在自己的
// appendHoverText 里调用 #addPlaceableTooltip 即可。
public interface PlaceableFood {

// 所有可放置食物共用的灰色提示行
    String PLACEABLE_TOOLTIP = "tooltip.flavor_immersed_daily_argo.placeable";

// 在 tooltip 末尾追加“可放置”提示行
    default void addPlaceableTooltip(List<Component> tooltip) {
        tooltip.add(Component.translatable(PLACEABLE_TOOLTIP));
    }
}
