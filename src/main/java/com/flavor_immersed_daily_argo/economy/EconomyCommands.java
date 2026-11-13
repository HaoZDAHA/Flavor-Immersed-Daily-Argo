package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.economy.shop.Shop;
import com.flavor_immersed_daily_argo.economy.shop.ShopManager;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;

// 经济玩法的管理指令（需要权限等级 2）。
//
// /fidaeco add <本模组货币> <数量> <其他模组物品> <数量> —— 增加一条外汇兑换；
// /fidaeco delete <本模组货币> <数量> <其他模组物品> <数量> —— 删除这条兑换；
// /fidaeco list —— 列出当前所有兑换条目；
// /fidashop list —— 列出自己名下的玩家店铺；
// /fidashop create <店铺名> —— 创建一家玩家店铺；
// /fidashop delete <店铺编号> —— 删除店铺（创造模式可删任意玩家名下的）。
//
// 兑换条目最终写在配置 economy.toml 的 exchange.entries 里；玩家店铺写在存档里。
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
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list").executes(EconomyCommands::listShops))
                .then(Commands.literal("create")
                        .then(Commands.argument("title", StringArgumentType.greedyString())
                                .executes(EconomyCommands::createShop)))
                .then(Commands.literal("delete")
                        .then(Commands.argument("shopId", IntegerArgumentType.integer(1))
                                .executes(EconomyCommands::deleteShop))));
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

// /fidashop list：列出全部店铺（系统店铺也计入编号）
    private static int listShops(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return playersOnly(context);
        }
        List<Shop> all = ShopManager.get(player.server).allShops(player.server);
        if (all.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "message.flavor_immersed_daily_argo.economy.command.shop_empty"), false);
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable(
                "message.flavor_immersed_daily_argo.economy.command.shop_list_header", all.size()), false);
        for (int i = 0; i < all.size(); i++) {
            Shop shop = all.get(i);
            int number = shop.numberIn(all);
            Component owner = shop.isSystem()
                    ? Component.translatable("message.flavor_immersed_daily_argo.economy.command.owner_system")
                    : Component.translatable("message.flavor_immersed_daily_argo.economy.command.owner_player", shop.ownerName());
            context.getSource().sendSuccess(() -> Component.literal("· #" + number + " ")
                    .append(shop.titleComponent())
                    .append(Component.literal(" [")).append(owner).append(Component.literal("]")), false);
        }
        return all.size();
    }

// /fidashop create <店铺名>：受 player_shops_enabled 与数量上限约束
    private static int createShop(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return playersOnly(context);
        }
        String title = StringArgumentType.getString(context, "title");
        ShopManager manager = ShopManager.get(player.server);
        if (!manager.createShop(player, title)) {
            context.getSource().sendFailure(Component.translatable(
                    "message.flavor_immersed_daily_argo.economy.command.shop_create_failed"));
            return 0;
        }
        Shop created = null;
        for (Shop shop : ownShops(player)) {
            created = shop;
        }
        ShopManager.syncAll(player.server);
        if (created == null) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "message.flavor_immersed_daily_argo.economy.command.shop_created_plain", title.trim()), true);
            return 1;
        }
        Shop result = created;
        List<Shop> all = manager.allShops(player.server);
        int shopNumber = result.numberIn(all);
        context.getSource().sendSuccess(() -> Component.translatable(
                "message.flavor_immersed_daily_argo.economy.command.shop_created",
                Component.translatable(result.title()), shopNumber), true);
        return 1;
    }

// /fidashop delete <店铺编号>：普通模式只能删自己的；创造模式可删任意玩家的
    private static int deleteShop(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return playersOnly(context);
        }
        int shopId = IntegerArgumentType.getInteger(context, "shopId");
        ShopManager manager = ShopManager.get(player.server);
        if (!manager.deleteShopByNumber(player, shopId, player.isCreative())) {
            context.getSource().sendFailure(Component.translatable(
                    "message.flavor_immersed_daily_argo.economy.command.shop_delete_failed"));
            return 0;
        }
        ShopManager.syncAll(player.server);
        context.getSource().sendSuccess(() -> Component.translatable(
                "message.flavor_immersed_daily_argo.economy.command.shop_deleted", shopId), true);
        return 1;
    }

// 店铺数据挂在玩家身上，命令方块 / 服务器控制台没法执行
    private static int playersOnly(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.translatable(
                "message.flavor_immersed_daily_argo.economy.command.shop_players_only"));
        return 0;
    }

    private static List<Shop> ownShops(ServerPlayer player) {
        String owner = player.getName().getString();
        List<Shop> own = new ArrayList<>();
        for (Shop shop : ShopManager.get(player.server).shops()) {
            if (shop.ownerName().equals(owner)) {
                own.add(shop);
            }
        }
        return own;
    }
}
