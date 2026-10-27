package com.flavor_immersed_daily_argo.textile;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

// 丝绸装备在 tooltip 里预览「最终模型」时携带的数据。
//
// 只是个纯数据壳子，真正的绘制在客户端的 ClientSilkArmorPreview 里做；拆开是为了让
// SilkArmorItem#getTooltipImage 所在的通用代码不碰到任何客户端类。
public record SilkArmorPreview(ItemStack stack) implements TooltipComponent {
}
