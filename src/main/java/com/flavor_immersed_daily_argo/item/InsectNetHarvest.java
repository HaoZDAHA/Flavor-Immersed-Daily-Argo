package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.recipe.InsectNetDrop;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

// 捕虫网的实战用途：手持捕虫网破坏草（矮草、高草、蕨、大型蕨）时，
// 每种昆虫各自按 InsectNetDrop#DROP_CHANCE 独立判定一次，判定成功就掉一只。
//
// 挂在 BlockDropsEvent 上，所以草原来的战利品（如种子）照常保留，昆虫是额外附赠的。
// 掉落列表与 JEI / EMI 的「捕虫」分类同源，见 InsectNetDrop#drops()。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class InsectNetHarvest {

    private InsectNetHarvest() {
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)) {
            return;
        }
        if (!player.getMainHandItem().is(ModItems.INSECT_NET.get())) {
            return;
        }
        if (!isGrass(event.getState())) {
            return;
        }
        RandomSource random = player.getRandom();
        for (InsectNetDrop drop : InsectNetDrop.drops()) {
            if (random.nextFloat() < drop.chance()) {
                event.getDrops().add(new ItemEntity(event.getLevel(),
                        event.getPos().getX() + 0.5D,
                        event.getPos().getY() + 0.5D,
                        event.getPos().getZ() + 0.5D,
                        new ItemStack(drop.item())));
            }
        }
    }

// 可捕虫的草类方块
    private static boolean isGrass(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.SHORT_GRASS || block == Blocks.TALL_GRASS
                || block == Blocks.FERN || block == Blocks.LARGE_FERN;
    }
}
