package com.flavor_immersed_daily_argo.client.block;

import com.flavor_immersed_daily_argo.client.entity.model.WaterwheelModel;
import com.flavor_immersed_daily_argo.block.WaterwheelBlock;
import com.flavor_immersed_daily_argo.block.WaterwheelBlockEntity;
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

// 灌溉水车方块实体渲染器
//
// 水车高约 3.8 格、轮子直径超过 3 格，远超单方块模型能表达的范围，所以不参与区块网格烘焙，
// 由本渲染器按原始尺寸（1 像素 = 1/16 格）绘制，并跟随方块朝向。
//
// 水轮按游戏时间匀速转，一圈 16 秒（与 Blockbench 导出的 waterwheel_round 等长）；
// 角度取的是全局游戏时间，所以并排的水车相位天然对齐，多台一起看不会各转各的。
public class WaterwheelRenderer implements BlockEntityRenderer<WaterwheelBlockEntity> {

// 模型贴图（256×256，由 bbmodel 内嵌贴图导出）
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            flavor_immersed_daily_argo.MODID, "textures/entity/waterwheel.png");

// 水轮转一圈的时长：导出动画长 16 秒
    private static final float TURN_TICKS = 16.0F * 20.0F;

    private final WaterwheelModel model;

    public WaterwheelRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new WaterwheelModel(context.getModelSet().bakeLayer(WaterwheelModel.LAYER_LOCATION));
    }

    @Override
    public void render(WaterwheelBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction facing = blockEntity.getBlockState().getValue(WaterwheelBlock.FACING);

        float angle = 0.0F;
        if (blockEntity.getLevel() != null) {
            angle = (blockEntity.getLevel().getGameTime() + partialTick) % TURN_TICKS / TURN_TICKS * 360.0F;
        }
        this.model.setWheelAngle(angle);

        poseStack.pushPose();
// 1) 移到方块中心（水平方向居中）
        poseStack.translate(0.5D, 0.0D, 0.5D);
// 2) 按 facing 旋转，让轮面朝向 FACING
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
// 3) 翻回 bbmodel 设计空间（同时翻转 x、y 可保持三角形绕序不变，避免面被背面剔除）；
//    注意：ModelPart 烘焙时已把像素除以 16，模型此刻已是“格”为单位，这里不能再乘 1/16
        poseStack.scale(-1.0F, -1.0F, 1.0F);
// 4) 抵消模型根节点 y = 24 像素（1.5 格）的偏移，使设计空间里 bb_y = 0 的落地线贴住方块底面
        poseStack.translate(0.0D, -1.5D, 0.0D);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
