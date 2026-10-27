package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
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
import net.minecraft.world.entity.Entity;

// 外卖袋模型。
//
// 几何与 UV 取自 Blockbench 导出的 takeaway_bag.java：bone 节点偏移到
// y=21，底下 12×3×8 的盒子落在 y=24（模型地面），所以袋子正好站在脚底，
// 尺寸约 0.75 格见方、约 0.9 格高。64×64 贴图。
public class TakeawayBagModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(flavor_immersed_daily_argo.MODID, "takeaway_bag"), "main");

    private final ModelPart root;

    public TakeawayBagModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(40, 0).addBox(0.0F, -18.0F, 0.0F, 10.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-0.998F, 0.0F, -4.0F, 12.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 11).addBox(8.98F, -8.7F, -3.1F, 2.0F, 9.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 26).addBox(-0.98F, -8.7F, -3.1F, 2.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 21.0F, 0.0F));

        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(0, 27).addBox(-5.999F, -9.0F, -2.0F, 12.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0F, -2.0F, 4.0F, 0.3054F, 0.0F, 0.0F));

        bone.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                        .texOffs(0, 11).addBox(-6.0F, -9.0F, -2.0F, 12.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0F, -2.0F, -3.0F, -0.2618F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
    }
}
