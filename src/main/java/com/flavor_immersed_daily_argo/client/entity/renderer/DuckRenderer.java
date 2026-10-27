package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.DuckModel;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.entity.Duck;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DuckRenderer extends MobRenderer<Duck, DuckModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "textures/entity/duck.png");
    private static final ResourceLocation BABY_TEXTURE =
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "textures/entity/duck_baby.png");

    public DuckRenderer(EntityRendererProvider.Context context) {
        super(context, new DuckModel(context.bakeLayer(DuckModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(Duck entity) {
        return entity.isBaby() ? BABY_TEXTURE : TEXTURE;
    }
}
