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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

// 灌溉水车模型
//
// 几何与 UV 严格取自 Blockbench 导出的 waterwheel.java（256×256 贴图）：
// 根节点 bone4 立在 y=24 的“地面”上，下面挂着轮子（wheel 及其
// wheel_plank / wheel_plank2 / wheel1 → bone → bone2
// → bone3 一串叶片）与两条支腿 leg。
//
// 导出动画 waterwheel_round 是让 wheel 绕 Z 轴转一整圈、时长 16 秒循环；
// 这里不搬 AnimationState，直接按游戏时间匀速设置轮子角度，多个水车相位还能对齐。
public class WaterwheelModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "waterwheel"), "main");

    private final ModelPart root;
    private final ModelPart wheel;

    public WaterwheelModel(ModelPart root) {
        this.root = root.getChild("bone4");
        this.wheel = this.root.getChild("wheel");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone4 = partdefinition.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition wheel = bone4.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(0, 126).addBox(-2.0F, -2.0F, -8.0F, 4.0F, 4.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -31.0F, 0.0F));

        PartDefinition wheel_plank = wheel.addOrReplaceChild("wheel_plank", CubeListBuilder.create(), PartPose.offset(0.0F, 31.0F, 0.0F));

        wheel_plank.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(72, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(128, 95).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, 0.2618F));

        wheel_plank.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(128, 114).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(80, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -0.2618F));

        wheel_plank.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(136, 38).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(112, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, 0.7854F));

        wheel_plank.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(136, 19).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(104, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -1.789F));

        wheel_plank.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(136, 0).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(96, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -1.309F));

        wheel_plank.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(128, 133).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(88, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition wheel_plank2 = wheel_plank.addOrReplaceChild("wheel_plank2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 11.0F));

        wheel_plank2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(128, 76).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(136, 57).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, 0.2618F));

        wheel_plank2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(144, 0).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(128, 57).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -0.2618F));

        wheel_plank2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(136, 133).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(128, 38).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, 0.7854F));

        wheel_plank2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(136, 114).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(128, 19).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -1.789F));

        wheel_plank2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(136, 95).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(128, 0).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -1.309F));

        wheel_plank2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(136, 76).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(120, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition wheel1 = wheel.addOrReplaceChild("wheel1", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, -7.0F));

        PartDefinition bone = wheel1.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -30.0F, -0.97F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-1.0F, 2.0F, -0.97F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 1.0F));

        bone.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(32, 42).addBox(-1.0F, -30.0F, -0.97F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 42).addBox(-1.0F, 2.0F, -0.97F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition bone2 = bone.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 84).addBox(-1.0F, -30.0F, -0.98F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(32, 84).addBox(-1.0F, 2.0F, -0.98F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0472F));

        bone2.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(64, 42).addBox(-1.0F, -30.0F, -0.98F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(64, 0).addBox(-1.0F, 2.0F, -0.98F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition bone3 = bone2.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(96, 42).addBox(-1.0F, -30.0F, -0.99F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(96, 84).addBox(-1.0F, 2.0F, -0.99F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5672F));

        bone3.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(96, 0).addBox(-1.0F, -30.0F, -0.99F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(64, 84).addBox(-1.0F, 2.0F, -0.99F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition leg = bone4.addOrReplaceChild("leg", CubeListBuilder.create(), PartPose.offset(0.0F, -30.0F, -7.0F));

        leg.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(56, 126).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 33.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(48, 126).addBox(-1.0F, -16.0F, -17.0F, 2.0F, 33.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 14.0F, 15.0F, 0.0F, 0.0F, 0.2182F));

        leg.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(64, 126).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 33.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 126).addBox(-1.0F, -16.0F, -17.0F, 2.0F, 33.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 14.0F, 15.0F, 0.0F, 0.0F, -0.2182F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

// 让水轮绕 Z 轴转到指定角度（度），复刻导出动画 waterwheel_round
    public void setWheelAngle(float degrees) {
        this.wheel.zRot = degrees * Mth.DEG_TO_RAD;
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
// 方块装饰模型：旋转由渲染器按游戏时间直接驱动
    }
}
