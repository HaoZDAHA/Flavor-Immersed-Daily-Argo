package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.TurkeyModel;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.entity.Turkey;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TurkeyRenderer extends MobRenderer<Turkey, TurkeyModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "textures/entity/turkey.png");
    private static final ResourceLocation BABY_TEXTURE =
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "textures/entity/turkey_baby.png");

    public TurkeyRenderer(EntityRendererProvider.Context context) {
        super(context, new TurkeyModel(context.bakeLayer(TurkeyModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(Turkey entity) {
        return entity.isBaby() ? BABY_TEXTURE : TEXTURE;
    }
}
