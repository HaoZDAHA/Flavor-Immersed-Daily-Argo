package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

// 林业网点的调试指令：手动给网点触发一次随机情况，方便验证各类事件与灾害的处理流程。
//
// /forestry fire [pos] —— 森林火灾；
// /forestry aphids [pos] —— 蚜虫侵蚀；
// /forestry weeds [pos] —— 杂草丛生；
// /forestry flood [pos] / drought [pos] —— 洪水 / 干涸；
// /forestry hypoxia [pos] / oxygen_over [pos] —— 水体缺氧 / 水氧过饱；
// /forestry acid_rain [pos] / strong_alkali [pos] —— 酸雨 / 水体强碱；
// /forestry barren_water [pos] / stench [pos] —— 水体贫瘠 / 水体发臭；
// /forestry osmosis [pos] —— 渗透压失衡。
//
// pos 省略时取离执行者最近的一处网点。网点按玩家保存，因此只有玩家能执行。
// 水质灾害走「上报」通道：把对应指标压到灾难区间并写进写字板的水体情况。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class ForestryCommands {

    private ForestryCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("forestry")
                .requires(source -> source.hasPermission(2))
                .then(eventBranch("fire", ForestryEventType.FIRE))
                .then(eventBranch("aphids", ForestryEventType.APHIDS))
                .then(eventBranch("weeds", ForestryEventType.WEEDS))
                .then(eventBranch("flood", ForestryEventType.FLOOD))
                .then(eventBranch("drought", ForestryEventType.DROUGHT))
                .then(eventBranch("hypoxia", ForestryEventType.HYPOXIA))
                .then(eventBranch("oxygen_over", ForestryEventType.OXYGEN_OVER))
                .then(eventBranch("acid_rain", ForestryEventType.ACID_RAIN))
                .then(eventBranch("strong_alkali", ForestryEventType.STRONG_ALKALI))
                .then(eventBranch("barren_water", ForestryEventType.BARREN_WATER))
                .then(eventBranch("stench", ForestryEventType.STENCH))
                .then(eventBranch("osmosis", ForestryEventType.OSMOSIS));
        dispatcher.register(root);
    }

// 一条事件子指令：无参时作用于最近的网点，带 pos 时作用于该坐标的网点
    private static LiteralArgumentBuilder<CommandSourceStack> eventBranch(String name, ForestryEventType type) {
        return Commands.literal(name)
                .executes(context -> trigger(context, type, null))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> trigger(context, type, BlockPosArgument.getBlockPos(context, "pos"))));
    }

    private static int trigger(CommandContext<CommandSourceStack> context, ForestryEventType type, BlockPos pos) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("message.flavor_immersed_daily_argo.forestry.command.player_only"));
            return 0;
        }
        if (!ForestryNetworkManager.triggerEvent(player, pos, type)) {
            source.sendFailure(Component.translatable("message.flavor_immersed_daily_argo.forestry.command.no_point"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("message.flavor_immersed_daily_argo.forestry.command.triggered",
                Component.translatable(eventKey(type))), true);
        return 1;
    }

    private static String eventKey(ForestryEventType type) {
        return type.translationKey();
    }
}
