package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.entity.ChineseMackerel;
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

// 鲅鱼模型
//
// 几何与 UV 取自 Blockbench 导出的 ChineseMackerelModel.bbmodel（128x128 贴图）。
public class ChineseMackerelModel extends HierarchicalModel<ChineseMackerel> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "chinese_mackerel"), "main");

	private final ModelPart bone;
	private final ModelPart fin2;
	private final ModelPart body;
	private final ModelPart fin;

	public ChineseMackerelModel(ModelPart root) {
		this.bone = root.getChild("bone");
		this.body = this.bone.getChild("body");
		this.fin2 = this.body.getChild("fin2");
		this.fin = this.body.getChild("fin");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 14.0F, 4.0F));
		PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 45).addBox(-3.0F, -4.0F, 7.0F, 6.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-4.0F, -5.0F, 16.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(36, 37).addBox(-3.0F, -4.0F, 32.0F, 6.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(36, 24).addBox(-3.0F, -3.0F, 39.0F, 6.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(46, 61).addBox(-1.0F, -3.0F, 48.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(54, 50).addBox(0.0F, -7.0F, 49.0F, 0.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(48, 0).addBox(-3.0F, -5.0F, 12.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 57).addBox(-3.0F, -3.0F, 4.0F, 6.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
				, PartPose.offset(0.0F, 5.0F, -28.0F));
// fin / fin2 是贴在身体表面上的背鳍、臀鳍与胸鳍。bbmodel 里它们和 body 同级挂在 bone 下，
// 但那样 body 一摆动，它们就会留在原地、从鱼身上穿出去；挂到 body 下面才会跟着身体一起摆。
// 支点换算：父级 body 原点 [0,5,-24] → 子级 [0,10,4]，即 (0, -5, 28)，静止时几何/UV 与 bbmodel 完全一致。
		PartDefinition fin2 = body.addOrReplaceChild("fin2", CubeListBuilder.create()
				.texOffs(18, 62).addBox(0.0F, -3.0F, 4.0F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
				, PartPose.offset(0.0F, -5.0F, 28.0F));
		PartDefinition fin = body.addOrReplaceChild("fin", CubeListBuilder.create()
				.texOffs(38, 50).addBox(0.0F, -3.0F, -12.0F, 0.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 24).addBox(0.0F, 8.0F, -12.0F, 0.0F, 3.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(38, 61).addBox(0.0F, -3.0F, 0.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				, PartPose.offset(0.0F, -5.0F, 28.0F));
		PartDefinition cube_r1 = fin.addOrReplaceChild("cube_r1", CubeListBuilder.create()
				.texOffs(48, 12).addBox(0.0F, -2.5F, -8.0F, 0.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 4.5F, -4.0F, 0.0F, -0.1309F, 0.0F));
		PartDefinition cube_r2 = fin.addOrReplaceChild("cube_r2", CubeListBuilder.create()
				.texOffs(22, 50).addBox(0.0F, -2.5F, -8.0F, 0.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 4.5F, -4.0F, 0.0F, 0.1309F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public ModelPart root() {
		return this.bone;
	}

	@Override
	public void setupAnim(ChineseMackerel entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		float swimAmount = Math.min(entity.getSwimAmount(ageInTicks - 1.0F), 1.0F);

// 摆动的相位基准。鲅鱼只有 body 一根骨骼，于是用"根部反向微摆 + 身体主摆"错开相位，
// 做出和金枪鱼 model 里各骨节递进摆动类似的波浪感：
// 尾鳍离支点（吻端）最远，甩动最大；头部几乎贴着支点，基本保持稳定。
		float tick = ageInTicks * 0.4F;

		this.bone.yRot = Mth.sin(tick + (float) Math.PI) * 0.07F;
		this.body.yRot = Mth.sin(tick) * 0.26F;

// fin / fin2 挂在 body 下，会跟着身体一起摆；这里再叠加一点滞后的、幅度更大的
// 独立摆动，让背鳍、臀鳍、胸鳍有划水的跟随感，而不是死板地跟着身体。
		this.fin.yRot = Mth.sin(tick - 0.6F) * 0.16F;
		this.fin2.yRot = Mth.sin(tick - 1.0F) * 0.12F;

		if (!entity.isInWater()) {
			this.bone.xRot = -1.2F;
			this.bone.y = 9.0F;
		} else {
			this.bone.xRot = swimAmount * 0.3F;
			this.bone.y = Mth.lerp(swimAmount, 14.0F, 12.0F);
		}
	}
}
