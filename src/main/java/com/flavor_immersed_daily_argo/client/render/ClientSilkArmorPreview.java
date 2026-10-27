package com.flavor_immersed_daily_argo.client.render;

import com.flavor_immersed_daily_argo.textile.SilkArmorPreview;
import com.flavor_immersed_daily_argo.textile.SilkDesign;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

// 丝绸装备 tooltip 里的「最终模型」预览。
//
// 按住 Shift 时，tooltip 上会多出一格方块，里面缓缓旋转的就是这件装备穿上身的样子：用
// SilkArmorTextures 现算出的花纹贴图、以及玩家在缝纫机里堆出来的 3D 体素，和界面上预览、
// 穿在身上三点一致。
//
// 不按 Shift 时高度 / 宽度都返回 0，等于没有这一格（tooltip 每帧都会重新量一遍，所以按下 / 松开
// Shift 会立刻展开 / 收起）。
public class ClientSilkArmorPreview implements ClientTooltipComponent {

// 预览格子的边长
    private static final int SIZE = 64;
// 模型离格子边的留白
    private static final float MARGIN = 8.0F;
// 视口里那块模型写进深度缓冲时才用得上的 z（放在 tooltip 背景之后，才不会被盖住）
    private static final float VIEW_Z = 50.0F;
// 俯角，稍微从上往下看一点
    private static final float VIEW_PITCH = 15.0F;
// 转一圈要多久（毫秒）
    private static final long SPIN_PERIOD = 8000L;

    private static final int FULL_BRIGHT = 0xF000F0;

// 原版人形骨架，整个 tooltip 复用同一个（渲染是单线程的，逐帧改可见性没问题）
    private static ModelPart root;
    private static HumanoidModel<LivingEntity> model;

    private final ItemStack stack;
    private final EquipmentSlot slot;

    public ClientSilkArmorPreview(SilkArmorPreview preview) {
        this.stack = preview.stack();
        this.slot = this.stack.getItem() instanceof ArmorItem armor
                ? armor.getEquipmentSlot() : EquipmentSlot.CHEST;
    }

// 按住 Shift 才占地方、才画
    private static boolean shown() {
        return Screen.hasShiftDown();
    }

    @Override
    public int getHeight() {
        return shown() ? SIZE : 0;
    }

    @Override
    public int getWidth(Font font) {
        return shown() ? SIZE : 0;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics gui) {
        if (!shown()) {
            return;
        }
        ensureModel();
        model.setAllVisible(false);
        applyVisibility(this.slot);

        Vector3f center = new Vector3f();
        float span = computeSpan(this.slot, center);
        float scale = (SIZE - 2.0F * MARGIN) / Math.max(span, 0.25F);
        float yaw = (Util.getMillis() % SPIN_PERIOD) / (float) SPIN_PERIOD * 360.0F;

        Matrix4f view = new Matrix4f()
                .translate(x + SIZE / 2.0F, y + SIZE / 2.0F, VIEW_Z)
                .scale(scale, scale, -scale)
                .rotateX((float) Math.toRadians(VIEW_PITCH))
                .rotateY((float) Math.toRadians(yaw))
                .translate(-center.x, -center.y, -center.z);

        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.mulPose(view);
        Lighting.setupForEntityInInventory();
        MultiBufferSource.BufferSource buffers = gui.bufferSource();
        VertexConsumer consumer = buffers.getBuffer(
                RenderType.armorCutoutNoCull(SilkArmorTextures.get(this.stack, this.slot)));
        model.renderToBuffer(pose, consumer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        gui.flush();

        renderVoxels(pose, gui);
        Lighting.setupFor3DItems();
        pose.popPose();
    }

// 把玩家在缝纫机里堆出来的体素也画出来（和穿在身上、界面预览用的是同一套画法）
    private void renderVoxels(PoseStack pose, GuiGraphics gui) {
        List<Integer> voxels = SilkDesign.getVoxels(this.stack);
        int count = SilkDesign.voxelCount(voxels);
        if (count == 0) {
            return;
        }
        int[] cubes = SilkDesign.cubesFor(this.slot);
        VertexConsumer consumer = gui.bufferSource().getBuffer(RenderType.debugQuads());
        for (int index = 0; index < count; index++) {
            int cube = SilkDesign.voxelCube(voxels, index);
            if (!SilkDesignLayer.contains(cubes, cube)) {
                continue;
            }
            ModelPart part = SilkDesignLayer.partOf(model, cube);
            if (part == null) {
                continue;
            }
            pose.pushPose();
            part.translateAndRotate(pose);
            SilkDesignLayer.addVoxelCube(consumer, pose.last().pose(),
                    SilkDesign.originX(cube) + SilkDesign.voxelI(voxels, index),
                    SilkDesign.originY(cube) + SilkDesign.voxelJ(voxels, index),
                    SilkDesign.originZ(cube) + SilkDesign.voxelK(voxels, index),
                    SilkDesign.voxelColor(voxels, index));
            pose.popPose();
        }
        gui.flush();
    }

// 与缝纫机预览一致：只显示当前部位对应的部件。
// 头部刻意不显示原版的 hat 部件——它采样的是贴图 32,0 那块我们从不绘制的区域，
// 一旦画出来就是一整块白壳把头包住，花纹全被盖掉。
    private static void applyVisibility(EquipmentSlot slot) {
        switch (slot) {
            case HEAD -> model.head.visible = true;
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            default -> {
            }
        }
    }

// 这个槽位会用到的部件（与穿戴渲染层一致），用于量包围盒
    private static int[] cubesFor(EquipmentSlot slot) {
        return SilkDesign.cubesFor(slot);
    }

// 量出这个槽位对应部件在模型坐标里的包围盒，写中心到 center，返回最长边（单位格）。
//
// 用来决定默认缩放与旋转轴心，免得头和腿大小差太多、转起来就出框。
    private static float computeSpan(EquipmentSlot slot, Vector3f center) {
        int[] wanted = cubesFor(slot);
        float[] bounds = {
                Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        root.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (!SilkDesignLayer.contains(wanted, cubeIdOf(path))) {
                return;
            }
            Matrix4f matrix = pose.pose();
            for (int xi = 0; xi < 2; xi++) {
                for (int yi = 0; yi < 2; yi++) {
                    for (int zi = 0; zi < 2; zi++) {
                        Vector3f p = matrix.transformPosition(
                                (xi == 0 ? cube.minX : cube.maxX) / 16.0F,
                                (yi == 0 ? cube.minY : cube.maxY) / 16.0F,
                                (zi == 0 ? cube.minZ : cube.maxZ) / 16.0F,
                                new Vector3f());
                        bounds[0] = Math.min(bounds[0], p.x);
                        bounds[1] = Math.min(bounds[1], p.y);
                        bounds[2] = Math.min(bounds[2], p.z);
                        bounds[3] = Math.max(bounds[3], p.x);
                        bounds[4] = Math.max(bounds[4], p.y);
                        bounds[5] = Math.max(bounds[5], p.z);
                    }
                }
            }
        });
        center.set((bounds[0] + bounds[3]) / 2.0F, (bounds[1] + bounds[4]) / 2.0F,
                (bounds[2] + bounds[5]) / 2.0F);
        return Math.max(bounds[3] - bounds[0], Math.max(bounds[4] - bounds[1], bounds[5] - bounds[2]));
    }

// 骨架里某个部件的路径 → cube 编号
    private static int cubeIdOf(String path) {
        return switch (path) {
            case "/head" -> SilkDesign.CUBE_HEAD;
            case "/body" -> SilkDesign.CUBE_BODY;
            case "/left_arm" -> SilkDesign.CUBE_LEFT_ARM;
            case "/right_arm" -> SilkDesign.CUBE_RIGHT_ARM;
            case "/left_leg" -> SilkDesign.CUBE_LEFT_LEG;
            case "/right_leg" -> SilkDesign.CUBE_RIGHT_LEG;
            default -> -1;
        };
    }

// 第一次用到时烘一份原尺寸的人形骨架
    private static void ensureModel() {
        if (model != null) {
            return;
        }
        root = LayerDefinition
                .create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32)
                .bakeRoot();
        model = new HumanoidModel<>(root);
// 默认 young = true 会按婴儿缩放，预览要原尺寸
        model.young = false;
    }
}
