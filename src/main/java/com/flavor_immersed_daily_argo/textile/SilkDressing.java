package com.flavor_immersed_daily_argo.textile;

import com.flavor_immersed_daily_argo.all.ModArmorMaterials;
import com.flavor_immersed_daily_argo.all.ModDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

// 「着装 / 拆卸」的搬运逻辑
//
// 把左侧槽里那件真护甲的附魔、属性、耐久整份搬到空白丝绸装备上（护甲原件同时留一份，
// 方便拆卸还原）；搬运的只是数据组件，所以丝绸装备穿在身上就等于穿了那件护甲。
public final class SilkDressing {

    private SilkDressing() {
    }

// 是否已经着装了一件护甲
    public static boolean isDressed(ItemStack garment) {
        return !SilkDesign.attachedArmor(garment).isEmpty();
    }

// 把护甲的数据搬到丝绸装备上，原件留一份在组件里
    public static void dress(ItemStack garment, ItemStack armor) {
        if (garment.isEmpty() || armor.isEmpty()) {
            return;
        }
        garment.set(ModDataComponents.SILK_ARMOR.get(), armor.copy());
        copy(armor, garment, DataComponents.ENCHANTMENTS);
        copy(armor, garment, DataComponents.ATTRIBUTE_MODIFIERS);
        copy(armor, garment, DataComponents.MAX_DAMAGE);
        copy(armor, garment, DataComponents.DAMAGE);
    }

// 拆卸：把那件护甲还回来，剩多少耐久就还多少。
//
// 返回：还原后的护甲；没着装就是空的
    public static ItemStack undress(ItemStack garment) {
        ItemStack armor = SilkDesign.attachedArmor(garment);
        if (armor.isEmpty()) {
            return ItemStack.EMPTY;
        }
// 按丝绸装备上剩下的耐久比例，折算回护甲自己的耐久
        int garmentMax = garment.getOrDefault(DataComponents.MAX_DAMAGE, 0);
        int garmentDamage = garment.getOrDefault(DataComponents.DAMAGE, 0);
        if (garmentMax > 0 && armor.isDamageableItem()) {
            float remaining = 1.0F - (float) garmentDamage / (float) garmentMax;
            int armorMax = armor.getMaxDamage();
            armor.setDamageValue(Math.max(0, Math.min(armorMax, Math.round(armorMax * (1.0F - remaining)))));
        }
        clear(garment);
        return armor;
    }

// 清掉着装带来的数据，并把耐久恢复成空白丝绸装备自己的样子
    public static void clear(ItemStack garment) {
        garment.remove(ModDataComponents.SILK_ARMOR.get());
        garment.remove(DataComponents.ENCHANTMENTS);
        garment.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        garment.remove(DataComponents.DAMAGE);
        if (garment.getItem() instanceof ArmorItem armorItem) {
            garment.set(DataComponents.MAX_DAMAGE,
                    armorItem.getType().getDurability(ModArmorMaterials.SILK_DURABILITY));
        }
    }

    private static <T> void copy(ItemStack from, ItemStack to, DataComponentType<T> type) {
        T value = from.get(type);
        if (value != null) {
            to.set(type, value);
        }
    }
}
