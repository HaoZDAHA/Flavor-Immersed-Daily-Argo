package com.flavor_immersed_daily_argo.client.entity.model;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.entity.Turkey;
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

public class TurkeyModel extends AgeableListModel<Turkey> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "turkey"), "main");

    private final ModelPart Turkey;
    private final ModelPart body;
    private final ModelPart leftleg;
    private final ModelPart leftleg2;
    private final ModelPart beast;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart tail;

    public TurkeyModel(ModelPart root) {
        this.Turkey = root.getChild("Turkey");
        this.body = this.Turkey.getChild("body");
        this.leftleg = this.Turkey.getChild("leftleg");
        this.leftleg2 = this.Turkey.getChild("leftleg2");
        this.beast = this.Turkey.getChild("beast");
        this.neck = this.Turkey.getChild("neck");
        this.head = this.Turkey.getChild("head");
        this.tail = this.Turkey.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Turkey = partdefinition.addOrReplaceChild("Turkey", CubeListBuilder.create(), PartPose.offset(0.0F, 26.0F, 0.0F));

        Turkey.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -12.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition leftleg = Turkey.addOrReplaceChild("leftleg", CubeListBuilder.create().texOffs(12, 27).addBox(0.0F, -2.01F, -1.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 0.0F, 0.0F));
        leftleg.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(26, 30).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -2.0F, 1.0F, -0.48F, 0.0F, 0.0F));
        leftleg.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(8, 28).addBox(0.0F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -3.0F, 2.0F, 0.3054F, 0.0F, 0.0F));
        leftleg.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(12, 21).addBox(-1.0F, -2.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -5.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition leftleg2 = Turkey.addOrReplaceChild("leftleg2", CubeListBuilder.create().texOffs(24, 27).addBox(0.0F, -2.01F, -1.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 0.0F, 0.0F));
        leftleg2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(30, 30).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -2.0F, 1.0F, -0.48F, 0.0F, 0.0F));
        leftleg2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(32, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -3.0F, 2.0F, 0.3054F, 0.0F, 0.0F));
        leftleg2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(24, 21).addBox(-1.0F, -2.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        Turkey.addOrReplaceChild("beast", CubeListBuilder.create().texOffs(0, 14).addBox(-3.0F, -13.0F, -6.0F, 6.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

        PartDefinition neck = Turkey.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        neck.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 28).addBox(-0.999F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -5.0F, -0.4363F, 0.0F, 0.0F));

        Turkey.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18, 15).addBox(-1.0F, -18.0F, -6.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(8, 30).addBox(-1.0F, -16.0F, -9.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

        Turkey.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 21).addBox(-2.0F, -13.0F, 4.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 30).addBox(-2.0F, -18.0F, 5.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(7.0F, -14.0F, 5.0F, 2.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(37, 0).addBox(2.0F, -16.0F, 5.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(27, 0).addBox(-7.0F, -16.0F, 5.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    @Override
    public void setupAnim(Turkey entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.yRot = 0; this.body.zScale = 1;
        this.leftleg.xRot = 0; this.leftleg.z = 0;
        this.leftleg2.xRot = 0; this.leftleg2.z = 0;
        this.beast.yRot = 0; this.beast.y = 0; this.beast.yScale = 1;
        this.neck.xRot = 0; this.neck.y = 0; this.neck.z = 0;
        this.head.xRot = 0; this.head.y = 0; this.head.z = 0;
        this.tail.zRot = 0;

        if (limbSwingAmount > 0.01F) {
            applyWalk((limbSwing / (Mth.PI * 2.0F) % 1.0F) * 0.5F, limbSwingAmount);
        } else {
            applyIdle((ageInTicks % 22.5F) / 22.5F);
        }
        if (entity.hurtTime > 7) {
            this.tail.zRot = Mth.sin(ageInTicks * 1.5F) * 0.5F;
        }
    }

    private void applyIdle(float t) {
        float[] ts = {0.0F, 0.5F, 1.0F};
        float[] bsz = {1.0F, 1.1F, 1.0F};
        float[] bty = {0.0F, -0.15F, 0.0F};
        float[] btSY= {1.0F, 1.1F, 1.0F};
        float[] nkx = {0.0F, -2.5F, 0.0F};
        float[] hdz = {0.0F, 1.0F, 0.0F};
        int i = t < 0.5F ? 0 : 1; float f = (t - ts[i]) / (ts[i + 1] - ts[i]);
        this.body.zScale  = lerp(bsz[i], bsz[i + 1], f);
        this.beast.y      = lerp(bty[i], bty[i + 1], f);
        this.beast.yScale = lerp(btSY[i], btSY[i + 1], f);
        this.neck.xRot    = lerp(nkx[i], nkx[i + 1], f) * Mth.DEG_TO_RAD;
        this.head.z       = lerp(hdz[i], hdz[i + 1], f);
    }

    private void applyWalk(float t, float a) {
        float[] times  = {0.0F, 0.125F, 0.25F, 0.375F, 0.5F};
        float[] bodyY  = {0.0F, 5.0F, 0.0F, -5.0F, 0.0F};
        float[] llX    = {0.0F, -17.5F, 0.0F, 12.5F, 0.0F};
        float[] llZ    = {0.0F, -2.0F, 0.0F, 1.0F, 0.0F};
        float[] l2X    = {0.0F, 12.5F, 0.0F, -20.0F, 0.0F};
        float[] l2Z    = {0.0F, 1.0F, 0.0F, -1.0F, 0.0F};
        float[] beastY = {0.0F, -5.0F, 0.0F, 5.0F, 0.0F};
        float[] neckX  = {0.0F, -2.5F, 0.0F, 7.5F, 0.0F};
        float[] neckY  = {0.0F, 0.0F, 0.0F, 1.0F, 0.0F};
        float[] neckZ  = {0.0F, 0.0F, 0.0F, 1.0F, 0.0F};
        float[] headX  = {0.0F, -5.0F, 0.0F, 5.0F, 0.0F};
        float[] headY  = {0.0F, 0.0F, 0.0F, 1.0F, 0.0F};
        float[] headZ  = {0.0F, 0.0F, 0.0F, 1.0F, 0.0F};
        float[] tailZ  = {0.0F, 5.0F, 0.0F, -5.0F, 0.0F};
        int i = 0; while (i < times.length - 1 && t >= times[i + 1]) i++;
        float f = Math.min((t - times[i]) / (times[i + 1] - times[i]), 1.0F);
        this.body.yRot     = lerp(bodyY[i], bodyY[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.leftleg.xRot  = lerp(llX[i], llX[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.leftleg.z     = lerp(llZ[i], llZ[i + 1], f) * a;
        this.leftleg2.xRot = lerp(l2X[i], l2X[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.leftleg2.z    = lerp(l2Z[i], l2Z[i + 1], f) * a;
        this.beast.yRot    = lerp(beastY[i], beastY[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.neck.xRot     = lerp(neckX[i], neckX[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.neck.y        = lerp(neckY[i], neckY[i + 1], f) * a;
        this.neck.z        = lerp(neckZ[i], neckZ[i + 1], f) * a;
        this.head.xRot     = lerp(headX[i], headX[i + 1], f) * a * Mth.DEG_TO_RAD;
        this.head.y        = lerp(headY[i], headY[i + 1], f) * a;
        this.head.z        = lerp(headZ[i], headZ[i + 1], f) * a;
        this.tail.zRot     = lerp(tailZ[i], tailZ[i + 1], f) * a * Mth.DEG_TO_RAD;
    }

    @Override protected Iterable<ModelPart> headParts() { return Collections.emptyList(); }
    @Override protected Iterable<ModelPart> bodyParts() { return List.of(Turkey); }
}
