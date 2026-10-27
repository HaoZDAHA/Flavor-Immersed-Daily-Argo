package net.neoforged.neoforge.network.handling;

import net.minecraft.world.entity.player.Player;

// NeoForge 1.21 的数据包上下文。handle() 里拿到的就是它。
public interface IPayloadContext {

    // 发出这个包的玩家（客户端方向）或收包的玩家（服务端方向）
    Player player();

    // 在主线程上执行；这里本来就已经在主线程，直接跑
    void enqueueWork(Runnable task);
}
