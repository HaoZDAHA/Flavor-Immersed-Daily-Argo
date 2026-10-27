package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.entity.Tuna;
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

// 金枪鱼模型
//
// 几何与 UV 严格取自 Blockbench 导出的 TunaModel.bbmodel（64x64 贴图）。
public class TunaModel extends HierarchicalModel<Tuna> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "tuna"), "main");

	private final ModelPart bone4;
	private final ModelPart bone;
	private final ModelPart bone2;
	private final ModelPart bone3;

	public TunaModel(ModelPart root) {
		this.bone4 = root.getChild("bone4");
		this.bone = this.bone4.getChild("bone");
		this.bone2 = this.bone4.getChild("bone2");
		this.bone3 = this.bone4.getChild("bone3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone4 = partdefinition.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition bone = bone4.addOrReplaceChild("bone", CubeListBuilder.create()
			.texOffs(26, 20).addBox(-3.0F, -9.0F, -9.0F, 6.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
			.texOffs(36, 12).addBox(-3.0F, -8.0F, -11.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(42, 33).addBox(-3.0F, -7.0F, -12.0F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -5.0F));
		PartDefinition bone2 = bone4.addOrReplaceChild("bone2", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-4.0F, -10.0F, -9.0F, 8.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
			.texOffs(42, 38).addBox(0.0F, -13.0F, -8.0F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
			.texOffs(12, 45).addBox(0.0F, 0.0F, -8.0F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
			.texOffs(32, 0).addBox(4.0F, -3.0F, -8.0F, 4.0F, 0.0F, 6.0F, new CubeDeformation(0.0F))
			.texOffs(32, 6).addBox(-8.0F, -3.0F, -8.0F, 4.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition bone3 = bone4.addOrReplaceChild("bone3", CubeListBuilder.create()
			.texOffs(0, 20).addBox(-4.0F, -9.0F, 1.0F, 8.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
			.texOffs(0, 33).addBox(-3.0F, -8.0F, 6.0F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
			.texOffs(42, 46).addBox(-2.0F, -6.0F, 9.0F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(18, 33).addBox(0.0F, -13.0F, 2.0F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
			.texOffs(0, 42).addBox(0.0F, -2.0F, 2.0F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
			.texOffs(32, 33).addBox(0.0F, -10.0F, 11.0F, 0.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public ModelPart root() {
		return this.bone4;
	}

	@Override
	public void setupAnim(Tuna entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
// 金枪鱼无 Blockbench 动画文件，用正弦摆动模拟尾鳍推进
		float swimAmount = Math.min(entity.getSwimAmount(ageInTicks - 1.0F), 1.0F);
		float tick = ageInTicks * 0.4F;

		this.bone.yRot = Mth.sin(tick) * 0.14F;
		this.bone2.yRot = Mth.sin(tick + 0.5F) * 0.20F;
		this.bone3.yRot = Mth.sin(tick + 1.0F) * 0.32F;

		if (!entity.isInWater()) {
			this.bone4.xRot = -1.2F;
			this.bone4.y = 19.0F;
		} else {
			this.bone4.xRot = swimAmount * 0.3F;
			this.bone4.y = Mth.lerp(swimAmount, 24.0F, 22.0F);
		}
	}
}
