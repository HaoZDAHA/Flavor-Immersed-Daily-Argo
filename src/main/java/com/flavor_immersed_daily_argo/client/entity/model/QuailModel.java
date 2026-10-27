package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.entity.Quail;
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

import java.util.Collections;
import java.util.List;

public class QuailModel extends AgeableListModel<Quail> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "quail"), "main");

    private final ModelPart goose;
    private final ModelPart body;
    private final ModelPart beast;
    private final ModelPart head;
    private final ModelPart mouse;
    private final ModelPart leftwing;
    private final ModelPart rightwing;
    private final ModelPart leftleg;
    private final ModelPart rightleg;

    public QuailModel(ModelPart root) {
        this.goose = root.getChild("goose");
        this.body = this.goose.getChild("body");
        this.beast = this.body.getChild("beast");
        this.head = this.goose.getChild("head");
        this.mouse = this.head.getChild("mouse");
        this.leftwing = this.goose.getChild("leftwing");
        this.rightwing = this.goose.getChild("rightwing");
        this.leftleg = this.goose.getChild("leftleg");
        this.rightleg = this.goose.getChild("rightleg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition goose = partdefinition.addOrReplaceChild("goose", CubeListBuilder.create(), PartPose.offset(0.0F, 21.0F, 0.0F));

        PartDefinition body = goose.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, -1.0F));
        body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(34, 5).addBox(-2.0F, -4.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 9.0F, 0.0436F, 0.0F, 0.0F));
        body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -1.0F, 8.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));
        PartDefinition beast = body.addOrReplaceChild("beast", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, -1.0F));
        beast.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(1, 28).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

        PartDefinition head = goose.addOrReplaceChild("head", CubeListBuilder.create().texOffs(19, 30).addBox(-2.0F, -9.0F, -3.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -1.0F));
        head.addOrReplaceChild("mouse", CubeListBuilder.create().texOffs(34, 0).addBox(-1.0F, -8.0F, -6.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition leftwing = goose.addOrReplaceChild("leftwing", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, 0.0F));
        leftwing.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(1, 17).addBox(-1.0F, -2.0F, -1.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 2.0F, 1.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition rightwing = goose.addOrReplaceChild("rightwing", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, 0.0F));
        rightwing.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(17, 17).addBox(0.0F, -2.0F, -1.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 1.0F, 1.0F, -0.1745F, 0.0F, 0.0F));

        goose.addOrReplaceChild("leftleg", CubeListBuilder.create().texOffs(34, 28).addBox(2.0F, 4.0F, 10.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(32, 25).addBox(1.0F, 8.01F, 8.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, -8.0F));

        goose.addOrReplaceChild("rightleg", CubeListBuilder.create().texOffs(34, 33).addBox(2.0F, -2.0F, -1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(34, 11).addBox(1.0F, 2.01F, -3.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 1.0F, 3.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    @Override
    public void setupAnim(Quail entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.yRot = 0; this.body.zScale = 1;
        this.beast.zRot = 0; this.beast.z = 0; this.beast.yScale = 1; this.beast.zScale = 1;
        this.head.zRot = 0; this.head.z = 0;
        this.leftwing.xRot = 0; this.rightwing.xRot = 0;
        this.leftleg.xRot = 0; this.rightleg.xRot = 0;

        if (limbSwingAmount > 0.01F) {
            applyWalk((limbSwing / (Mth.PI * 2.0F) % 1.0F) * 0.5F, limbSwingAmount);
        } else {
            applyIdle((ageInTicks % 20.0F) / 20.0F);
        }
        if (entity.hurtTime > 7) {
            this.leftwing.xRot = Mth.sin(ageInTicks * 1.5F) * 0.5F;
            this.rightwing.xRot = Mth.sin(ageInTicks * 1.5F) * 0.5F;
        }
    }

    private void applyIdle(float t) {
        float[] ts = {0.0F, 0.5F, 1.0F};
        float[] bsz  = {1.0F, 1.1F, 1.0F};
        float[] btz  = {0.0F, 1.0F, 0.0F};
        float[] btSY = {1.0F, 1.1F, 1.0F};
        float[] btSZ = {1.0F, 1.1F, 1.0F};
        float[] hdz  = {0.0F, 1.0F, 0.0F};
        float[] lwX  = {0.0F, -10.0F, 0.0F};
        float[] rwX  = {0.0F, -10.0F, 0.0F};
        int i = t < 0.5F ? 0 : 1; float f = (t - ts[i]) / (ts[i + 1] - ts[i]);
        this.body.zScale    = lerp(bsz[i], bsz[i + 1], f);
        this.beast.z        = lerp(btz[i], btz[i + 1], f);
        this.beast.yScale   = lerp(btSY[i], btSY[i + 1], f);
        this.beast.zScale   = lerp(btSZ[i], btSZ[i + 1], f);
        this.head.z         = lerp(hdz[i], hdz[i + 1], f);
        this.leftwing.xRot  = lerp(lwX[i], lwX[i + 1], f) * Mth.DEG_TO_RAD;
        this.rightwing.xRot = lerp(rwX[i], rwX[i + 1], f) * Mth.DEG_TO_RAD;
    }

    private void applyWalk(float t, float a) {
        float[] times  = {0.0F, 0.125F, 0.25F, 0.375F, 0.5F};
        float[] bodyY  = {0.0F, 7.5F, 0.0F, -5.0F, 0.0F};
        float[] beastZ = {0.0F, -10.0F, 0.0F, 7.5F, 0.0F};
        float[] headZ  = {0.0F, -5.0F, 0.0F, 5.0F, 0.0F};
        float[] lwX    = {0.0F, 10.0F, 0.0F, -17.5F, 0.0F};
        float[] rwX    = {0.0F, -12.5F, 0.0F, 12.5F, 0.0F};
        float[] llX    = {0.0F, 37.5F, 0.0F, -40.0F, 0.0F};
        float[] rlX    = {0.0F, -47.5F, 0.0F, 45.0F, 0.0F};
        int i = 0; while (i < times.length - 1 && t >= times[i + 1]) i++;
        float f = Math.min((t - times[i]) / (times[i + 1] - times[i]), 1.0F);
        this.body.yRot      = lerp(bodyY[i], bodyY[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.beast.zRot     = lerp(beastZ[i], beastZ[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.head.zRot      = lerp(headZ[i], headZ[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.leftwing.xRot  = lerp(lwX[i], lwX[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.rightwing.xRot = lerp(rwX[i], rwX[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.leftleg.xRot   = lerp(llX[i], llX[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.rightleg.xRot  = lerp(rlX[i], rlX[i + 1], f) * a * Mth.DEG_TO_RAD;
    }

    @Override protected Iterable<ModelPart> headParts() { return Collections.emptyList(); }
    @Override protected Iterable<ModelPart> bodyParts() { return List.of(goose); }
}