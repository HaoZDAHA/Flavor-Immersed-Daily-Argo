package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.entity.Bamboorat;
import net.minecraft.client.model.AgeableListModel;
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

import java.util.Collections;
import java.util.List;

public class BambooratModel extends AgeableListModel<Bamboorat> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "bamboorat"), "main");

	private final ModelPart bone;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart qianhead;
	private final ModelPart houhead;

	public BambooratModel(ModelPart root) {
		this.bone = root.getChild("bone");
		this.body = this.bone.getChild("body");
		this.head = this.bone.getChild("head");
		this.qianhead = this.bone.getChild("qianhead");
		this.houhead = this.bone.getChild("houhead");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 25.0F, 0.0F));

		PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(1, 0).addBox(-2.0F, -6.0F, -3.0F, 4.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(22, 18).addBox(-1.0F, -5.0F, 5.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(14, 14).addBox(-1.0F, -1.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -4.0F, 7.0F, 0.0F, 0.9163F, 0.0F));

		body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(22, 21).addBox(-1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -4.0F, 7.0F, 0.0F, 1.5708F, 0.0F));

		bone.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 14).addBox(-2.0F, -5.0F, -7.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(16, 22).addBox(-1.0F, -3.0F, -8.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 1.0F));

		bone.addOrReplaceChild("qianhead", CubeListBuilder.create().texOffs(14, 18).addBox(2.0F, -2.0F, -4.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(0, 21).addBox(-3.0F, -2.0F, -4.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		bone.addOrReplaceChild("houhead", CubeListBuilder.create().texOffs(8, 22).addBox(2.0F, -2.0F, 2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(22, 14).addBox(-3.0F, -2.0F, 2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	@Override
	public void setupAnim(Bamboorat entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.body.yRot = 0;
		this.body.yScale = 1;
		this.head.xRot = 0;
		this.head.yRot = 0;
		this.qianhead.xRot = 0;
		this.houhead.xRot = 0;
		this.houhead.y = 0;

		if (limbSwingAmount > 0.01F) {
			applyWalk((limbSwing / (Mth.PI * 2.0F) % 1.0F) * 0.5F, limbSwingAmount);
		} else {
			applyIdle((ageInTicks % 40.0F) / 40.0F);
		}
	}

	private void applyIdle(float t) {
		float[] ts = {0.0F, 0.5F, 1.0F};
		float[] bsy = {1.0F, 1.1F, 1.0F};
		float[] hdx = {0.0F, -5.0F, 0.0F};
		int i = t < 0.5F ? 0 : 1;
		float f = (t - ts[i]) / (ts[i + 1] - ts[i]);
		this.body.yScale = lerp(bsy[i], bsy[i + 1], f);
		this.head.xRot = lerp(hdx[i], hdx[i + 1], f) * Mth.DEG_TO_RAD;
	}

	private void applyWalk(float t, float a) {
		float[] times = {0.0F, 0.125F, 0.25F, 0.375F, 0.5F};
		float[] bodyRY = {0.0F, -5.0F, 0.0F, 7.5F, 0.0F};
		float[] headRY = {0.0F, 5.0F, 0.0F, -5.0F, 0.0F};
		float[] qhRX = {0.0F, 10.0F, 0.0F, -12.5F, 0.0F};
		float[] hhRX = {0.0F, -10.0F, 0.0F, 15.0F, 0.0F};
		float[] hhPY = {0.0F, 1.0F, 0.0F, -1.0F, 0.0F};
		int i = 0;
		while (i < times.length - 1 && t >= times[i + 1]) i++;
		float f = Math.min((t - times[i]) / (times[i + 1] - times[i]), 1.0F);
		this.body.yRot = lerp(bodyRY[i], bodyRY[i + 1], f) * a * Mth.DEG_TO_RAD;
		this.head.yRot = lerp(headRY[i], headRY[i + 1], f) * a * Mth.DEG_TO_RAD;
		this.qianhead.xRot = lerp(qhRX[i], qhRX[i + 1], f) * a * Mth.DEG_TO_RAD;
		this.houhead.xRot = lerp(hhRX[i], hhRX[i + 1], f) * a * Mth.DEG_TO_RAD;
		this.houhead.y = lerp(hhPY[i], hhPY[i + 1], f) * a;
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return Collections.emptyList();
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return List.of(bone);
	}
}
