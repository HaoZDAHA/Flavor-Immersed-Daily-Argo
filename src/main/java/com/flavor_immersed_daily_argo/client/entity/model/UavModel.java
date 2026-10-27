package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.entity.Uav;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
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

// 多功能无人机模型。
//
// 几何与 UV 严格取自 Blockbench 导出的 uav.bbmodel（64×64 贴图），动画取自同一份导出的
// model.java。导出的四个旋翼部件名是 "4" / "3" / "2" / ""，
// 空串与非数字开头的名字在 Java 里没法当字段用，这里统一改叫 rotor_a ~ rotor_d
// （符号一一对应，动画通道名同步改过）。
//
// 模型是朝 +z 建的：机头、相机吊舱都在 +z 一侧。
public class UavModel extends HierarchicalModel<Uav> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "uav"), "main");

// go 动画长度（秒），与导出一致
    private static final float GO_LENGTH = 1.5833F;

// 一段旋翼自转里关键帧的间隔数：0 ~ 1.5833 秒之间共 20 个关键帧
    private static final int SPIN_STEPS = 19;

// 飞行时的姿势：四个旋翼高速自转，机身轻微俯仰侧倾。
//
// 导出文件里每个旋翼只有一条 ROTATION 通道，另外各有一条 SCALE 通道全是 1.0；机身的 POSITION
// 通道也全是 0——这些都不会改变画面，省掉。旋翼的转向两两相反（对角同向）用来抵消反扭矩。
    public static final AnimationDefinition uav_go = AnimationDefinition.Builder.withLength(GO_LENGTH).looping()
            .addAnimation("rotor_a", spin(1.0F))
            .addAnimation("rotor_b", spin(-1.0F))
            .addAnimation("rotor_c", spin(1.0F))
            .addAnimation("rotor_d", spin(-1.0F))
            .addAnimation("bone2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1667F, KeyframeAnimations.degreeVec(4.9953F, 0.2178F, -2.4905F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.375F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5833F, KeyframeAnimations.degreeVec(4.9574F, -0.6518F, 7.4718F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.75F, KeyframeAnimations.degreeVec(-0.0426F, -0.6518F, 7.4718F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9583F, KeyframeAnimations.degreeVec(-5.0426F, -0.6518F, 7.4718F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1667F, KeyframeAnimations.degreeVec(-4.9149F, -1.3036F, -0.001F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.375F, KeyframeAnimations.degreeVec(-4.783F, -1.7265F, -4.9848F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5833F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

// 一条「整圈整圈转」的旋翼通道：绕 y 轴在 0 与 sign × 360° 之间来回打满，
// 也就是每两个关键帧之间正好转一整圈。sign 是转向，对角两个旋翼取同一个符号。
    private static AnimationChannel spin(float sign) {
        Keyframe[] frames = new Keyframe[SPIN_STEPS + 1];
        for (int i = 0; i <= SPIN_STEPS; i++) {
            float seconds = i * (GO_LENGTH / SPIN_STEPS);
            float degrees = (i % 2 == 0) ? 0.0F : sign * 360.0F;
            frames[i] = new Keyframe(seconds, KeyframeAnimations.degreeVec(0.0F, degrees, 0.0F),
                    AnimationChannel.Interpolations.LINEAR);
        }
        return new AnimationChannel(AnimationChannel.Targets.ROTATION, frames);
    }

// 整棵模型的根节点：动画里有直接打在根的子节点（bone2）上的通道，根得是它的父级才找得到
    private final ModelPart root;

    public UavModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone2 = partdefinition.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.0F, 15.0F, 0.0F));

// 左前旋翼
        PartDefinition rotorA = bone2.addOrReplaceChild("rotor_a", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-6.0F, -0.005F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-8.0F, -1.0F, -10.0F));

        rotorA.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-6.0F, -0.01F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

// 右前旋翼
        PartDefinition rotorB = bone2.addOrReplaceChild("rotor_b", CubeListBuilder.create()
                .texOffs(20, 19).addBox(-6.0F, -0.005F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(8.0F, -1.0F, -10.0F));

        rotorB.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                .texOffs(0, 21).addBox(-6.0F, -0.01F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

// 左后旋翼
        PartDefinition rotorC = bone2.addOrReplaceChild("rotor_c", CubeListBuilder.create()
                .texOffs(20, 15).addBox(-6.0F, -0.005F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-8.0F, -1.0F, 10.0F));

        rotorC.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                .texOffs(20, 17).addBox(-6.0F, -0.01F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

// 右后旋翼
        PartDefinition rotorD = bone2.addOrReplaceChild("rotor_d", CubeListBuilder.create()
                .texOffs(20, 11).addBox(-6.0F, -0.005F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(8.0F, -1.0F, 10.0F));

        rotorD.addOrReplaceChild("cube_r4", CubeListBuilder.create()
                .texOffs(20, 13).addBox(-6.0F, -0.01F, -1.0F, 12.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone = bone2.addOrReplaceChild("bone", CubeListBuilder.create()
                .texOffs(0, 11).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(8, 36).addBox(-1.0F, -1.0F, 3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.0F, -4.0F, -5.0F, 6.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(16, 36).addBox(-1.0F, -4.0F, 5.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 27).addBox(-2.0F, -5.0F, -3.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        bone.addOrReplaceChild("cube_r5", CubeListBuilder.create()
                .texOffs(4, 36).addBox(0.0F, -7.0F, 0.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(0.0F, -1.0F, -6.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(28, 34).addBox(0.0F, -7.0F, -7.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, 3.0F, 3.0F, 0.0F, 0.0F, 0.2618F));

        bone.addOrReplaceChild("cube_r6", CubeListBuilder.create()
                .texOffs(20, 27).addBox(-1.0F, -1.0F, -5.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(34, 29).addBox(-1.0F, -7.0F, -6.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-1.0F, -7.0F, 1.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0F, 3.0F, 2.0F, 0.0F, 0.0F, -0.2618F));

        bone.addOrReplaceChild("cube_r7", CubeListBuilder.create()
                .texOffs(22, 38).addBox(1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 34).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-7.0F, -6.0F, 9.0F, 0.0F, -2.3562F, 0.0F));

        bone.addOrReplaceChild("cube_r8", CubeListBuilder.create()
                .texOffs(32, 7).addBox(-3.0F, -1.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, -4.0F, 6.0F, 0.0F, -2.3562F, 0.0F));

        bone.addOrReplaceChild("cube_r9", CubeListBuilder.create()
                .texOffs(32, 37).addBox(-2.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 34).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.0F, -6.0F, -9.0F, 0.0F, -2.3562F, 0.0F));

        bone.addOrReplaceChild("cube_r10", CubeListBuilder.create()
                .texOffs(28, 25).addBox(-3.0F, -1.0F, -1.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0F, -4.0F, -6.0F, 0.0F, -2.3562F, 0.0F));

        bone.addOrReplaceChild("cube_r11", CubeListBuilder.create()
                .texOffs(36, 37).addBox(1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(34, 27).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.0F, -6.0F, 9.0F, 0.0F, -0.7854F, 0.0F));

        bone.addOrReplaceChild("cube_r12", CubeListBuilder.create()
                .texOffs(28, 23).addBox(-3.0F, -1.0F, -1.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0F, -4.0F, 6.0F, 0.0F, -0.7854F, 0.0F));

        bone.addOrReplaceChild("cube_r13", CubeListBuilder.create()
                .texOffs(22, 36).addBox(-2.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(32, 9).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-7.0F, -6.0F, -9.0F, 0.0F, -0.7854F, 0.0F));

        bone.addOrReplaceChild("cube_r14", CubeListBuilder.create()
                .texOffs(28, 21).addBox(-3.0F, -1.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, -4.0F, -6.0F, 0.0F, -0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(Uav entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
// 恢复默认姿势，避免上一帧动画残留
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (entity.shouldSpinRotors()) {
// 通电（悬停 / 飞行 / 自动 / 被操作），或者断电后还在半空缓降：旋翼都转
            entity.goAnimationState.startIfStopped(entity.tickCount);
            this.animate(entity.goAnimationState, uav_go, ageInTicks, 1.0F);
        } else {
// 落地停稳了才彻底不动
            entity.goAnimationState.stop();
        }
    }
}
