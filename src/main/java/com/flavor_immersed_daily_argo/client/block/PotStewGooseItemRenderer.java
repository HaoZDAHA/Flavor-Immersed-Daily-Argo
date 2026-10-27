package com.flavor_immersed_daily_argo.client.block;

import com.flavor_immersed_daily_argo.client.entity.model.PotStewGooseStage0Model;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

// 铁锅炖大鹅物品渲染器（GUI / 手持 / 掉落物）
//
// 物品模型 JSON 的父模型是 builtin/entity，原版会走
// IClientItemExtensions#getCustomRenderer()，因此这里复用方块实体渲染器的
// 模型与变换，把完整那一锅（阶段 0）按原始尺寸画出来（缩放/角度由物品模型 JSON 的
// display 控制）。
public class PotStewGooseItemRenderer extends BlockEntityWithoutLevelRenderer {

    private final PotStewGooseStage0Model model;

    public PotStewGooseItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        this.model = new PotStewGooseStage0Model(modelSet.bakeLayer(PotStewGooseStage0Model.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        PotStewGooseRenderer.renderModel(poseStack, buffer, packedLight, packedOverlay, this.model, Direction.NORTH);
    }
}
