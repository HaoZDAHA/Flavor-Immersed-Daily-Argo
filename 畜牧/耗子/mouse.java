// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class mouse<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "mouse"), "main");
	private final ModelPart bone;
	private final ModelPart body2;
	private final ModelPart tail;
	private final ModelPart leg;
	private final ModelPart leg2;
	private final ModelPart body1;
	private final ModelPart head;
	private final ModelPart arm;
	private final ModelPart leg4;

	public mouse(ModelPart root) {
		this.bone = root.getChild("bone");
		this.body2 = root.getChild("body2");
		this.tail = root.getChild("tail");
		this.leg = root.getChild("leg");
		this.leg2 = root.getChild("leg2");
		this.body1 = root.getChild("body1");
		this.head = root.getChild("head");
		this.arm = root.getChild("arm");
		this.leg4 = root.getChild("leg4");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(-4.0F, 24.0F, 2.0F));

		PartDefinition body2 = bone.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -5.0F, -7.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -1.0F, 8.0F));

		PartDefinition tail = body2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leg = bone.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(24, 14).addBox(-1.0F, -1.0F, -5.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(24, 0).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 4.0F));

		PartDefinition leg2 = leg.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(24, 14).addBox(-1.0F, -1.0F, -5.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(24, 0).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, 0.0F));

		PartDefinition body1 = bone.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 11).addBox(-3.0F, -2.0F, -5.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -3.0F, 0.0F));

		PartDefinition head = bone.addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 10).addBox(-1.0F, -0.8612F, -3.0979F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(18, 21).addBox(-2.0F, -2.8612F, -1.0979F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(24, 7).addBox(-4.0F, -4.8612F, 1.9021F, 8.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(30, 21).addBox(-4.0F, -1.8612F, -2.0979F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -2.0F, -7.0F));

		PartDefinition arm = bone.addOrReplaceChild("arm", CubeListBuilder.create().texOffs(24, 14).addBox(-1.0F, -1.0F, -5.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(26, 2).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -2.0F));

		PartDefinition leg4 = bone.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(24, 14).addBox(-1.0F, -1.0F, -5.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(26, 2).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, -2.0F));

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