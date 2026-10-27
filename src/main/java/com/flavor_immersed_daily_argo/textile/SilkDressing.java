package com.flavor_immersed_daily_argo.textile;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

// 「着装 / 拆卸」的搬运逻辑
//
// 把左侧槽里那件真护甲的附魔、属性、耐久整份搬到空白丝绸装备上（护甲原件同时留一份，
// 方便拆卸还原）；搬运的是原版那条 NBT 路径，所以丝绸装备穿在身上就等于穿了那件护甲。
public final class SilkDressing {

// 原版护甲上承载附魔 / 属性修饰符的 NBT 键
    private static final String ENCHANTMENTS_TAG = "Enchantments";
    private static final String ATTRIBUTE_MODIFIERS_TAG = "AttributeModifiers";

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
        ModDataComponents.SILK_ARMOR.get().set(garment, armor.copy());
        copyTag(armor, garment, ENCHANTMENTS_TAG);
        copyTag(armor, garment, ATTRIBUTE_MODIFIERS_TAG);
// 耐久按比例搬过去：1.20.1 的耐久上限是物品自带的，改不了，只能对齐“磨损程度”
        if (armor.isDamageableItem() && garment.isDamageableItem()
                && armor.getMaxDamage() > 0 && garment.getMaxDamage() > 0) {
            float worn = (float) armor.getDamageValue() / (float) armor.getMaxDamage();
            garment.setDamageValue(Math.min(garment.getMaxDamage() - 1,
                    Math.round(garment.getMaxDamage() * worn)));
        }
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
        int garmentMax = garment.getMaxDamage();
        int garmentDamage = garment.getDamageValue();
        if (garmentMax > 0 && armor.isDamageableItem()) {
            int armorMax = armor.getMaxDamage();
            armor.setDamageValue(Math.max(0, Math.min(armorMax,
                    Math.round(armorMax * (float) garmentDamage / (float) garmentMax))));
        }
        clear(garment);
        return armor;
    }

// 清掉着装带来的数据，并把耐久恢复成空白丝绸装备自己的样子
    public static void clear(ItemStack garment) {
        ModDataComponents.SILK_ARMOR.get().remove(garment);
        CompoundTag tag = garment.getTag();
        if (tag != null) {
            tag.remove(ENCHANTMENTS_TAG);
            tag.remove(ATTRIBUTE_MODIFIERS_TAG);
        }
        garment.setDamageValue(0);
    }

// 把 from 身上 key 这条 NBT 原样搬到 to 上
    private static void copyTag(ItemStack from, ItemStack to, String key) {
        CompoundTag tag = from.getTag();
        if (tag != null && tag.contains(key)) {
            to.getOrCreateTag().put(key, tag.get(key).copy());
        }
    }
}
