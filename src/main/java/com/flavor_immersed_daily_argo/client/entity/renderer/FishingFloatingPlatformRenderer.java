package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.FishingFloatingPlatformModel;
import com.flavor_immersed_daily_argo.entity.FishingFloatingPlatform;
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

// 充气钓鱼浮台的渲染器：把 Blockbench 导出的模型直接画出来。
//
// 位姿换算与 HandcartRenderer 同一套路——模型朝 +z 建，翻转 x、y 保持三角形绕序，
// 再转 -yaw，最后把「模型地面」对到实体脚底。
public class FishingFloatingPlatformRenderer extends EntityRenderer<FishingFloatingPlatform> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "textures/entity/fishing_floating_platform.png");

// 模型最低点在 y=31 像素。导出坐标里 y 是往下的，翻转之后「越大越靠下」，
// 所以取的是 cube 的 y 最大值（平台底面），而不是最小值——照最小值抬会把整块平台埋进水里。
    private static final float MODEL_GROUND = 31.0F / 16.0F;

    private final FishingFloatingPlatformModel model;

    public FishingFloatingPlatformRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FishingFloatingPlatformModel(context.bakeLayer(FishingFloatingPlatformModel.LAYER_LOCATION));
    }

    @Override
    public void render(FishingFloatingPlatform entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -MODEL_GROUND, 0.0F);

        this.model.setupAnim(entity, 0.0F, 0.0F, partialTicks + entity.tickCount, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FishingFloatingPlatform entity) {
        return TEXTURE;
    }
}