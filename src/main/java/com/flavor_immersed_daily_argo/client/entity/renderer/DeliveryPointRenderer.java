package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.entity.DeliveryPoint;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// 配送点实体的渲染器：直接借原版的村民模型 + 默认村民皮肤，把这个不动的收件人画出来。
//
// 不走 LivingEntityRenderer，所以不会做任何动画，站着不动；手臂交叉的姿势
// 是村民模型自身的默认姿态。
public class DeliveryPointRenderer extends EntityRenderer<DeliveryPoint> {

// 原版村民的默认皮肤
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/villager/villager.png");

    private final VillagerModel model;

    public DeliveryPointRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER));
    }

    @Override
    public void render(DeliveryPoint entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
// 与原版 LivingEntityRenderer 相同的朝向 + 落地修正，让村民站在实体脚底、朝着它的 yaw
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DeliveryPoint entity) {
        return TEXTURE;
    }
}
