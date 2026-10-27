// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class braised_prawns<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "braised_prawns"), "main");
	private final ModelPart bone;
	private final ModelPart pot;
	private final ModelPart bone2;
	private final ModelPart prawn;
	private final ModelPart prawn2;
	private final ModelPart prawn3;
	private final ModelPart prawn4;
	private final ModelPart prawn5;
	private final ModelPart prawn6;
	private final ModelPart prawn7;
	private final ModelPart prawn8;
	private final ModelPart prawn9;
	private final ModelPart prawn10;
	private final ModelPart prawn11;
	private final ModelPart prawn12;

	public braised_prawns(ModelPart root) {
		this.bone = root.getChild("bone");
		this.pot = root.getChild("pot");
		this.bone2 = root.getChild("bone2");
		this.prawn = root.getChild("prawn");
		this.prawn2 = root.getChild("prawn2");
		this.prawn3 = root.getChild("prawn3");
		this.prawn4 = root.getChild("prawn4");
		this.prawn5 = root.getChild("prawn5");
		this.prawn6 = root.getChild("prawn6");
		this.prawn7 = root.getChild("prawn7");
		this.prawn8 = root.getChild("prawn8");
		this.prawn9 = root.getChild("prawn9");
		this.prawn10 = root.getChild("prawn10");
		this.prawn11 = root.getChild("prawn11");
		this.prawn12 = root.getChild("prawn12");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition pot = bone.addOrReplaceChild("pot", CubeListBuilder.create().texOffs(0, 0).addBox(1.874F, 1.3954F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 47).addBox(16.874F, -1.1046F, -4.0F, 3.0F, 0.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(52, 12).addBox(-3.626F, -1.1046F, -4.0F, 2.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.874F, -2.3954F, 0.0F));

		PartDefinition cube_r1 = pot.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(48, 0).addBox(3.595F, 5.085F, -2.8536F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.374F, 0.0429F, 0.0F, 2.5873F, -0.5214F, -2.2483F));

		PartDefinition cube_r2 = pot.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(20, 47).addBox(-8.595F, 5.3525F, -2.1464F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.374F, 0.0429F, 0.0F, -2.5873F, -0.5214F, 2.2483F));

		PartDefinition cube_r3 = pot.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(48, 6).addBox(-2.9F, -0.3F, -2.8F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.374F, -0.1046F, 6.5F, 0.5942F, -0.5484F, -0.9136F));

		PartDefinition cube_r4 = pot.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(40, 47).addBox(-2.5F, -0.3F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.374F, -0.1046F, -6.5F, -0.5942F, -0.5484F, 0.9136F));

		PartDefinition cube_r5 = pot.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 13).addBox(-0.5F, -2.0F, -6.0F, 1.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.829F));

		PartDefinition cube_r6 = pot.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(26, 13).addBox(-0.5F, -2.0F, -6.0F, 1.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition bone2 = pot.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(8.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r7 = bone2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(26, 30).addBox(-0.5F, -2.0F, -6.0F, 1.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.829F));

		PartDefinition cube_r8 = bone2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 30).addBox(-0.5F, -2.0F, -6.0F, 1.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition prawn = bone.addOrReplaceChild("prawn", CubeListBuilder.create(), PartPose.offset(-0.7374F, -1.85F, -1.314F));

		PartDefinition cube_r9 = prawn.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(14, 16).addBox(0.4386F, -0.75F, -1.5613F, 1.5F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r10 = prawn.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0688F, -0.15F, -1.8525F, 3.0F, 0.0F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(5, 16).addBox(-1.945F, -0.15F, -1.4352F, 1.0F, 0.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(14, 13).addBox(-1.0688F, -0.6501F, -1.8525F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r11 = prawn.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(22, 13).addBox(0.686F, 0.35F, 0.7374F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 17).addBox(0.686F, -0.65F, -1.2626F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

		PartDefinition prawn2 = prawn.addOrReplaceChild("prawn2", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.9001F, 0.0417F, 2.9261F, 0.0F, 1.7017F, 0.0F));

		PartDefinition cube_r12 = prawn2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(14, 16).addBox(0.4386F, -0.75F, -1.5613F, 1.5F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r13 = prawn2.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0688F, -0.15F, -1.8525F, 3.0F, 0.0F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(5, 16).addBox(-1.945F, -0.15F, -1.4352F, 1.0F, 0.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(14, 13).addBox(-1.0688F, -0.6501F, -1.8525F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r14 = prawn2.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(22, 13).addBox(0.686F, 0.35F, 0.7374F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 17).addBox(0.686F, -0.65F, -1.2626F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, -0.3927F, 0.0F));

		PartDefinition prawn3 = prawn.addOrReplaceChild("prawn3", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0999F, 0.0417F, 4.9261F, 0.0F, -1.309F, 0.0F));

		PartDefinition cube_r15 = prawn3.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(14, 16).addBox(0.4386F, -0.75F, -1.5613F, 1.5F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r16 = prawn3.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0688F, -0.15F, -1.8525F, 3.0F, 0.0F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(5, 16).addBox(-1.945F, -0.15F, -1.4352F, 1.0F, 0.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(14, 13).addBox(-1.0688F, -0.6501F, -1.8525F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r17 = prawn3.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(22, 13).addBox(0.686F, 0.35F, 0.7374F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 17).addBox(0.686F, -0.65F, -1.2626F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, -0.3927F, 0.0F));

		PartDefinition prawn4 = prawn3.addOrReplaceChild("prawn4", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 0.0F, 3.0F, 0.0F, 1.7017F, 0.0F));

		PartDefinition cube_r18 = prawn4.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(14, 16).addBox(0.4386F, -0.75F, -1.5613F, 1.5F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r19 = prawn4.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0688F, -0.15F, -1.8525F, 3.0F, 0.0F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(5, 16).addBox(-1.945F, -0.15F, -1.4352F, 1.0F, 0.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(14, 13).addBox(-1.0688F, -0.6501F, -1.8525F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r20 = prawn4.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(22, 13).addBox(0.686F, 0.35F, 0.7374F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 17).addBox(0.686F, -0.65F, -1.2626F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, -0.3927F, 0.0F));

		PartDefinition prawn5 = prawn.addOrReplaceChild("prawn5", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0999F, 0.0417F, -0.0739F, 0.0F, -1.7453F, 0.0F));

		PartDefinition cube_r21 = prawn5.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(14, 16).addBox(0.4386F, -0.75F, -1.5613F, 1.5F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r22 = prawn5.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0688F, -0.15F, -1.8525F, 3.0F, 0.0F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(5, 16).addBox(-1.945F, -0.15F, -1.4352F, 1.0F, 0.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(14, 13).addBox(-1.0688F, -0.6501F, -1.8525F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r23 = prawn5.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(22, 13).addBox(0.686F, 0.35F, 0.7374F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 17).addBox(0.686F, -0.65F, -1.2626F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, -0.3927F, 0.0F));

		PartDefinition prawn6 = prawn5.addOrReplaceChild("prawn6", CubeListBuilder.create(), PartPose.offsetAndRotation(7.0F, 0.0F, 3.0F, 0.0F, 1.7017F, 0.0F));

		PartDefinition cube_r24 = prawn6.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(14, 16).addBox(0.4386F, -0.75F, -1.5613F, 1.5F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r25 = prawn6.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0688F, -0.15F, -1.8525F, 3.0F, 0.0F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(5, 16).addBox(-1.945F, -0.15F, -1.4352F, 1.0F, 0.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(14, 13).addBox(-1.0688F, -0.6501F, -1.8525F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r26 = prawn6.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(22, 13).addBox(0.686F, 0.35F, 0.7374F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 17).addBox(0.686F, -0.65F, -1.2626F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, -0.3927F, 0.0F));

		PartDefinition prawn7 = prawn5.addOrReplaceChild("prawn7", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 8.0F, 0.0F, -1.309F, 0.0F));

		PartDefinition cube_r27 = prawn7.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(14, 16).addBox(0.4386F, -0.75F, -1.5613F, 1.5F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r28 = prawn7.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0688F, -0.15F, -1.8525F, 3.0F, 0.0F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(5, 16).addBox(-1.945F, -0.15F, -1.4352F, 1.0F, 0.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(14, 13).addBox(-1.0688F, -0.6501F, -1.8525F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r29 = prawn7.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(22, 13).addBox(0.686F, 0.35F, 0.7374F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 17).addBox(0.686F, -0.65F, -1.2626F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, -0.3927F, 0.0F));

		PartDefinition prawn8 = prawn7.addOrReplaceChild("prawn8", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.0F, 0.0F, 1.0F, 0.0F, 1.7017F, 0.0F));

		PartDefinition cube_r30 = prawn8.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(14, 16).addBox(0.4386F, -0.75F, -1.5613F, 1.5F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r31 = prawn8.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0688F, -0.15F, -1.8525F, 3.0F, 0.0F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(5, 16).addBox(-1.945F, -0.15F, -1.4352F, 1.0F, 0.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(14, 13).addBox(-1.0688F, -0.6501F, -1.8525F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r32 = prawn8.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(22, 13).addBox(0.686F, 0.35F, 0.7374F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 17).addBox(0.686F, -0.65F, -1.2626F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, -0.3927F, 0.0F));

		PartDefinition prawn9 = prawn.addOrReplaceChild("prawn9", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0999F, -0.9583F, -3.0739F, 0.0F, -0.8727F, 0.0F));

		PartDefinition cube_r33 = prawn9.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(14, 16).addBox(0.4386F, -0.75F, -1.5613F, 1.5F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r34 = prawn9.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0688F, -0.15F, -1.8525F, 3.0F, 0.0F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(5, 16).addBox(-1.945F, -0.15F, -1.4352F, 1.0F, 0.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(14, 13).addBox(-1.0688F, -0.6501F, -1.8525F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r35 = prawn9.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(22, 13).addBox(0.686F, 0.35F, 0.7374F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 17).addBox(0.686F, -0.65F, -1.2626F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, -0.3927F, 0.0F));

		PartDefinition prawn10 = prawn9.addOrReplaceChild("prawn10", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 0.0F, 3.0F, 0.0F, 1.7017F, 0.0F));

		PartDefinition cube_r36 = prawn10.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(14, 16).addBox(0.4386F, -0.75F, -1.5613F, 1.5F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0416F, 0.0739F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r37 = prawn10.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0688F, -0.15F, -1.8525F, 3.0F, 0.0F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(5, 16).addBox(-1.945F, -0.15F, -1.4352F, 1.0F, 0.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(14, 13).addBox(-1.0688F, -0.6501F, -1.8525F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0416F, 0.0739F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r38 = prawn10.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(22, 13).addBox(0.686F, 0.35F, 0.7374F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 17).addBox(0.686F, -0.65F, -1.2626F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0416F, 0.0739F, 0.0F, -0.3927F, 0.0F));

		PartDefinition prawn11 = prawn9.addOrReplaceChild("prawn11", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, 0.0F, 4.0F, 0.0F, -2.0508F, 0.0F));

		PartDefinition cube_r39 = prawn11.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(14, 16).addBox(0.4386F, -0.75F, -1.5613F, 1.5F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r40 = prawn11.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0688F, -0.15F, -1.8525F, 3.0F, 0.0F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(5, 16).addBox(-1.945F, -0.15F, -1.4352F, 1.0F, 0.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(14, 13).addBox(-1.0688F, -0.6501F, -1.8525F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r41 = prawn11.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(22, 13).addBox(0.686F, 0.35F, 0.7374F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 17).addBox(0.686F, -0.65F, -1.2626F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0417F, 0.0739F, 0.0F, -0.3927F, 0.0F));

		PartDefinition prawn12 = prawn11.addOrReplaceChild("prawn12", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 0.0F, 3.0F, 0.0F, 1.7017F, 0.0F));

		PartDefinition cube_r42 = prawn12.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(14, 16).addBox(0.4386F, -0.75F, -1.5613F, 1.5F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0416F, 0.0739F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r43 = prawn12.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(22, 13).addBox(-1.0688F, -0.15F, -1.8525F, 3.0F, 0.0F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(5, 16).addBox(-1.945F, -0.15F, -1.4352F, 1.0F, 0.0F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(14, 13).addBox(-1.0688F, -0.6501F, -1.8525F, 3.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0416F, 0.0739F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r44 = prawn12.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(22, 13).addBox(0.686F, 0.35F, 0.7374F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 17).addBox(0.686F, -0.65F, -1.2626F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0999F, -0.0416F, 0.0739F, 0.0F, -0.3927F, 0.0F));

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