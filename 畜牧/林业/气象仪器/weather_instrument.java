// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class weather_instrument<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "weather_instrument"), "main");
	private final ModelPart bone;
	private final ModelPart round;

	public weather_instrument(ModelPart root) {
		this.bone = root.getChild("bone");
		this.round = root.getChild("round");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(56, 4).addBox(-1.0F, 7.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(56, 36).addBox(-3.0F, 37.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(56, 44).addBox(-3.0F, 20.0F, -4.0F, 6.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(16, 64).addBox(-2.0F, 23.0F, -1.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(56, 0).addBox(-11.0F, 14.0F, -0.5F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 64).addBox(7.0F, 8.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(64, 4).addBox(-2.0F, 5.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -15.0F, 0.0F));

		PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(56, 56).addBox(-3.5F, -5.0F, -0.7F, 7.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.5F, 11.0F, -1.5F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(64, 23).addBox(-1.1F, -1.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, 12.5F, -1.0F, 0.3927F, 0.0F, 0.0F));

		PartDefinition round = bone.addOrReplaceChild("round", CubeListBuilder.create().texOffs(64, 10).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(0.0F, -4.0F, -14.0F, 0.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r3 = round.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 32).addBox(0.0F, -2.0F, -14.0F, 0.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}