package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.ScarecrowModel;
import com.flavor_immersed_daily_argo.client.entity.ScarecrowSkins;
import com.flavor_immersed_daily_argo.entity.Scarecrow;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

// 稻草人的渲染器：把 Blockbench 导出的模型直接画出来。
//
// 位姿换算与 PoopRenderer 完全一致——模型地面在 y=24（木杆底端），
// 这里翻转 x、y 再下移 1.501 格，把「模型地面」对到实体脚底；翻 x、y 同时也能保持三角形绕序不变，
// 避免面被背面剔除掉。
//
// 朝向要自己转：EntityRenderDispatcher 只把位姿平移到实体位置，yaw 是
// LivingEntityRenderer#setupRotations 里加的，本渲染器走的是 EntityRenderer 这条路，
// 不自己加就会永远朝同一个方向。旋转顺序照抄原版：先转 yaw，再翻转缩放，最后下移。
//
// 角度用 -yaw 而不是原版的 180 - yaw：原版那套是给「模型朝 -z」的模型写的，
// 这个 Blockbench 模型是朝 +z 建的，直接照抄会让稻草人的后脑勺对着玩家。
//
// 名字如果是某个正版账号的 ID，就把脑袋、躯干和双臂换成那个玩家的皮肤（连同皮肤的盔甲层），
// 其余的木杆、稻草照旧——见 ScarecrowSkins。
public class ScarecrowRenderer extends EntityRenderer<Scarecrow> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "textures/entity/scarecrow.png");

    private final ScarecrowModel model;

    public ScarecrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ScarecrowModel(context.bakeLayer(ScarecrowModel.LAYER_LOCATION));
    }

    @Override
    public void render(Scarecrow entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        PlayerSkin skin = ScarecrowSkins.get(entity.getCustomName());
        this.model.setSkinned(skin != null);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

// 稻草人自己的部分：木杆、稻草裙、肩杆、头上的草垛，以及没皮肤时的横杆手臂
        VertexConsumer scarecrowBuffer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderScarecrowTexture(poseStack, scarecrowBuffer, packedLight);

// 名字是正版玩家 ID：脑袋、躯干、双臂换成那个玩家的皮肤，连带盔甲层
        if (skin != null) {
            VertexConsumer skinBuffer = buffer.getBuffer(RenderType.entityTranslucent(skin.texture()));
            this.model.renderSkin(poseStack, skinBuffer, packedLight);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Scarecrow entity) {
        PlayerSkin skin = ScarecrowSkins.get(entity.getCustomName());
        return skin != null ? skin.texture() : TEXTURE;
    }
}
