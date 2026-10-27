package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.entity.Redfin;
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
import net.minecraft.util.Mth;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;

public class RedfinModel extends HierarchicalModel<Redfin> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(new ResourceLocation(flavor_immersed_daily_argo.MODID, "redfin"), "main");

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart leftFin;
	private final ModelPart rightFin;
	private final ModelPart tailfin;

	public RedfinModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = this.body.getChild("head");
		this.leftFin = this.body.getChild("leftFin");
		this.rightFin = this.body.getChild("rightFin");
		this.tailfin = this.body.getChild("tailfin");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
			.texOffs(5, 5).addBox(-1.0F, -3.5F, 6.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(22, 7).addBox(0.0F, -5.5F, 0.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
		body.addOrReplaceChild("head", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-0.9992F, -1.3008F, -3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
			.texOffs(11, 0).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -2.0F));
		body.addOrReplaceChild("leftFin", CubeListBuilder.create()
			.texOffs(24, 4).addBox(0.0F, 0.0F, -2.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(24, 4).addBox(0.0F, 1.0F, 1.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.6109F));
		body.addOrReplaceChild("rightFin", CubeListBuilder.create()
			.texOffs(24, 1).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(24, 1).addBox(-2.0F, 1.0F, 1.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.6109F));
		body.addOrReplaceChild("tailfin", CubeListBuilder.create()
			.texOffs(20, 1).addBox(0.0F, -4.0F, 0.0F, 0.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));
		body.addOrReplaceChild("waist", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public ModelPart root() {
		return this.body;
	}

	@Override
	public void setupAnim(Redfin entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		float swimAmount = Math.min(entity.getSwimAmount(ageInTicks - 1.0F), 1.0F);
		float tick = ageInTicks * 0.3F;

		this.body.yRot = Mth.sin(tick) * 0.1F;
		this.head.yRot = -this.body.yRot;
		this.tailfin.yRot = Mth.sin(tick + 0.5F) * 0.45F;

		if (!entity.isInWater()) {
			this.body.xRot = -1.2F;
			this.body.y = 19.0F;
		} else {
			this.body.xRot = swimAmount * 0.3F;
			this.body.y = Mth.lerp(swimAmount, 24.0F, 22.0F);
		}
	}
}
