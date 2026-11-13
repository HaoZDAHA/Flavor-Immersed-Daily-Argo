package com.flavor_immersed_daily_argo.compat.mixin;

import com.flavor_immersed_daily_argo.compat.ProcessingStageLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Optional parent JEI display getter; never changes server recipe data or registration. */
@Pseudo
@Mixin(targets = "com.flavor_immersed_daily.integration.jei.ButcheringRecipe", remap = false)
public abstract class ParentButcheringJeiLanguageMixin {
    @Inject(method = "getToolName()Ljava/lang/String;", at = @At("RETURN"),
            cancellable = true, require = 0, remap = false)
    private void fida$localizeStage(CallbackInfoReturnable<String> callback) {
        callback.setReturnValue(ProcessingStageLanguage.display(callback.getReturnValue()));
    }
}
