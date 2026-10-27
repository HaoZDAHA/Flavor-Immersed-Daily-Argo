package com.flavor_immersed_daily_argo.client.block;

import com.flavor_immersed_daily_argo.client.entity.model.RawSwordfishModel;
import com.flavor_immersed_daily_argo.block.RawSwordfishBlock;
import com.flavor_immersed_daily_argo.block.RawSwordfishBlockEntity;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

// 生剑鱼方块实体渲染器
//
// 剑鱼模型远超单个方块（约 4.9 格长、1.9 格高），无法作为普通方块模型烘焙，
// 因此由本渲染器按原始尺寸（1 像素 = 1/16 格）绘制，并跟随方块朝向。
//
// 物品（GUI / 手持）的渲染复用 #renderModel，见 RawSwordfishItemRenderer。
public class RawSwordfishRenderer implements BlockEntityRenderer<RawSwordfishBlockEntity> {

// 模型贴图（128x128，由 bbmodel 内嵌贴图导出）
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            flavor_immersed_daily_argo.MODID, "textures/entity/raw_swordfish.png");

    private final RawSwordfishModel model;

    public RawSwordfishRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new RawSwordfishModel(context.getModelSet().bakeLayer(RawSwordfishModel.LAYER_LOCATION));
    }

    @Override
    public void render(RawSwordfishBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction facing = blockEntity.getBlockState().getValue(RawSwordfishBlock.FACING);
        renderModel(poseStack, bufferSource, packedLight, packedOverlay, this.model, facing);
    }

// 按原始尺寸绘制剑鱼模型（方块与物品共用）。
//
// 导出模型与 bbmodel 设计空间之间只差一个 Y 翻转（java_y = 24 - bb_y），
// 这里用 scale(-1, -1, 1) 翻回来：同时翻转 x、y 可以保持三角形绕序不变，
// 避免面被背面剔除掉。翻回后模型自动落在设计空间上：
//
// 腹线（bb_y = 0）正好贴着方块底面，背鳍最高点约在 +1.44 格；
// 水平方向以方块中心为基准，模型包围盒中心恰好是方块中心 (0.5, 0.5, 0.5)；
// 剑吻朝本地 -Z，facing=north 时朝北。
//
// 物品渲染时 PoseStack 已被原版平移 (-0.5, -0.5, -0.5)，与"模型中心 = 方块中心"
// 正好互相抵消，所以物品也会以模型中心为轴心旋转缩放，无需额外补偿。
    public static void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                                   RawSwordfishModel model, Direction facing) {
        poseStack.pushPose();
// 1) 移到方块中心（水平方向居中）
        poseStack.translate(0.5, 0.0, 0.5);
// 2) 按 facing 旋转，使剑吻指向 FACING
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
// 3) 翻回 bbmodel 设计空间（同时翻转 x、y 可保持三角形绕序不变，避免面被背面剔除）。
//    注意：ModelPart 烘焙时已把像素除以 16，模型此刻已是"格"为单位，这里不能再乘 1/16
        poseStack.scale(-1.0F, -1.0F, 1.0F);
// 4) 抵消模型根节点 y = 24 像素（1.5 格）的偏移，使 bb_y = 0 的腹线贴住方块底面；
// 并让模型 z 中心（-0.5 像素 = -0.03125 格）落在方块中心
        poseStack.translate(0.0F, -1.5F, 0.03125F);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF);
        poseStack.popPose();
    }
}
