package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.PoopModel;
import com.flavor_immersed_daily_argo.entity.Poop;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// 粪便实体的渲染器：直接把 Blockbench 导出的方块模型画出来。
//
// 只负责地上那坨 Poop；被扔出去的是另一枚弹射物 ThrownPoop，
// 由原版的 ThrownItemRenderer 直接按物品贴图画，不走这里。
public class PoopRenderer extends EntityRenderer<Poop> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "textures/entity/poop.png");

    private final PoopModel model;

    public PoopRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PoopModel(context.bakeLayer(PoopModel.LAYER_LOCATION));
    }

    @Override
    public void render(Poop entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
// 模型以 y=24（像素）为地面，而 EntityRenderer 的位姿原点就在实体脚底；
// 这里照原版 MobRenderer 的做法翻转 y 再下移 1.501 格，把「模型地面」对到脚底。
// 同时翻转 x、y 可以保持三角形绕序不变，避免面被背面剔除掉。
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Poop entity) {
        return TEXTURE;
    }
}
