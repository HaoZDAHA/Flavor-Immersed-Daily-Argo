package com.flavor_immersed_daily_argo.all;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.IEventBus;

// 纺织系列的盔甲材质
//
// 「空白丝绸」这一套只是画布：防御、韧性、击退抗性都给 0，玩家真正要的护甲属性是在缝纫机里
// 从别的护甲「着装」转移过来的。贴图固定用纯白的
// textures/models/armor/silk_layer_1.png / _layer_2.png，
// 实际穿在身上的花纹由缝纫机的动态图层另外叠上去。
//
// 1.20.1 的 ArmorMaterial 是接口而不是注册项，这里直接给一份实现。
public final class ModArmorMaterials {

// 空白丝绸装备的耐久系数：和原版的部位耐久基数相乘得到自身耐久上限
    public static final int SILK_DURABILITY = 15;

    public static final ArmorMaterial SILK = new ArmorMaterial() {

        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return healthFor(type) * SILK_DURABILITY;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return 0;
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_LEATHER;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.SILK.get());
        }

// 贴图名：assets/flavor_immersed_daily_argo/textures/models/armor/silk_layer_1.png
        @Override
        public String getName() {
            return "silk";
        }

        @Override
        public float getToughness() {
            return 0.0F;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.0F;
        }
    };

// 原版各部位的耐久基数
    private static int healthFor(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 13;
            case LEGGINGS -> 15;
            case CHESTPLATE -> 16;
            case HELMET -> 11;
        };
    }

    private ModArmorMaterials() {
    }

// 1.20.1 没有盔甲材质注册表，这里不需要注册
    public static void register(IEventBus modEventBus) {
    }
}
