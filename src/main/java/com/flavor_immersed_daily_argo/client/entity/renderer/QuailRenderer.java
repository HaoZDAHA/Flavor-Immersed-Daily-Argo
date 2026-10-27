package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.QuailModel;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.entity.Quail;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class QuailRenderer extends MobRenderer<Quail, QuailModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "textures/entity/quail.png");
    private static final ResourceLocation BABY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "textures/entity/quail_baby.png");

    public QuailRenderer(EntityRendererProvider.Context context) {
        super(context, new QuailModel(context.bakeLayer(QuailModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(Quail entity) {
        return entity.isBaby() ? BABY_TEXTURE : TEXTURE;
    }
}
