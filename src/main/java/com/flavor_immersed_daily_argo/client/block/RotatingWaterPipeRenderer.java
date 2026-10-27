package com.flavor_immersed_daily_argo.client.block;

import com.flavor_immersed_daily_argo.client.entity.model.RotatingWaterPipeModel;
import com.flavor_immersed_daily_argo.block.RotatingWaterPipeBlock;
import com.flavor_immersed_daily_argo.block.RotatingWaterPipeBlockEntity;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.joml.Quaternionf;

// 旋转水管方块实体渲染器
//
// 整机（底座 + 立柱 + 旋转臂）不参与区块网格烘焙，由本渲染器按原始尺寸（1 像素 = 1/16 格）
// 绘制，臂按游戏时间匀速转，一圈 2 秒（与 Blockbench 导出的 round 等长）。
// 角度取全局游戏时间，所以并排的洒水器相位天然对齐。
//
// 平放：点在方块顶面上放下时（FACE=FLOOR）模型就按 Blockbench 里的原姿态立在地上，
// 底座贴在方块底面，旋转臂绕竖轴转。
//
// 贴墙：把立着的模型绕着方块中心横过来——上方向转到朝外（RotatingWaterPipeBlock#FACING），
// 底座落在墙面上。这一步用的旋转与 RotatingWaterPipeBlock#emitSpray 里换算水花位置的一模一样，
// 于是看到的水必然跟着模型走。
public class RotatingWaterPipeRenderer implements BlockEntityRenderer<RotatingWaterPipeBlockEntity> {

// 模型贴图（64×64，由 bbmodel 内嵌贴图导出）
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            flavor_immersed_daily_argo.MODID, "textures/entity/rotating_water_pipe.png");

    private final RotatingWaterPipeModel model;

    public RotatingWaterPipeRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new RotatingWaterPipeModel(context.getModelSet().bakeLayer(RotatingWaterPipeModel.LAYER_LOCATION));
    }

    @Override
    public void render(RotatingWaterPipeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float angle = 0.0F;
        if (blockEntity.getLevel() != null) {
            angle = (blockEntity.getLevel().getGameTime() + partialTick) % RotatingWaterPipeBlock.TURN_TICKS
                    / RotatingWaterPipeBlock.TURN_TICKS * 360.0F;
        }
// 导出动画让 bone2 绕 Y 由 0 转到 −360°；模型空间翻过 x/y 后，视觉上的旋转方向也反了一次
        this.model.setArmAngle(-angle);

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(RotatingWaterPipeBlock.FACING);
        boolean onGround = state.getValue(RotatingWaterPipeBlock.FACE) == AttachFace.FLOOR;

        poseStack.pushPose();
        if (!onGround) {
// 绕方块中心：先绕 X 把立着的模型放倒（上方向指向北），再绕 Y 转到贴住对应墙面
            Quaternionf upright = new Quaternionf().rotateX((float) Math.toRadians(-90.0D));
            Quaternionf turn = new Quaternionf().rotateY((float) Math.toRadians(180.0D - facing.toYRot()));
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(turn.mul(upright));
            poseStack.translate(-0.5D, -0.5D, -0.5D);
        }
// 平放时不做那步放倒：模型保持竖姿态，直接立在地上
// 移到方块中心（水平方向居中）
        poseStack.translate(0.5D, 0.0D, 0.5D);
// 翻回 bbmodel 设计空间（同时翻转 x、y 可保持三角形绕序不变，避免面被背面剔除）；
// 注意：ModelPart 烘焙时已把像素除以 16，模型此刻已是“格”为单位，这里不能再乘 1/16
        poseStack.scale(-1.0F, -1.0F, 1.0F);
// 抵消设计空间里 bb_y = 24 像素（1.5 格）的落地线，使底座贴住方块底面
        poseStack.translate(0.0D, -1.5D, 0.0D);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
