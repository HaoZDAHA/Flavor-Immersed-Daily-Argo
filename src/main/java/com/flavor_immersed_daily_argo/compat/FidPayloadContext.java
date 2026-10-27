package com.flavor_immersed_daily_argo.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// 把 Forge 的收包上下文包成 1.21 的 IPayloadContext
final class FidPayloadContext implements IPayloadContext {

    private final NetworkEvent.Context context;
    private final boolean toClient;

    FidPayloadContext(NetworkEvent.Context context, boolean toClient) {
        this.context = context;
        this.toClient = toClient;
    }

    @Override
    public Player player() {
        if (this.toClient) {
            return FidClientPlayer.get();
        }
        ServerPlayer sender = this.context.getSender();
        return sender;
    }

    @Override
    public void enqueueWork(Runnable task) {
        this.context.enqueueWork(task);
    }
}
