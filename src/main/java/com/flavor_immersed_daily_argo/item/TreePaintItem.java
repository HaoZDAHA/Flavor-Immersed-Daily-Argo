package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.forestry.ForestryNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

// 树漆：手持右键网点的树（原木），消除该区块上的蚜虫侵蚀。
public class TreePaintItem extends TooltipItem {

    public TreePaintItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !level.getBlockState(pos).is(BlockTags.LOGS)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && ForestryNetworkManager.paintTree(serverPlayer, pos)) {
            context.getItemInHand().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
