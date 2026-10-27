package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.client.ClientScreens;
import com.flavor_immersed_daily_argo.forestry.ForestryNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

// 护林员写字板。
//
// 右键一株树的原木即标记一处林业网点（潜行右键为解除）；右键空气则打开网点界面，
// 界面里可以查看各网点坐标与评分、并解除网点。
public class RangerClipboardItem extends TooltipItem {

    public RangerClipboardItem(Properties properties, String tooltipKey) {
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
        if (player instanceof ServerPlayer serverPlayer) {
            ForestryNetworkManager.useOnLog(serverPlayer, pos, player.isShiftKeyDown());
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            ClientScreens.openForestryHandbook();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
