package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.List;

// 经济玩法的管理指令（需要权限等级 2）。
//
// /fidaeco add <本模组货币> <数量> <其他模组物品> <数量> —— 增加一条外汇兑换；
// /fidaeco delete <本模组货币> <数量> <其他模组物品> <数量> —— 删除这条兑换；
// /fidaeco list —— 列出当前所有兑换条目。
//
// 条目最终写在配置 economy.toml 的 exchange.entries 里。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class EconomyCommands {

    private EconomyCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandBuildContext buildContext = event.getBuildContext();
        dispatcher.register(Commands.literal("fidaeco")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list").executes(EconomyCommands::list))
                .then(branch("add", buildContext, true))
                .then(branch("delete", buildContext, false)));
    }

// /fidaeco add|delete <本模组货币> <数量> <其他模组物品> <数量>
    private static LiteralArgumentBuilder<CommandSourceStack> branch(String name, CommandBuildContext buildContext,
                                                                    boolean add) {
        return Commands.literal(name)
                .then(Commands.argument("source", ItemArgument.item(buildContext))
                        .then(Commands.argument("sourceCount", IntegerArgumentType.integer(1))
                                .then(Commands.argument("target", ItemArgument.item(buildContext))
                                        .then(Commands.argument("targetCount", IntegerArgumentType.integer(1))
                                                .executes(context -> apply(context, add))))));
    }

    private static int apply(CommandContext<CommandSourceStack> context, boolean add) {
        ItemInput source = ItemArgument.getItem(context, "source");
        ItemInput target = ItemArgument.getItem(context, "target");
        int sourceCount = IntegerArgumentType.getInteger(context, "sourceCount");
        int targetCount = IntegerArgumentType.getInteger(context, "targetCount");
        ExchangeEntry entry = new ExchangeEntry(source.getItem(), sourceCount,
                target.getItem(), targetCount);

        boolean changed = add ? EconomyConfig.add(entry) : EconomyConfig.remove(entry);
        if (!changed) {
            context.getSource().sendFailure(Component.translatable(add
                    ? "message.flavor_immersed_daily_argo.economy.command.add_failed"
                    : "message.flavor_immersed_daily_argo.economy.command.remove_failed"));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable(add
                ? "message.flavor_immersed_daily_argo.economy.command.added"
                : "message.flavor_immersed_daily_argo.economy.command.removed",
                entry.serialize()), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        List<ExchangeEntry> entries = EconomyConfig.entries();
        if (entries.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "message.flavor_immersed_daily_argo.economy.command.empty"), false);
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable(
                "message.flavor_immersed_daily_argo.economy.command.list_header", entries.size()), false);
        for (ExchangeEntry entry : entries) {
            context.getSource().sendSuccess(() -> Component.literal("· " + entry.serialize()), false);
        }
        return entries.size();
    }
}
