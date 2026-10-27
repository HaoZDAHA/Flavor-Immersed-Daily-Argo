package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry.Diet;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

// 鱼饵。
//
// 每款鱼饵只对应一种食性（草饵→食草、肉饵→食肉、腐饵→食腐、蚯蚓→活体），装入南山钓竿的
// 鱼饵槽后只对该食性的鱼有效；杂食鱼不受限制（不装饵或装任意饵都可能咬钩）。
// 鱼饵可堆叠，每次成功钓到对应食性的鱼会消耗一个。
public class BaitItem extends Item {

    private final Diet diet;

    public BaitItem(Diet diet, Properties properties) {
        super(properties);
        this.diet = diet;
    }

// 该鱼饵对应的食性
    public Diet diet() {
        return this.diet;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
// 标明它属于鱼饵槽，免得跟鱼线 / 浮漂之类的配件混在一起找错槽
        tooltipComponents.add(Component.literal(" ").withStyle(ChatFormatting.RESET)
                .append(Component.translatable("tooltip.flavor_immersed_daily_argo.part_category")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("gui.flavor_immersed_daily_argo.bait").withStyle(ChatFormatting.GOLD)));
        tooltipComponents.add(Component.literal(" ").withStyle(ChatFormatting.RESET)
                .append(Component.translatable("tooltip.flavor_immersed_daily_argo.bait_diet")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.translatable(this.diet.translationKey()).withStyle(ChatFormatting.WHITE)));
    }
}
