package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.economy.DeliveryOrder;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.fishing.FishingRodModData;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

// 数据组件注册
public final class ModDataComponents {

    private ModDataComponents() {
    }

    public static final DeferredRegister<DataComponentType<?>> REGISTRY =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, flavor_immersed_daily_argo.MODID);

// 钓竿改装数据组件
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FishingRodModData>> ROD_MOD_DATA =
            REGISTRY.register("rod_mod_data", () -> DataComponentType.<FishingRodModData>builder()
                    .persistent(FishingRodModData.CODEC)
                    .networkSynchronized(FishingRodModData.STREAM_CODEC)
                    .build());

// 编制麻袋里装着的生物数据
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> CAPTURED_ENTITY =
            REGISTRY.register("captured_entity", () -> DataComponentType.<CompoundTag>builder()
                    .persistent(CompoundTag.CODEC)
                    .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
                    .build());

// 编制麻袋里那只动物的体重（kg）：只有该物种有体重标准时才有这个组件
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> CAPTURED_WEIGHT =
            REGISTRY.register("captured_weight", () -> DataComponentType.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .networkSynchronized(ByteBufCodecs.DOUBLE)
                    .build());

// 抄网里兜着的活鱼（最多 3 条，每条都是一份完整的实体 NBT）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<CompoundTag>>> CAPTURED_ENTITIES =
            REGISTRY.register("captured_entities", () -> DataComponentType.<List<CompoundTag>>builder()
                    .persistent(CompoundTag.CODEC.listOf())
                    .networkSynchronized(ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.collection(java.util.ArrayList::new)))
                    .build());

// 无人机遥控器绑定的那架无人机（存 UUID 字符串，没有这个组件就是还没绑）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> UAV_BOUND_DRONE =
            REGISTRY.register("uav_bound_drone", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

// 无人机看护模块上记录的两个对角坐标（每个点 3 个 int：x, y, z；没记录就没有这个组件）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Integer>>> UAV_MODULE_POINTS =
            REGISTRY.register("uav_module_points", () -> DataComponentType.<List<Integer>>builder()
                    .persistent(Codec.INT.listOf())
                    .networkSynchronized(ByteBufCodecs.INT.apply(ByteBufCodecs.list()))
                    .build());

// 杆秤称出来的重量（斤）：只有上过秤的物品才有这个组件，因此没称过的照常堆叠
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> WEIGHED_WEIGHT =
            REGISTRY.register("weighed_weight", () -> DataComponentType.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .networkSynchronized(ByteBufCodecs.DOUBLE)
                    .build());

// 针管里封存的是哪种生物的遗传液（存实体注册名字符串）
// 没有这个组件就是一支空针管；有就是「筒装生物遗传液」，tooltip 会写明来源生物
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SYRINGE_DNA =
            REGISTRY.register("syringe_dna", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

// 空白丝绸装备上的画：16×16 一共 256 个 ARGB 像素，行优先
// 没有这个组件 = 还没画过，渲染时直接是纯白模板
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Integer>>> SILK_DESIGN =
            REGISTRY.register("silk_design", () -> DataComponentType.<List<Integer>>builder()
                    .persistent(Codec.INT.listOf())
                    .networkSynchronized(ByteBufCodecs.INT.apply(ByteBufCodecs.list()))
                    .build());

// 空白丝绸装备上「着装」进来的那件真护甲（原样存一份，方便拆卸还原）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStack>> SILK_ARMOR =
            REGISTRY.register("silk_armor", () -> DataComponentType.<ItemStack>builder()
                    .persistent(ItemStack.CODEC)
                    .networkSynchronized(ItemStack.STREAM_CODEC)
                    .build());

// 空白丝绸装备上「建模」出来的 3D 体素（每个体素 5 个 int：cube, i, j, k, ARGB）
// 和表面花纹并存：花纹是贴在身上的那一层，体素是从身上长出来 / 堆出去的小方块
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Integer>>> SILK_VOXELS =
            REGISTRY.register("silk_voxels", () -> DataComponentType.<List<Integer>>builder()
                    .persistent(Codec.INT.listOf())
                    .networkSynchronized(ByteBufCodecs.INT.apply(ByteBufCodecs.list()))
                    .build());

// 经济系列

// 纸币面额（金融价值）：一元纸币是 1、一百元纸币是 100
// 每种纸币在注册时就把这个组件当作默认值塞进去，tooltip 直接读它
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MONEY_VALUE =
            REGISTRY.register("money_value", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

// 银行卡的持卡主人（存玩家名；没有这个组件 / 空串就是还没绑定）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CARD_OWNER =
            REGISTRY.register("card_owner", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

// 银行卡的卡面账户 / 金融价值（存款余额，默认 0）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> CARD_ACCOUNT =
            REGISTRY.register("card_account", () -> DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build());

// 银行卡里的 9 个槽位：只放钱币，纯存储，不计入卡面账户
// 必须用 OPTIONAL_CODEC / OPTIONAL_STREAM_CODEC：空槽就是 ItemStack.EMPTY，普通编解码器会存档失败。
// 槽位数不做定长约束：老存档里存的是 27 格，定长会直接读不出来；读取时由 CreditCardItem.getItems 归一到 9 格。
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> CARD_ITEMS =
            REGISTRY.register("card_items", () -> DataComponentType.<List<ItemStack>>builder()
                    .persistent(ItemStack.OPTIONAL_CODEC.listOf())
                    .networkSynchronized(ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.collection(java.util.ArrayList::new)))
                    .build());

// 导航手机是否已展开（展开后才能右键接单界面）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> PHONE_OPEN =
            REGISTRY.register("phone_open", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

// 「打包好的外卖」携带的订单（起点 / 终点 xz、截止刻、配送费）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DeliveryOrder>> DELIVERY_ORDER =
            REGISTRY.register("delivery_order", () -> DataComponentType.<DeliveryOrder>builder()
                    .persistent(DeliveryOrder.CODEC)
                    .networkSynchronized(DeliveryOrder.STREAM_CODEC)
                    .build());

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
