// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class scarecrow<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "scarecrow"), "main");
	private final ModelPart bone;

	public scarecrow(ModelPart root) {
		this.bone = root.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(16, 16).addBox(4.0F, -16.0F, -1.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 16).addBox(7.0F, -22.0F, 0.0F, 2.0F, 32.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(16, 32).addBox(4.0F, -4.0F, -1.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(32, 32).addBox(4.0F, -4.0F, 3.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(16, 36).addBox(12.0F, -4.0F, -1.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(24, 36).addBox(4.0F, -4.0F, -1.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 14.0F, -1.0F));

		PartDefinition right_arm_r1 = bone.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(32, 8).addBox(-6.0F, -4.0F, -2.0F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -12.0F, -1.0F, 0.0F, 0.0F, -0.1309F));

		PartDefinition left_arm_r1 = bone.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(32, 0).addBox(-6.0F, -4.0F, -2.0F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0F, -12.0F, -1.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(8, 16).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 32.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -14.0F, 1.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition head_r1 = bone.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(-14, 50).addBox(-7.0F, -4.01F, -7.0F, 14.0F, 0.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(28, 52).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -19.0F, 0.0F, 0.0F, 0.0F, 0.0873F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}