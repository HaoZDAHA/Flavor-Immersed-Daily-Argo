package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.herding.PetNestFolk;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

// 哨子：吹响后，附近游荡中的宠物会转为跟随，邻近的坐骑也会循声向你走来。
//
// 宠物只认「游荡模式」的那些（也就是已在宠物窝安家的猫狗），切回跟随由
// PetNestFolk#setMode 一并换好 AI；坐骑则认已驯服的 AbstractHorse
// （马 / 驴 / 骡 / 骆驼 / 羊驼），让它们循路走回玩家身边。
public class WhistleItem extends TooltipItem {

// 一次哨声的作用半径（约 3 个区块）
    private static final double RADIUS = 48.0D;
// 坐骑走向玩家的寻路速度
    private static final double MOUNT_SPEED = 1.0D;
// 吹一次的冷却（tick）
    private static final int COOLDOWN = 40;

    public WhistleItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            call(serverPlayer);
            player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

// 召唤附近游荡中的宠物与坐骑
    private static void call(ServerPlayer player) {
        Level level = player.level();
        AABB box = player.getBoundingBox().inflate(RADIUS);
        level.playSound(null, player.blockPosition(),
                SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 1.0F, 1.6F);

        int pets = 0;
        for (TamableAnimal pet : level.getEntitiesOfClass(TamableAnimal.class, box)) {
            if (PetNestFolk.getMode(pet) == PetNestFolk.PetMode.WANDER) {
                PetNestFolk.setMode(pet, PetNestFolk.PetMode.FOLLOW);
                pets++;
            }
        }

        int mounts = 0;
        for (AbstractHorse horse : level.getEntitiesOfClass(AbstractHorse.class, box)) {
            if (horse.isTamed()) {
                horse.getNavigation().moveTo(player, MOUNT_SPEED);
                mounts++;
            }
        }

        if (pets + mounts > 0) {
            player.displayClientMessage(Component.translatable(
                    "message.flavor_immersed_daily_argo.whistle.called", pets, mounts), true);
        } else {
            player.displayClientMessage(Component.translatable(
                    "message.flavor_immersed_daily_argo.whistle.none"), true);
        }
    }
}
