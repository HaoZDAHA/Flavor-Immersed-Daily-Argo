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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

// 气象仪器模型
//
// 几何与 UV 严格取自 Blockbench 导出的 weather_instrument.java（128×128 贴图）：
// 根节点 bone 是整根塔架（含横臂与配重），下面挂着会转的 round（风车盘）。
//
// 导出动画 none（6 秒一圈）与 heavy（0.5 秒一圈）都是让 round 绕 Y 轴
// 匀速转一整圈；这里不搬 AnimationState，直接由渲染器按游戏时间设置旋转角度。
public class WeatherInstrumentModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(
                    flavor_immersed_daily_argo.MODID, "weather_instrument"), "main");

    private final ModelPart bone;
    private final ModelPart round;

    public WeatherInstrumentModel(ModelPart root) {
        this.bone = root.getChild("bone");
        this.round = this.bone.getChild("round");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create()
                .texOffs(56, 4).addBox(-1.0F, 7.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 36).addBox(-3.0F, 37.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(56, 44).addBox(-3.0F, 20.0F, -4.0F, 6.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(16, 64).addBox(-2.0F, 23.0F, -1.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(56, 0).addBox(-11.0F, 14.0F, -0.5F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 64).addBox(7.0F, 8.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(64, 4).addBox(-2.0F, 5.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -15.0F, 0.0F));

        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                .texOffs(56, 56).addBox(-3.5F, -5.0F, -0.7F, 7.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-9.5F, 11.0F, -1.5F, -0.3927F, 0.0F, 0.0F));

        bone.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                .texOffs(64, 23).addBox(-1.1F, -1.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-10.0F, 12.5F, -1.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition round = bone.addOrReplaceChild("round", CubeListBuilder.create()
                .texOffs(64, 10).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, -4.0F, -14.0F, 0.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        round.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                .texOffs(0, 32).addBox(0.0F, -2.0F, -14.0F, 0.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.bone;
    }

// 让风车盘绕 Y 轴转到指定角度（度）
    public void setRoundAngle(float degrees) {
        this.round.yRot = degrees * Mth.DEG_TO_RAD;
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
// 方块装饰模型：旋转由渲染器按游戏时间直接驱动
    }
}
