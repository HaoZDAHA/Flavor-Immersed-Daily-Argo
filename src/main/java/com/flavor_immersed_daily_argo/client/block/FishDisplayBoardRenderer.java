package com.flavor_immersed_daily_argo.client.block;

import com.flavor_immersed_daily_argo.fishing.FishDisplayBoardBlock;
import com.flavor_immersed_daily_argo.fishing.FishDisplayBoardBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;

// 钓鱼展示架方块实体渲染器：把挂着的那条水产的活体实体模型画在木板上。
//
// 物品只是记账用，真要画出来的是它对应的实体——对应关系取自钓鱼手册数据
// （FishDisplayBoardBlock#entityOf）。实体只创建一次并缓存，逐帧重设朝向，
// 既不 tick 也不产生 AI，所以是静止的。
public class FishDisplayBoardRenderer implements BlockEntityRenderer<FishDisplayBoardBlockEntity> {

// 挂上去的水产统一缩放到的显示高度（格）
    private static final float DISPLAY_SIZE = 0.9F;
// 缩放上下限，防止体型极端的水产撑爆展示架、或小得看不见
    private static final float MIN_SCALE = 0.2F;
    private static final float MAX_SCALE = 3.0F;

// 木板（模型里占 z 14~16）贴脸那一面到方块中心的距离（格）
    private static final double BOARD_FACE = 0.375D;
// 鱼身厚度的一半：鱼类模型里鱼身只有 2px 厚（0.125 格），悬挂时贴到板面上
    private static final double FISH_HALF_THICKNESS = 1.0D / 16.0D;
// 鱼背与板面之间留的缝，免得两面贴死互相打架
    private static final double BOARD_GAP = 0.005D;
// 挂着的那条往木板方向贴过去的距离：让鱼背正好靠上板面，不再悬空
    private static final double BOARD_OFFSET = BOARD_FACE - FISH_HALF_THICKNESS - BOARD_GAP;
// 沿板面再往鱼头方向挪 2px。鱼类模型的鱼头都在局部 -Z，配上抛竿用的朝向角后，
// 世界里的鱼头方向就是 facing.getClockWise()，所以往这个方向推 2px 即可。
    private static final double HEAD_SHIFT = 2.0D / 16.0D;
// 鱼类模型「离水」姿势会把整条鱼抬高 5 个模型单位（体节 y 从 24 挪到 19）。
// 这里把它改成水中水准姿势（见 #setInWaterPose），鱼身不再上抬，得把这段高度补回来，
// 鱼才停在原来那个跟木板居中的位置上。
    private static final double POSTURE_LIFT = 5.0D / 16.0D;

// 每个展示架缓存一份预览实体，避免逐帧重建
    private final Map<FishDisplayBoardBlockEntity, Cached> cache = new WeakHashMap<>();

    private record Cached(Item item, Level level, Entity entity) {
    }

    public FishDisplayBoardRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FishDisplayBoardBlockEntity board, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = board.displayed();
        if (stack.isEmpty()) {
            return;
        }
        Entity entity = entityFor(board, stack.getItem());
        if (entity == null) {
            return;
        }

// 鱼贴着木板挂、侧面对着外面看的人
        Direction facing = board.getBlockState().getValue(FishDisplayBoardBlock.FACING);
        float yaw = facing.toYRot() + 90.0F;
        entity.setYRot(yaw);
        entity.setXRot(0.0F);
        entity.yRotO = yaw;
        entity.xRotO = 0.0F;
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = yaw;
            living.yBodyRotO = yaw;
            living.yHeadRot = yaw;
            living.yHeadRotO = yaw;
        }

// 按体型挑缩放，让各条水产在架子上大小相近；以包围盒居中，整体挂在方块正中偏木板一侧
        double body = Math.max(Math.max(entity.getBbWidth(), entity.getBbHeight()), 0.25D);
        float scale = Mth.clamp((float) (DISPLAY_SIZE / body), MIN_SCALE, MAX_SCALE);
        Direction back = facing.getOpposite();
        Direction head = facing.getClockWise();

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        poseStack.pushPose();
        poseStack.translate(0.5D + back.getStepX() * BOARD_OFFSET + head.getStepX() * HEAD_SHIFT,
                0.5D - body * scale / 2.0D + POSTURE_LIFT,
                0.5D + back.getStepZ() * BOARD_OFFSET + head.getStepZ() * HEAD_SHIFT);
        dispatcher.render(entity, 0.0D, 0.0D, 0.0D, yaw, 1.0F, poseStack, bufferSource, packedLight);
        poseStack.popPose();
        dispatcher.setRenderShadow(true);
    }

// 取（并缓存）该物品对应的预览实体；手册里没实体或客户端世界未就绪时为空
    @Nullable
    private Entity entityFor(FishDisplayBoardBlockEntity board, Item item) {
        Level level = board.getLevel();
        Cached cached = this.cache.get(board);
        if (cached != null && cached.item() == item && cached.level() == level) {
            return cached.entity();
        }
        ResourceLocation entityId = FishDisplayBoardBlock.entityOf(item);
        if (entityId == null || level == null) {
            return null;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).orElse(null);
        if (type == null) {
            return null;
        }
        Entity entity = type.create(level);
        if (entity == null) {
            return null;
        }
        setInWaterPose(entity);
        this.cache.put(board, new Cached(item, level, entity));
        return entity;
    }

// 原版把「是否泡在水里」藏在 Entity#isInWater() 底下的 wasTouchingWater 字段，
// 是 protected 且没有公开 setter。预览实体从不 tick，这个字段一直是 false，鱼类模型于是摆出
// 「离水挣扎」的仰头翘尾姿势（body.xRot = -1.2F），在展板上就成了抬头望天。
// 这里反射把它置为 true，模型改走水中水准姿态（body.xRot = 0），鱼身才是平的；
// 读一次缓存，反射不可用时安全地退回原姿势。做法与 CreativeTabBanners 读原版字段一致。
    private static final Field WAS_TOUCHING_WATER = wasTouchingWaterField();

    @Nullable
    private static Field wasTouchingWaterField() {
        try {
            Field field = Entity.class.getDeclaredField("wasTouchingWater");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static void setInWaterPose(Entity entity) {
        if (WAS_TOUCHING_WATER == null) {
            return;
        }
        try {
            WAS_TOUCHING_WATER.setBoolean(entity, true);
        } catch (IllegalAccessException e) {
// 只是姿势不水平，不影响别的
        }
    }
}
