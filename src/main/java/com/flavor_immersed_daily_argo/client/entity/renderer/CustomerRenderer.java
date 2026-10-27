package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.client.entity.model.CustomerModel;
import com.flavor_immersed_daily_argo.entity.Customer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

// 顾客渲染器：用玩家模型 + 原版默认皮肤把这位堂食客人画出来。
//
// 坐下时把整具模型向下沉一点，让髋部正好落到座垫顶面上（腿此时已在模型里抬平）。
public class CustomerRenderer extends LivingEntityRenderer<Customer, CustomerModel> {

// 坐下时把模型整体下沉这么多格，让髋部正好落在座垫顶面上（数值按观感微调）。
//
// 取正值：LivingEntityRenderer.render 会先 scale(-1, -1, 1) 翻转 y 再调用
// #scale，此时局部 +y 指向世界下方，所以 translate(0, +0.75, 0) 表现为「向下沉」。
// 若实际方向相反，改成 -0.75F 即可。
    private static final float SEAT_SINK = 0.75F;

    public CustomerRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomerModel(context.bakeLayer(CustomerModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    protected void scale(Customer entity, PoseStack poseStack, float partialTicks) {
        if (entity.isSeated()) {
            poseStack.translate(0.0F, SEAT_SINK, 0.0F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Customer entity) {
// 原版默认皮肤有好几套，按 UUID 挑一套，让店里的顾客长得不一样
        return DefaultPlayerSkin.get(entity.getUUID()).texture();
    }
}
