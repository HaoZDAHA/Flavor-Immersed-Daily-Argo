package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
// 油焖大虾第 1 阶段的实体模型（Blockbench 导出后手工整理）。
//
// 只保留根节点 root 与 bone 两级取件：其余 pot / prawnX
// 都是 bone 的嵌套子节点，由烘焙后的模型树自行持有；对它们调用 root.getChild()
// 会在启动时抛 NoSuchElementException，导致资源包重载失败。
public class BraisedPrawnsStage1Model extends HierarchicalModel<Entity> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(new ResourceLocation(flavor_immersed_daily_argo.MODID, "braised_prawns_stage1"), "main");

	private final ModelPart root;
	private final ModelPart bone;

	public BraisedPrawnsStage1Model(ModelPart root) {
		this.root = root;
		this.bone = root.getChild("bone");
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

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
						  float netHeadYaw, float headPitch) {
// 方块装饰模型：几何静态，吃掉一口就换下一个阶段的模型
	}
}