package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.OptionalDouble;

// 测距绳：手持时在当前所在区块的地表画一圈方形边框，便于确认林业网点的范围。
//
// 只画贴地的一圈水平方框——不带竖直方向的边，且保留深度测试（不穿墙），
// 站在地块里就能一眼看出区块的实际范围。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class RangingRopeRenderer {

// 线框颜色：青绿
    private static final float RED = 0.20F;
    private static final float GREEN = 0.90F;
    private static final float BLUE = 0.90F;

// 贴地方框的渲染类型：带深度测试，被地形挡住时不穿墙
    private static final RenderType CHUNK_OUTLINE = RenderType.create(
            "ranging_rope_chunk_border",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(2.0D)))
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    private RangingRopeRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            return;
        }
        if (!holdingRope(player)) {
            return;
        }

        ChunkPos chunk = player.chunkPosition();
        double x0 = chunk.getMinBlockX();
        double z0 = chunk.getMinBlockZ();
        double x1 = x0 + 16.0D;
        double z1 = z0 + 16.0D;
// 只取玩家脚下那格地表的高度，方框正好落在脚边的地面上；略微抬高避免与地面 z-fighting
        double y = level.getHeight(Heightmap.Types.WORLD_SURFACE, player.getBlockX(), player.getBlockZ()) + 0.02D;

        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
// 关卡渲染的坐标系原点在相机上，所以要减掉相机位置
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        PoseStack.Pose pose = poseStack.last();

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(CHUNK_OUTLINE);
        line(consumer, pose, x0, y, z0, x1, y, z0);
        line(consumer, pose, x1, y, z0, x1, y, z1);
        line(consumer, pose, x1, y, z1, x0, y, z1);
        line(consumer, pose, x0, y, z1, x0, y, z0);
        buffers.endBatch(CHUNK_OUTLINE);
        poseStack.popPose();
    }

// 画一条线段（两顶点共用朝向法线）
    private static void line(VertexConsumer consumer, PoseStack.Pose pose,
                             double ax, double ay, double az, double bx, double by, double bz) {
        float nx = (float) (bx - ax);
        float ny = (float) (by - ay);
        float nz = (float) (bz - az);
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 1.0E-5F) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        consumer.addVertex(pose, (float) ax, (float) ay, (float) az).setColor(RED, GREEN, BLUE, 1.0F).setNormal(nx, ny, nz);
        consumer.addVertex(pose, (float) bx, (float) by, (float) bz).setColor(RED, GREEN, BLUE, 1.0F).setNormal(nx, ny, nz);
    }

    private static boolean holdingRope(LocalPlayer player) {
        return player.getMainHandItem().is(ModItems.RANGING_ROPE.get())
                || player.getOffhandItem().is(ModItems.RANGING_ROPE.get());
    }
}
