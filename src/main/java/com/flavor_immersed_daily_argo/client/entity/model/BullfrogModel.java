package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.entity.Bullfrog;
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

public class BullfrogModel extends AgeableListModel<Bullfrog> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(new ResourceLocation(flavor_immersed_daily_argo.MODID, "bullfrog"), "main");

	private final ModelPart bone;
	private final ModelPart body;
	private final ModelPart qianleg;
	private final ModelPart houleg;

	public BullfrogModel(ModelPart root) {
		this.bone = root.getChild("bone");
		this.body = this.bone.getChild("body");
		this.qianleg = this.bone.getChild("qianleg");
		this.houleg = this.bone.getChild("houleg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 0.0F));

		bone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 18).addBox(1.0F, -6.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(8, 18).addBox(-3.0F, -6.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		bone.addOrReplaceChild("qianleg", CubeListBuilder.create().texOffs(0, 22).addBox(-4.0F, -2.0F, -4.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(6, 22).addBox(3.0F, -2.0F, -4.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(12, 22).addBox(-4.0F, 1.0F, -5.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(20, 22).addBox(2.0F, 1.0F, -5.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		bone.addOrReplaceChild("houleg", CubeListBuilder.create().texOffs(0, 12).addBox(2.0F, -1.0F, 1.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(14, 12).addBox(-5.0F, -1.0F, 1.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(16, 18).addBox(-5.0F, 1.0F, -1.0F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 20).addBox(2.0F, 1.0F, -1.0F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	@Override
	public void setupAnim(Bullfrog entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.body.xRot = 0;
		this.body.y = 0;
		this.body.z = 0;
		this.body.zScale = 1;
		this.qianleg.xRot = 0;
		this.qianleg.y = 0;
		this.qianleg.z = 0;
		this.houleg.xRot = 0;
		this.houleg.y = 0;
		this.houleg.z = 0;

		if (limbSwingAmount > 0.01F) {
			applyWalk((limbSwing / (Mth.PI * 2.0F) % 1.0F) * 0.75F, limbSwingAmount);
		} else {
			applyIdle((ageInTicks % 10.0F) / 10.0F);
		}
	}

	private void applyIdle(float t) {
		float[] ts = {0.0F, 0.5F, 1.0F};
		float[] bsz = {1.0F, 1.1F, 1.0F};
		int i = t < 0.5F ? 0 : 1;
		float f = (t - ts[i]) / (ts[i + 1] - ts[i]);
		this.body.zScale = lerp(bsz[i], bsz[i + 1], f);
	}

	private void applyWalk(float t, float a) {
		float[] times = {0.0F, 0.25F, 0.5F, 0.75F};
		float[] bodyRX = {0.0F, 10.0F, -15.0F, 0.0F};
		float[] bodyPY = {0.0F, 0.0F, 6.0F, 0.0F};
		float[] bodyPZ = {0.0F, 1.0F, 0.0F, 0.0F};
		float[] qlRX = {0.0F, -22.5F, -45.0F, 0.0F};
		float[] qlPY = {0.0F, -2.0F, 4.0F, 0.0F};
		float[] qlPZ = {0.0F, 0.0F, -2.0F, 0.0F};
		float[] hlRX = {0.0F, 10.0F, 50.0F, 0.0F};
		float[] hlPY = {0.0F, -1.0F, 3.0F, 0.0F};
		int i = 0;
		while (i < times.length - 1 && t >= times[i + 1]) i++;
		float f = Math.min((t - times[i]) / (times[i + 1] - times[i]), 1.0F);
		this.body.xRot = lerp(bodyRX[i], bodyRX[i + 1], f) * a * Mth.DEG_TO_RAD;
		this.body.y = lerp(bodyPY[i], bodyPY[i + 1], f) * a;
		this.body.z = lerp(bodyPZ[i], bodyPZ[i + 1], f) * a;
		this.qianleg.xRot = lerp(qlRX[i], qlRX[i + 1], f) * a * Mth.DEG_TO_RAD;
		this.qianleg.y = lerp(qlPY[i], qlPY[i + 1], f) * a;
		this.qianleg.z = lerp(qlPZ[i], qlPZ[i + 1], f) * a;
		this.houleg.xRot = lerp(hlRX[i], hlRX[i + 1], f) * a * Mth.DEG_TO_RAD;
		this.houleg.y = lerp(hlPY[i], hlPY[i + 1], f) * a;
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
