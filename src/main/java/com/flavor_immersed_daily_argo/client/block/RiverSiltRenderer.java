package com.flavor_immersed_daily_argo.client.block;

import com.flavor_immersed_daily_argo.herding.RiverSiltBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

// 河道淤泥方块实体渲染器
//
// 把养在淤泥里的东西平铺在方块顶面（方块正好一格高，顶面就是 y = 1.0）：
//
// 未成熟：只画一份苗种，尺寸随生长进度从六成放大到原大；
// 成熟：画三份产物，各自错开位置、转一个角度，像散落在淤泥上。
public class RiverSiltRenderer implements BlockEntityRenderer<RiverSiltBlockEntity> {

// 平铺在顶面：略高于 1.0，避免和方块顶面 Z-fighting
    private static final double SURFACE_Y = 1.001D;

// 未成熟苗种的大小：从 0.6 长到 1.0
    private static final float SEED_MIN_SCALE = 0.6F;
    private static final float SEED_MAX_SCALE = 1.0F;

// 成熟后三份产物的摆法：X 偏移、Z 偏移、绕竖轴角度、大小
    private static final float[][] MATURE_LAYOUT = {
            {-0.22F, -0.16F, 25.0F, 0.72F},
            {0.20F, -0.05F, 140.0F, 0.78F},
            {0.02F, 0.22F, 285.0F, 0.68F}
    };

// 每份物品在基准位置上的随机浮动：XZ 最大偏移量
    private static final float XZ_JITTER = 0.14F;

    private final ItemRenderer itemRenderer;

    public RiverSiltRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RiverSiltBlockEntity silt, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!silt.hasCrop()) {
            return;
        }
// 淤泥是不透明的整格方块，方块实体拿到的 packedLight 取自它自身位置，几乎为 0；
// 物品实际躺在顶面上，所以改取正上方那一格的光照，否则渲染出来是黑的
        int light = silt.getLevel() == null
                ? packedLight
                : LevelRenderer.getLightColor(silt.getLevel(), silt.getBlockPos().above());
        if (silt.isMature()) {
            ItemStack result = silt.getResult();
            if (result.isEmpty()) {
                return;
            }
            for (int i = 0; i < MATURE_LAYOUT.length; i++) {
                float[] layout = MATURE_LAYOUT[i];
                this.drawItem(poseStack, bufferSource, light, packedOverlay, result,
                        layout[0], layout[1], layout[2], layout[3], i, silt);
            }
            return;
        }
        float progress = silt.growthProgress();
        float scale = SEED_MIN_SCALE + (SEED_MAX_SCALE - SEED_MIN_SCALE) * progress;
        this.drawItem(poseStack, bufferSource, light, packedOverlay, silt.getSeed(),
                0.0F, 0.0F, 15.0F, scale, 0, silt);
    }

// 平铺一份物品：先按偏移站到顶面上，再绕竖轴转向，最后绕 X 轴翻倒成躺平
    private void drawItem(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                          ItemStack stack, float offsetX, float offsetZ, float yRot, float scale, int index,
                          RiverSiltBlockEntity silt) {
// 以方块坐标和序号作种子，每个位置、每一份物品都有稳定的随机偏移与朝向，不会随帧抖动
        BlockPos pos = silt.getBlockPos();
        RandomSource random = RandomSource.create(pos.asLong() * 341873128712L + index * 132897987541L);
        float jitterX = (random.nextFloat() - 0.5F) * XZ_JITTER;
        float jitterZ = (random.nextFloat() - 0.5F) * XZ_JITTER;
// 朝向在整个圆周里随机，看起来更自然
        float randomRot = random.nextFloat() * 360.0F;
        poseStack.pushPose();
        poseStack.translate(0.5D + offsetX + jitterX, SURFACE_Y, 0.5D + offsetZ + jitterZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot + randomRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, bufferSource, silt.getLevel(), 0);
        poseStack.popPose();
    }
}
