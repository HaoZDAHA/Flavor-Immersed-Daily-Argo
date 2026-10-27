package com.flavor_immersed_daily_argo.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

// 自定义渲染类型。
//
// 1.20.1 里 RenderStateShard 的状态常量（RENDERTYPE_LINES_SHADER、NO_CULL…）都是 protected，
// 外部包不能直接写 RenderStateShard.NO_CULL；这里改为继承 RenderStateShard，在子类体内
// 按简单名取到这些常量，再把拼好的 RenderType 暴露出去，供各渲染器共用。
public final class FidRenderTypes extends RenderStateShard {

// 鱼线：折线，写回主缓冲（原版那条走 ITEM_ENTITY_TARGET）。
//
// 「极佳」画质下水在单独缓冲里后合成，线若进了另一块缓冲就不参与主缓冲的深度比较，
// 看上去像被水浸着。改回主缓冲并保留深度写入后，水面会按深度正确遮挡，线在水上方始终是实的。
    public static final RenderType FISHING_LINE = RenderType.create(
            "flavor_immersed_daily_argo:fishing_line",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINE_STRIP,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

// 测距绳的贴地方框：2 像素宽的线，带深度测试，被地形挡住时不穿墙
    public static final RenderType CHUNK_BORDER = RenderType.create(
            "flavor_immersed_daily_argo:ranging_rope_chunk_border",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLineState(new LineStateShard(OptionalDouble.of(2.0D)))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

// 无人机高亮线框：深度测试永远通过，隔着方块也能看见
    public static final RenderType UAV_HIGHLIGHT = RenderType.create(
            "flavor_immersed_daily_argo:uav_block_highlight",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLineState(new LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    private FidRenderTypes() {
        super("flavor_immersed_daily_argo:render_types", () -> {
        }, () -> {
        });
    }
}
