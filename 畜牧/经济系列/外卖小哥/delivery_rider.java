// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class delivery_rider<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "delivery_rider"), "main");
	private final ModelPart bone;
	private final ModelPart left_arm;
	private final ModelPart left_hand;
	private final ModelPart right_arm;
	private final ModelPart right_hand;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart right_leg;
	private final ModelPart tight_foot;
	private final ModelPart left_leg;
	private final ModelPart left_foot;

	public delivery_rider(ModelPart root) {
		this.bone = root.getChild("bone");
		this.left_arm = root.getChild("left_arm");
		this.left_hand = root.getChild("left_hand");
		this.right_arm = root.getChild("right_arm");
		this.right_hand = root.getChild("right_hand");
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.right_leg = root.getChild("right_leg");
		this.tight_foot = root.getChild("tight_foot");
		this.left_leg = root.getChild("left_leg");
		this.left_foot = root.getChild("left_foot");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition left_arm = bone.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(7.0F, -21.0F, 0.0F));

		PartDefinition cube_r1 = left_arm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(46, 0).addBox(-3.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0436F, 0.0F, -0.0873F));

		PartDefinition left_hand = bone.addOrReplaceChild("left_hand", CubeListBuilder.create(), PartPose.offset(7.0F, -18.0F, 0.0F));

		PartDefinition cube_r2 = left_hand.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(32, 48).addBox(-1.7F, -6.0F, -1.0F, 3.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 3.0F, -3.0F, 0.9599F, 0.0F, 0.0F));

		PartDefinition cube_r3 = left_hand.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(32, 38).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 2.0F, -2.0F, -0.6109F, 0.0F, 0.0F));

		PartDefinition right_arm = bone.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, -21.0F, 0.0F));

		PartDefinition cube_r4 = right_arm.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0436F, 0.0F, 0.1309F));

		PartDefinition right_hand = bone.addOrReplaceChild("right_hand", CubeListBuilder.create(), PartPose.offset(-5.0F, -21.0F, 0.0F));

		PartDefinition cube_r5 = right_hand.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(16, 38).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 5.0F, -1.0F, -0.5236F, 0.0F, 0.0F));

		PartDefinition head = bone.addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 26).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.0F, -12.0F, 0.0F, 10.0F, 11.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(24, 16).addBox(-5.0F, -12.0F, -5.0F, 10.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -23.0F, -1.0F));

		PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition cube_r6 = body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -6.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0436F, 0.0F, 0.0F));

		PartDefinition right_leg = bone.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-2.0F, -13.0F, 0.0F));

		PartDefinition cube_r7 = right_leg.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.1314F, 0.0865F, -0.0114F));

		PartDefinition tight_foot = bone.addOrReplaceChild("tight_foot", CubeListBuilder.create(), PartPose.offset(-2.0F, 0.0F, 0.0F));

		PartDefinition cube_r8 = tight_foot.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, -3.5F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.088F, 0.1304F, 0.0115F));

		PartDefinition left_leg = bone.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.0F, -12.0F, 0.0F));

		PartDefinition cube_r9 = left_leg.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(48, 26).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 3.0F, 0.0F, -0.1314F, -0.0865F, 0.0114F));

		PartDefinition left_foot = bone.addOrReplaceChild("left_foot", CubeListBuilder.create(), PartPose.offset(1.0F, -6.0F, 0.0F));

		PartDefinition cube_r10 = left_foot.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(30, 0).addBox(-2.0F, -3.5F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 3.0F, 0.0F, 0.0876F, -0.0869F, -0.0076F));

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