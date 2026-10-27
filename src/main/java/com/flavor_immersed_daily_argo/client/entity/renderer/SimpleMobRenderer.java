package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import java.util.function.Function;

// 通用的单体生物渲染器。
//
// 本模组里绝大多数水产、家禽、小动物都满足同一种形态：
//
// 实体类继承 Mob；
// 模型是 Blockbench 导出的固定 EntityModel，只靠实体自身的动画（animate）驱动；
// 整只生物只用一张固定贴图，没有幼体贴图、没有染色、没有装备层。
//
// 这类渲染器原本每个实体一个类，内容除了泛型参数和贴图名以外完全一致。
// 现在统一收敛到这一个泛型实现，注册时直接内联构造，减少几十个样板类。
//
// T：实体类型
// M：对应的模型类型
public class SimpleMobRenderer<T extends Mob, M extends EntityModel<T>> extends MobRenderer<T, M> {

    private final ResourceLocation texture;

    public SimpleMobRenderer(EntityRendererProvider.Context context, M model, ResourceLocation texture, float shadowRadius) {
        super(context, model, shadowRadius);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

// 生成 <modid>:textures/entity/<name>.png 形式的实体贴图路径。
    public static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "textures/entity/" + name + ".png");
    }

// 生成一个渲染器注册用的 provider：注册处只需要写「层定义 + 模型构造器 + 贴图名 + 阴影大小」。
//
// layer：模型层定义（同时用于在 RegisterLayerDefinitions 里提前烘焙）
// factory：模型构造器，通常是 XxxModel::new
// textureName：贴图文件名（不含路径与后缀）
// shadow：阴影半径
    public static <T extends Mob, M extends EntityModel<T>> EntityRendererProvider<T> provider(
            ModelLayerLocation layer, Function<ModelPart, M> factory, String textureName, float shadow) {
        return context -> new SimpleMobRenderer<>(context, factory.apply(context.bakeLayer(layer)), texture(textureName), shadow);
    }
}
