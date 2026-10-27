// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class uav<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "uav"), "main");
	private final ModelPart bone2;
	private final ModelPart 4;
	private final ModelPart 3;
	private final ModelPart 2;
	private final ModelPart ;
	private final ModelPart bone;

	public uav(ModelPart root) {
		this.bone2 = root.getChild("bone2");
		this.4 = root.getChild("4");
		this.3 = root.getChild("3");
		this.2 = root.getChild("2");
		this. = root.getChild("");
		this.bone = root.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone2 = partdefinition.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.0F, 15.0F, 0.0F));

		PartDefinition 4 = bone2.addOrReplaceChild("4", CubeListBuilder.create().texOffs(0, 23).addBox(-6.0F, -0.005F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, -1.0F, -10.0F));

		PartDefinition cube_r1 = 4.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 25).addBox(-6.0F, -0.01F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition 3 = bone2.addOrReplaceChild("3", CubeListBuilder.create().texOffs(20, 19).addBox(-6.0F, -0.005F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -1.0F, -10.0F));

		PartDefinition cube_r2 = 3.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 21).addBox(-6.0F, -0.01F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition 2 = bone2.addOrReplaceChild("2", CubeListBuilder.create().texOffs(20, 15).addBox(-6.0F, -0.005F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, -1.0F, 10.0F));

		PartDefinition cube_r3 = 2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(20, 17).addBox(-6.0F, -0.01F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition  = bone2.addOrReplaceChild("", CubeListBuilder.create().texOffs(20, 11).addBox(-6.0F, -0.005F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -1.0F, 10.0F));

		PartDefinition cube_r4 = .addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(20, 13).addBox(-6.0F, -0.01F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition bone = bone2.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 11).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(8, 36).addBox(-1.0F, -1.0F, 3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-3.0F, -4.0F, -5.0F, 6.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(16, 36).addBox(-1.0F, -4.0F, 5.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 27).addBox(-2.0F, -5.0F, -3.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition cube_r5 = bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(4, 36).addBox(0.0F, -7.0F, 0.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 0).addBox(0.0F, -1.0F, -6.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(28, 34).addBox(0.0F, -7.0F, -7.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 3.0F, 3.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition cube_r6 = bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(20, 27).addBox(-1.0F, -1.0F, -5.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(34, 29).addBox(-1.0F, -7.0F, -6.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 36).addBox(-1.0F, -7.0F, 1.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 3.0F, 2.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition cube_r7 = bone.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(22, 38).addBox(1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(14, 34).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, -6.0F, 9.0F, 0.0F, -2.3562F, 0.0F));

		PartDefinition cube_r8 = bone.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(32, 7).addBox(-3.0F, -1.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -4.0F, 6.0F, 0.0F, -2.3562F, 0.0F));

		PartDefinition cube_r9 = bone.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(32, 37).addBox(-2.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 34).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, -6.0F, -9.0F, 0.0F, -2.3562F, 0.0F));

		PartDefinition cube_r10 = bone.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(28, 25).addBox(-3.0F, -1.0F, -1.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -4.0F, -6.0F, 0.0F, -2.3562F, 0.0F));

		PartDefinition cube_r11 = bone.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(36, 37).addBox(1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(34, 27).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, -6.0F, 9.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r12 = bone.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(28, 23).addBox(-3.0F, -1.0F, -1.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -4.0F, 6.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r13 = bone.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(22, 36).addBox(-2.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 9).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, -6.0F, -9.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r14 = bone.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(28, 21).addBox(-3.0F, -1.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -4.0F, -6.0F, 0.0F, -0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}