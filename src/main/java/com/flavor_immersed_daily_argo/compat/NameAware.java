package com.flavor_immersed_daily_argo.compat;

// 需要在注册时知道自己的注册名（比如数据组件要用名字做 NBT 键）的对象实现这个接口。
public interface NameAware {

    void fidSetName(String name);
}
