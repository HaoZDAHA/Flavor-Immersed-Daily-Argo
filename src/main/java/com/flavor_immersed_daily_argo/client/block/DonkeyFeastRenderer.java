package com.flavor_immersed_daily_argo.client.block;

import com.flavor_immersed_daily_argo.client.entity.model.DonkeyFeastModel;
import com.flavor_immersed_daily_argo.client.entity.model.DonkeyFeastStage1Model;
import com.flavor_immersed_daily_argo.client.entity.model.DonkeyFeastStage2Model;
import com.flavor_immersed_daily_argo.client.entity.model.DonkeyFeastStage3Model;
import com.flavor_immersed_daily_argo.block.DonkeyFeastBlockEntity;
import com.flavor_immersed_daily_argo.block.EntityFoodDishBlock;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

// 全驴宴方块实体渲染器
//
// 圆桌 3 格长、菜品带大量嵌套旋转，导出成的是实体模型，写不进方块模型 JSON，所以不参与
// 区块网格烘焙，由本渲染器按原始尺寸（1 像素 = 1/16 格）绘制，并跟随方块朝向。
//
// 一共 4 个模型阶段：0 是满满一桌，1/2/3 依次吃空。整桌 20 口，每吃 5 口换下一个模型。
// 桌上那圈菜按 Blockbench 导出的 round 动画慢慢转（绕竖轴一整圈约 32 秒），
// 角度取全局游戏时间，所以相邻几桌的相位天然对齐。
public class DonkeyFeastRenderer implements BlockEntityRenderer<DonkeyFeastBlockEntity> {

// 模型贴图（256×256，由 bbmodel 内嵌贴图导出）
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            flavor_immersed_daily_argo.MODID, "textures/entity/donkey_feast.png");

// 圆桌转一圈的时长：导出动画 round 长 31.9167 秒
    private static final float TURN_TICKS = 31.9167F * 20.0F;

// 模型阶段数，以及每个阶段顶几口（20 口 / 4 个模型）
    private static final int MODEL_STAGES = 4;
    private static final int BITES_PER_MODEL = 5;

    private final List<HierarchicalModel<Entity>> models;

    public DonkeyFeastRenderer(BlockEntityRendererProvider.Context context) {
        this.models = List.of(
                new DonkeyFeastModel(context.getModelSet().bakeLayer(DonkeyFeastModel.LAYER_LOCATION)),
                new DonkeyFeastStage1Model(context.getModelSet().bakeLayer(DonkeyFeastStage1Model.LAYER_LOCATION)),
                new DonkeyFeastStage2Model(context.getModelSet().bakeLayer(DonkeyFeastStage2Model.LAYER_LOCATION)),
                new DonkeyFeastStage3Model(context.getModelSet().bakeLayer(DonkeyFeastStage3Model.LAYER_LOCATION)));
    }

    @Override
    public void render(DonkeyFeastBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();

        float angle = 0.0F;
        if (blockEntity.getLevel() != null) {
            angle = (blockEntity.getLevel().getGameTime() + partialTick) % TURN_TICKS / TURN_TICKS * 360.0F;
        }

        int stage = state.getValue(EntityFoodDishBlock.STAGE);
        HierarchicalModel<Entity> model = this.models.get(Math.min(MODEL_STAGES - 1, stage / BITES_PER_MODEL));
        model.root().getChild("bone").yRot = angle * Mth.DEG_TO_RAD;

        Direction facing = state.getValue(EntityFoodDishBlock.FACING);
        renderModel(poseStack, bufferSource, packedLight, packedOverlay, model, facing);
    }

// 按原始尺寸绘制整桌宴席（方块与物品共用）。
//
// 导出模型与 bbmodel 设计空间之间只差一个 Y 翻转（java_y = 24 - bb_y），
// 这里用 scale(-1, -1, 1) 翻回来：同时翻转 x、y 可以保持三角形绕序不变，
// 避免面被背面剔除掉。翻回后桌腿落地线（bb_y = 0）正好贴着方块底面。
    public static void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                                   HierarchicalModel<Entity> model, Direction facing) {
        poseStack.pushPose();
// 1) 移到方块中心（水平方向居中）
        poseStack.translate(0.5D, 0.0D, 0.5D);
// 2) 按 facing 旋转，让整桌跟着转向
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
// 3) 翻回 bbmodel 设计空间（同时翻转 x、y 可保持三角形绕序不变，避免面被背面剔除）。
//    注意：ModelPart 烘焙时已把像素除以 16，模型此刻已是“格”为单位，这里不能再乘 1/16
        poseStack.scale(-1.0F, -1.0F, 1.0F);
// 4) 抵消模型根节点 y = 24 像素（1.5 格）的偏移，使 bb_y = 0 的落地线贴住方块底面
        poseStack.translate(0.0D, -1.5D, 0.0D);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
