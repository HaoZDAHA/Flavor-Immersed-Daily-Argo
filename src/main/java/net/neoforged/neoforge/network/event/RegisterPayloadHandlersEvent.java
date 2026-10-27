package net.neoforged.neoforge.network.event;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;

// NeoForge 1.21 在模组总线上发的「登记数据包」事件。
// 1.20.1 没有这个事件，主类直接 new 一个再调用即可。
public class RegisterPayloadHandlersEvent {

    public PayloadRegistrar registrar(String version) {
        return new PayloadRegistrar(version);
    }
}
