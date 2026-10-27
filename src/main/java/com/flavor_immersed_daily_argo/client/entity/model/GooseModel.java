package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.entity.Goose;
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

public class GooseModel extends AgeableListModel<Goose> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "goose"), "main");

    private final ModelPart right_leg;
    private final ModelPart left_leg;
    private final ModelPart right_wing;
    private final ModelPart left_wing;
    private final ModelPart head;
    private final ModelPart mouse;
    private final ModelPart body;
    private final ModelPart beast;
    private final ModelPart neck;

    public GooseModel(ModelPart root) {
        this.right_leg = root.getChild("right_leg");
        this.left_leg = root.getChild("left_leg");
        this.right_wing = root.getChild("right_wing");
        this.left_wing = root.getChild("left_wing");
        this.head = root.getChild("head");
        this.mouse = this.head.getChild("mouse");
        this.body = root.getChild("body");
        this.beast = this.body.getChild("beast");
        this.neck = root.getChild("neck");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(34, 33).addBox(2.0F, -2.0F, 2.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(34, 11).addBox(1.0F, 2.01F, 0.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 22.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(34, 28).addBox(2.0F, 7.0F, 2.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(32, 25).addBox(1.0F, 11.01F, 0.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.0F, 0.0F));

        PartDefinition right_wing = partdefinition.addOrReplaceChild("right_wing", CubeListBuilder.create(), PartPose.offset(-4.0F, 16.0F, 0.0F));
        right_wing.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(16, 16).addBox(0.0F, -3.0F, -1.0F, 1.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 1.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition left_wing = partdefinition.addOrReplaceChild("left_wing", CubeListBuilder.create(), PartPose.offset(4.0F, 16.0F, 0.0F));
        left_wing.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -3.0F, -1.0F, 1.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18, 28).addBox(-2.0F, -15.0F, -3.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, -1.0F));
        head.addOrReplaceChild("mouse", CubeListBuilder.create().texOffs(37, 0).addBox(-1.0F, -13.0F, -5.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(34, 0).addBox(-2.0F, -14.0F, -6.0F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 20.0F, -1.0F));
        body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(34, 5).addBox(-2.0F, -4.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 9.0F, -0.1745F, 0.0F, 0.0F));
        body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -1.0F, 8.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));
        PartDefinition beast = body.addOrReplaceChild("beast", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, -1.0F));
        beast.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 28).addBox(-3.0F, -5.0F, -2.0F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

        PartDefinition neck = partdefinition.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 13.0F, -2.0F, 0.0436F, 0.0F, 0.0F));
        neck.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(5, 25).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0787F, -0.3945F, -0.0873F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    @Override
    public void setupAnim(Goose entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.swingTime > 0) {
            applyAttack(entity.swingTime / 6.0F * 0.25F);
        } else if (limbSwingAmount > 0.01F) {
            applyWalk((limbSwing / (Mth.PI * 2.0F) % 1.0F) * 0.5F, limbSwingAmount);
        } else {
            applyIdle((ageInTicks % 20.0F) / 20.0F);
        }
        if (entity.hurtTime > 7) {
            this.right_wing.zRot = Mth.sin(ageInTicks * 1.5F) * 0.5F;
            this.left_wing.zRot = -Mth.sin(ageInTicks * 1.5F) * 0.5F;
        }
    }

    private void applyIdle(float t) {
        float[] ts = {0.0F, 0.5F, 1.0F};
        float[] bsz = {1.0F, 1.1F, 1.0F};
        float[] btz = {0.0F, 1.0F, 0.0F};
        float[] bty = {1.0F, 1.1F, 1.0F};
        float[] bts = {1.0F, 1.1F, 1.0F};
        float[] hdz = {0.0F, 1.0F, 0.0F};
        float[] nkx = {0.0F, -5.0F, 0.0F};
        int i = t < 0.5F ? 0 : 1; float f = (t - ts[i]) / (ts[i + 1] - ts[i]);
        this.body.zScale  = lerp(bsz[i], bsz[i + 1], f);
        this.beast.z      = lerp(btz[i], btz[i + 1], f);
        this.beast.yScale = lerp(bty[i], bty[i + 1], f);
        this.beast.zScale = lerp(bts[i], bts[i + 1], f);
        this.head.z       = lerp(hdz[i], hdz[i + 1], f);
        this.neck.xRot    = lerp(nkx[i], nkx[i + 1], f) * Mth.DEG_TO_RAD;
    }

    private void applyWalk(float t, float a) {
        float[] times  = {0.0F, 0.125F, 0.25F, 0.375F, 0.5F};
        float[] bodyY  = {0.0F, 7.5F, 0.0F, -5.0F, 0.0F};
        float[] beastZ = {0.0F, -10.0F, 0.0F, 7.5F, 0.0F};
        float[] headZ  = {0.0F, -5.0F, 0.0F, 5.0F, 0.0F};
        float[] neckZ  = {0.0F, -5.0F, 0.0F, 5.0F, 0.0F};
        float[] lwXR   = {0.0F, 10.0F, 0.0F, -17.5F, 0.0F};
        float[] rwXR   = {0.0F, -12.5F, 0.0F, 12.5F, 0.0F};
        float[] llXR   = {0.0F, 37.5F, 0.0F, -40.0F, 0.0F};
        float[] rlXR   = {0.0F, -47.5F, 0.0F, 45.0F, 0.0F};
        int i = 0; while (i < times.length - 1 && t >= times[i + 1]) i++;
        float f = Math.min((t - times[i]) / (times[i + 1] - times[i]), 1.0F);
        this.body.yRot       = lerp(bodyY[i], bodyY[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.beast.zRot      = lerp(beastZ[i], beastZ[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.head.zRot       = lerp(headZ[i], headZ[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.neck.zRot       = lerp(neckZ[i], neckZ[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.left_wing.xRot  = lerp(lwXR[i], lwXR[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.right_wing.xRot = lerp(rwXR[i], rwXR[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.left_leg.xRot   = lerp(llXR[i], llXR[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.right_leg.xRot  = lerp(rlXR[i], rlXR[i + 1], f) * a * Mth.DEG_TO_RAD;
    }

    private void applyAttack(float t) {
        float[] times  = {0.0F, 0.0417F, 0.25F};
        float[] bodyX  = {0.0F, 5.0F, 0.0F};
        float[] bodyZ  = {0.0F, 2.0F, 0.0F};
        float[] beastX = {0.0F, 12.5F, 0.0F};
        float[] beastZ = {0.0F, 1.0F, 0.0F};
        float[] beastSZ={1.0F, 1.1F, 1.0F};
        float[] headY  = {0.0F, -1.0F, 0.0F};
        float[] headZ  = {0.0F, -7.0F, 0.0F};
        float[] lwYR   = {0.0F, 60.0F, 0.0F};
        float[] rwYR   = {0.0F, -62.5F, 0.0F};
        float[] neckX  = {0.0F, 50.0F, 0.0F};
        float[] neckY  = {0.0F, -1.0F, 0.0F};
        float[] neckZ  = {0.0F, 1.0F, 0.0F};
        float[] neckSY = {1.0F, 1.7F, 1.0F};
        float[] llX    = {0.0F, -15.0F, 0.0F};
        float[] rlX    = {0.0F, -17.5F, 0.0F};
        int i = t <= times[1] ? 0 : 1;
        float f = (t - times[i]) / (times[i < 2 ? i + 1 : 2] - times[i]);
        this.body.xRot       = lerp(bodyX[i], bodyX[i + 1], f) * Mth.DEG_TO_RAD;
        this.body.z          = lerp(bodyZ[i], bodyZ[i + 1], f);
        this.beast.xRot      = lerp(beastX[i], beastX[i + 1], f) * Mth.DEG_TO_RAD;
        this.beast.z         = lerp(beastZ[i], beastZ[i + 1], f);
        this.beast.zScale    = lerp(beastSZ[i], beastSZ[i + 1], f);
        this.head.y          = lerp(headY[i], headY[i + 1], f);
        this.head.z          = lerp(headZ[i], headZ[i + 1], f);
        this.left_wing.yRot  = lerp(lwYR[i], lwYR[i + 1], f) * Mth.DEG_TO_RAD;
        this.right_wing.yRot = lerp(rwYR[i], rwYR[i + 1], f) * Mth.DEG_TO_RAD;
        this.neck.xRot       = lerp(neckX[i], neckX[i + 1], f) * Mth.DEG_TO_RAD;
        this.neck.y          = lerp(neckY[i], neckY[i + 1], f);
        this.neck.z          = lerp(neckZ[i], neckZ[i + 1], f);
        this.neck.yScale     = lerp(neckSY[i], neckSY[i + 1], f);
        this.left_leg.xRot   = lerp(llX[i], llX[i + 1], f) * Mth.DEG_TO_RAD;
        this.right_leg.xRot  = lerp(rlX[i], rlX[i + 1], f) * Mth.DEG_TO_RAD;
    }

    @Override protected Iterable<ModelPart> headParts() { return List.of(head); }
    @Override protected Iterable<ModelPart> bodyParts() { return List.of(body, neck, right_leg, left_leg, right_wing, left_wing); }
}