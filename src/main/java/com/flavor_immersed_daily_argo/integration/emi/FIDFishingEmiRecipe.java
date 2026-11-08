package com.flavor_immersed_daily_argo.integration.emi;

import com.flavor_immersed_daily_argo.fishing.FishingHandbookEntry;
import com.flavor_immersed_daily_argo.fishing.FishingHandbookResource;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

// EMI「南山钓竿钓获」配方页：一页一条水产，左边是产物，下面列出图鉴参数。
// 与 JEI 侧共用 FishingHandbookResource.lines()，参数文案保持一致。
final class FIDFishingEmiRecipe extends BasicEmiRecipe {

    private static final int WIDTH = 190;
    private static final int HEIGHT = 96;
    private static final int TEXT_COLOR = 0xFF404040;

    private final FishingHandbookEntry entry;

    FIDFishingEmiRecipe(FishingHandbookEntry entry) {
        super(FIDEmiPlugin.FISHING, recipeId(entry), WIDTH, HEIGHT);
        this.entry = entry;
        this.outputs.add(EmiStack.of(entry.icon()));
    }

    private static ResourceLocation recipeId(FishingHandbookEntry entry) {
        ResourceLocation source = entry.hasItem() ? entry.itemId : entry.entityId;
        return FIDEmiPlugin.id("fishing/" + source.getNamespace() + "_" + source.getPath());
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiStack.of(this.entry.icon()), 4, 4).recipeContext(this);
        List<Component> lines = FishingHandbookResource.lines(this.entry);
        int y = 26;
        for (Component line : lines) {
            widgets.addText(line, 4, y, TEXT_COLOR, false);
            y += 10;
        }
    }
}
