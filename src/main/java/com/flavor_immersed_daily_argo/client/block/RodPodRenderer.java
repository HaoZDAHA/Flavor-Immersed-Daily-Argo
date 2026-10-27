package com.flavor_immersed_daily_argo.client.block;

import com.flavor_immersed_daily_argo.client.entity.renderer.ModFishingHookRenderer;
import com.flavor_immersed_daily_argo.fishing.FishingRodModelOverrides;
import com.flavor_immersed_daily_argo.fishing.RodPodBlock;
import com.flavor_immersed_daily_argo.fishing.RodPodBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import org.joml.Matrix4f;
import org.joml.Vector3f;

// 竿架方块实体渲染器：把架上那根南山钓竿斜靠在托架上画出来，并从竿梢拉一条鱼线到浮漂的落点，
// 落点上再补画一枚浮漂——架竿时那一竿的浮漂实体已经收回，这里画出来才看得出还在钓。
public class RodPodRenderer implements BlockEntityRenderer<RodPodBlockEntity> {

// 鱼线细分段数，与原版浮漂的鱼线一致
    private static final int LINE_SEGMENTS = 16;
// 鱼线下垂幅度（格）
    private static final double SAG = 0.12D;
// 浮漂贴图画在鱼钩位置上方这么多格，与原版一致
    private static final double BOBBER_OFFSET = 0.25D;
// 鱼线起点在物品模型里的位置。
//
// 竿体贴图里竿尖画在 (14, 1) 像素，换算到 0~1 是 (0.906, 0.906)；这里再往里收半个像素，
// 保证线头压在竿尖的像素上，不会浮在贴图右上角的透明区里。
    private static final Vector3f ROD_TIP = new Vector3f(0.875F, 0.875F, 0.5F);
// 鱼线起点沿竿身往里缩这么多格（只缩水平方向），保证线头压在竿子上
    private static final float LINE_INSET = 0.08F;
// 鱼线起点比竿梢再抬高这么多格（一像素），免得线头掉到竿尖下面
    private static final float LINE_RISE = 1.0F / 16.0F;

    private final ItemRenderer itemRenderer;

    public RodPodRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RodPodBlockEntity pod, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack rod = pod.getRod();
        if (rod.isEmpty()) {
            return;
        }
        Direction facing = pod.getBlockState().getValue(RodPodBlock.FACING);

        poseStack.pushPose();
// 先站到方块中心，再整体按方块的朝向转，最后往里挪一点落到立柱的托架上。
// 这样竿子跟 blockstate 里的模型转的是同一个角度，四个朝向都压在同一处托架上。
        poseStack.translate(0.5D, 1.19D, 0.5D);
// blockstate 里 facing 是按 90° 一档转模型的，这里用同一个角度（north→0、east→90、south→180、west→270）
        float blockY = (facing.toYRot() + 180.0F) % 360.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(-blockY));
// 再顺时针转 90°，竿身才朝右侧倒
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(0.0D, 0.0D, -0.1D);
// 贴图里的竿子走的是对角线，绕 Z 转 45° 把它摆平，竿尖朝模型的 +X 方向
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0F));
// 物品在 FIXED 视角下自带一次 Y 轴 180°（继承自 item/generated），放在最里层补掉，
// 免得竿子被翻个面
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

// 竿架上画的是玩家组装好的竿子：竿体 + 线轮。
// 贴图里的鱼线 / 浮漂 / 铅坠都不叠——鱼线下面自己画，浮漂只画在落点上
        BakedModel model = FishingRodModelOverrides.podModel(rod);
        this.itemRenderer.render(rod, ItemDisplayContext.FIXED, false, poseStack, bufferSource,
                packedLight, packedOverlay, model);

// 竿梢在方块坐标系里的位置：把模型局部坐标里的竿尖经同一套视角变换送出来，
// 之后整条鱼线都在方块坐标系里画，落点不用再反算进模型空间
        ForgeHooksClient.handleCameraTransforms(poseStack, model, ItemDisplayContext.FIXED, false);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        Vector3f rodTip = new Matrix4f(poseStack.last().pose()).transformPosition(new Vector3f(ROD_TIP));
        poseStack.popPose();

        if (pod.hasCastPos()) {
            renderFishingLine(pod, rodTip, poseStack, bufferSource);
            renderBobber(pod, rod, poseStack, bufferSource, packedLight);
        }
    }

// 竿梢到浮漂落点的鱼线。
//
// 画法与浮漂渲染器一致：黑色折线，中间略微下垂。
    private static void renderFishingLine(RodPodBlockEntity pod, Vector3f tip, PoseStack poseStack,
                                          MultiBufferSource buffer) {
        Vec3 cast = pod.getCastPos();
        BlockPos origin = pod.getBlockPos();
        float endX = (float) (cast.x - origin.getX());
        float endY = (float) (cast.y - origin.getY() + BOBBER_OFFSET);
        float endZ = (float) (cast.z - origin.getZ());

        float dx = endX - tip.x;
        float dy = endY - tip.y;
        float dz = endZ - tip.z;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-4F) {
            return;
        }

// 起点只沿水平方向往竿身里缩，竖直方向则抬高一个像素：
// 若照鱼线方向缩，起点会被带着往浮漂那边沉一点，线头看起来就掉在竿尖下面了
        float flatLength = (float) Math.sqrt(dx * dx + dz * dz);
        float insetX = flatLength > 1.0E-4F ? dx / flatLength * LINE_INSET : 0.0F;
        float insetZ = flatLength > 1.0E-4F ? dz / flatLength * LINE_INSET : 0.0F;
        float startX = tip.x - insetX;
        float startY = tip.y + LINE_RISE;
        float startZ = tip.z - insetZ;

        VertexConsumer consumer = buffer.getBuffer(ModFishingHookRenderer.LINE_TYPE);
        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i <= LINE_SEGMENTS; i++) {
            float t = (float) i / (float) LINE_SEGMENTS;
            float sag = (float) (SAG * t * (1.0D - t));
            consumer.vertex(pose.pose(),
                            startX + dx * t,
                            startY + dy * t - sag,
                            startZ + dz * t)
                    .color(0, 0, 0, 255)
                    .normal(pose.normal(), dx / length, dy / length, dz / length);
        }
    }

// 落点处的浮漂：照浮漂实体的画法，一张朝向镜头的方片，贴图用这根竿子装的浮漂。
    private static void renderBobber(RodPodBlockEntity pod, ItemStack rod, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight) {
        Vec3 cast = pod.getCastPos();
        BlockPos origin = pod.getBlockPos();
        int light = pod.getLevel() == null
                ? packedLight
                : LevelRenderer.getLightColor(pod.getLevel(), BlockPos.containing(cast));

        poseStack.pushPose();
        poseStack.translate(cast.x - origin.getX(), cast.y - origin.getY(), cast.z - origin.getZ());
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(ModFishingHookRenderer.floatTexture(rod)));
        bobberVertex(consumer, pose, light, 0.0F, 0, 0, 1);
        bobberVertex(consumer, pose, light, 1.0F, 0, 1, 1);
        bobberVertex(consumer, pose, light, 1.0F, 1, 1, 0);
        bobberVertex(consumer, pose, light, 0.0F, 1, 0, 0);
        poseStack.popPose();
    }

    private static void bobberVertex(VertexConsumer consumer, PoseStack.Pose pose, int packedLight,
                                     float x, int y, int u, int v) {
        consumer.vertex(pose.pose(), x - 0.5F, (float) y - 0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .uv((float) u, (float) v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F);
    }
}
