package com.flavor_immersed_daily_argo.client.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.flavor_immersed_daily_argo.entity.Harvester;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class HarvesterModel extends EntityModel<Harvester> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "harvester"), "main");

    private final ModelPart bone;
    private final ModelPart roll;
    private final ModelPart wheel;

    public HarvesterModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.bone = root.getChild("bone");
        this.roll = root.getChild("roll");
        this.wheel = root.getChild("wheel");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 146).addBox(-25.0F, -3.0F, -74.0F, 36.0F, 8.0F, 20.0F, new CubeDeformation(0.0F))
        .texOffs(0, 32).addBox(-26.0F, -45.0F, -81.0F, 39.0F, 3.0F, 27.0F, new CubeDeformation(0.0F))
        .texOffs(206, 197).addBox(-24.0F, -46.0F, -54.0F, 35.0F, 4.0F, 17.0F, new CubeDeformation(0.0F))
        .texOffs(0, 0).addBox(-26.0F, -50.0F, -83.0F, 39.0F, 5.0F, 27.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 11.0F, 62.0F));

        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 220).addBox(-28.0F, -16.0F, 24.0F, 42.0F, 12.0F, 3.0F, new CubeDeformation(0.0F))
        .texOffs(222, 154).addBox(-28.0F, -16.0F, 19.0F, 42.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
        .texOffs(0, 62).addBox(-25.0F, -16.0F, -5.0F, 36.0F, 12.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(194, 218).addBox(-28.0F, -16.0F, 25.0F, 42.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
        .texOffs(92, 212).addBox(-28.0F, -16.0F, 16.0F, 42.0F, 12.0F, 9.0F, new CubeDeformation(0.0F))
        .texOffs(110, 113).addBox(-25.0F, -16.0F, -5.0F, 36.0F, 12.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -12.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r3 = bone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(224, 113).addBox(-28.0F, 1.0F, 26.0F, 42.0F, 9.0F, 5.0F, new CubeDeformation(0.0F))
        .texOffs(120, 62).addBox(-28.0F, -16.0F, 27.0F, 42.0F, 17.0F, 15.0F, new CubeDeformation(0.0F))
        .texOffs(112, 146).addBox(-28.0F, -16.0F, 14.0F, 42.0F, 17.0F, 13.0F, new CubeDeformation(0.0F))
        .texOffs(206, 176).addBox(-28.0F, 1.0F, 14.0F, 42.0F, 9.0F, 12.0F, new CubeDeformation(0.0F))
        .texOffs(0, 98).addBox(-25.0F, -16.0F, -5.0F, 36.0F, 26.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -38.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r4 = bone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(174, 236).addBox(-28.0F, -13.0F, 16.0F, 3.0F, 13.0F, 15.0F, new CubeDeformation(0.0F))
        .texOffs(38, 235).addBox(11.0F, -13.0F, 16.0F, 3.0F, 13.0F, 15.0F, new CubeDeformation(0.0F))
        .texOffs(92, 194).addBox(-28.0F, -16.0F, 16.0F, 42.0F, 3.0F, 15.0F, new CubeDeformation(0.0F))
        .texOffs(92, 176).addBox(-28.0F, 0.0F, 16.0F, 42.0F, 3.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.0F, -21.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r5 = bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(108, 265).addBox(-10.0F, -16.0F, -5.0F, 6.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -58.0F, -43.0F, -1.2217F, 0.0F, 0.0F));

        PartDefinition cube_r6 = bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(272, 28).addBox(-15.0F, 11.0F, -5.0F, 5.0F, 16.0F, 7.0F, new CubeDeformation(0.0F))
        .texOffs(140, 265).addBox(-79.0F, 11.0F, -5.0F, 5.0F, 16.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(38.0F, 3.0F, -104.0F, 1.789F, 0.0F, 0.0F));

        PartDefinition cube_r7 = bone.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(132, 0).addBox(-36.0F, 5.0F, -5.0F, 59.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -96.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r8 = bone.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(222, 146).addBox(-31.0F, 5.0F, -5.0F, 49.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.0F, -84.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r9 = bone.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(76, 265).addBox(-9.99F, -16.0F, -5.0F, 6.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -50.0F, -36.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition cube_r10 = bone.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(226, 28).addBox(-10.0F, -16.0F, -5.0F, 6.0F, 6.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -51.0F, -31.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r11 = bone.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(132, 12).addBox(-26.1F, -9.0F, -5.0F, 4.0F, 4.0F, 43.0F, new CubeDeformation(0.0F))
        .texOffs(0, 174).addBox(-56.9F, -9.0F, -5.0F, 4.0F, 4.0F, 42.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(33.0F, -8.0F, -65.0F, 1.7017F, 0.0F, 0.0F));

        PartDefinition cube_r12 = bone.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(110, 98).addBox(-36.0F, 5.0F, -5.0F, 59.0F, 5.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -84.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition decorate = bone.addOrReplaceChild("decorate", CubeListBuilder.create().texOffs(250, 256).addBox(-61.0F, 5.0F, 77.0F, 3.0F, 2.0F, 17.0F, new CubeDeformation(0.0F))
        .texOffs(174, 264).addBox(-53.0F, -20.0F, 37.0F, 14.0F, 15.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(40.0F, 0.0F, -93.0F));

        PartDefinition cube_r13 = decorate.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(234, 77).addBox(-13.0F, -14.0F, 14.0F, 14.0F, 15.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-40.0F, 11.0F, 37.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r14 = decorate.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(282, 291).addBox(-14.0F, -15.0F, 14.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
        .texOffs(76, 258).addBox(-34.0F, -15.0F, 14.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-24.0F, 2.0F, 42.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r15 = decorate.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(262, 0).addBox(-13.0F, -14.0F, 14.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
        .texOffs(248, 98).addBox(-33.0F, -14.0F, 14.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-24.0F, 12.0F, 42.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition roll = partdefinition.addOrReplaceChild("roll", CubeListBuilder.create(), PartPose.offset(-0.7143F, 15.119F, -27.5F));

        PartDefinition cube_r16 = roll.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(210, 256).addBox(-33.0F, 1.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
        .texOffs(222, 171).addBox(-31.0F, 1.0F, 10.0F, 49.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
        .texOffs(226, 24).addBox(-31.0F, 1.0F, 5.0F, 49.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(226, 20).addBox(-31.0F, 1.0F, 0.0F, 49.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(226, 16).addBox(-31.0F, -15.0F, 5.0F, 49.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(226, 12).addBox(-31.0F, -15.0F, 0.0F, 49.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(224, 139).addBox(-31.0F, -15.0F, 11.0F, 49.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(224, 135).addBox(-31.0F, -5.0F, -5.0F, 49.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(224, 131).addBox(-31.0F, -10.0F, -5.0F, 49.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(274, 291).addBox(-33.0F, -13.0F, -5.0F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(132, 59).addBox(-31.0F, 0.0F, -5.0F, 49.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(224, 127).addBox(-31.0F, -4.0F, 11.0F, 49.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(222, 94).addBox(-31.0F, -10.0F, 11.0F, 49.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(120, 290).addBox(-33.0F, -13.0F, 11.0F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(120, 94).addBox(-31.0F, -15.0F, -5.0F, 49.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(38, 263).addBox(-32.0F, -15.0F, -5.0F, 1.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
        .texOffs(0, 261).addBox(-33.0F, -15.0F, -5.0F, 1.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
        .texOffs(196, 281).addBox(18.0F, -13.0F, 11.0F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(250, 236).addBox(18.0F, -15.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
        .texOffs(164, 265).addBox(18.0F, -13.0F, -5.0F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(210, 236).addBox(18.0F, 1.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.7143F, 3.881F, 6.5F, 1.5708F, 0.0F, 0.0F));

        PartDefinition wheel = partdefinition.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(0, 312).addBox(-40.0F, 0.0F, -8.0F, 7.0F, 17.0F, 45.0F, new CubeDeformation(0.0F)), PartPose.offset(58.0F, 7.0F, 10.0F));

        PartDefinition cube_r17 = wheel.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 312).addBox(-3.5F, -8.5F, -22.5F, 7.0F, 17.0F, 45.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-79.5F, 8.5F, 14.5F, 0.0F, 3.1416F, 0.0F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override
    public void setupAnim(Harvester entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
// 驾驶时 roll 组（收割滚筒）快速绕 X 轴旋转
        if (entity.isVehicle()) {
            this.roll.xRot = ageInTicks * 0.8F;
        } else {
            this.roll.xRot = 0.0F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        roll.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        wheel.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }
}