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


public class DonkeyFeastStage2Model extends HierarchicalModel<Entity> {
	private final ModelPart root;
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(flavor_immersed_daily_argo.MODID, "donkey_feast_stage2"), "main");
	private final ModelPart bone;
	public DonkeyFeastStage2Model(ModelPart root) {
		this.root = root;
		this.bone = root.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(100, 112).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(121, 47).addBox(9.0F, -9.0F, -5.0F, 10.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition table = bone.addOrReplaceChild("table", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.0F, -24.0F, 16.0F, 2.0F, 48.0F, new CubeDeformation(0.0F))
		.texOffs(0, 50).addBox(-24.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(64, 50).addBox(8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition bone2 = table.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(-15.8119F, -1.0F, 8.0058F));

		PartDefinition cube_r1 = bone2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(54, 98).addBox(-7.0F, -0.99F, -5.0F, 14.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, 0.0F));

		PartDefinition cube_r2 = bone2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(56, 68).addBox(-7.1881F, -0.999F, -7.0058F, 14.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, 7.0F, 0.0F, 0.9599F, 0.0F));

		PartDefinition bone3 = bone2.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(4.4889F, 0.0055F, -20.135F, 0.0F, -1.6144F, 0.0F));

		PartDefinition cube_r3 = bone3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(106, 98).addBox(-7.0F, -0.99F, -5.0F, 14.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.4889F, -0.0055F, -3.865F, 0.0F, 0.5236F, 0.0F));

		PartDefinition cube_r4 = bone3.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 83).addBox(-7.1881F, -0.999F, -7.0058F, 14.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5111F, -0.0055F, 3.135F, 0.0F, 0.9599F, 0.0F));

		PartDefinition bone4 = bone2.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offsetAndRotation(32.0F, 0.0F, -16.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r5 = bone4.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(108, 83).addBox(-7.0F, -0.99F, -5.0F, 14.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, 0.0F));

		PartDefinition cube_r6 = bone4.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(54, 83).addBox(-7.1881F, -0.999F, -7.0058F, 14.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, 7.0F, 0.0F, 0.9599F, 0.0F));

		PartDefinition bone5 = bone4.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offsetAndRotation(4.4889F, 0.0055F, -20.135F, 0.0F, -1.6144F, 0.0F));

		PartDefinition cube_r7 = bone5.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(110, 68).addBox(-7.0F, -0.99F, -5.0F, 14.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.4889F, -0.0055F, -3.865F, 0.0F, 0.5236F, 0.0F));

		PartDefinition cube_r8 = bone5.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 98).addBox(-7.1881F, -0.999F, -7.0058F, 14.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5111F, -0.0055F, 3.135F, 0.0F, 0.9599F, 0.0F));

		PartDefinition plate = bone.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(0, 68).addBox(-7.0F, -4.0F, -7.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(54, 112).addBox(-7.0F, -4.0F, 10.0F, 14.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(54, 112).addBox(-7.0F, -4.0F, -19.0F, 14.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r9 = plate.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(92, 13).addBox(0.5F, -2.5F, -4.0F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.5F, -4.5F, -9.0F, -3.1416F, 0.5672F, 3.1416F));

		PartDefinition cube_r10 = plate.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(52, 56).addBox(1.267F, -1.0F, 0.6294F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(53, 56).addBox(-6.733F, -1.0F, -1.3706F, 8.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(52, 56).addBox(1.267F, -1.0F, -1.3706F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0959F, -5.0F, -12.1867F, 0.0F, -0.4363F, 0.0F));

		PartDefinition cube_r11 = plate.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(57, 61).addBox(1.1701F, 0.0F, 0.5498F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.7435F, -5.0F, 12.5F, -3.1416F, -1.0472F, -3.1416F));

		PartDefinition cube_r12 = plate.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(57, 61).addBox(1.7072F, 0.0F, -0.5718F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.7435F, -5.0F, 12.5F, 0.0F, 0.6981F, 0.0F));

		PartDefinition cube_r13 = plate.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(57, 61).addBox(1.7442F, 0.0F, -2.2656F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.7435F, -5.0F, 12.5F, 0.0F, 1.3526F, 0.0F));

		PartDefinition cube_r14 = plate.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(71, 39).addBox(-5.4633F, 0.332F, -3.7981F, 12.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.6524F, -4.333F, 7.0958F, -3.1416F, 1.0908F, 3.1416F));

		PartDefinition cube_r15 = plate.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(71, 39).addBox(-6.0F, 0.0F, -4.5F, 12.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -4.001F, 14.5F, 0.0F, 0.2618F, 0.0F));

		PartDefinition cube_r16 = plate.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(71, 39).addBox(-6.0F, 0.0F, -4.5F, 12.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -4.001F, -14.5F, 0.0F, 0.2618F, 0.0F));

		PartDefinition cube_r17 = plate.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(6, 74).addBox(-7.9516F, 1.25F, -3.4738F, 12.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.9516F, -5.25F, -11.5262F, 0.0F, -0.9163F, 0.0F));

		PartDefinition cube_r18 = plate.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(19, 80).addBox(-1.0F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -11.5F, 5.0F, 1.5708F, 0.7418F, 1.5708F));

		PartDefinition cube_r19 = plate.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(6, 74).addBox(-5.7435F, 1.0F, -4.5F, 12.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.7435F, -5.0F, 12.5F, 0.0F, 1.0036F, 0.0F));

		PartDefinition cube_r20 = plate.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(3, 70).addBox(-4.3476F, 0.333F, -6.0958F, 9.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.6524F, -4.333F, 7.0958F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r21 = plate.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(3, 70).addBox(-4.5F, 0.5F, -6.0F, 9.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.5F, -4.5F, -9.0F, 0.0F, -0.5672F, 0.0F));

		PartDefinition dish = bone.addOrReplaceChild("dish", CubeListBuilder.create().texOffs(0, 113).addBox(-7.0F, -7.0F, 12.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(133, 116).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(133, 116).addBox(-7.0F, -6.0F, -17.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(134, 116).addBox(4.0F, -6.0F, 12.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition donkeymeat = dish.addOrReplaceChild("donkeymeat", CubeListBuilder.create().texOffs(0, 113).addBox(-1.0F, -7.0F, 11.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -30.0F));

		PartDefinition donkeymeat2 = donkeymeat.addOrReplaceChild("donkeymeat2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 20.0F));

		PartDefinition cube_r22 = donkeymeat2.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(1, 114).addBox(-1.0F, -3.0F, -3.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 15.0F, 0.0F, 0.0F, -0.6981F));

		PartDefinition cube_r23 = donkeymeat2.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(1, 114).addBox(-1.75F, -1.5F, -2.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, -5.5F, 13.0F, -0.3198F, 0.422F, -0.6799F));

		PartDefinition cube_r24 = donkeymeat2.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(2, 114).addBox(0.2153F, -1.2745F, -3.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, -4.5F, 13.0F, -0.7675F, 0.6484F, -1.0115F));

		PartDefinition donkeymeat3 = donkeymeat2.addOrReplaceChild("donkeymeat3", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.6463F, -5.0103F, 7.0989F, 0.0F, 3.0543F, 0.0F));

		PartDefinition cube_r25 = donkeymeat3.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(1, 114).addBox(-1.75F, -1.5F, -2.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.3963F, -0.4897F, -0.0989F, -0.3198F, 0.422F, -0.6799F));

		PartDefinition cube_r26 = donkeymeat3.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(2, 114).addBox(0.2153F, -1.2745F, -3.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.3963F, 0.5103F, -0.0989F, -0.7675F, 0.6484F, -1.0115F));

		PartDefinition cube_r27 = donkeymeat3.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(2, 114).addBox(-1.0514F, -2.4443F, -3.1678F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.911F, 0.6773F, -13.0848F, 1.7468F, -0.1289F, -1.5937F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(52, 20).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}
    @Override
    public ModelPart root() {
        return this.root;
    }

// 让整张圆桌绕竖轴转到指定角度（度），复刻导出动画 round
    public void setRoundAngle(float degrees) {
        this.bone.yRot = degrees * Mth.DEG_TO_RAD;
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
// 静态摆件：模型固定，靠 stage 换模型
    }
}