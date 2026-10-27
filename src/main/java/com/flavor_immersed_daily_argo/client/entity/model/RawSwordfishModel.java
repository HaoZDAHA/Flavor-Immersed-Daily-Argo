package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

// 生剑鱼模型（立体方块模型）
//
// 几何与 UV 完全取自 Blockbench 导出的 raw_swordfish.java（15 个 cube，128x128 贴图），
// 只做了 1.21.1 的签名适配并去掉动画：去掉了 EntityModel 的 renderToBuffer 重写，
// 改由 HierarchicalModel 渲染根节点。
//
// 剑鱼几何在 z 轴跨度约 -40~+39 像素（约 4.9 格），超出原版方块模型 JSON 的坐标硬限制
// （-16~32），无法导出为普通方块模型；因此它不参与区块网格烘焙，改由
// RawSwordfishRenderer（方块实体渲染器）按原始尺寸绘制。
public class RawSwordfishModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "raw_swordfish"), "main");

    private final ModelPart root;

    public RawSwordfishModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition head = bone.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 38).addBox(0.0F, -18.0F, 20.0F, 0.0F, 16.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(58, 14).addBox(-4.0F, -4.0F, 16.0F, 8.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 19).addBox(-4.0F, -4.0F, 21.0F, 8.0F, 9.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(38, 14).addBox(-3.0F, 3.0F, 18.0F, 6.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(58, 37).addBox(-2.0F, -2.0F, 9.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(44, 64).addBox(-1.0F, -2.0F, 1.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(44, 51).addBox(-1.0F, -2.0F, -10.0F, 2.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, -30.0F));

// 左侧胸鳍（绕 Z 轴 +45°）
        head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 65).addBox(0.0F, -4.5F, -2.5F, 0.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.0F, 5.5F, 29.5F, 0.0F, 0.0F, 0.7854F));

// 右侧胸鳍（绕 Z 轴 -45°）
        head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(64, 64).addBox(0.0F, -4.5F, -2.5F, 0.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0F, 5.5F, 29.5F, 0.0F, 0.0F, -0.7854F));

        bone.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(38, 0).addBox(-3.0F, -7.0F, -17.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(58, 25).addBox(-2.0F, -6.0F, -9.0F, 4.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(22, 38).addBox(-1.0F, -8.0F, -2.0F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(36, 19).addBox(0.0F, -15.0F, -1.0F, 0.0F, 21.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 29.0F));

        bone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(22, 51).addBox(0.0F, -23.0F, -1.0F, 0.0F, 16.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -1.0F, 8.0F, 8.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 2.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
// 方块装饰模型，无动画
    }
}
