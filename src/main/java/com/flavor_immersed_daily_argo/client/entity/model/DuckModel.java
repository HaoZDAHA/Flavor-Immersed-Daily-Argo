package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.entity.Duck;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

public class DuckModel extends AgeableListModel<Duck> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(flavor_immersed_daily_argo.MODID, "duck"), "main");

    private final ModelPart right_leg;
    private final ModelPart left_leg;
    private final ModelPart right_wing;
    private final ModelPart left_wing;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart bill;

    public DuckModel(ModelPart root) {
        this.right_leg = root.getChild("right_leg");
        this.left_leg = root.getChild("left_leg");
        this.right_wing = root.getChild("right_wing");
        this.left_wing = root.getChild("left_wing");
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.bill = this.head.getChild("bill");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(34, 33).addBox(2.0F, -2.0F, 2.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(34, 11).addBox(1.0F, 2.01F, 0.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 22.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(34, 28).addBox(2.0F, 7.0F, 2.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(32, 25).addBox(1.0F, 11.01F, 0.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 16.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, -1.0F, -1.0F, 1.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 16.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18, 28).addBox(-2.0F, -4.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, -1.0F));

        head.addOrReplaceChild("bill", CubeListBuilder.create().texOffs(34, 0).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -4.0F));

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 28).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(33, 17).addBox(-1.0F, -4.9213F, -1.3945F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(34, 5).addBox(-2.0F, -4.0F, 8.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 18.0F, -2.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Duck entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.right_leg.xRot = Mth.cos(limbSwing * 0.6662F) * 0.6F * limbSwingAmount;
        this.left_leg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 0.6F * limbSwingAmount;
        if (entity.hurtTime > 7) {
            this.right_wing.zRot = Mth.sin(ageInTicks * 1.5F) * 0.5F;
            this.left_wing.zRot = -Mth.sin(ageInTicks * 1.5F) * 0.5F;
        } else {
            this.right_wing.zRot = 0.0F;
            this.left_wing.zRot = 0.0F;
        }
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return List.of(head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return List.of(body, right_leg, left_leg, right_wing, left_wing);
    }
}
