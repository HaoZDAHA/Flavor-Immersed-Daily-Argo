package com.flavor_immersed_daily_argo.client.entity.renderer;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.fishing.FishingRodModMenu;
import com.flavor_immersed_daily_argo.fishing.FishingRodStats;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

// 投出去的鱼钩实体（浮漂）的渲染：用钓竿浮漂槽位里那枚浮漂的贴图取代原版钓鱼浮漂。
//
// 原版浮漂是一张朝向镜头的方形贴图，这里保留同样的画法（0.5 缩放 + 相机朝向），
// 仅把贴图换成当前浮漂；鱼线也照原版方式绘制，因此抛竿后浮漂与鱼线的观感不变。
//
// 兜底：主人手里不是南山钓竿、或浮漂槽位为空 / 不是本模组浮漂时，退回原版贴图，
// 保证原版钓竿与其它模组的浮漂不受影响。
public class ModFishingHookRenderer extends EntityRenderer<FishingHook> {

// 原版钓鱼浮漂贴图：未装浮漂时的兜底
    private static final ResourceLocation VANILLA_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/fishing_hook.png");
// 鱼线用的折线渲染类型。
//
// 设置照抄原版 RenderType#lineStrip()，只把输出目标换回主缓冲。原版那条会把线画进
// 「entity 半透明目标」（ITEM_ENTITY_TARGET）——「极佳」画质下水是在单独的缓冲里
// 后合成的，线进了另一块缓冲后没有参与主缓冲的深度比较，看上去就像被水浸着：线明明在水面之上，
// 却随视角被水一层层冲淡。改回主缓冲并保留深度写入后，水面会按深度正确遮挡，线在水上方
// 始终是实的。
    public static final RenderType LINE_TYPE = RenderType.create(
            "flavor_immersed_daily_argo:fishing_line",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINE_STRIP,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));
// 第一人称下鱼线起点落在近裁剪面上的换算基准，与原版一致
    private static final double VIEW_BOBBING_SCALE = 960.0D;
// 鱼线曲线细分段数，与原版一致
    private static final int LINE_SEGMENTS = 16;

    public ModFishingHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FishingHook entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        Player player = entity.getPlayerOwner();
        if (player == null) {
            return;
        }
        renderBobber(entity, poseStack, buffer, packedLight);
        renderLine(entity, player, partialTicks, poseStack, buffer);
    }

// 朝向镜头的浮漂贴图；装了本模组浮漂就用它的物品贴图，否则用原版
    private void renderBobber(FishingHook entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(floatTexture(entity)));
        bobberVertex(consumer, pose, packedLight, 0.0F, 0, 0, 1);
        bobberVertex(consumer, pose, packedLight, 1.0F, 0, 1, 1);
        bobberVertex(consumer, pose, packedLight, 1.0F, 1, 1, 0);
        bobberVertex(consumer, pose, packedLight, 0.0F, 1, 0, 0);
        poseStack.popPose();
    }

// 从竿梢到浮漂的鱼线，绘制方式与原版一致
    private void renderLine(FishingHook entity, Player player, float partialTicks, PoseStack poseStack,
                            MultiBufferSource buffer) {
        int armSign = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        if (!player.getMainHandItem().canPerformAction(net.neoforged.neoforge.common.ItemAbilities.FISHING_ROD_CAST)) {
            armSign = -armSign;
        }

        float swing = Mth.sin(Mth.sqrt(player.getAttackAnim(partialTicks)) * (float) Math.PI);
        float bodyYaw = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180F);
        double sinYaw = Mth.sin(bodyYaw);
        double cosYaw = Mth.cos(bodyYaw);
        double armOffset = (double) armSign * 0.35D;

        double startX;
        double startY;
        double startZ;
        float heightOffset;
        boolean firstPerson = (this.entityRenderDispatcher.camera == null
                || this.entityRenderDispatcher.options.getCameraType().isFirstPerson())
                && player == Minecraft.getInstance().player;
        if (firstPerson) {
            double scale = VIEW_BOBBING_SCALE
                    / (double) this.entityRenderDispatcher.options.fov().get().intValue();
            Vec3 nearPlane = this.entityRenderDispatcher.camera.getNearPlane()
                    .getPointOnPlane((float) armSign * 0.525F, -0.1F);
            nearPlane = nearPlane.scale(scale);
            nearPlane = nearPlane.yRot(swing * 0.5F);
            nearPlane = nearPlane.xRot(-swing * 0.7F);
            startX = Mth.lerp((double) partialTicks, player.xo, player.getX()) + nearPlane.x;
            startY = Mth.lerp((double) partialTicks, player.yo, player.getY()) + nearPlane.y;
            startZ = Mth.lerp((double) partialTicks, player.zo, player.getZ()) + nearPlane.z;
            heightOffset = player.getEyeHeight();
        } else {
            startX = Mth.lerp((double) partialTicks, player.xo, player.getX()) - cosYaw * armOffset - sinYaw * 0.8D;
            startY = player.yo + (double) player.getEyeHeight()
                    + (player.getY() - player.yo) * (double) partialTicks - 0.45D;
            startZ = Mth.lerp((double) partialTicks, player.zo, player.getZ()) - sinYaw * armOffset + cosYaw * 0.8D;
            heightOffset = player.isCrouching() ? -0.1875F : 0.0F;
        }

        double hookX = Mth.lerp((double) partialTicks, entity.xo, entity.getX());
        double hookY = Mth.lerp((double) partialTicks, entity.yo, entity.getY()) + 0.25D;
        double hookZ = Mth.lerp((double) partialTicks, entity.zo, entity.getZ());
        float dx = (float) (startX - hookX);
        float dy = (float) (startY - hookY) + heightOffset;
        float dz = (float) (startZ - hookZ);

        VertexConsumer consumer = buffer.getBuffer(LINE_TYPE);
        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i <= LINE_SEGMENTS; i++) {
            lineVertex(dx, dy, dz, consumer, pose, (float) i / (float) LINE_SEGMENTS,
                    (float) (i + 1) / (float) LINE_SEGMENTS);
        }
    }

// 当前浮漂应使用的贴图；没装本模组浮漂时退回原版
    private static ResourceLocation floatTexture(FishingHook entity) {
        Player owner = entity.getPlayerOwner();
        return floatTexture(owner == null ? ItemStack.EMPTY : rodStackOf(owner));
    }

// 竿子上那枚浮漂的贴图；没装本模组浮漂时退回原版。
//
// 竿架按记下的落点补画浮漂，手上已经没有竿子可查，所以直接按架着的那根竿子取。
    public static ResourceLocation floatTexture(ItemStack rod) {
        if (!rod.isEmpty()) {
            List<ItemStack> mods = FishingRodModMenu.getData(rod).mods();
            if (FishingRodStats.SLOT_FLOAT < mods.size()) {
                ItemStack floater = mods.get(FishingRodStats.SLOT_FLOAT);
                for (DeferredHolder<Item, Item> candidate : ModItems.ROD_FLOATS) {
                    if (floater.is(candidate.get())) {
                        ResourceLocation id = BuiltInRegistries.ITEM.getKey(floater.getItem());
                        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                                "textures/item/" + id.getPath() + ".png");
                    }
                }
            }
        }
        return VANILLA_TEXTURE;
    }

// 主人当前手持的南山钓竿；不在手上返回空栈
    private static ItemStack rodStackOf(Player player) {
        if (player.getMainHandItem().is(ModItems.MOD_FISHING_ROD.get())) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().is(ModItems.MOD_FISHING_ROD.get())) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static void bobberVertex(VertexConsumer consumer, PoseStack.Pose pose, int packedLight,
                                     float x, int y, int u, int v) {
        consumer.addVertex(pose.pose(), x - 0.5F, (float) y - 0.5F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv((float) u, (float) v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static void lineVertex(float dx, float dy, float dz, VertexConsumer consumer, PoseStack.Pose pose,
                                   float from, float to) {
        float x = dx * from;
        float y = dy * (from * from + from) * 0.5F + 0.25F;
        float z = dz * from;
        float stepX = dx * to - x;
        float stepY = dy * (to * to + to) * 0.5F + 0.25F - y;
        float stepZ = dz * to - z;
        float length = Mth.sqrt(stepX * stepX + stepY * stepY + stepZ * stepZ);
        stepX /= length;
        stepY /= length;
        stepZ /= length;
        consumer.addVertex(pose.pose(), x, y, z)
                .setColor(0, 0, 0, 255)
                .setNormal(pose, stepX, stepY, stepZ);
    }

    @Override
    public ResourceLocation getTextureLocation(FishingHook entity) {
        return floatTexture(entity);
    }
}
