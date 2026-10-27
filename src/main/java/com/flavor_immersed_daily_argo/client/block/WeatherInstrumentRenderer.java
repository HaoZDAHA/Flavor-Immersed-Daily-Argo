package com.flavor_immersed_daily_argo.client.block;

import com.flavor_immersed_daily_argo.client.entity.model.WeatherInstrumentModel;
import com.flavor_immersed_daily_argo.block.WeatherInstrumentBlock;
import com.flavor_immersed_daily_argo.block.WeatherInstrumentBlockEntity;
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

// 气象仪器方块实体渲染器
//
// 模型约 2.8 格高，超出单方块模型能表达的范围，由本渲染器按原始尺寸绘制，并跟随方块朝向。
//
// 风车盘转速由方块状态 WeatherInstrumentBlock#HEAVY 决定：平时 6 秒一圈（none 动画），
// 该区块出现非常规天气时 0.5 秒一圈（heavy 动画）。角度取全局游戏时间，并排的仪器相位一致。
public class WeatherInstrumentRenderer implements BlockEntityRenderer<WeatherInstrumentBlockEntity> {

// 模型贴图（128×128，由 bbmodel 内嵌贴图导出）
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            flavor_immersed_daily_argo.MODID, "textures/entity/weather_instrument.png");

// none 动画一圈的时长：6 秒
    private static final float PERIOD_NONE = 6.0F * 20.0F;
// heavy 动画一圈的时长：0.5 秒
    private static final float PERIOD_HEAVY = 0.5F * 20.0F;

    private final WeatherInstrumentModel model;

    public WeatherInstrumentRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new WeatherInstrumentModel(
                context.getModelSet().bakeLayer(WeatherInstrumentModel.LAYER_LOCATION));
    }

    @Override
    public void render(WeatherInstrumentBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        boolean heavy = blockEntity.getBlockState().getValue(WeatherInstrumentBlock.HEAVY);
        Direction facing = blockEntity.getBlockState().getValue(WeatherInstrumentBlock.FACING);

        float period = heavy ? PERIOD_HEAVY : PERIOD_NONE;
        float angle = 0.0F;
        if (blockEntity.getLevel() != null) {
            angle = (blockEntity.getLevel().getGameTime() + partialTick) % period / period * 360.0F;
        }
// 导出的动画是朝负方向转的，这里取负保持方向一致
        this.model.setRoundAngle(-angle);

        poseStack.pushPose();
// 1) 移到方块中心（水平方向居中）
        poseStack.translate(0.5D, 0.0D, 0.5D);
// 2) 按 facing 旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
// 3) 翻回 bbmodel 设计空间（同时翻转 x、y 可保持三角形绕序不变，避免面被背面剔除）
        poseStack.scale(-1.0F, -1.0F, 1.0F);
// 4) 抵消模型根节点落地的 y = 24 像素（1.5 格），使 bb_y = 0 的落地线贴住方块底面
        poseStack.translate(0.0D, -1.5D, 0.0D);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
