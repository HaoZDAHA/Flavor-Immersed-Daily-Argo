// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class pot_stew_goose1<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "pot_stew_goose1"), "main");
	private final ModelPart bone;
	private final ModelPart bone3;
	private final ModelPart bone4;
	private final ModelPart bone5;
	private final ModelPart bone6;
	private final ModelPart meat;
	private final ModelPart bone7;
	private final ModelPart bone8;
	private final ModelPart pot;
	private final ModelPart bone2;

	public pot_stew_goose1(ModelPart root) {
		this.bone = root.getChild("bone");
		this.bone3 = root.getChild("bone3");
		this.bone4 = root.getChild("bone4");
		this.bone5 = root.getChild("bone5");
		this.bone6 = root.getChild("bone6");
		this.meat = root.getChild("meat");
		this.bone7 = root.getChild("bone7");
		this.bone8 = root.getChild("bone8");
		this.pot = root.getChild("pot");
		this.bone2 = root.getChild("bone2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bone3 = bone.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(-7.374F, -2.8954F, 3.0F));

		PartDefinition cake_r1 = bone3.addOrReplaceChild("cake_r1", CubeListBuilder.create().texOffs(52, 35).addBox(-1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(52, 35).addBox(-1.5F, -0.5F, -4.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(52, 35).addBox(-1.5F, -0.5F, -7.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition bone4 = bone3.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offsetAndRotation(8.0F, 0.0F, 4.0F, 0.0F, 1.6144F, 0.0F));

		PartDefinition cake_r2 = bone4.addOrReplaceChild("cake_r2", CubeListBuilder.create().texOffs(52, 35).addBox(-1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(52, 35).addBox(-1.5F, -0.5F, -7.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition bone5 = bone4.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offsetAndRotation(14.0F, 0.0F, 0.0F, 0.0F, 3.098F, 0.0F));

		PartDefinition cake_r3 = bone5.addOrReplaceChild("cake_r3", CubeListBuilder.create().texOffs(52, 35).addBox(-2.026F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(52, 35).addBox(-2.026F, -0.5F, -4.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(52, 35).addBox(-2.026F, -0.5F, -7.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition bone6 = bone3.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offsetAndRotation(15.0F, 0.0F, -3.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cake_r4 = bone6.addOrReplaceChild("cake_r4", CubeListBuilder.create().texOffs(52, 35).addBox(-1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(52, 35).addBox(-1.5F, -0.5F, -4.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(52, 35).addBox(-1.5F, -0.5F, -7.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition meat = bone.addOrReplaceChild("meat", CubeListBuilder.create().texOffs(52, 38).addBox(3.0F, -3.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(52, 31).addBox(-5.0F, -1.9846F, -5.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(65, 39).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.126F, -0.3954F, 0.0F));

		PartDefinition GREEN_r1 = meat.addOrReplaceChild("GREEN_r1", CubeListBuilder.create().texOffs(65, 39).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 1.0F, 4.0F, 0.0F, 0.6545F, 0.0F));

		PartDefinition GREEN_r2 = meat.addOrReplaceChild("GREEN_r2", CubeListBuilder.create().texOffs(65, 39).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.0F, 1.0F, 0.0F, 0.6545F, 0.0F));

		PartDefinition GREEN_r3 = meat.addOrReplaceChild("GREEN_r3", CubeListBuilder.create().texOffs(65, 39).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -2.5F, -3.0F, 0.0F, -0.3054F, 0.0F));

		PartDefinition GREEN_r4 = meat.addOrReplaceChild("GREEN_r4", CubeListBuilder.create().texOffs(65, 39).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.0F, -2.0F, 0.0F, 0.3927F, 0.0F));

		PartDefinition POTATO_r1 = meat.addOrReplaceChild("POTATO_r1", CubeListBuilder.create().texOffs(59, 41).addBox(-1.0F, -2.0046F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, -5.0F, 0.0F, 0.4363F, 0.0F));

		PartDefinition POTATO_r2 = meat.addOrReplaceChild("POTATO_r2", CubeListBuilder.create().texOffs(59, 41).addBox(-1.0F, -2.0046F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, 0.4363F, 0.0F));

		PartDefinition POTATO_r3 = meat.addOrReplaceChild("POTATO_r3", CubeListBuilder.create().texOffs(59, 41).addBox(-1.0F, -2.0046F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.0F, 0.4363F, 0.0F));

		PartDefinition POTATO_r4 = meat.addOrReplaceChild("POTATO_r4", CubeListBuilder.create().texOffs(59, 41).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -1.0F, 2.0F, 0.0F, -0.6109F, 0.0F));

		PartDefinition bian2_r1 = meat.addOrReplaceChild("bian2_r1", CubeListBuilder.create().texOffs(52, 19).addBox(-0.3592F, -0.9523F, -1.6381F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0965F, -0.9923F, -2.9625F, -3.1416F, -1.2217F, 3.1416F));

		PartDefinition bian_r1 = meat.addOrReplaceChild("bian_r1", CubeListBuilder.create().texOffs(52, 31).addBox(-2.0965F, -0.9423F, -2.0375F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0965F, -0.9923F, -2.9625F, 0.0F, -1.1781F, 0.0F));

		PartDefinition bian2_r2 = meat.addOrReplaceChild("bian2_r2", CubeListBuilder.create().texOffs(52, 19).addBox(-1.0F, -2.0F, -1.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, -3.0F, 0.0F, -0.7418F, 0.0F));

		PartDefinition bian_r2 = meat.addOrReplaceChild("bian_r2", CubeListBuilder.create().texOffs(52, 31).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -1.0F, 2.0F, 0.0F, 0.3491F, 0.0F));

		PartDefinition fang_r1 = meat.addOrReplaceChild("fang_r1", CubeListBuilder.create().texOffs(52, 38).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -2.0F, 4.0F, 0.0F, -0.1309F, 0.0F));

		PartDefinition bone7 = meat.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offset(1.5F, -1.5F, -3.25F));

		PartDefinition cube_r1 = bone7.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(52, 42).addBox(-0.5F, -0.6046F, 0.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(52, 25).addBox(-1.5F, -1.5F, -2.75F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition bone8 = bone7.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, 0.0F, 7.0F, 0.0F, -1.7017F, 0.0F));

		PartDefinition cube_r2 = bone8.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(52, 42).addBox(-0.5F, -0.5F, 0.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(52, 25).addBox(-1.5F, -1.5F, -2.75F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition pot = bone.addOrReplaceChild("pot", CubeListBuilder.create().texOffs(0, 0).addBox(1.874F, 1.3954F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 47).addBox(16.874F, -1.1046F, -4.0F, 3.0F, 0.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(52, 12).addBox(-3.626F, -1.1046F, -4.0F, 2.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.874F, -2.3954F, 0.0F));

		PartDefinition cube_r3 = pot.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(48, 0).addBox(3.595F, 5.5525F, -2.8536F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.374F, 0.0429F, 0.0F, 2.5873F, -0.5214F, -2.2483F));

		PartDefinition cube_r4 = pot.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(20, 47).addBox(-8.595F, 5.3525F, -2.1464F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.374F, 0.0429F, 0.0F, -2.5873F, -0.5214F, 2.2483F));

		PartDefinition cube_r5 = pot.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(48, 6).addBox(-2.9F, -0.3F, -2.8F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.374F, -0.1046F, 6.5F, 0.5942F, -0.5484F, -0.9136F));

		PartDefinition cube_r6 = pot.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(40, 47).addBox(-2.5F, -0.3F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.374F, -0.1046F, -5.5F, -0.5942F, -0.5484F, 0.9136F));

		PartDefinition cube_r7 = pot.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 13).addBox(-0.5F, -2.0F, -6.0F, 1.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.829F));

		PartDefinition cube_r8 = pot.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(26, 13).addBox(-0.5F, -2.0F, -6.0F, 1.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition bone2 = pot.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(8.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r9 = bone2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(26, 30).addBox(-0.5F, -2.0F, -6.0F, 1.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.829F));

		PartDefinition cube_r10 = bone2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 30).addBox(-0.5F, -2.0F, -6.0F, 1.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

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