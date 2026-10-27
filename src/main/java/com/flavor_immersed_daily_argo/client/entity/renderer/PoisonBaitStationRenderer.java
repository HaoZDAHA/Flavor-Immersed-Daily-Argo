package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.PoisonBaitStationModel;
import com.flavor_immersed_daily_argo.entity.PoisonBaitStation;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// 毒饵站的渲染器：把 Blockbench 导出的模型直接画出来。
//
// 位姿换算与 ScarecrowRenderer 一致——模型地面在 y=24，翻转 x、y 再下移 1.501 格，
// 把「模型地面」对到实体脚底；盒子四四方方，朝向不影响观感。
public class PoisonBaitStationRenderer extends EntityRenderer<PoisonBaitStation> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "textures/entity/poison_bait_station.png");

    private final PoisonBaitStationModel model;

    public PoisonBaitStationRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PoisonBaitStationModel(context.bakeLayer(PoisonBaitStationModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoisonBaitStation entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PoisonBaitStation entity) {
        return TEXTURE;
    }
}
