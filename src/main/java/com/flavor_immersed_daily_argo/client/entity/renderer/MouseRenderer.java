package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.MouseModel;
import com.flavor_immersed_daily_argo.entity.Mouse;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

// 老鼠的渲染器：Blockbench 模型 + 啃食时爪子里叼着的物品图标。
//
// 模型交给 MobRenderer 的常规流程（原版会自己翻转 y 并把模型地面对到脚底）。物品图标则另外补一笔：
// 啃食时在嘴巴前方（arm/leg4/body2 之间的空隙）摆一份战利品，
// 朝哪边就跟着实体朝向转。
public class MouseRenderer extends MobRenderer<Mouse, MouseModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "textures/entity/mouse.png");

// 嘴里那份东西的高度（格，从脚底算起）
    private static final float MOUTH_HEIGHT = 0.16F;

// 往正前方探出的距离（格），落在两条前腿与脑袋围出的空当里
    private static final float MOUTH_FORWARD = 0.26F;

// 老鼠个头小，物品图标也缩小一点
    private static final float ITEM_SCALE = 0.5F;

    private final ItemRenderer itemRenderer;

    public MouseRenderer(EntityRendererProvider.Context context) {
        super(context, new MouseModel(context.bakeLayer(MouseModel.LAYER_LOCATION)), 0.2F);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(Mouse entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        ItemStack food = entity.getEatenItem();
        if (!entity.isEating() || food.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, MOUTH_HEIGHT, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        poseStack.translate(0.0F, 0.0F, MOUTH_FORWARD);
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        this.itemRenderer.renderStatic(food, ItemDisplayContext.GROUND, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Mouse entity) {
        return TEXTURE;
    }
}
