package com.flavor_immersed_daily_argo.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

// 灌溉水车物品
//
// 与普通方块物品只差一点：瞄准水面右键时，把水车放到水面上方一格，而不是把那一格水替换掉，
// 这样水车才是“立在水面上”，而不是“泡在水里”。
public class WaterwheelItem extends TooltipBlockItem {

    public WaterwheelItem(Block block, Properties properties, String tooltipKey) {
        super(block, properties, tooltipKey);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
// 准星射线会穿过水（水没有碰撞箱），所以 UseOnContext 的点击位置其实是水底那块方块；
// 用 BlockPlaceContext 才能算到“真正会被替换的那一格”——也就是那格水本身。
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        BlockPos placePos = placeContext.getClickedPos();
        if (context.getLevel().getFluidState(placePos).is(FluidTags.WATER)) {
// 抬到水面上方一格再走原版放置流程：那一格是空气，水车就落在水面上
            return super.useOn(BlockPlaceContext.at(placeContext, placePos.above(), Direction.UP));
        }
        return super.useOn(context);
    }
}
