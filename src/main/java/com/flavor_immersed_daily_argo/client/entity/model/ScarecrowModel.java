package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.entity.Scarecrow;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// 稻草人模型。
//
// 几何与 UV 照搬 Blockbench 导出的 scarecrow.java（64×64 贴图），只做了 1.21.1 的签名适配并去掉动画：
// 不重写 EntityModel 的 renderToBuffer，改由 HierarchicalModel 渲染根节点。
//
// 模型的地面同样是 y=24（木杆底端），与 PoopModel 一致，所以渲染器里共用同一套
// 「翻 y 再下移 1.501 格」的位姿换算。
//
// <h2>为什么要拆成这么多部件</h2>
// 稻草人的脑袋和躯干，UV 与原版玩家的 head/body 一模一样（都是 (0,0) 和 (16,16)），
// 所以只要换个贴图就是玩家的头／身子。手臂则不同：模型里是两根横着的木杆，贴图按
// 「12×4×4 的横杆」排的，没法直接套玩家的手臂贴图。
//
// 于是这里把每个部件拆成独立的 ModelPart：
//
// bone 自己的 cube 和 head_straw 永远用稻草人自己的贴图（木杆、稻草裙、肩杆、头上的草垛与帽檐）；
// head/body 平时也走稻草人贴图，名字是正版玩家 ID 时改由 #renderSkin 用皮肤贴图单独画；
// 手臂准备了两套：没皮肤用原来的横杆 right_arm_bar/left_arm_bar，
// 有皮肤用和玩家手臂几何、UV 完全一致的 right_arm_skin/left_arm_skin
// ——把玩家的手臂转 90° 放平，形状和原来的横杆几乎重合。
// 帽子 hat、夹克 jacket、袖子 right_sleeve/left_sleeve
// 是皮肤的「盔甲层」（第二层），只在有皮肤时跟着一起画。
public class ScarecrowModel extends HierarchicalModel<Scarecrow> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "scarecrow"), "main");

    private static final CubeDeformation NO_DEFORMATION = CubeDeformation.NONE;

// 皮肤第二层的膨胀量，和原版玩家模型一样是 0.25
    private static final float ARMOR_LAYER = 0.25F;

// 帽子那一圈膨胀得更多，跟原版 HumanoidModel 的 hat 保持一致
    private static final float HAT_LAYER = 0.5F;

    private static final float HALF_PI = (float) (Math.PI / 2.0);

// 原来横杆手臂的固定下垂角度，皮肤版手臂也照这个角度
    private static final float ARM_DROOP = 0.1309F;

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArmBar;
    private final ModelPart leftArmBar;
    private final ModelPart rightArmSkin;
    private final ModelPart leftArmSkin;
    private final ModelPart hat;
    private final ModelPart jacket;
    private final ModelPart rightSleeve;
    private final ModelPart leftSleeve;

    public ScarecrowModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
        this.head = this.bone.getChild("head");
        this.body = this.bone.getChild("body");
        this.rightArmBar = this.bone.getChild("right_arm_bar");
        this.leftArmBar = this.bone.getChild("left_arm_bar");
        this.rightArmSkin = this.bone.getChild("right_arm_skin");
        this.leftArmSkin = this.bone.getChild("left_arm_skin");
        this.hat = this.head.getChild("hat");
        this.jacket = this.body.getChild("jacket");
        this.rightSleeve = this.rightArmSkin.getChild("right_sleeve");
        this.leftSleeve = this.leftArmSkin.getChild("left_sleeve");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

// 稻草人骨架：中间那根立杆 + 底下的稻草裙 + 横向的肩杆
        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(7.0F, -22.0F, 0.0F, 2.0F, 32.0F, 2.0F, NO_DEFORMATION)
                        .texOffs(16, 32).addBox(4.0F, -4.0F, -1.0F, 8.0F, 4.0F, 0.0F, NO_DEFORMATION)
                        .texOffs(32, 32).addBox(4.0F, -4.0F, 3.0F, 8.0F, 4.0F, 0.0F, NO_DEFORMATION)
                        .texOffs(16, 36).addBox(12.0F, -4.0F, -1.0F, 0.0F, 4.0F, 4.0F, NO_DEFORMATION)
                        .texOffs(24, 36).addBox(4.0F, -4.0F, -1.0F, 0.0F, 4.0F, 4.0F, NO_DEFORMATION),
                PartPose.offset(-8.0F, 14.0F, -1.0F));

        bone.addOrReplaceChild("shoulder_bar", CubeListBuilder.create()
                        .texOffs(8, 16).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 32.0F, 2.0F, NO_DEFORMATION),
                PartPose.offsetAndRotation(8.0F, -14.0F, 1.0F, 0.0F, 0.0F, HALF_PI));

// 躯干：UV 与原版玩家的 body 完全一致，所以换贴图就直接变成玩家的身子
        PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 16).addBox(4.0F, -16.0F, -1.0F, 8.0F, 12.0F, 4.0F, NO_DEFORMATION),
                PartPose.ZERO);

// 躯干的盔甲层（夹克）
        body.addOrReplaceChild("jacket", CubeListBuilder.create()
                        .texOffs(16, 32).addBox(4.0F, -16.0F, -1.0F, 8.0F, 12.0F, 4.0F, NO_DEFORMATION.extend(ARMOR_LAYER)),
                PartPose.ZERO);

// 头：麻布脑袋，UV 与原版玩家的 head 完全一致
        PartDefinition head = bone.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, NO_DEFORMATION),
                PartPose.offsetAndRotation(8.0F, -19.0F, 0.0F, 0.0F, 0.0F, 0.0873F));

// 头部的盔甲层（帽子）
        head.addOrReplaceChild("hat", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, NO_DEFORMATION.extend(HAT_LAYER)),
                PartPose.ZERO);

// 头上的稻草：顶上的草垛和一圈扁平的帽檐，跟着头一起转，但永远用稻草人自己的贴图
        bone.addOrReplaceChild("head_straw", CubeListBuilder.create()
                        .texOffs(-14, 50).addBox(-7.0F, -4.01F, -7.0F, 14.0F, 0.0F, 14.0F, NO_DEFORMATION)
                        .texOffs(28, 52).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 4.0F, 8.0F, NO_DEFORMATION),
                PartPose.offsetAndRotation(8.0F, -19.0F, 0.0F, 0.0F, 0.0F, 0.0873F));

// 原来的横杆手臂：没皮肤时用这两根
        bone.addOrReplaceChild("right_arm_bar", CubeListBuilder.create()
                        .texOffs(32, 8).addBox(-6.0F, -4.0F, -2.0F, 12.0F, 4.0F, 4.0F, NO_DEFORMATION),
                PartPose.offsetAndRotation(-1.0F, -12.0F, -1.0F, 0.0F, 0.0F, -ARM_DROOP));

        bone.addOrReplaceChild("left_arm_bar", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-6.0F, -4.0F, -2.0F, 12.0F, 4.0F, 4.0F, NO_DEFORMATION),
                PartPose.offsetAndRotation(17.0F, -12.0F, -1.0F, 0.0F, 0.0F, ARM_DROOP));

// 皮肤版手臂：几何与 UV 完全照抄原版玩家（宽 4、长 12、厚 4），再绕 z 转 90° 放平，
// 位置特意选成和上面的横杆几乎重合，这样换皮肤时手臂不会跳一下。
        PartDefinition rightArmSkin = bone.addOrReplaceChild("right_arm_skin", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, NO_DEFORMATION),
                PartPose.offsetAndRotation(3.0F, -13.0F, -1.0F, 0.0F, 0.0F, HALF_PI - ARM_DROOP));

        rightArmSkin.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                        .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, NO_DEFORMATION.extend(ARMOR_LAYER)),
                PartPose.ZERO);

        PartDefinition leftArmSkin = bone.addOrReplaceChild("left_arm_skin", CubeListBuilder.create()
                        .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, NO_DEFORMATION),
                PartPose.offsetAndRotation(13.0F, -13.0F, -1.0F, 0.0F, 0.0F, -(HALF_PI - ARM_DROOP)));

        leftArmSkin.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                        .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, NO_DEFORMATION.extend(ARMOR_LAYER)),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

// 切换「这个稻草人要不要走玩家皮肤」。
//
// 有皮肤时头、躯干要藏起来，免得本体那一遍又用稻草人贴图画一次；它们会由
// #renderSkin 单独画。手臂同理，横杆换成玩家手臂。
//
// 盔甲层（帽子、夹克、袖子）也要一起开关：它们挂在头／躯干下面，不关的话没皮肤时
// 会被当成普通部件，用稻草人贴图在头上、身上多糊一层，看起来就是纹理全乱了。
    public void setSkinned(boolean skinned) {
        this.head.visible = !skinned;
        this.body.visible = !skinned;
        this.rightArmBar.visible = !skinned;
        this.leftArmBar.visible = !skinned;
        this.rightArmSkin.visible = false;
        this.leftArmSkin.visible = false;
        this.hat.visible = skinned;
        this.jacket.visible = skinned;
        this.rightSleeve.visible = skinned;
        this.leftSleeve.visible = skinned;
    }

// 用稻草人自己的贴图画剩下的部分：木杆、稻草裙、肩杆、头上的草垛，以及没皮肤时的横杆手臂。
    public void renderScarecrowTexture(PoseStack poseStack, VertexConsumer buffer, int packedLight) {
        this.bone.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
    }

// 用玩家皮肤画头、躯干和双臂，连带皮肤自带的盔甲层（帽子、夹克、袖子）。
//
// 这几个部件是 bone 的子节点，直接单独画的话会缺了 bone 自己的位移，
// 所以先把 bone 的变换压进去。
    public void renderSkin(PoseStack poseStack, VertexConsumer buffer, int packedLight) {
        this.head.visible = true;
        this.body.visible = true;
        this.rightArmSkin.visible = true;
        this.leftArmSkin.visible = true;

        poseStack.pushPose();
        this.bone.translateAndRotate(poseStack);
        this.head.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        this.body.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        this.rightArmSkin.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        this.leftArmSkin.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        this.head.visible = false;
        this.body.visible = false;
        this.rightArmSkin.visible = false;
        this.leftArmSkin.visible = false;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

// 静物，没有任何动画
    @Override
    public void setupAnim(Scarecrow entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
    }
}
