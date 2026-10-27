// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class donkey_feast<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "donkey_feast"), "main");
	private final ModelPart bone;
	private final ModelPart table;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone4;
	private final ModelPart bone5;
	private final ModelPart plate;
	private final ModelPart dish;
	private final ModelPart donkeymeat;
	private final ModelPart donkeymeat2;
	private final ModelPart donkeymeat3;
	private final ModelPart bb_main;

	public donkey_feast(ModelPart root) {
		this.bone = root.getChild("bone");
		this.table = root.getChild("table");
		this.bone2 = root.getChild("bone2");
		this.bone3 = root.getChild("bone3");
		this.bone4 = root.getChild("bone4");
		this.bone5 = root.getChild("bone5");
		this.plate = root.getChild("plate");
		this.dish = root.getChild("dish");
		this.donkeymeat = root.getChild("donkeymeat");
		this.donkeymeat2 = root.getChild("donkeymeat2");
		this.donkeymeat3 = root.getChild("donkeymeat3");
		this.bb_main = root.getChild("bb_main");
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

		PartDefinition cube_r10 = plate.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(92, 13).addBox(0.5F, -2.5F, -4.0F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(3, 70).addBox(-4.5F, 0.5F, -6.0F, 9.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.5F, -4.5F, -9.0F, 0.0F, -0.5672F, 0.0F));

		PartDefinition cube_r11 = plate.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(52, 56).addBox(1.5408F, -2.75F, 1.548F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 56).addBox(-5.4592F, -2.75F, -0.452F, 7.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(52, 56).addBox(1.5408F, -2.75F, -0.452F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.9516F, -5.25F, -11.5262F, 0.0F, -0.4363F, 0.0F));

		PartDefinition cube_r12 = plate.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(52, 56).addBox(1.3754F, -2.75F, -2.079F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(55, 56).addBox(-4.6246F, -2.75F, -4.079F, 6.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(52, 56).addBox(1.3754F, -2.75F, -4.079F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.9516F, -5.25F, -11.5262F, 0.0F, -0.3491F, 0.0F));

		PartDefinition cube_r13 = plate.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(52, 56).addBox(2.0484F, -0.75F, -1.4738F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(53, 56).addBox(-5.9516F, -0.75F, -3.4738F, 8.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(52, 56).addBox(2.0484F, -0.75F, -3.4738F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 56).addBox(2.0484F, -0.75F, 3.5262F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 56).addBox(2.0484F, -0.75F, 1.5262F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(53, 56).addBox(-5.9516F, -0.75F, 1.5262F, 8.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(6, 74).addBox(-7.9516F, 1.25F, -3.4738F, 12.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.9516F, -5.25F, -11.5262F, 0.0F, -0.9163F, 0.0F));

		PartDefinition cube_r14 = plate.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(57, 61).addBox(0.1711F, -1.0F, 0.5061F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(57, 61).addBox(1.1701F, 0.0F, 0.5498F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.7435F, -5.0F, 12.5F, -3.1416F, -1.0472F, -3.1416F));

		PartDefinition cube_r15 = plate.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(57, 61).addBox(-2.017F, -2.0F, -1.253F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.7435F, -5.0F, 12.5F, 0.0F, -0.8727F, 0.0F));

		PartDefinition cube_r16 = plate.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(57, 61).addBox(0.407F, -1.0F, -1.5918F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(57, 61).addBox(0.749F, 0.0F, -2.5315F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.7435F, -5.0F, 12.5F, 3.1416F, -1.3526F, 3.1416F));

		PartDefinition cube_r17 = plate.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(57, 61).addBox(0.4528F, -1.0F, -1.2248F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(57, 61).addBox(1.7072F, 0.0F, -0.5718F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.7435F, -5.0F, 12.5F, 0.0F, 0.6981F, 0.0F));

		PartDefinition cube_r18 = plate.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(57, 61).addBox(0.2565F, -1.0F, -3.5F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(6, 74).addBox(-5.7435F, 1.0F, -4.5F, 12.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.7435F, -5.0F, 12.5F, 0.0F, 1.0036F, 0.0F));

		PartDefinition cube_r19 = plate.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(57, 61).addBox(1.7442F, 0.0F, -2.2656F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.7435F, -5.0F, 12.5F, 0.0F, 1.3526F, 0.0F));

		PartDefinition cube_r20 = plate.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(71, 39).addBox(-5.4633F, 0.332F, -3.7981F, 12.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.6524F, -4.333F, 7.0958F, -3.1416F, 1.0908F, 3.1416F));

		PartDefinition cube_r21 = plate.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(71, 39).addBox(-6.0F, 0.0F, -4.5F, 12.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -4.001F, 14.5F, 0.0F, 0.2618F, 0.0F));

		PartDefinition cube_r22 = plate.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(71, 39).addBox(-6.0F, 0.0F, -4.5F, 12.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -4.001F, -14.5F, 0.0F, 0.2618F, 0.0F));

		PartDefinition cube_r23 = plate.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(19, 80).addBox(-1.0F, -0.5F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -11.5F, 5.0F, 1.5708F, 0.7418F, 1.5708F));

		PartDefinition cube_r24 = plate.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(3, 70).addBox(-4.3476F, 0.333F, -6.0958F, 9.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.6524F, -4.333F, 7.0958F, 0.0F, 0.3927F, 0.0F));

		PartDefinition dish = bone.addOrReplaceChild("dish", CubeListBuilder.create().texOffs(0, 113).addBox(-7.0F, -7.0F, 12.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 113).addBox(-5.0F, -7.0F, 13.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(133, 116).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(133, 116).addBox(-7.0F, -6.0F, -17.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(134, 116).addBox(4.0F, -6.0F, 12.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 113).addBox(0.0F, -7.0F, 12.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 113).addBox(-3.0F, -7.0F, 12.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r25 = dish.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(1, 113).addBox(-0.5F, -1.5F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -5.5F, 14.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition cube_r26 = dish.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(1, 113).addBox(-0.5F, -1.5F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -5.5F, 16.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition donkeymeat = dish.addOrReplaceChild("donkeymeat", CubeListBuilder.create().texOffs(0, 113).addBox(-3.0F, -7.0F, 13.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 113).addBox(-1.0F, -7.0F, 11.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 113).addBox(4.0F, -7.0F, 13.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 113).addBox(1.0F, -7.0F, 13.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -30.0F));

		PartDefinition cube_r27 = donkeymeat.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(1, 113).addBox(-0.5F, -1.5F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, -5.5F, 15.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition cube_r28 = donkeymeat.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(1, 113).addBox(-0.5F, -1.5F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -5.5F, 15.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition donkeymeat2 = donkeymeat.addOrReplaceChild("donkeymeat2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 20.0F));

		PartDefinition cube_r29 = donkeymeat2.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(1, 114).addBox(-1.0F, -3.0F, -3.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 15.0F, 0.0F, 0.0F, -0.6981F));

		PartDefinition cube_r30 = donkeymeat2.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(2, 114).addBox(0.0F, -1.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7294F, -4.5F, 9.9012F, -1.5708F, 0.6545F, -1.5708F));

		PartDefinition cube_r31 = donkeymeat2.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(1, 114).addBox(-1.75F, -1.5F, -2.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, -5.5F, 13.0F, -0.3198F, 0.422F, -0.6799F));

		PartDefinition cube_r32 = donkeymeat2.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(2, 114).addBox(0.2153F, -1.2745F, -3.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, -4.5F, 13.0F, -0.7675F, 0.6484F, -1.0115F));

		PartDefinition cube_r33 = donkeymeat2.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(2, 114).addBox(-0.5F, -1.5F, -2.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -5.5F, 14.0F, -0.1593F, -0.0859F, -1.0479F));

		PartDefinition cube_r34 = donkeymeat2.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(2, 114).addBox(0.0F, -3.0F, -3.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -4.0F, 15.0F, 0.2849F, -0.274F, -0.8249F));

		PartDefinition cube_r35 = donkeymeat2.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(2, 114).addBox(0.0F, -1.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -4.5F, 12.5F, 0.8635F, -0.4891F, -1.1889F));

		PartDefinition donkeymeat3 = donkeymeat2.addOrReplaceChild("donkeymeat3", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.6463F, -5.0103F, 7.0989F, 0.0F, 3.0543F, 0.0F));

		PartDefinition cube_r36 = donkeymeat3.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(1, 114).addBox(-1.0F, -3.0F, -3.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3537F, 1.0103F, 1.9011F, 0.0F, 0.0F, -0.6981F));

		PartDefinition cube_r37 = donkeymeat3.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(2, 114).addBox(0.0F, -1.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3757F, 0.5103F, -3.1978F, -1.5708F, 0.6545F, -1.5708F));

		PartDefinition cube_r38 = donkeymeat3.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(1, 114).addBox(-1.75F, -1.5F, -2.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.3963F, -0.4897F, -0.0989F, -0.3198F, 0.422F, -0.6799F));

		PartDefinition cube_r39 = donkeymeat3.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(2, 114).addBox(0.2153F, -1.2745F, -3.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.3963F, 0.5103F, -0.0989F, -0.7675F, 0.6484F, -1.0115F));

		PartDefinition cube_r40 = donkeymeat3.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(2, 114).addBox(4.0191F, 2.4203F, -2.5142F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.911F, 0.6773F, -13.0848F, 0.9594F, -0.6816F, -1.1578F));

		PartDefinition cube_r41 = donkeymeat3.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(1, 114).addBox(1.5491F, 2.0188F, -3.3741F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.911F, 0.6773F, -13.0848F, 1.2697F, -0.3633F, -1.5812F));

		PartDefinition cube_r42 = donkeymeat3.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(2, 114).addBox(0.84F, 0.068F, -3.4244F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.911F, 0.6773F, -13.0848F, 1.3371F, -0.5234F, -1.6403F));

		PartDefinition cube_r43 = donkeymeat3.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(2, 114).addBox(-1.0514F, -2.4443F, -3.1678F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.911F, 0.6773F, -13.0848F, 1.7468F, -0.1289F, -1.5937F));

		PartDefinition cube_r44 = donkeymeat3.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(2, 114).addBox(-0.1222F, -1.9657F, -3.7051F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.911F, 0.6773F, -13.0848F, 1.7828F, -0.6003F, -1.6918F));

		PartDefinition cube_r45 = donkeymeat3.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(2, 114).addBox(-0.5F, -1.5F, -2.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1463F, -0.4897F, 0.9011F, -0.1593F, -0.0859F, -1.0479F));

		PartDefinition cube_r46 = donkeymeat3.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(2, 114).addBox(0.0F, -3.0F, -3.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.3537F, 1.0103F, 1.9011F, 0.2849F, -0.274F, -0.8249F));

		PartDefinition cube_r47 = donkeymeat3.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(2, 114).addBox(0.0F, -1.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.3537F, 0.5103F, -0.5989F, 0.8635F, -0.4891F, -1.1889F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(52, 20).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}