package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.CartonModel;
import com.flavor_immersed_daily_argo.client.entity.model.UavModel;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.entity.Uav;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

// 多功能无人机的渲染器：把 Blockbench 导出的模型直接画出来。
//
// 位姿换算与 HandcartRenderer 同一套路——模型朝 +z 建（机头、相机吊舱都在 +z 一侧），
// 翻转 x、y 保持三角形绕序，再转 -yaw。
public class UavRenderer extends EntityRenderer<Uav> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "textures/entity/uav.png");

// 模型最低点（机身下缘的支脚）在 y=24 像素。
// 导出坐标里 y 是往下的，翻转之后「越大越靠下」，所以取的是全部 cube 里 y 的最大值 24，
// 照最小值抬会把整架无人机顶到半空中。
    private static final float MODEL_GROUND = 24.0F / 16.0F;

// 使用槽里的东西挂在机身下方的高度（格，从无人机脚底算起）。
//
// 机身与支脚都挤在 y 0 ~ 0.5 之间，物品要挂到机身下面才看得见。GROUND 这个
// 展示姿态自带「抬高 3/16 格、缩到 0.25」的变换，所以这里先用负值把那截抬升抵掉，再压到机腹以下。
    private static final float LOAD_Y = -0.35F;

// 机身的骨骼名：go 动画（机身晃 + 旋翼自转）就是打在它身上的，物品要跟着晃就得读它
    private static final String BODY_BONE = "bone2";

// 模型机头朝 -z，而物品正面朝 +z，差一个 180°
    private static final float LOAD_YAW_OFFSET = 180.0F;

// 纸箱的模型地面也在 y=24 像素（跟无人机、粪便一样是「地面压在 y=24」的建法）
    private static final float CARTON_MODEL_GROUND = 24.0F / 16.0F;

// 箱子半个高度（格）。挂点对准箱子中心，箱子才不会整个长在挂点上方、捅进机腹里
    private static final float CARTON_HALF_HEIGHT = 0.375F;

    private final UavModel model;
    private final CartonModel cartonModel;
    private final ItemRenderer itemRenderer;

    public UavRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new UavModel(context.bakeLayer(UavModel.LAYER_LOCATION));
        this.cartonModel = new CartonModel(context.bakeLayer(CartonModel.LAYER_LOCATION));
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.6F;
    }

    @Override
    public void render(Uav entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -MODEL_GROUND - 0.001F, 0.0F);

        this.model.setupAnim(entity, 0.0F, 0.0F, partialTicks + entity.tickCount, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();

        this.renderUseSlot(entity, entityYaw, poseStack, buffer, packedLight);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

// 使用槽里的东西挂在机身下方，一眼看出这架无人机装了什么。
//
// 机身在空中一直是晃的（go 动画就打在最外层那根 #BODY_BONE 上），所以这里不只是
// 跟着机头转，还把机身这一帧的姿势原样搬过来、绕同一个枢轴摆，物品才跟着机身一起晃。
    private void renderUseSlot(Uav entity, float entityYaw, PoseStack poseStack,
                               MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getInventory().getItem(Uav.USE_SLOT);
        if (stack.isEmpty()) {
            return;
        }
        ModelPart body = this.model.root().getChild(BODY_BONE);

        poseStack.pushPose();
// 跟机身同一个基准朝向
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));

// 绕机身的枢轴摆——机身就是绕这个点晃的，物品跟它同一个动作
        float pivotY = MODEL_GROUND - body.y / 16.0F;
        poseStack.translate(0.0F, pivotY, 0.0F);
// 机身整体带过一次 (-1, -1, 1) 的翻转（等价于绕 z 转 180°），画面上绕 x / y 的旋转会反号、
// 绕 z 的不变；按这个符号摆，物品才和机身是同一个动作而不是对着晃
        if (body.zRot != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotation(body.zRot));
        }
        if (body.yRot != 0.0F) {
            poseStack.mulPose(Axis.YP.rotation(-body.yRot));
        }
        if (body.xRot != 0.0F) {
            poseStack.mulPose(Axis.XP.rotation(-body.xRot));
        }
// 物品自身的朝向修正放最里面（绕它自己的 y 轴转），这样只改「头朝哪边」，
// 不会顺带把上面那些旋转轴也翻过去——放最外面会让物品反着晃
        poseStack.mulPose(Axis.YP.rotationDegrees(LOAD_YAW_OFFSET));
        poseStack.translate(0.0F, LOAD_Y - pivotY, 0.0F);

        if (stack.is(ModItems.CARTON.get())) {
// 纸箱是立体的箱子，画它自己的实体模型，别用那张 16×16 的平面手持贴图
            this.renderCarton(poseStack, buffer, packedLight);
        } else {
            this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight,
                    OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        }
        poseStack.popPose();
    }

// 把纸箱的实体模型吊在挂点上（沿用调用处那套「跟机身一起晃」的位姿）。
//
// 箱子的模型是按「地面压在 y=24 像素」建的，跟实体渲染一样翻一次 x、y 再下移
// #CARTON_MODEL_GROUND，把模型地面对到挂点；挂点再抬半个箱高，箱子才会以挂点为中心
// 挂着，而不是整个长在挂点上方捅进机腹里。
    private void renderCarton(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, -CARTON_HALF_HEIGHT, 0.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -CARTON_MODEL_GROUND - 0.001F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(CartonRenderer.TEXTURE));
        this.cartonModel.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Uav entity) {
        return TEXTURE;
    }
}
