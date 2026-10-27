package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

// 空桶也能直接捞本模组的鱼。
//
// 原版 Bucketable.bucketMobPickup 只认水桶，所以这里补一条「空桶 + 鱼」的分支：
// 捞起后把空桶换成该鱼自己的桶装物品，动作与原版捞鱼一致（播放入桶音效、鱼消失）。
// 不要求鱼在水里——搁浅在岸上扑腾的鱼同样能一桶兜走。
// 只处理「桶装物品是本模组的」那些鱼，没有桶装形态的鱼保持原样。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class FishBucketEvents {

    private FishBucketEvents() {
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof AbstractFish fish) || !fish.isAlive()) {
            return;
        }
        if (!event.getItemStack().is(Items.BUCKET)) {
            return;
        }

        ItemStack bucket = fish.getBucketItemStack();
        if (bucket.isEmpty()
                || !flavor_immersed_daily_argo.MODID.equals(
                        BuiltInRegistries.ITEM.getKey(bucket.getItem()).getNamespace())) {
            return;
        }

        if (!event.getLevel().isClientSide) {
            Player player = event.getEntity();
            ItemStack held = event.getItemStack();

            fish.saveToBucketTag(bucket);
            fish.playSound(fish.getPickupSound(), 1.0F, 1.0F);
            held.shrink(1);
            if (held.isEmpty()) {
                player.setItemInHand(event.getHand(), bucket);
            } else if (!player.getInventory().add(bucket)) {
                player.drop(bucket, false);
            }
            fish.discard();
        }

// 两条侧都要取消：只取消事件还不够，后续的「使用物品」仍会落穿到手里的空桶上，
// 于是桶装物品的水被倒出来（原地生成一格水）。返回 SUCCESS 让双端都不再往下走。
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
