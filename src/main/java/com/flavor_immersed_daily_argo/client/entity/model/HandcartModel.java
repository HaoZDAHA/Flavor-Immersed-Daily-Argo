package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.entity.Handcart;
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

// 拉力木车模型。
//
// 几何与 UV 严格取自 Blockbench 导出的 handcart.bbmodel（256×256 贴图），
// 动画取自同一份导出的 model.java。
//
// 模型是朝 +z 建的：车把横杆在 -z 一侧，车斗朝 +z 拖在后面。
public class HandcartModel extends HierarchicalModel<Handcart> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(flavor_immersed_daily_argo.MODID, "handcart"), "main");

// 平时（没被牵引、停在原地）的姿势：车身前倾 12.5°。
//
// 导出的原版长度是 0 秒——只有一帧的静态姿势。这里给成 1 秒循环：
// 播放时长做除数，用 0 会在取余时得到 NaN 把姿势算坏，改成 1 秒视觉效果完全一样。
    public static final AnimationDefinition handcart_free = AnimationDefinition.Builder.withLength(1.0F).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

// 被牵引着走动时的姿势：车轮滚一圈，车身随路面轻微颠簸。
//
// 导出文件里 body 的三条通道关键帧全是 0（等于不动），而没播动画时 body 本来就停在初始姿势上，
// 所以这里省掉；bone 的 POSITION / SCALE 也全是 0/1，同样省掉。
    public static final AnimationDefinition handcart_free2 = AnimationDefinition.Builder.withLength(1.0F).looping()
            .addAnimation("bone", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.0833F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4167F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5833F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8333F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("wheel", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(180.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.75F, KeyframeAnimations.degreeVec(270.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(360.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

// 整棵模型的根节点（bone 的父级）。动画里有针对 bone 自身的通道，根必须是它的父级才找得到。
    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart body;

// 载货模式才出现的那只大箱子，跟着 body 一起动
    private final ModelPart expand;
    private final ModelPart wheel;

    public HandcartModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
        this.body = this.bone.getChild("body");
        this.expand = this.body.getChild("expand");
        this.wheel = this.bone.getChild("wheel");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(-8.0F, 13.0F, 0.0F));

        PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(104, 103).addBox(7.0F, -4.0F, -55.0F, 3.0F, 4.0F, 28.0F, new CubeDeformation(0.0F))
                .texOffs(0, 107).addBox(-10.0F, -4.0F, -55.0F, 3.0F, 4.0F, 28.0F, new CubeDeformation(0.0F))
                .texOffs(62, 135).addBox(-16.0F, -9.0F, -29.0F, 34.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(92, 55).addBox(13.0F, -9.0F, -24.0F, 5.0F, 7.0F, 41.0F, new CubeDeformation(0.0F))
                .texOffs(0, 55).addBox(-16.0F, -9.0F, -24.0F, 5.0F, 7.0F, 41.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-16.0F, -2.0F, -29.0F, 34.0F, 5.0F, 50.0F, new CubeDeformation(0.0F))
                .texOffs(62, 107).addBox(-7.0F, -4.0F, -55.0F, 14.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, 49.0F));

// 载货模式的大箱子：几何取自 handcart_expand.bbmodel，挂在 body 底下，
// 平时整块隐藏，装上箱子后才亮出来（见 setupAnim）
        PartDefinition expand = body.addOrReplaceChild("expand", CubeListBuilder.create()
                .texOffs(192, 224).addBox(-7.0F, -29.0F, 25.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 11.0F, -49.0F));

        expand.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                .texOffs(192, 224).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -21.0F, 53.0F, 0.0F, -0.48F, 0.0F));

        PartDefinition wheel = bone.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(0, 103).addBox(-47.0F, -1.0F, -1.0F, 50.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(31.0F, 1.0F, 47.0F));

        PartDefinition left = wheel.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 139).addBox(-3.0F, -23.0F, -13.0F, 2.0F, 24.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 12.0F, 10.0F));

        PartDefinition cube_r1 = left.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(16, 139).addBox(-1.0F, -12.0F, -3.0F, 2.0F, 24.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -12.0F, -10.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r2 = left.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(62, 114).addBox(-1.5F, -6.0F, -1.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -18.0F, -17.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r3 = left.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(92, 114).addBox(-1.0F, -6.5F, -1.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.6464F, -17.3536F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r4 = left.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(82, 114).addBox(-1.0F, -6.5F, -2.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -17.6464F, -2.3536F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r5 = left.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(72, 114).addBox(-1.5F, -6.0F, -2.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -5.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition left2 = left.addOrReplaceChild("left2", CubeListBuilder.create().texOffs(0, 139).addBox(-3.0F, -23.0F, -13.0F, 2.0F, 24.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-43.0F, 0.0F, 0.0F));

        PartDefinition cube_r6 = left2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(16, 139).addBox(-1.0F, -12.0F, -3.0F, 2.0F, 24.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -12.0F, -10.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r7 = left2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(62, 114).addBox(-1.5F, -6.0F, -1.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -18.0F, -17.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r8 = left2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(92, 114).addBox(-1.0F, -6.5F, -1.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.6464F, -17.3536F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r9 = left2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(82, 114).addBox(-1.0F, -6.5F, -2.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -17.6464F, -2.3536F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r10 = left2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(72, 114).addBox(-1.5F, -6.0F, -2.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -5.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(Handcart entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
// 恢复默认姿势，避免上一帧动画残留
        this.root().getAllParts().forEach(ModelPart::resetPose);

// 装了箱子才把那只大箱子亮出来；resetPose 不动 visible，这里每帧照实设一遍最稳
        this.expand.visible = entity.isExpanded();

        if (!entity.isFollowing()) {
// 平时：车停在原地，车身前倾
            entity.free2AnimationState.stop();
            entity.freeAnimationState.startIfStopped(entity.tickCount);
            this.animate(entity.freeAnimationState, handcart_free, ageInTicks, 1.0F);
        } else if (entity.isMoving()) {
// 被拉着走：车轮转起来
            entity.freeAnimationState.stop();
            entity.free2AnimationState.startIfStopped(entity.tickCount);
            this.animate(entity.free2AnimationState, handcart_free2, ageInTicks, 1.0F);
        } else {
// 挂上了但停着没动：不放动画，车身放平
            entity.freeAnimationState.stop();
            entity.free2AnimationState.stop();
        }
    }
}
