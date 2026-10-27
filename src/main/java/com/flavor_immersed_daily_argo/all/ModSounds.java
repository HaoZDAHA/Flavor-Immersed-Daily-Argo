package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 音效注册
//
// 音频文件放在 assets/<命名空间>/sounds/ 下，事件名与文件名一致时，
// sounds.json 里写一条即可（见 assets/flavor_immersed_daily_argo/sounds.json）。
public final class ModSounds {

    private ModSounds() {
    }

    public static final DeferredRegister<SoundEvent> REGISTRY =
            DeferredRegister.create(Registries.SOUND_EVENT, flavor_immersed_daily_argo.MODID);

// 收割机撞上 / 砍到生物时的音效（见 entity/Harvester 的前方攻击）
    public static final DeferredHolder<SoundEvent, SoundEvent> HARVESTER_ATTACK =
            REGISTRY.register("harvester_attack", () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(flavor_immersed_daily_argo.MODID, "harvester_attack")));

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
