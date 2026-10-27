package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.herding.AnimalStats;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// 数据附件注册表：挂在实体上的自定义数据。
public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> REGISTRY =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
                    com.flavor_immersed_daily_argo.flavor_immersed_daily_argo.MODID);

// 家养动物的四项状态栏；同步到客户端，便于界面与 HUD 直接读取
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AnimalStats>> ANIMAL_STATS =
            REGISTRY.register("animal_stats", () -> AttachmentType.builder(() -> AnimalStats.NONE)
// 只有真正拥有状态栏的动物才写进存档
                    .serialize(AnimalStats.CODEC, AnimalStats::active)
                    .sync(AnimalStats.STREAM_CODEC)
                    .build());

// 「是在拥有状态栏的同类身边长大的」标记。
//
// 幼崽阶段被打上标记，等它长大成人时再补上状态栏 —— 这样只要中途被带走，
// 就不会凭空获得状态栏。
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> RAISED_NEARBY =
            REGISTRY.register("raised_nearby", () -> AttachmentType.builder(() -> Boolean.FALSE)
                    .serialize(Codec.BOOL, Boolean::booleanValue)
                    .build());

// 妊娠中的动物肚子里怀的是哪种生物的崽（存实体注册名字符串）。
//
// 被「筒装生物遗传液」扎中时记下，等妊娠 buff 到期时据此生下对应的幼崽
// （见 herding/PregnancyHandler）。纯服务端逻辑，不需要同步。
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> PREGNANCY_DNA =
            REGISTRY.register("pregnancy_dna", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .build());

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
