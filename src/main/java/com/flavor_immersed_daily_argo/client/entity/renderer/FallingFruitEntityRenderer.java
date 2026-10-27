package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.entity.FallingFruit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

// 坠落果子的渲染器：直接把实体携带的悬挂果实方块模型画出来。
// 落地后按实体 ID 生成一个固定的随机翻滚角度，服务端与各客户端表现一致。
public class FallingFruitEntityRenderer extends EntityRenderer<FallingFruit> {

    public FallingFruitEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FallingFruit entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        BlockState blockState = entity.getBlockState();
        if (blockState.isAir()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(-0.5, -0.4, -0.5);

// 落地的果子随机旋转翻滚：以方块中心为轴，角度由实体 ID 决定，服务端与各客户端一致
        if (entity.isSettled()) {
            RandomSource random = RandomSource.create(entity.getId());
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees((random.nextFloat() - 0.5F) * 120.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((random.nextFloat() - 0.5F) * 120.0F));
            poseStack.translate(-0.5, -0.5, -0.5);
        }

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                blockState, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FallingFruit entity) {
        return null;
    }
}
