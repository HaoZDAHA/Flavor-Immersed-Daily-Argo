package net.neoforged.neoforge.network.registration;

import com.flavor_immersed_daily_argo.compat.FidNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

// NeoForge 1.21 的数据包登记器。1.20.1 落到 FidNetwork 的 SimpleChannel 上。
public final class PayloadRegistrar {

    private final String version;

    public PayloadRegistrar(String version) {
        this.version = version;
    }

    public String version() {
        return this.version;
    }

    public <T extends CustomPacketPayload> void playToServer(CustomPacketPayload.Type<T> type,
                                                            StreamCodec<?, T> codec,
                                                            IPayloadHandler<T> handler) {
        FidNetwork.register(type, codec, false, handler);
    }

    public <T extends CustomPacketPayload> void playToClient(CustomPacketPayload.Type<T> type,
                                                            StreamCodec<?, T> codec,
                                                            IPayloadHandler<T> handler) {
        FidNetwork.register(type, codec, true, handler);
    }

    // 双向同码（源码里没有用到，留着以备后续包）
    public <T extends CustomPacketPayload> void playBidirectional(CustomPacketPayload.Type<T> type,
                                                                StreamCodec<? super ByteBuf, T> codec,
                                                                IPayloadHandler<T> handler) {
        FidNetwork.register(type, codec, true, handler);
        FidNetwork.register(type, codec, false, handler);
    }
}
