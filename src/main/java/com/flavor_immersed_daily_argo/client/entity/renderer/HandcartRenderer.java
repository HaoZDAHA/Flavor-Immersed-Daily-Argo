package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.HandcartModel;
import com.flavor_immersed_daily_argo.entity.Handcart;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// 拉力木车的渲染器：把 Blockbench 导出的模型直接画出来。
//
// 位姿换算与 ScarecrowRenderer 同一套路——模型是朝 +z 建的，翻转 x、y 保持三角形绕序，
// 再转 -yaw（原版的 180 - yaw 是给朝 -z 的模型写的，照抄会让车屁股对着玩家）。
// 平移写在最后，它在位姿栈里最先作用到模型上，所以量和旋转都在模型自己的坐标系里。
//
// 模型沿 z 从 -6 像素（车把握把）伸到 70 像素（车斗尾板）。实体原点特意落在 z=11.5 像素处，
// 好让车把握把正好待在原点「正前方一格」——跟随的时候就是把这一格按在玩家身上（见 Handcart），
// 于是整辆车要沿自身 z 挪回来 #MODEL_PIVOT_Z。
public class HandcartRenderer extends EntityRenderer<Handcart> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "textures/entity/handcart.png");

// 模型最低点（轮子最底下那一圈）在 y=27 像素。
// 注意导出坐标里 y 是往下的，翻转之后「越大越靠下」，所以取的是全部 cube 里 y 的最大值 27，
// 而不是最小值 3——照最小值抬会把整辆车埋进地里 1.3 格。
    private static final float MODEL_GROUND = 27.0F / 16.0F;

// 实体原点落在模型 z=11.5 像素处。
//
// 车把的握把横杆在模型 z=-6~-3 像素（中点 -4.5），原点取 11.5 后，它到原点的距离正好是
// 16 像素 = 1 格，且落在原点正前方那一格里——跟随的落点就按这「一格」来算。
// 模型 +z 是车斗、-z 是车把，翻转后这一格朝着车的行进方向，也就朝着拉车的玩家。
    private static final float MODEL_PIVOT_Z = 11.5F / 16.0F;

    private final HandcartModel model;

    public HandcartRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new HandcartModel(context.bakeLayer(HandcartModel.LAYER_LOCATION));
        this.shadowRadius = 1.0F;
    }

    @Override
    public void render(Handcart entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
// 把歪斜的旋转中心挪到车轮轴那根线上：轮子是着地点，俯仰时该它们钉在地上，而不是绕着车把翘
        poseStack.translate(0.0F, 0.0F, (float) Handcart.AXLE_BACK);
        applyGroundTilt(entity, partialTicks, poseStack);
        poseStack.translate(0.0F, 0.0F, (float) -Handcart.AXLE_BACK);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -MODEL_GROUND - 0.001F, -MODEL_PIVOT_Z);

        this.model.setupAnim(entity, 0.0F, 0.0F, partialTicks + entity.tickCount, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Handcart entity) {
        return TEXTURE;
    }

// 让车身贴着地面歪：左边高一个方块、右边是平地时车就左高右低；爬坡时前高后低、下坡反过来。
//
// 角度不在这里采地形——那样每帧照当前的方块表面直接算，沿台阶走时会一帧一帧地闪。
// 实体每 tick 采一次(Handcart#getTiltRoll/Handcart#getTiltPitch)并做低通，
// 这里只把两 tick 之间插值好的结果转上去。
//
// 旋转加在 yaw 之后、模型翻转之前：那一层正好是「车尾朝 +z、左右朝 ±x」的车身坐标系，
// 绕 z 转是左右侧倾，绕 x 转是前后俯仰。
    private static void applyGroundTilt(Handcart entity, float partialTicks, PoseStack poseStack) {
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getTiltRoll(partialTicks)));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getTiltPitch(partialTicks)));
    }
}
