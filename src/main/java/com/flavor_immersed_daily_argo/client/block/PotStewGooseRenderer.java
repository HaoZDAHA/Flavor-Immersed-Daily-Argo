package com.flavor_immersed_daily_argo.client.block;

import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage0Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage10Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage11Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage1Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage2Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage3Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage4Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage5Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage6Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage7Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage8Model;
import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage9Model;
import com.flavor_immersed_daily_argo.block.EntityFoodDishBlock;
import com.flavor_immersed_daily_argo.block.PotStewGooseBlockEntity;
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

// 铁锅炖大鹅方块实体渲染器
//
// 锅带一圈饼子、还带多段嵌套旋转，导出成的是实体模型，写不进方块模型 JSON，所以不参与
// 区块网格烘焙，由本渲染器按原始尺寸（1 像素 = 1/16 格）绘制，并跟随方块朝向。
//
// 12 个模型就是 12 个阶段：每吃掉一口，方块状态的 stage 前进一格，这里就换上
// 对应阶段的模型；第 12 个（stage=11）是最空的那一锅，再右键整锅收走。
public class PotStewGooseRenderer implements BlockEntityRenderer<PotStewGooseBlockEntity> {

// 模型贴图（128×128，由 bbmodel 内嵌贴图导出）
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            flavor_immersed_daily_argo.MODID, "textures/entity/pot_stew_goose.png");

// 12 个阶段按吃掉的顺序排列，下标 = 方块状态的 stage
    private final List<HierarchicalModel<Entity>> stages;

    public PotStewGooseRenderer(BlockEntityRendererProvider.Context context) {
        var modelSet = context.getModelSet();
        this.stages = List.of(
                new PotStewGooseStage0Model(modelSet.bakeLayer(PotStewGooseStage0Model.LAYER_LOCATION)),
                new PotStewGooseStage1Model(modelSet.bakeLayer(PotStewGooseStage1Model.LAYER_LOCATION)),
                new PotStewGooseStage2Model(modelSet.bakeLayer(PotStewGooseStage2Model.LAYER_LOCATION)),
                new PotStewGooseStage3Model(modelSet.bakeLayer(PotStewGooseStage3Model.LAYER_LOCATION)),
                new PotStewGooseStage4Model(modelSet.bakeLayer(PotStewGooseStage4Model.LAYER_LOCATION)),
                new PotStewGooseStage5Model(modelSet.bakeLayer(PotStewGooseStage5Model.LAYER_LOCATION)),
                new PotStewGooseStage6Model(modelSet.bakeLayer(PotStewGooseStage6Model.LAYER_LOCATION)),
                new PotStewGooseStage7Model(modelSet.bakeLayer(PotStewGooseStage7Model.LAYER_LOCATION)),
                new PotStewGooseStage8Model(modelSet.bakeLayer(PotStewGooseStage8Model.LAYER_LOCATION)),
                new PotStewGooseStage9Model(modelSet.bakeLayer(PotStewGooseStage9Model.LAYER_LOCATION)),
                new PotStewGooseStage10Model(modelSet.bakeLayer(PotStewGooseStage10Model.LAYER_LOCATION)),
                new PotStewGooseStage11Model(modelSet.bakeLayer(PotStewGooseStage11Model.LAYER_LOCATION)));
    }

    @Override
    public void render(PotStewGooseBlockEntity blockEntity, float partialTick, PoseStack poseStack,
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
        model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
