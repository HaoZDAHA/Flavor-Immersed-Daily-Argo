package com.flavor_immersed_daily_argo.client.render;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.textile.SilkArmorItem;
import com.flavor_immersed_daily_argo.textile.SilkDesign;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.List;

// 空白丝绸装备穿在身上的花纹渲染层
//
// 原版 HumanoidArmorLayer 的盔甲模型膨胀很大（外甲 1.0、内甲 0.5），穿上去会显得又厚又
// 大，和缝纫机里那块几乎贴着身体画出来的预览对不上。所以纯白的 silk_layer_1/2.png 已经
// 改成全透明（原版那一层等于什么都不画），丝绸装备整个交给这一层来渲染：用膨胀很小的模型，把
// SilkArmorTextures 现算出来的贴图（没画过的地方是纯白底色）叠上去，看起来就是一层薄薄的
// 布料。画幅里玩家自己加的像素方块也会一并还原。
public class SilkDesignLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

// 花纹层用的盔甲模型。膨胀刻意取远小于原版盔甲的值（外甲 0.5、内甲 0.3），
// 让丝绸看起来像贴身的薄布；仍要略大于玩家外层皮肤（0.25），免得被皮肤盖住 / z-fighting。
    public static final ModelLayerLocation OUTER_LAYER = new ModelLayerLocation(
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "silk_design_outer"), "main");
    public static final ModelLayerLocation INNER_LAYER = new ModelLayerLocation(
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "silk_design_inner"), "main");

    private final HumanoidModel<T> outer;
    private final HumanoidModel<T> inner;

    public SilkDesignLayer(RenderLayerParent<T, M> parent) {
        super(parent);
        var models = Minecraft.getInstance().getEntityModels();
        this.outer = new HumanoidModel<>(models.bakeLayer(OUTER_LAYER));
        this.inner = new HumanoidModel<>(models.bakeLayer(INNER_LAYER));
    }

// 供客户端注册时用：外甲 / 内甲两层模型的定义（膨胀值都调小了，让丝绸显得薄）
    public static LayerDefinition outerDefinition() {
        return LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.5F), 0.0F), 64, 32);
    }

    public static LayerDefinition innerDefinition() {
        return LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.3F), 0.0F), 64, 32);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        renderPiece(poseStack, bufferSource, entity, EquipmentSlot.HEAD, packedLight, this.outer);
        renderPiece(poseStack, bufferSource, entity, EquipmentSlot.CHEST, packedLight, this.outer);
        renderPiece(poseStack, bufferSource, entity, EquipmentSlot.LEGS, packedLight, this.inner);
        renderPiece(poseStack, bufferSource, entity, EquipmentSlot.FEET, packedLight, this.outer);
// 花纹画完再补上玩家在缝纫机里长出来的小方块（真立方体，会往外凸）
        renderVoxelPiece(poseStack, bufferSource, entity, EquipmentSlot.HEAD, this.outer);
        renderVoxelPiece(poseStack, bufferSource, entity, EquipmentSlot.CHEST, this.outer);
        renderVoxelPiece(poseStack, bufferSource, entity, EquipmentSlot.LEGS, this.inner);
        renderVoxelPiece(poseStack, bufferSource, entity, EquipmentSlot.FEET, this.outer);
    }

// 把某个槽位对应部位上堆出来的体素画成一个个小立方体
    private void renderVoxelPiece(PoseStack poseStack, MultiBufferSource bufferSource, T entity,
                                  EquipmentSlot slot, HumanoidModel<T> model) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (!(stack.getItem() instanceof SilkArmorItem)) {
            return;
        }
        List<Integer> voxels = SilkDesign.getVoxels(stack);
        if (voxels.isEmpty()) {
            return;
        }
        this.getParentModel().copyPropertiesTo(model);
        int[] cubes = SilkDesign.cubesFor(slot);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.debugQuads());
        int count = SilkDesign.voxelCount(voxels);
        for (int index = 0; index < count; index++) {
            int cube = SilkDesign.voxelCube(voxels, index);
            if (!contains(cubes, cube)) {
                continue;
            }
            ModelPart part = partOf(model, cube);
            if (part == null) {
                continue;
            }
            poseStack.pushPose();
            part.translateAndRotate(poseStack);
            Matrix4f matrix = poseStack.last().pose();
            addVoxelCube(consumer, matrix,
                    SilkDesign.originX(cube) + SilkDesign.voxelI(voxels, index),
                    SilkDesign.originY(cube) + SilkDesign.voxelJ(voxels, index),
                    SilkDesign.originZ(cube) + SilkDesign.voxelK(voxels, index),
                    SilkDesign.voxelColor(voxels, index));
            poseStack.popPose();
        }
    }

    public static boolean contains(int[] values, int value) {
        for (int candidate : values) {
            if (candidate == value) {
                return true;
            }
        }
        return false;
    }

// cube 编号 → 人形模型里对应的部件
    public static ModelPart partOf(HumanoidModel<?> model, int cube) {
        return switch (cube) {
            case SilkDesign.CUBE_HEAD -> model.head;
            case SilkDesign.CUBE_BODY -> model.body;
            case SilkDesign.CUBE_LEFT_ARM -> model.leftArm;
            case SilkDesign.CUBE_RIGHT_ARM -> model.rightArm;
            case SilkDesign.CUBE_LEFT_LEG -> model.leftLeg;
            case SilkDesign.CUBE_RIGHT_LEG -> model.rightLeg;
            default -> null;
        };
    }

// 画一个体素小立方体（模型坐标，单位是 1 个贴图像素 = 1/16 格）。
//
// 用 RenderType#debugQuads()（POSITION_COLOR，无光照），所以六个面各自乘一个明暗系数
// 来凑出立体感。界面的 3D 视口里也用这个方法，保证「看到的」和「穿在身上的」一致。
    public static void addVoxelCube(VertexConsumer consumer, Matrix4f matrix,
                                    float px, float py, float pz, int argb) {
        float s = 1.0F / 16.0F;
        float x0 = px * s;
        float y0 = py * s;
        float z0 = pz * s;
        float x1 = (px + 1.0F) * s;
        float y1 = (py + 1.0F) * s;
        float z1 = (pz + 1.0F) * s;
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
// 上下面最亮 / 最暗，四个侧面依次过渡
        quad(consumer, matrix, x0, y0, z0, x0, y0, z1, x1, y0, z1, x1, y0, z0, r, g, b, a, 1.00F);
        quad(consumer, matrix, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, r, g, b, a, 0.45F);
        quad(consumer, matrix, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, r, g, b, a, 0.80F);
        quad(consumer, matrix, x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1, r, g, b, a, 0.60F);
        quad(consumer, matrix, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1, r, g, b, a, 0.70F);
        quad(consumer, matrix, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, r, g, b, a, 0.90F);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             int r, int g, int b, int a, float shade) {
        vertex(consumer, matrix, ax, ay, az, r, g, b, a, shade);
        vertex(consumer, matrix, bx, by, bz, r, g, b, a, shade);
        vertex(consumer, matrix, cx, cy, cz, r, g, b, a, shade);
        vertex(consumer, matrix, dx, dy, dz, r, g, b, a, shade);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix,
                               float x, float y, float z, int r, int g, int b, int a, float shade) {
        consumer.vertex(matrix, x, y, z)
                .color((int) (r * shade), (int) (g * shade), (int) (b * shade), a);
    }

    private void renderPiece(PoseStack poseStack, MultiBufferSource bufferSource, T entity,
                             EquipmentSlot slot, int packedLight, HumanoidModel<T> model) {
        ItemStack stack = entity.getItemBySlot(slot);
// 原版那层纯白贴图已经改成全透明，所以还没画过的空白丝绸也得靠这一层画出白布底色
        if (!(stack.getItem() instanceof SilkArmorItem)) {
            return;
        }
        this.getParentModel().copyPropertiesTo(model);
        setPartVisibility(model, slot);
        VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.armorCutoutNoCull(SilkArmorTextures.get(stack, slot)));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

// 与缝纫机预览一致：只显示当前部位对应的那些模型部件。
// 头部刻意不显示原版的 hat 部件——它采样的是贴图 32,0 那块我们从不绘制的区域，
// 画出来就是一整块白壳把头包住，花纹全被盖掉。
    private static void setPartVisibility(HumanoidModel<?> model, EquipmentSlot slot) {
        model.setAllVisible(false);
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
}
