package com.flavor_immersed_daily_argo.item;

// 测距绳：手持时在客户端绘制当前所在区块的边界线框，方便确认网点范围。
//
// 本身没有任何右键行为，渲染逻辑见 forestry.RangingRopeRenderer。
public class RangingRopeItem extends TooltipItem {

    public RangingRopeItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }
}
