package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.TakeawayBagModel;
import com.flavor_immersed_daily_argo.entity.TakeawayBag;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// 外卖袋实体的渲染器：把 Blockbench 导出的袋子模型画出来。
public class TakeawayBagRenderer extends EntityRenderer<TakeawayBag> {

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "textures/entity/takeaway_bag.png");

    private final TakeawayBagModel model;

    public TakeawayBagRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TakeawayBagModel(context.bakeLayer(TakeawayBagModel.LAYER_LOCATION));
    }

    @Override
    public void render(TakeawayBag entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
// 与纸箱一致：模型以 y=24（像素）为地面，而位姿原点在实体脚底，
// 翻转 y、x 后下移 1.501 格把「模型地面」对到脚底，同时保持三角形绕序。
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TakeawayBag entity) {
        return TEXTURE;
    }
}
