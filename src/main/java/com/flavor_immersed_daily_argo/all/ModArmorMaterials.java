package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;

// 纺织系列的盔甲材质
//
// 「空白丝绸」这一套只是画布：防御、韧性、击退抗性都给 0，玩家真正要的护甲属性是在缝纫机里
// 从别的护甲「着装」转移过来的。贴图固定用纯白的
// textures/models/armor/silk_layer_1.png / _layer_2.png，
// 实际穿在身上的花纹由缝纫机的动态图层另外叠上去。
public final class ModArmorMaterials {

// 空白丝绸装备的耐久系数：和 ArmorItem.Type#getDurability 相乘得到自身耐久上限
    public static final int SILK_DURABILITY = 15;

    public static final DeferredRegister<ArmorMaterial> REGISTRY =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, flavor_immersed_daily_argo.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SILK =
            REGISTRY.register("silk", () -> new ArmorMaterial(
                    Map.of(
                            ArmorItem.Type.HELMET, 0,
                            ArmorItem.Type.CHESTPLATE, 0,
                            ArmorItem.Type.LEGGINGS, 0,
                            ArmorItem.Type.BOOTS, 0),
                    15,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(ModItems.SILK.get()),
                    List.of(new ArmorMaterial.Layer(
                            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "silk"))),
                    0.0F,
                    0.0F));

    private ModArmorMaterials() {
    }

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
