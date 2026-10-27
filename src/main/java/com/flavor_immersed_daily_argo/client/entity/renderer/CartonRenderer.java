package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.CartonModel;
import com.flavor_immersed_daily_argo.entity.Carton;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// 纸箱实体的渲染器：直接把 Blockbench 导出的方块模型画出来。
public class CartonRenderer extends EntityRenderer<Carton> {

// 纸箱实体贴图。无人机吊着纸箱飞的时候（见 UavRenderer）画的也是这个模型，共用一份贴图
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "textures/entity/carton.png");

    private final CartonModel model;

    public CartonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CartonModel(context.bakeLayer(CartonModel.LAYER_LOCATION));
    }

    @Override
    public void render(Carton entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
// 模型以 y=24（像素）为地面，而 EntityRenderer 的位姿原点就在实体脚底；
// 照粪便 / 稻草人的做法翻转 y 再下移 1.501 格，把「模型地面」对到脚底。
// 同时翻转 x、y 可以保持三角形绕序不变，避免面被背面剔除掉。
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Carton entity) {
        return TEXTURE;
    }
}
