package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.client.ClientScreens;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// 钓鱼手册：右键打开书与笔风格的钓获图鉴（条目由 data 包下 fishing_handbook 目录的 JSON 驱动）
public class FishingHandbookItem extends Item {

    public FishingHandbookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            ClientScreens.openFishingHandbook();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
