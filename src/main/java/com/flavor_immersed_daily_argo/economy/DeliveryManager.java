package com.flavor_immersed_daily_argo.economy;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.entity.DeliveryPoint;
import com.flavor_immersed_daily_argo.entity.TakeawayBag;
import com.flavor_immersed_daily_argo.entity.TakeawayBagItem;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

// 外卖订单的「调度台」。
//
// 订单只活在内存里：每次打开导航手机都重新洗一批（还没接单的旧单直接作废，已接的不动），
// 因此不需要存档；服务器重启后重新打开手机即可刷新出新单。
//
// 接单才落物：刷新出来的订单只是界面上的条目，不碰世界。玩家在接单界面点「接单」
// 之后，才会在出发地生成外卖袋、在目的地生成配送点——没接的单不会白白占实体与区块。
//
// 起点是玩家附近（24~80 格），终点在起点外 200~600 格；配送费按终点距离
// 每 10 格 1 元折算，限时 8 分钟（从刷新那一刻起算）。
//
// 起点 / 终点常常落在没加载的区块里，实体不能立刻生成，所以每隔 1 秒扫一遍：
// 哪个已接单的订单对应的区块加载了，就补生成路边的那只外卖袋 / 目的地的配送点。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public final class DeliveryManager {

// 每次打开手机刷新出的订单条数
    private static final int MAX_ORDERS = 3;

// 配送时限：8 分钟
    private static final long TIMEOUT_TICKS = 8L * 60L * 20L;

// 出发地到玩家的距离范围
    private static final double ORIGIN_MIN = 24.0D;
    private static final double ORIGIN_MAX = 80.0D;

// 目的地到出发地的距离范围
    private static final double DEST_MIN = 200.0D;
    private static final double DEST_MAX = 600.0D;

// 每多少格折算 1 元配送费
    private static final double BLOCKS_PER_YUAN = 10.0D;

// 出发点 / 终点缺实地时，向外这一圈内找最近的一格实地落脚（避免落进水里）
    private static final int SPAWN_SEARCH_RADIUS = 12;

    private static final List<Entry> ORDERS = new ArrayList<>();

    private DeliveryManager() {
    }

// 一条在途订单：订单本身 + 接单状态 + 已生成实体的编号（null = 还没生成）
    private static final class Entry {
        final ResourceKey<Level> dimension;
// 出发地若在生成时没法验证、落物时被挪到旁边实地，这里会被换成新坐标
        DeliveryOrder order;
        boolean accepted;
        UUID bagId;
        UUID pointId;
        boolean pickedUp;

        Entry(ResourceKey<Level> dimension, DeliveryOrder order) {
            this.dimension = dimension;
            this.order = order;
        }
    }

// 对外接口

// 打开手机时刷新：作废本维度里所有「还没接单」的订单，重新洗一批，并把当前在途订单返回给界面。
//
// 已经接下的单不受影响，继续躺在列表里等玩家去送。
    public static List<PhoneOrderView> refresh(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        Iterator<Entry> iterator = ORDERS.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (entry.dimension.equals(level.dimension()) && !entry.accepted) {
                iterator.remove();
            }
        }
        for (int i = 0; i < MAX_ORDERS; i++) {
            ORDERS.add(new Entry(level.dimension(), generate(level, player)));
        }
        tick(player.server);
        return views(level);
    }

// 玩家在接单界面点下「接单」：标记为已接，并尝试立刻在出发地摆出外卖袋
// （起点离玩家不远，区块多半已加载；没加载就交给 #tick 补生成）。
//
// 返回：更新后的订单列表，回发给界面
    public static List<PhoneOrderView> accept(ServerPlayer player, long orderId) {
        ServerLevel level = player.serverLevel();
        for (Entry entry : ORDERS) {
            if (entry.dimension.equals(level.dimension()) && entry.order.id() == orderId && !entry.accepted) {
                entry.accepted = true;
                if (entry.bagId == null && level.hasChunkAt(originPos(entry.order))) {
                    spawnBag(level, entry);
                }
                break;
            }
        }
        tick(player.server);
        return views(level);
    }

// 外卖袋被捡起：标记为已取货，之后刷新时不会再被清掉
    public static void markPickedUp(long orderId) {
        for (Entry entry : ORDERS) {
            if (entry.order.id() == orderId) {
                entry.pickedUp = true;
                entry.bagId = null;
                return;
            }
        }
    }

// 玩家在接单界面点下「取消」：把这单整个作废——清掉已生成的两端实体、收回身上那条「打包好的外卖」，
// 再把订单摘出在途列表。
//
// 不管这一单接没接、取没取货都能取消，免得留下收不回也交不掉的死单。
//
// 返回：更新后的订单列表，回发给界面
    public static List<PhoneOrderView> cancel(ServerPlayer player, long orderId) {
        ServerLevel level = player.serverLevel();
        Iterator<Entry> iterator = ORDERS.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (entry.dimension.equals(level.dimension()) && entry.order.id() == orderId) {
                discard(level, entry.bagId);
                discard(level, entry.pointId);
                removeCarriedBag(player, orderId);
                iterator.remove();
                break;
            }
        }
        tick(player.server);
        return views(level);
    }

// 订单送达：作废这条订单，并清掉可能还在的两端实体
    public static void complete(ServerLevel level, long orderId) {
        Iterator<Entry> iterator = ORDERS.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (entry.order.id() == orderId) {
                discard(level, entry.bagId);
                discard(level, entry.pointId);
                iterator.remove();
            }
        }
    }

// 把一笔钱按面额从大到小摊成纸币掉落物（满 64 张就先出一整摞大票）
    public static void spawnMoney(ServerLevel level, Vec3 pos, long amount) {
        long remaining = amount;
        for (int denomination : CreditCardItem.DENOMINATIONS) {
            while (remaining >= denomination) {
                int count = (int) Math.min(64L, remaining / denomination);
                ItemStack stack = new ItemStack(CreditCardItem.moneyFor(denomination).getItem(), count);
                ItemEntity drop = new ItemEntity(level, pos.x, pos.y + 0.5D, pos.z, stack);
                drop.setDefaultPickUpDelay();
                level.addFreshEntity(drop);
                remaining -= (long) count * denomination;
            }
        }
    }

// 调度

// 每秒扫一遍：超时的作废，已接单且区块加载了的补生成两端实体
    public static void tick(MinecraftServer server) {
        if (ORDERS.isEmpty()) {
            return;
        }
        Iterator<Entry> iterator = ORDERS.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            ServerLevel level = server.getLevel(entry.dimension);
            if (level == null) {
                iterator.remove();
                continue;
            }
            if (level.getGameTime() > entry.order.deadline()) {
                discard(level, entry.bagId);
                discard(level, entry.pointId);
                iterator.remove();
                continue;
            }
            if (!entry.accepted) {
// 还没接的单不落物
                continue;
            }
            if (!entry.pickedUp && entry.bagId == null
                    && level.hasChunkAt(originPos(entry.order))) {
                spawnBag(level, entry);
            }
            if (entry.pointId == null && level.hasChunkAt(destPos(entry.order))) {
                spawnPoint(level, entry);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 == 0) {
            tick(event.getServer());
            syncPhoneHolders(event.getServer());
        }
    }

// 每秒把当前订单状态推给「手持导航手机」的玩家，供 HUD 指南针读取（不弹接单界面）。
//
// 订单本身很少变，但玩家会走位；坐标不变、方向和距离由客户端实时算，所以这里只推列表。
    private static void syncPhoneHolders(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!holdsPhone(player)) {
                continue;
            }
            PacketDistributor.sendToPlayer(player,
                    new PhoneOrderSyncPacket(views(player.serverLevel()), false));
        }
    }

// 主手或副手拿着导航手机
    private static boolean holdsPhone(Player player) {
        return player.getMainHandItem().is(ModItems.NAV_PHONE.get())
                || player.getOffhandItem().is(ModItems.NAV_PHONE.get());
    }

// 生成

    private static DeliveryOrder generate(ServerLevel level, Player player) {
        RandomSource random = level.random;

// 出发点离玩家不远，区块多半已加载：这里直接挑一格实地，坐标一次定死。
        int[] origin = pickSolidSpot(level, player, random, ORIGIN_MIN, ORIGIN_MAX);
        int originX = origin[0];
        int originZ = origin[1];

// 目的地远在 200~600 格外，区块通常没加载，没法当场验证，等玩家走近、区块加载时再挪到实地。
        double destAngle = random.nextDouble() * Math.PI * 2.0D;
        double destDistance = DEST_MIN + random.nextDouble() * (DEST_MAX - DEST_MIN);
        int destX = originX + (int) Math.round(Math.cos(destAngle) * destDistance);
        int destZ = originZ + (int) Math.round(Math.sin(destAngle) * destDistance);

        long money = Math.max(1L, Math.round(destDistance / BLOCKS_PER_YUAN));
        long deadline = level.getGameTime() + TIMEOUT_TICKS;
        return new DeliveryOrder(random.nextLong(), originX, originZ, destX, destZ, deadline, money);
    }

// 在距玩家 min~max 格的位置随机取点，并尽量落到实地（固体方块之上、不碰水）。
//
// 区块已加载就验证、并就近修正到实地；24 次都没验成（比如全落在没加载的区块）就退回最后一次的
// 随机坐标，留到落物时再验证。
    private static int[] pickSolidSpot(ServerLevel level, Player player, RandomSource random,
                                       double min, double max) {
        int lastX = 0;
        int lastZ = 0;
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = min + random.nextDouble() * (max - min);
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
            lastX = x;
            lastZ = z;
            if (!level.hasChunkAt(new BlockPos(x, level.getMinBuildHeight(), z))) {
// 区块没加载，验证不了，换一个点再试
                continue;
            }
            BlockPos stand = findStandablePos(level, x, z);
            if (stand != null) {
                return new int[]{stand.getX(), stand.getZ()};
            }
        }
        return new int[]{lastX, lastZ};
    }

// 在出发地摆出外卖袋。落点必须是实地上（固体方块之上、不碰水）：出发地若在生成时因为区块没加载
// 没能验证，这里会就近挪到实地，并把订单坐标换成新位置——手机、袋子从此对得上。
//
// 这一带若压根没有实地（比如整片水面），先不落物，等下次扫描再试。
    private static void spawnBag(ServerLevel level, Entry entry) {
        BlockPos pos = findStandablePos(level, entry.order.originX(), entry.order.originZ());
        if (pos == null) {
            return;
        }
        if (pos.getX() != entry.order.originX() || pos.getZ() != entry.order.originZ()) {
            entry.order = withOrigin(entry.order, pos.getX(), pos.getZ());
        }
        TakeawayBag bag = ModEntities.TAKEAWAY_BAG.get().create(level);
        if (bag == null) {
            return;
        }
        bag.setOrder(entry.order);
        bag.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        level.addFreshEntity(bag);
        entry.bagId = bag.getUUID();
    }

// 在目的地摆出配送点。目的地远在几百格外，生成订单时区块多半没加载，没法当场验证，所以在这里
// 才把落点修正到实地上；订单一并改成新坐标，手机、配送点、以及还没被捡走的袋子都跟着一致。
    private static void spawnPoint(ServerLevel level, Entry entry) {
        BlockPos pos = findStandablePos(level, entry.order.destX(), entry.order.destZ());
        if (pos == null) {
            return;
        }
        if (pos.getX() != entry.order.destX() || pos.getZ() != entry.order.destZ()) {
            entry.order = withDest(entry.order, pos.getX(), pos.getZ());
            syncBagOrder(level, entry);
        }
        DeliveryPoint point = ModEntities.DELIVERY_POINT.get().create(level);
        if (point == null) {
            return;
        }
        point.setOrder(entry.order);
        point.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        level.addFreshEntity(point);
        entry.pointId = point.getUUID();
    }

// 目的地被挪动时，把还搁在出发地的那只袋子的订单也换成新坐标，免得它显示的还是旧终点
    private static void syncBagOrder(ServerLevel level, Entry entry) {
        if (entry.bagId != null && level.getEntity(entry.bagId) instanceof TakeawayBag bag) {
            bag.setOrder(entry.order);
        }
    }

// 工具

// 该维度里当前在途的订单（连同接单 / 取货状态），发给接单界面与 HUD
    private static List<PhoneOrderView> views(ServerLevel level) {
        List<PhoneOrderView> visible = new ArrayList<>();
        for (Entry entry : ORDERS) {
            if (entry.dimension.equals(level.dimension())) {
                visible.add(new PhoneOrderView(entry.order, entry.accepted, entry.pickedUp));
            }
        }
        return visible;
    }

    private static BlockPos originPos(DeliveryOrder order) {
        return new BlockPos(order.originX(), 0, order.originZ());
    }

    private static BlockPos destPos(DeliveryOrder order) {
        return new BlockPos(order.destX(), 0, order.destZ());
    }

    private static DeliveryOrder withOrigin(DeliveryOrder order, int x, int z) {
        return new DeliveryOrder(order.id(), x, z, order.destX(), order.destZ(),
                order.deadline(), order.money());
    }

    private static DeliveryOrder withDest(DeliveryOrder order, int x, int z) {
        return new DeliveryOrder(order.id(), order.originX(), order.originZ(), x, z,
                order.deadline(), order.money());
    }

// 在 (x,z) 一带找一格能落脚的实地：站人那格不泡水、脚下是结实地面。
//
// 先在正上方试，不行就以 #SPAWN_SEARCH_RADIUS 为半径一圈圈往外找最近的一格。整片都
// 是水 / 空气（还没加载的区块也算）就返回 null。只认已加载的区块，不会为了找地强行生成。
    private static BlockPos findStandablePos(ServerLevel level, int x, int z) {
        BlockPos direct = solidGround(level, x, z);
        if (direct != null) {
            return direct;
        }
        for (int radius = 1; radius <= SPAWN_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    BlockPos found = solidGround(level, x + dx, z + dz);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }

// 该 xz 上能站人的那一格实地；不是实地（水面 / 悬空 / 区块未加载）就返回 null
    private static BlockPos solidGround(ServerLevel level, int x, int z) {
        BlockPos columnBase = new BlockPos(x, level.getMinBuildHeight(), z);
        if (!level.hasChunkAt(columnBase)) {
            return null;
        }
// MOTION_BLOCKING_NO_LEAVES 会把水面也算进高度，所以拿到"地表"后还得回头确认脚下是不是结实地面
        BlockPos stand = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, columnBase);
        BlockPos ground = stand.below();
        if (!level.getFluidState(stand).isEmpty() || !level.getFluidState(ground).isEmpty()) {
            return null;
        }
        BlockState below = level.getBlockState(ground);
        if (!below.isFaceSturdy(level, ground, Direction.UP)) {
            return null;
        }
        return stand;
    }

// 把玩家身上那条对应此单的「打包好的外卖」清掉（取消订单时用，免得留一件交不掉的死物）
    private static void removeCarriedBag(ServerPlayer player, long orderId) {
        boolean removed = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!(stack.getItem() instanceof TakeawayBagItem)) {
                continue;
            }
            DeliveryOrder carried = stack.get(ModDataComponents.DELIVERY_ORDER.get());
            if (carried != null && carried.id() == orderId) {
                player.getInventory().setItem(slot, ItemStack.EMPTY);
                removed = true;
            }
        }
        if (removed) {
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
    }

    private static void discard(ServerLevel level, UUID id) {
        if (id == null) {
            return;
        }
        Entity entity = level.getEntity(id);
        if (entity != null) {
            entity.discard();
        }
    }
}
