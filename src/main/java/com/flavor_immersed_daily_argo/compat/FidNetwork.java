package com.flavor_immersed_daily_argo.compat;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

// 1.21 的 CustomPacketPayload 体系在 1.20.1 上的落地：
// 所有自定义包共用一个 SimpleChannel，用一个「类型 id + 裸字节」的信封转发，
// 收到后再按 id 找到对应的 StreamCodec 解出来，交给登记过的处理器。
public final class FidNetwork {

    private static final String PROTOCOL = "1";

    private static final Map<ResourceLocation, Entry> BY_ID = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Boolean> DIRECTIONS = new HashMap<>();

    private static SimpleChannel channel;
    private static boolean initialized;

    private FidNetwork() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(flavor_immersed_daily_argo.MODID, "main"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
        channel.messageBuilder(FidClientPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FidNetwork::encodeClient)
                .decoder(FidNetwork::decodeClient)
                .consumerMainThread(FidNetwork::handleClient)
                .add();
        channel.messageBuilder(FidServerPacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FidNetwork::encodeServer)
                .decoder(FidNetwork::decodeServer)
                .consumerMainThread(FidNetwork::handleServer)
                .add();
        FidAttachmentSync.register();
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type,
                                                                Object codec,
                                                                boolean toClient,
                                                                IPayloadHandler<T> handler) {
        init();
        BY_ID.put(type.id(), new Entry(type.id(), (net.minecraft.network.codec.StreamCodec<Object, Object>) codec,
                toClient, (IPayloadHandler<CustomPacketPayload>) (IPayloadHandler<?>) handler));
        DIRECTIONS.put(type.id(), toClient);
    }

    // ---- 发送 ----

    public static void sendToServer(CustomPacketPayload payload) {
        init();
        channel.sendToServer((FidServerPacket) envelope(payload));
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        init();
        channel.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                (FidClientPacket) envelope(payload));
    }

    public static void sendToAllPlayers(CustomPacketPayload payload) {
        init();
        if (channel == null) {
            return;
        }
        channel.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                (FidClientPacket) envelope(payload));
    }

    private static FidHolder envelope(CustomPacketPayload payload) {
        Entry entry = BY_ID.get(payload.type().id());
        if (entry == null) {
            throw new IllegalArgumentException("数据包未登记: " + payload.type().id());
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        entry.encode(buffer, payload);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return entry.toClient ? new FidClientPacket(entry.id, bytes) : new FidServerPacket(entry.id, bytes);
    }

    // ---- 收包 ----

    private static void encodeClient(FidClientPacket packet, FriendlyByteBuf buffer) {
        write(packet.id(), packet.data(), buffer);
    }

    private static void encodeServer(FidServerPacket packet, FriendlyByteBuf buffer) {
        write(packet.id(), packet.data(), buffer);
    }

    private static void write(ResourceLocation id, byte[] data, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(id);
        buffer.writeByteArray(data);
    }

    private static FidClientPacket decodeClient(FriendlyByteBuf buffer) {
        return new FidClientPacket(buffer.readResourceLocation(), buffer.readByteArray());
    }

    private static FidServerPacket decodeServer(FriendlyByteBuf buffer) {
        return new FidServerPacket(buffer.readResourceLocation(), buffer.readByteArray());
    }

    private static void handleClient(FidClientPacket packet, Supplier<NetworkEvent.Context> supplier) {
        dispatch(packet.id(), packet.data(), supplier, true);
    }

    private static void handleServer(FidServerPacket packet, Supplier<NetworkEvent.Context> supplier) {
        dispatch(packet.id(), packet.data(), supplier, false);
    }

    private static void dispatch(ResourceLocation id, byte[] data, Supplier<NetworkEvent.Context> supplier,
                                 boolean toClient) {
        NetworkEvent.Context context = supplier.get();
        Entry entry = BY_ID.get(id);
        if (entry == null || entry.toClient != toClient) {
            context.setPacketHandled(true);
            return;
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        CustomPacketPayload payload = entry.decode(buffer);
        context.enqueueWork(() -> entry.handle(payload, new FidPayloadContext(context, toClient)));
        context.setPacketHandled(true);
    }

    private record Entry(ResourceLocation id,
                         net.minecraft.network.codec.StreamCodec<Object, Object> codec,
                         boolean toClient,
                         IPayloadHandler<CustomPacketPayload> handler) {

        void encode(ByteBuf buffer, CustomPacketPayload payload) {
            this.codec.encode(buffer, payload);
        }

        CustomPacketPayload decode(ByteBuf buffer) {
            return (CustomPacketPayload) this.codec.decode(buffer);
        }

        void handle(CustomPacketPayload payload, FidPayloadContext context) {
            this.handler.handle(payload, context);
        }
    }

    public interface FidHolder {
        ResourceLocation id();

        byte[] data();
    }

    public record FidClientPacket(ResourceLocation id, byte[] data) implements FidHolder {
    }

    public record FidServerPacket(ResourceLocation id, byte[] data) implements FidHolder {
    }
}
