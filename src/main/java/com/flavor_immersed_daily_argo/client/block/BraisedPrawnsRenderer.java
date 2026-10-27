package com.flavor_immersed_daily_argo.client.block;

import com.flavor_immersed_daily_argo.client.entity.model.BraisedPrawnsStage0Model;
import com.flavor_immersed_daily_argo.client.entity.model.BraisedPrawnsStage1Model;
import com.flavor_immersed_daily_argo.client.entity.model.BraisedPrawnsStage2Model;
import com.flavor_immersed_daily_argo.client.entity.model.BraisedPrawnsStage3Model;
import com.flavor_immersed_daily_argo.block.BraisedPrawnsBlockEntity;
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

// 油焖大虾方块实体渲染器
//
// 锅和一圈虾都带多段嵌套旋转，导出成的是实体模型，写不进方块模型 JSON，所以不参与
// 区块网格烘焙，由本渲染器按原始尺寸（1 像素 = 1/16 格）绘制，并跟随方块朝向。
//
// 4 个模型就是 4 个阶段：每吃掉一口，方块状态的 stage 前进一格，这里就换上
// 对应阶段的模型；第 4 个（stage=3）是最空的那一锅，再右键整锅收走。
public class BraisedPrawnsRenderer implements BlockEntityRenderer<BraisedPrawnsBlockEntity> {

// 模型贴图（128×128，由 bbmodel 内嵌贴图导出）
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            flavor_immersed_daily_argo.MODID, "textures/entity/braised_prawns.png");

// 4 个阶段按吃掉的顺序排列，下标 = 方块状态的 stage
    private final List<HierarchicalModel<Entity>> stages;

    public BraisedPrawnsRenderer(BlockEntityRendererProvider.Context context) {
        var modelSet = context.getModelSet();
        this.stages = List.of(
                new BraisedPrawnsStage0Model(modelSet.bakeLayer(BraisedPrawnsStage0Model.LAYER_LOCATION)),
                new BraisedPrawnsStage1Model(modelSet.bakeLayer(BraisedPrawnsStage1Model.LAYER_LOCATION)),
                new BraisedPrawnsStage2Model(modelSet.bakeLayer(BraisedPrawnsStage2Model.LAYER_LOCATION)),
                new BraisedPrawnsStage3Model(modelSet.bakeLayer(BraisedPrawnsStage3Model.LAYER_LOCATION)));
    }

    @Override
    public void render(BraisedPrawnsBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        int stage = Mth.clamp(state.getValue(EntityFoodDishBlock.STAGE), 0, this.stages.size() - 1);
        Direction facing = state.getValue(EntityFoodDishBlock.FACING);
        renderModel(poseStack, bufferSource, packedLight, packedOverlay, this.stages.get(stage), facing);
    }

// 按原始尺寸绘制某个阶段的模型（方块与物品共用）。
//
// 导出模型与 bbmodel 设计空间之间只差一个 Y 翻转（java_y = 24 - bb_y），
// 这里用 scale(-1, -1, 1) 翻回来：同时翻转 x、y 可以保持三角形绕序不变，
// 避免面被背面剔除掉。翻回后模型的落地线（bb_y = 0）正好贴着方块底面。
    public static void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                                   HierarchicalModel<Entity> model, Direction facing) {
        poseStack.pushPose();
// 1) 移到方块中心（水平方向居中）
        poseStack.translate(0.5D, 0.0D, 0.5D);
// 2) 按 facing 旋转，让整锅跟着转向
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
// 3) 翻回 bbmodel 设计空间（同时翻转 x、y 可保持三角形绕序不变，避免面被背面剔除）。
//    注意：ModelPart 烘焙时已把像素除以 16，模型此刻已是“格”为单位，这里不能再乘 1/16
        poseStack.scale(-1.0F, -1.0F, 1.0F);
// 4) 抵消模型根节点 y = 24 像素（1.5 格）的偏移，使 bb_y = 0 的落地线贴住方块底面
        poseStack.translate(0.0D, -1.5D, 0.0D);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF);
        poseStack.popPose();
    }
}
