package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.entity.Customer;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

// 顾客模型：直接复用原版玩家几何（64×64 贴图），因此看起来就是一位普通玩家坐在店里。
//
// 平时和玩家一样站着（走路摆臂摆腿由 PlayerModel#setupAnim 处理）；落座后双腿连同
// 裤腿层一起绕髋关节向前抬起放平，手臂也略微前收，摆出坐在凳子上的姿势。
public class CustomerModel extends PlayerModel<Customer> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "customer"), "main");

// 坐下时腿向前抬起的角度（照抄原版骑乘/坐姿）
    private static final float SEATED_LEG_XROT = -1.4137167F;

    public CustomerModel(ModelPart root) {
        super(root, false);
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64);
    }

    @Override
    public void setupAnim(Customer entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entity.isSeated()) {
// 坐下：双腿（含裤腿层）绕髋关节抬起放平，手臂也略前收，像坐在凳子上
            this.rightLeg.xRot = SEATED_LEG_XROT;
            this.rightLeg.yRot = 0.0F;
            this.rightLeg.zRot = 0.0F;
            this.leftLeg.xRot = SEATED_LEG_XROT;
            this.leftLeg.yRot = 0.0F;
            this.leftLeg.zRot = 0.0F;
            this.rightPants.xRot = SEATED_LEG_XROT;
            this.rightPants.yRot = 0.0F;
            this.rightPants.zRot = 0.0F;
            this.leftPants.xRot = SEATED_LEG_XROT;
            this.leftPants.yRot = 0.0F;
            this.leftPants.zRot = 0.0F;
            this.rightArm.xRot = -0.6F;
            this.leftArm.xRot = -0.6F;
            this.rightSleeve.xRot = -0.6F;
            this.leftSleeve.xRot = -0.6F;
        }
    }
}
