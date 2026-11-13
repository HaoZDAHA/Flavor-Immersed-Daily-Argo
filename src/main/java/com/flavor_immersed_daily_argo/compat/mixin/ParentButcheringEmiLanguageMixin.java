package com.flavor_immersed_daily_argo.compat.mixin;

import com.flavor_immersed_daily_argo.compat.ProcessingStageLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Optional parent EMI display call; unknown/custom labels pass through unchanged. */
@Pseudo
@Mixin(targets = "com.flavor_immersed_daily.integration.emi.FIDButcheringEmiRecipe", remap = false)
public abstract class ParentButcheringEmiLanguageMixin {
    @ModifyArg(method = "addWidgets(Ldev/emi/emi/api/widget/WidgetHolder;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                    remap = false), index = 0, require = 0, remap = false)
    private String fida$localizeStage(String original) {
        return ProcessingStageLanguage.display(original);
    }
}
