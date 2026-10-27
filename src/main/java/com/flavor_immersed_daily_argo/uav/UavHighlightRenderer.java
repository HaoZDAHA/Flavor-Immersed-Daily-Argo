package com.flavor_immersed_daily_argo.uav;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import com.flavor_immersed_daily_argo.entity.Uav;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.OptionalDouble;

// 无人机高亮标记模块的线框渲染。
//
// 附身飞无人机时右键方块，服务端会把那一格记进无人机的同步数据（见 Uav#markBlock）。
// 这里在关卡渲染的末尾给那格画一圈线框——
//
// 无视地形：这个 RenderType 关掉了深度测试（RenderStateShard#NO_DEPTH_TEST），
// 所以隔着墙也能看见，正好当「透视标记」用。
// 仅限于附身视角：只有本机正在操作这架无人机时才画（见 UavClient#getOperatingUav()），
// 退出操作视角线框立即消失。
// 只要在渲染区块内：方块所在的区块没加载就不画。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class UavHighlightRenderer {

// 线框颜色：暖黄
    private static final float RED = 1.0F;
    private static final float GREEN = 0.85F;
    private static final float BLUE = 0.15F;

// 穿墙线框用的渲染类型：照着原版 RenderType.LINES 拼，只把深度测试换成「永远通过」。
// 顶点格式与 LevelRenderer#renderVoxelShape 一致（位置 + 颜色 + 法线）。
    private static final RenderType HIGHLIGHT_LINES = RenderType.create(
            "uav_block_highlight",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    private UavHighlightRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
// 挑最靠后的世界渲染阶段画：地形、实体、半透明方块和粒子都画完了，
// 再加上这个 RenderType 关掉了深度测试，线框才稳定地盖在这一帧所有东西上面
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Uav uav = UavClient.getOperatingUav();
        if (uav == null) {
            return;
        }
        BlockPos pos = uav.getHighlightPos();
        if (pos == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !level.isLoaded(pos)) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getShape(level, pos);
        if (shape.isEmpty()) {
// 没有碰撞形状的方块（火把之类）就没得描边了，退回整格方块，免得右键了却什么都不显示
            shape = Shapes.block();
        }

        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
// 关卡渲染的坐标系原点在相机上，所以要减掉相机位置
        poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(HIGHLIGHT_LINES);
// 注意：NO_DEPTH_TEST 这个 shard 在深度函数为 519(always) 时是「什么都不做」的，
// 而关卡渲染到这一步深度测试还开着，所以必须手动关掉，线框才能真正隔着方块显示
        RenderSystem.disableDepthTest();
        LevelRenderer.renderVoxelShape(poseStack, consumer, shape, 0.0D, 0.0D, 0.0D, RED, GREEN, BLUE, 1.0F, true);
        buffers.endBatch(HIGHLIGHT_LINES);
// 画完立刻恢复，后面的天气 / 世界边界还指望深度测试保持正常
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }
}
