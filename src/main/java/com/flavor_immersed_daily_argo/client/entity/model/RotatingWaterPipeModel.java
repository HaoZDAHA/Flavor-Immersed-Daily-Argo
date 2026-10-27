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

// 旋转水管模型
//
// 几何与 UV 严格取自 Blockbench 导出的 rotating_water_pipe.java（64×64 贴图）：
// 根节点 bone 是底座与立柱，下面挂着绕竖轴转的旋转臂 bone2（含镜像的两条斜臂
// cube_r1 / cube_r2）。
//
// 导出动画 round 让 bone2 在 2 秒里绕 Y 轴由 0 转到 −360°；这里不搬
// AnimationState，直接按游戏时间匀速设置转角，多台洒水器相位还能对齐。
public class RotatingWaterPipeModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(flavor_immersed_daily_argo.MODID, "rotating_water_pipe"), "main");

    private final ModelPart root;
    private final ModelPart arm;

    public RotatingWaterPipeModel(ModelPart root) {
        this.root = root.getChild("bone");
        this.arm = this.root.getChild("bone2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(3.0F, 7.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-6.0F, 15.0F, 0.0F));

        PartDefinition bone2 = bone.addOrReplaceChild("bone2",
                CubeListBuilder.create()
                        .texOffs(24, 36).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 36).addBox(-1.0F, 0.0F, -0.999F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(6.0F, 0.0F, 0.0F));

        bone2.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(0, 40).addBox(0.75F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 32).addBox(-4.25F, -1.0F, -1.001F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.25F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2182F));

        bone2.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(24, 32).addBox(-0.75F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(12, 40).addBox(-2.75F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.25F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2182F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

// 让旋转臂绕竖轴转到指定角度（度），复刻导出动画 round（0 → −360°）
    public void setArmAngle(float degrees) {
        this.arm.yRot = degrees * Mth.DEG_TO_RAD;
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
// 方块装饰模型：旋转由渲染器按游戏时间直接驱动
    }
}