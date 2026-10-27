// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class waterwheel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "waterwheel"), "main");
	private final ModelPart bone4;
	private final ModelPart wheel;
	private final ModelPart wheel_plank;
	private final ModelPart wheel_plank2;
	private final ModelPart wheel1;
	private final ModelPart bone;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart leg;

	public waterwheel(ModelPart root) {
		this.bone4 = root.getChild("bone4");
		this.wheel = root.getChild("wheel");
		this.wheel_plank = root.getChild("wheel_plank");
		this.wheel_plank2 = root.getChild("wheel_plank2");
		this.wheel1 = root.getChild("wheel1");
		this.bone = root.getChild("bone");
		this.bone2 = root.getChild("bone2");
		this.bone3 = root.getChild("bone3");
		this.leg = root.getChild("leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone4 = partdefinition.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition wheel = bone4.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(0, 126).addBox(-2.0F, -2.0F, -8.0F, 4.0F, 4.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -31.0F, 0.0F));

		PartDefinition wheel_plank = wheel.addOrReplaceChild("wheel_plank", CubeListBuilder.create(), PartPose.offset(0.0F, 31.0F, 0.0F));

		PartDefinition cube_r1 = wheel_plank.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(72, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(128, 95).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition cube_r2 = wheel_plank.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(128, 114).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(80, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition cube_r3 = wheel_plank.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(136, 38).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(112, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r4 = wheel_plank.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(136, 19).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(104, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -1.789F));

		PartDefinition cube_r5 = wheel_plank.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(136, 0).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(96, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -1.309F));

		PartDefinition cube_r6 = wheel_plank.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(128, 133).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(88, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition wheel_plank2 = wheel_plank.addOrReplaceChild("wheel_plank2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 11.0F));

		PartDefinition cube_r7 = wheel_plank2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(128, 76).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(136, 57).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition cube_r8 = wheel_plank2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(144, 0).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(128, 57).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition cube_r9 = wheel_plank2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(136, 133).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(128, 38).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r10 = wheel_plank2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(136, 114).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(128, 19).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -1.789F));

		PartDefinition cube_r11 = wheel_plank2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(136, 95).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(128, 0).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -1.309F));

		PartDefinition cube_r12 = wheel_plank2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(136, 76).addBox(24.8475F, -8.0625F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(120, 126).addBox(-25.8475F, -7.9375F, -7.0F, 1.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.017F, -30.6294F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition wheel1 = wheel.addOrReplaceChild("wheel1", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, -7.0F));

		PartDefinition bone = wheel1.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -30.0F, -0.97F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(32, 0).addBox(-1.0F, 2.0F, -0.97F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 1.0F));

		PartDefinition cube_r13 = bone.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(32, 42).addBox(-1.0F, -30.0F, -0.97F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(0, 42).addBox(-1.0F, 2.0F, -0.97F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition bone2 = bone.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 84).addBox(-1.0F, -30.0F, -0.98F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(32, 84).addBox(-1.0F, 2.0F, -0.98F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0472F));

		PartDefinition cube_r14 = bone2.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(64, 42).addBox(-1.0F, -30.0F, -0.98F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(64, 0).addBox(-1.0F, 2.0F, -0.98F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition bone3 = bone2.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(96, 42).addBox(-1.0F, -30.0F, -0.99F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(96, 84).addBox(-1.0F, 2.0F, -0.99F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5672F));

		PartDefinition cube_r15 = bone3.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(96, 0).addBox(-1.0F, -30.0F, -0.99F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(64, 84).addBox(-1.0F, 2.0F, -0.99F, 2.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition leg = bone4.addOrReplaceChild("leg", CubeListBuilder.create(), PartPose.offset(0.0F, -30.0F, -7.0F));

		PartDefinition cube_r16 = leg.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(56, 126).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 33.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(48, 126).addBox(-1.0F, -16.0F, -17.0F, 2.0F, 33.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 14.0F, 15.0F, 0.0F, 0.0F, 0.2182F));

		PartDefinition cube_r17 = leg.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(64, 126).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 33.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(40, 126).addBox(-1.0F, -16.0F, -17.0F, 2.0F, 33.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 14.0F, 15.0F, 0.0F, 0.0F, -0.2182F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}