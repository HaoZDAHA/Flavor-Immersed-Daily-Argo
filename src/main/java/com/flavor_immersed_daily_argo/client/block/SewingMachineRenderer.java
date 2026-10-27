package com.flavor_immersed_daily_argo.client.block;

import com.flavor_immersed_daily_argo.textile.SewingMachineBlock;
import com.flavor_immersed_daily_argo.textile.SewingMachineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

// 缝纫机方块实体渲染器
//
// 把台面上那件正在加工的空白丝绸装备平铺在台面板上：高约 16 像素（y = 1.0），
// 沿模型正方向再挪 2 像素（x = +0.125），并随方块的 FACING 一起转。
public class SewingMachineRenderer implements BlockEntityRenderer<SewingMachineBlockEntity> {

// 台面上的抬升高度：16 像素
    private static final double GARMENT_Y = 1.0D;
// 沿模型正方向的偏移：2 像素
    private static final double GARMENT_OFFSET_X = 2.0D / 16.0D;

    private final ItemRenderer itemRenderer;

    public SewingMachineRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(SewingMachineBlockEntity machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack garment = machine.getGarment();
        if (garment.isEmpty()) {
            return;
        }
        Direction facing = machine.getBlockState().getValue(SewingMachineBlock.FACING);

        poseStack.pushPose();
// 站到方块中心，沿正方向挪 2 像素，抬到台面上
        poseStack.translate(0.5D + GARMENT_OFFSET_X, GARMENT_Y, 0.5D);
// 和 blockstate 转模型用的是同一个角度（north→0、east→90、south→180、west→270）
        float blockY = (facing.toYRot() + 180.0F) % 360.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(-blockY));
// 平铺在台面上
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(1.2F, 1.2F, 1.2F);

        this.itemRenderer.renderStatic(garment, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, bufferSource, machine.getLevel(), 0);
        poseStack.popPose();
    }
}
