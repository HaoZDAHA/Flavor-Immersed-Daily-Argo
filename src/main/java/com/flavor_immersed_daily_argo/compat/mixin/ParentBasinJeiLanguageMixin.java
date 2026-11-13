package com.flavor_immersed_daily_argo.compat.mixin;

import com.flavor_immersed_daily_argo.compat.ProcessingStageLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Parent basin JEI's verified display-only description accessor. */
@Pseudo
@Mixin(targets = "com.flavor_immersed_daily.integration.jei.WoodBasinInfoRecipe", remap = false)
public abstract class ParentBasinJeiLanguageMixin {
    @Inject(method = "getDescription()Ljava/lang/String;", at = @At("RETURN"),
            cancellable = true, require = 0, remap = false)
    private void fida$localizeStage(CallbackInfoReturnable<String> callback) {
        callback.setReturnValue(ProcessingStageLanguage.display(callback.getReturnValue()));
    }
}
