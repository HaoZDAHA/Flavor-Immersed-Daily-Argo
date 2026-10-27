package com.flavor_immersed_daily_argo.integration.emi;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.recipe.InsectNetDrop;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

// EMI「捕虫」配方页：捕虫网 + 草 → 对应昆虫，右侧标注掉落概率。
// 与 JEI 侧共用 jei.flavor_immersed_daily_argo.insect_net.* 文案与同一份掉落数据。
final class FIDInsectNetEmiRecipe extends BasicEmiRecipe {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 50;

    private static final int HINT_COLOR = 0xFF808080;

    private final InsectNetDrop drop;

    FIDInsectNetEmiRecipe(InsectNetDrop drop) {
        super(FIDEmiPlugin.INSECT_NET,
                FIDEmiPlugin.id("insect_net/" + BuiltInRegistries.ITEM.getKey(drop.item()).getPath()),
                WIDTH, HEIGHT);
        this.drop = drop;
        this.inputs.add(EmiStack.of(ModItems.INSECT_NET.get()));
        this.inputs.add(EmiStack.of(new ItemStack(Blocks.SHORT_GRASS)));
        this.outputs.add(EmiStack.of(drop.item()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiStack.of(ModItems.INSECT_NET.get()), 24, 16);
        widgets.addSlot(EmiStack.of(new ItemStack(Blocks.SHORT_GRASS)), 48, 16);
        widgets.addSlot(EmiStack.of(drop.item()), 100, 16).recipeContext(this);

        widgets.addText(Component.literal("→"), 72, 21, HINT_COLOR, false);
        widgets.addText(Component.translatable("jei.flavor_immersed_daily_argo.insect_net.chance",
                Math.round(drop.chance() * 100.0F)), 120, 21, HINT_COLOR, false);
    }
}
