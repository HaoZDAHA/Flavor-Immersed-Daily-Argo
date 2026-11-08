package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.flavor_immersed_daily_argo.economy.shop.Shop;
import com.flavor_immersed_daily_argo.economy.shop.ShopManager;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
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
// /fidaeco list —— 列出兑换条目
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
        dispatcher.register(Commands.literal("fidashop")
                .requires(source -> source.hasPermission(2) && source.getEntity() instanceof ServerPlayer)
                .then(Commands.literal("list").executes(EconomyCommands::listShops))
                .then(Commands.literal("create")
                        .executes(context -> createShop(context, ""))
                        .then(Commands.argument("title", StringArgumentType.greedyString())
                                .executes(context -> createShop(context, StringArgumentType.getString(context, "title")))))
                .then(Commands.literal("delete")
                        .then(Commands.argument("shopId", IntegerArgumentType.integer(1))
                                .suggests((context, builder) -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    List<Shop> all = ShopManager.get(player.server).allShops(player.server);
                                    return SharedSuggestionProvider.suggest(all.stream()
                                            .map(shop -> Integer.toString(shop.numberIn(all))), builder);
                                })
                                .executes(EconomyCommands::deleteShop))));
    }

// /fidashop list：列出全量店铺（系统 + 玩家），带编号与归属
    private static int listShops(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        List<Shop> all = ShopManager.get(player.server).allShops(player.server);
        context.getSource().sendSuccess(() -> Component.translatable(
                "message.flavor_immersed_daily_argo.shop.command.list", all.size()), false);
        for (Shop shop : all) {
            int number = shop.numberIn(all);
            String owner = shop.isSystem() ? "系统" : "玩家：" + shop.ownerName();
            context.getSource().sendSuccess(() -> Component.literal("· #" + number + " ")
                    .append(Component.translatable(shop.title()))
                    .append(Component.literal(" [" + owner + "]")), false);
        }
        return all.size();
    }

// /fidashop create <店铺名>：受 player_shops_enabled 与数量上限约束
    private static int createShop(CommandContext<CommandSourceStack> context, String title) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Shop created = ShopManager.get(player.server).createShop(player, title);
        if (created == null) {
            context.getSource().sendFailure(Component.translatable(
                    "message.flavor_immersed_daily_argo.shop.command.create_failed"));
            return 0;
        }
        ShopManager.syncAll(player.server);
        List<Shop> all = ShopManager.get(player.server).allShops(player.server);
        int number = created.numberIn(all);
        context.getSource().sendSuccess(() -> Component.translatable(
                "message.flavor_immersed_daily_argo.shop.command.created", number), false);
        return 1;
    }

// /fidashop delete <店铺编号>：普通模式只能删自己的；创造模式可删任意店铺与系统店铺
    private static int deleteShop(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int shopId = IntegerArgumentType.getInteger(context, "shopId");
        if (!ShopManager.get(player.server).deleteShopByNumber(player, shopId, player.isCreative())) {
            context.getSource().sendFailure(Component.translatable(
                    "message.flavor_immersed_daily_argo.shop.command.delete_failed"));
            return 0;
        }
        ShopManager.syncAll(player.server);
        context.getSource().sendSuccess(() -> Component.translatable(
                "message.flavor_immersed_daily_argo.shop.command.deleted", shopId), false);
        return 1;
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
