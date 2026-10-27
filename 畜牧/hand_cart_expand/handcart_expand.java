// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class handcart_expand<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "handcart_expand"), "main");
	private final ModelPart bone;
	private final ModelPart body;
	private final ModelPart expand;
	private final ModelPart wheel;
	private final ModelPart left;
	private final ModelPart left2;

	public handcart_expand(ModelPart root) {
		this.bone = root.getChild("bone");
		this.body = root.getChild("body");
		this.expand = root.getChild("expand");
		this.wheel = root.getChild("wheel");
		this.left = root.getChild("left");
		this.left2 = root.getChild("left2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(-8.0F, 13.0F, 0.0F));

		PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(104, 103).addBox(7.0F, -4.0F, -55.0F, 3.0F, 4.0F, 28.0F, new CubeDeformation(0.0F))
		.texOffs(0, 107).addBox(-10.0F, -4.0F, -55.0F, 3.0F, 4.0F, 28.0F, new CubeDeformation(0.0F))
		.texOffs(62, 135).addBox(-16.0F, -9.0F, -29.0F, 34.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(92, 55).addBox(13.0F, -9.0F, -24.0F, 5.0F, 7.0F, 41.0F, new CubeDeformation(0.0F))
		.texOffs(0, 55).addBox(-16.0F, -9.0F, -24.0F, 5.0F, 7.0F, 41.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-16.0F, -2.0F, -29.0F, 34.0F, 5.0F, 50.0F, new CubeDeformation(0.0F))
		.texOffs(62, 107).addBox(-7.0F, -4.0F, -55.0F, 14.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, 49.0F));

		PartDefinition expand = body.addOrReplaceChild("expand", CubeListBuilder.create().texOffs(192, 224).addBox(-7.0F, -29.0F, 25.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.0F, -49.0F));

		PartDefinition cube_r1 = expand.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(192, 224).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -21.0F, 53.0F, 0.0F, -0.48F, 0.0F));

		PartDefinition wheel = bone.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(0, 103).addBox(-47.0F, -1.0F, -1.0F, 50.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(31.0F, 1.0F, 47.0F));

		PartDefinition left = wheel.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 139).addBox(-3.0F, -23.0F, -13.0F, 2.0F, 24.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 12.0F, 10.0F));

		PartDefinition cube_r2 = left.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(16, 139).addBox(-1.0F, -12.0F, -3.0F, 2.0F, 24.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -12.0F, -10.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r3 = left.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(62, 114).addBox(-1.5F, -6.0F, -1.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -18.0F, -17.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r4 = left.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(92, 114).addBox(-1.0F, -6.5F, -1.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.6464F, -17.3536F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r5 = left.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(82, 114).addBox(-1.0F, -6.5F, -2.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -17.6464F, -2.3536F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r6 = left.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(72, 114).addBox(-1.5F, -6.0F, -2.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -5.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition left2 = left.addOrReplaceChild("left2", CubeListBuilder.create().texOffs(0, 139).addBox(-3.0F, -23.0F, -13.0F, 2.0F, 24.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-43.0F, 0.0F, 0.0F));

		PartDefinition cube_r7 = left2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(16, 139).addBox(-1.0F, -12.0F, -3.0F, 2.0F, 24.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -12.0F, -10.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r8 = left2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(62, 114).addBox(-1.5F, -6.0F, -1.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -18.0F, -17.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r9 = left2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(92, 114).addBox(-1.0F, -6.5F, -1.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.6464F, -17.3536F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r10 = left2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(82, 114).addBox(-1.0F, -6.5F, -2.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -17.6464F, -2.3536F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r11 = left2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(72, 114).addBox(-1.5F, -6.0F, -2.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -5.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}