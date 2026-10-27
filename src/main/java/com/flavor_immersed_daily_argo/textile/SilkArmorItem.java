package com.flavor_immersed_daily_argo.textile;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

// 空白丝绸装备
//
// 本质是一件「画布护甲」：本身防御为 0，玩家在缝纫机里画花纹，并把另一件真护甲
// 「着装」进来，把这件真护甲的附魔、属性、耐久都转移到自己身上（见
// com.flavor_immersed_daily_argo.all.ModDataComponents#SILK_ARMOR）。
//
// 耐久见底时不会报废：只会把着装进来的护甲数据清掉，自己恢复成一块空画布。
public class SilkArmorItem extends ArmorItem {

    public SilkArmorItem(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        ItemStack worn = SilkDesign.attachedArmor(stack);
        if (!worn.isEmpty()) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.flavor_immersed_daily_argo.silk_armor_attached", worn.getHoverName()));
        }
        if (hasPreview(stack)) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.flavor_immersed_daily_argo.silk_armor_preview"));
        }
    }

// 定制过的丝绸装备会在 tooltip 里多带一格模型预览（按住 Shift 显示）。
//
// 只有画过花纹或堆过体素的才算「定制过」；一块没动过的白布就不用占地方了。
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return hasPreview(stack) ? Optional.of(new SilkArmorPreview(stack)) : Optional.empty();
    }

    private static boolean hasPreview(ItemStack stack) {
        return SilkDesign.hasDesign(stack) || SilkDesign.hasVoxels(stack);
    }

// 耐久耗尽时不报废，改成把着装数据清掉
    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        int max = stack.getMaxDamage();
        if (max > 0 && stack.getDamageValue() + amount >= max) {
            SilkDressing.clear(stack);
            return 0;
        }
        return amount;
    }
}
