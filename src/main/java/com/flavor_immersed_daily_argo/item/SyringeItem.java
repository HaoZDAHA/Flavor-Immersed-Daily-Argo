package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

// 针管。
//
// 同一件东西有两副面孔：空的是一支普通针管，扎过动物之后就封上了那种生物的遗传液
// （ModDataComponents#SYRINGE_DNA），贴图、名字、光效、tooltip 全都跟着变。
//
// 扎人与受孕的判定都在 herding/SyringeHandler 里，这里只管「长什么样、叫什么」。
public class SyringeItem extends TooltipItem {

    public SyringeItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

// 封着遗传液时带一层附魔光效
    @Override
    public boolean isFoil(ItemStack stack) {
        return hasDna(stack);
    }

// 封着遗传液时改叫「筒装生物遗传液」
    @Override
    public Component getName(ItemStack stack) {
        if (hasDna(stack)) {
            return Component.translatable("item.flavor_immersed_daily_argo.syringe_dna");
        }
        return super.getName(stack);
    }

// 遗传液要记清楚是从谁身上抽出来的
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        String dna = stack.get(ModDataComponents.SYRINGE_DNA.get());
        if (dna == null || dna.isEmpty()) {
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(dna)).orElse(null);
        Component source = type != null ? type.getDescription() : Component.literal(dna);
        tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.syringe_dna.source", source));
    }

// 这支针管里封没封遗传液
    public static boolean hasDna(ItemStack stack) {
        String dna = stack.get(ModDataComponents.SYRINGE_DNA.get());
        return dna != null && !dna.isEmpty();
    }
}
