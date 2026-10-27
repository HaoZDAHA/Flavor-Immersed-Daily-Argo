package com.flavor_immersed_daily_argo.herding;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

// 宠物窝的三处事件接线。
//
// 入场——猫狗一进世界就把游荡 / 猎鼠目标挂上（读档也会走这里，所以必须按类名去重）。
// 过世——住在宠物窝里的宠物死时留一份快照，交给窝次日复活。
// 交互——空手右键自己驯服的猫狗，在「跟随 / 蹲下 / 游荡」之间轮换。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class PetNestEvents {

// 这些字段不该带进复活快照：换新号、满血、别一醒来就坐着
    private static final String UUID_TAG = "UUID";
    private static final String HEALTH_TAG = "Health";
    private static final String SITTING_TAG = "Sitting";

    private PetNestEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof TamableAnimal pet)) {
            return;
        }
        if (!(pet instanceof Cat) && !(pet instanceof Wolf)) {
            return;
        }
        PetNestFolk.ensureGoals(pet);
        PetNestFolk.applyMode(pet, PetNestFolk.getMode(pet));
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof TamableAnimal pet) || !(pet.level() instanceof ServerLevel level)) {
            return;
        }
        if ((!(pet instanceof Cat) && !(pet instanceof Wolf)) || !pet.isTame()) {
            return;
        }
        BlockPos nestPos = PetNestFolk.nestPos(pet);
        if (nestPos == null || !PetNestFolk.isBound(pet, level)) {
            return;
        }
        if (!(level.getBlockEntity(nestPos) instanceof PetNestBlockEntity nest)) {
            return;
        }
        CompoundTag snapshot = new CompoundTag();
        if (!pet.save(snapshot)) {
            return;
        }
        snapshot.remove(UUID_TAG);
        snapshot.remove(HEALTH_TAG);
        snapshot.remove(SITTING_TAG);
        nest.onPetDied(snapshot);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof TamableAnimal pet)) {
            return;
        }
        if (!(pet instanceof Cat) && !(pet instanceof Wolf)) {
            return;
        }
        Player player = event.getEntity();
        if (!pet.isTame() || !pet.isOwnedBy(player) || !event.getItemStack().isEmpty()) {
// 手里拿着东西就照旧走原版交互：染料染项圈、喂食、给狗穿狼铠……
            return;
        }
// 两侧都取消：原版 setOrderedToSit(!isOrderedToSit()) 会被彻底绕开
        event.setCanceled(true);
        if (event.getLevel().isClientSide) {
            return;
        }
        PetNestFolk.PetMode mode = PetNestFolk.cycleMode(pet);
        player.displayClientMessage(
                Component.translatable(mode == null
                        ? "message.flavor_immersed_daily_argo.pet_nest.mode_need_nest"
                        : switch (mode) {
                            case FOLLOW -> "message.flavor_immersed_daily_argo.pet_nest.mode_follow";
                            case SIT -> "message.flavor_immersed_daily_argo.pet_nest.mode_sit";
                            case WANDER -> "message.flavor_immersed_daily_argo.pet_nest.mode_wander";
                        }),
                true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}