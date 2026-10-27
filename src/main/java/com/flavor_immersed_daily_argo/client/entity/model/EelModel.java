package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.entity.Eel;
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
import net.minecraft.util.Mth;

// 鳗鱼模型
//
// 几何与 UV 严格取自 Blockbench 导出的 RawEelModel.bbmodel（64x64 贴图）。
public class EelModel extends HierarchicalModel<Eel> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "eel"), "main");

	private final ModelPart bone;
	private final ModelPart head;
	private final ModelPart firstbody;
	private final ModelPart secondbody;
	private final ModelPart hair;

	public EelModel(ModelPart root) {
		this.bone = root.getChild("bone");
		this.head = this.bone.getChild("head");
		this.firstbody = this.bone.getChild("firstbody");
		this.secondbody = this.bone.getChild("secondbody");
		this.hair = this.bone.getChild("hair");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition head = bone.addOrReplaceChild("head", CubeListBuilder.create()
			.texOffs(0, 28).addBox(-1.0F, -4.0F, -12.0F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
			.texOffs(24, 12).addBox(-1.0F, -3.0F, -15.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
			.texOffs(24, 24).addBox(-1.0F, -1.0F, -13.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
			.texOffs(14, -4).addBox(0.0F, -5.0F, -9.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -5.0F));
		PartDefinition firstbody = bone.addOrReplaceChild("firstbody", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-1.0F, -4.0F, -5.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
			.texOffs(14, -4).addBox(0.0F, -5.0F, -4.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
			.texOffs(14, -4).addBox(0.0F, -5.0F, 0.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -5.0F));
		PartDefinition secondbody = bone.addOrReplaceChild("secondbody", CubeListBuilder.create()
			.texOffs(0, 14).addBox(-1.0F, -4.0F, 5.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
			.texOffs(14, -4).addBox(0.0F, -5.0F, 6.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
			.texOffs(14, -4).addBox(0.0F, -5.0F, 10.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -5.0F));
		PartDefinition hair = bone.addOrReplaceChild("hair", CubeListBuilder.create()
			.texOffs(18, 33).addBox(-1.0F, -1.0F, 15.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
			.texOffs(32, 33).addBox(-1.0F, -4.0F, 15.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
			.texOffs(24, 0).addBox(-1.0F, -3.0F, 15.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
			.texOffs(14, -4).addBox(0.0F, -5.0F, 16.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
			.texOffs(14, -4).addBox(0.0F, -4.0F, 20.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
			.texOffs(17, -1).addBox(0.0F, -4.0F, 24.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -5.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public ModelPart root() {
		return this.bone;
	}

	@Override
	public void setupAnim(Eel entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
// 鳗鱼无 Blockbench 动画文件，用正弦摆动模拟蛇形游动
		float swimAmount = Math.min(entity.getSwimAmount(ageInTicks - 1.0F), 1.0F);
		float tick = ageInTicks * 0.35F;

		this.head.yRot = Mth.sin(tick) * 0.06F;
		this.firstbody.yRot = Mth.sin(tick + 0.4F) * 0.16F;
		this.secondbody.yRot = Mth.sin(tick + 0.8F) * 0.26F;
		this.hair.yRot = Mth.sin(tick + 1.2F) * 0.36F;

		if (!entity.isInWater()) {
			this.bone.xRot = -1.2F;
			this.bone.y = 19.0F;
		} else {
			this.bone.xRot = swimAmount * 0.3F;
			this.bone.y = Mth.lerp(swimAmount, 24.0F, 22.0F);
		}
	}
}
