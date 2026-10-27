package com.flavor_immersed_daily_argo.textile;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

// 用木盆给蚕茧缫丝
//
// 父模组的木盆（flavor_immersed_daily:woodbasin）本身有一套"装水 → 放入 → 等待 → 取出"
// 的洗涤流程，一次只吃一个物品。这里额外接管：只要盆里装着纯净水（watered=true），
// 手持蚕茧右键就能一次把整摞都缫成丝绸，并副产同样数量的生蚕蛹，不用一个个等。
//
// 木盆及其 watered 属性都按注册名 / 属性名在运行时查找，因此不依赖
// flavor_immersed_daily 的编译期；没装该模组时这条链路自然不生效。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class BasinSoakHandler {

// 木盆的注册名（来自 flavor_immersed_daily 模组）
    private static final ResourceLocation WOOD_BASIN =
            new ResourceLocation("flavor_immersed_daily", "woodbasin");

// 木盆"是否已经装了水"的属性名
    private static final String WATERED = "watered";

    private BasinSoakHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (!held.is(ModItems.SILKWORM_COCOON.get())) {
            return;
        }
        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        if (!isWateredBasin(state)) {
            return;
        }

// 抢先接管这次右键：不跑木盆自己那套逐个洗涤的逻辑
        event.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DENY);
        event.setUseItem(net.minecraftforge.eventbus.api.Event.Result.DENY);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (level.isClientSide) {
            return;
        }

        int count = held.getCount();
        Player player = event.getEntity();
        give(player, new ItemStack(ModItems.SILK.get(), count));
        give(player, new ItemStack(ModItems.RAW_SILKWORM_CHRYSALIS.get(), count));
        if (!player.getAbilities().instabuild) {
            held.shrink(count);
        }

// 这一批缫完，盆里的水也耗掉了
        Property<?> property = state.getBlock().getStateDefinition().getProperty(WATERED);
        if (property instanceof BooleanProperty watered) {
            level.setBlock(event.getPos(), state.setValue(watered, false), Block.UPDATE_ALL);
            level.playSound(null, event.getPos(), SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

// 是不是"装着水的木盆"
    private static boolean isWateredBasin(BlockState state) {
        Block block = state.getBlock();
        if (!block.builtInRegistryHolder().key().location().equals(WOOD_BASIN)) {
            return false;
        }
        Property<?> property = block.getStateDefinition().getProperty(WATERED);
        return property instanceof BooleanProperty watered && state.getValue(watered);
    }

// 优先塞进背包，塞不下就掉在脚边
    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
