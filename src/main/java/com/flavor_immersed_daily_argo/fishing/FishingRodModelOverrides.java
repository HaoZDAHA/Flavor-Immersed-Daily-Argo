package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.registries.DeferredHolder;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 南山钓竿的配件贴图叠加。
//
// 竿体是底座：fishing_rod.json 用 body 谓词选中 fishing_rod_body_<竿体>，
// 把原版钓竿贴图换成竿体贴图。
//
// 线轮 / 鱼线 / 浮漂 / 铅坠则都只是与钓竿同尺寸的整物品覆盖图，几何完全一致，彼此之间也没有
// 「谁必须占哪个位置」的区别，因此不给每种配件组合各生成一份模型（那会是
// 竿体 × 线轮 × 鱼线 × 浮漂 × 铅坠 的组合数，六千多个），而是每个配件只生成一个独立模型，
// 渲染时按槽位实际装的配件依次叠在竿体模型之上。
//
// 叠加用的是「渲染 pass」：ItemOverrides#resolve 里先照常解析出竿体模型，再用
// RodPassModel 把配件叠加层挂在它后面，由 BakedModel#getRenderPasses 交回给
// 原版物品渲染器逐层绘制。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FishingRodModelOverrides {

// 已烘焙的配件叠加层模型，顺序即叠加上去的顺序；某一组缺模型时为空表，该组就整体不渲染
    private static List<BakedModel> reelOverlays = List.of();
    private static List<BakedModel> lineOverlays = List.of();
    private static List<BakedModel> floatOverlays = List.of();
    private static List<BakedModel> sinkerOverlays = List.of();

    private FishingRodModelOverrides() {
    }

// 钓竿模型的原始烘焙结果（未套配件层），用来自行解析 body 谓词
    private static BakedModel rawRodModel = null;

// 竿架上的竿子用的模型：玩家组装的竿体，只叠线轮。
//
// 和手上抛竿不同，这里刻意不走 cast 谓词：抛竿贴图原版只有一张、不分竿体，
// 用了就只剩原版木色，看不到玩家装的竿体。方块实体渲染又没有实体，这里直接按竿体谓词解析。
//
// 鱼线 / 浮漂 / 铅坠都不叠：竿架上那根竿子是「架着等鱼」的状态，鱼线由渲染器自己从竿梢
// 连到落点，浮漂也只是落点上的一张贴图，再叠上贴图里的鱼线、浮漂、铅坠就重复了。
    public static BakedModel podModel(ItemStack stack) {
        BakedModel body;
        if (rawRodModel == null) {
            body = Minecraft.getInstance().getItemRenderer()
                    .getModel(stack, Minecraft.getInstance().level, null, 0);
        } else {
            body = rawRodModel.getOverrides().resolve(rawRodModel, stack, Minecraft.getInstance().level, null, 0);
            if (body == null) {
                body = rawRodModel;
            }
        }
        List<BakedModel> overlays = new ArrayList<>(1);
        addOverlay(overlays, reelOverlays, partVariant(stack, FishingRodStats.SLOT_REEL, ModItems.ROD_REELS));
        return new RodPassModel(body, overlays);
    }

// 配件叠加层对应的模型 id：与物品模型同目录，文件名加 _overlay 后缀
    private static ResourceLocation overlayId(DeferredHolder<Item, Item> part) {
        return new ResourceLocation(flavor_immersed_daily_argo.MODID,
                "item/" + part.getId().getPath() + "_overlay");
    }

// 配件叠加层的模型不挂在任何物品上，正常流程不会烘焙，这里按「侧载模型」注册进来。
    @SubscribeEvent
    static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (List<DeferredHolder<Item, Item>> parts : overlayGroups()) {
            for (DeferredHolder<Item, Item> part : parts) {
                event.register(overlayId(part));
            }
        }
    }

    @SubscribeEvent
    static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();

        reelOverlays = bakeOverlays(models, ModItems.ROD_REELS);
        lineOverlays = bakeOverlays(models, ModItems.ROD_LINES);
        floatOverlays = bakeOverlays(models, ModItems.ROD_FLOATS);
        sinkerOverlays = bakeOverlays(models, ModItems.ROD_SINKERS);

        ModelResourceLocation rodModel = new ModelResourceLocation(ModItems.MOD_FISHING_ROD.getId(), "inventory");
        BakedModel base = models.get(rodModel);
        if (base != null) {
            rawRodModel = base;
            models.put(rodModel, new RodModel(base));
        }
    }

// 配件叠加层按「线轮 / 鱼线 / 浮漂 / 铅坠」分组，与 ModItems 里的配件顺序表一一对应
    private static List<List<DeferredHolder<Item, Item>>> overlayGroups() {
        return List.of(ModItems.ROD_REELS, ModItems.ROD_LINES, ModItems.ROD_FLOATS, ModItems.ROD_SINKERS);
    }

    private static List<BakedModel> bakeOverlays(Map<ResourceLocation, BakedModel> models,
                                                 List<DeferredHolder<Item, Item>> parts) {
        List<BakedModel> baked = new ArrayList<>(parts.size());
        for (DeferredHolder<Item, Item> part : parts) {
            BakedModel overlay = models.get(overlayId(part));
            if (overlay == null) {
// 缺一个就整组不渲染，避免下标错位叠错贴图
                return List.of();
            }
            baked.add(overlay);
        }
        return List.copyOf(baked);
    }

// 读取钓竿指定槽位装的配件，返回它在 variants 中的下标 + 1；未装或不认识返回 0。
    public static int partVariant(ItemStack stack, int slot, List<DeferredHolder<Item, Item>> variants) {
        List<ItemStack> mods = FishingRodModMenu.getData(stack).mods();
        if (mods.size() <= slot) {
            return 0;
        }
        ItemStack part = mods.get(slot);
        for (int i = 0; i < variants.size(); i++) {
            if (part.is(variants.get(i).get())) {
                return i + 1;
            }
        }
        return 0;
    }

// 钓竿是否正在抛竿（手上这把竿有活跃鱼钩）。
//
// 与钓竿模型的 cast 谓词同一套判断：抛竿时鱼线 / 浮漂 / 铅坠都不渲染。
    public static boolean isCasting(ItemStack stack, @Nullable LivingEntity entity) {
        if (!(entity instanceof Player player) || player.fishing == null) {
            return false;
        }
        boolean mainHand = player.getMainHandItem() == stack;
        boolean offHand = player.getOffhandItem() == stack;
        if (player.getMainHandItem().getItem() instanceof FishingRodItem) {
            offHand = false;
        }
        return mainHand || offHand;
    }

// 按槽位算出这根竿当前要叠的配件层，顺序即渲染顺序（后叠的盖在前面的之上）。
//
// 线轮：装上就叠
// 鱼线：装上且未抛竿时叠
// 浮漂 / 铅坠：都在鱼线之上，所以装了鱼线且未抛竿时才叠
    private static List<BakedModel> overlaysFor(ItemStack stack, @Nullable LivingEntity entity) {
        List<BakedModel> passes = new ArrayList<>(4);
        addOverlay(passes, reelOverlays, partVariant(stack, FishingRodStats.SLOT_REEL, ModItems.ROD_REELS));

        if (!isCasting(stack, entity)) {
            int line = partVariant(stack, FishingRodStats.SLOT_LINE, ModItems.ROD_LINES);
            addOverlay(passes, lineOverlays, line);
            if (line > 0) {
                addOverlay(passes, floatOverlays,
                        partVariant(stack, FishingRodStats.SLOT_FLOAT, ModItems.ROD_FLOATS));
                addOverlay(passes, sinkerOverlays,
                        partVariant(stack, FishingRodStats.SLOT_SINKER, ModItems.ROD_SINKERS));
            }
        }
        return passes;
    }

// variant 是「下标 + 1」，0 表示该槽位没装这个配件，此时不叠这一层
    private static void addOverlay(List<BakedModel> passes, List<BakedModel> overlays, int variant) {
        if (variant > 0 && variant <= overlays.size()) {
            passes.add(overlays.get(variant - 1));
        }
    }

// 只换掉 override 的解析方式，其余（几何、变换、粒子图标等）全部沿用原模型
    private static final class RodModel extends BakedModelWrapper<BakedModel> {

        private final ItemOverrides overrides;

        private RodModel(BakedModel base) {
            super(base);
            this.overrides = new OverlayOverrides(base);
        }

        @Override
        public ItemOverrides getOverrides() {
            return this.overrides;
        }
    }

// 解析出竿体模型（body 谓词部分仍由 JSON 的 override 负责），
// 再把当前该叠的配件层一起交给 RodPassModel。
    private static final class OverlayOverrides extends ItemOverrides {

        private final BakedModel base;

        private OverlayOverrides(BakedModel base) {
            this.base = base;
        }

        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
            BakedModel resolved = this.base.getOverrides().resolve(this.base, stack, level, entity, seed);
            if (resolved == null) {
                resolved = this.base;
            }
            return new RodPassModel(resolved, overlaysFor(stack, entity));
        }
    }

// 在解析出来的钓竿模型之上，依次叠上当前所装配件的渲染层。
//
// 每个配件贴图都是几何与原模型一致的整物品覆盖图，所以用渲染 pass 叠上去就够，
// 不必为每种配件组合都生成一份多层模型。
    private static final class RodPassModel extends BakedModelWrapper<BakedModel> {

        private final List<BakedModel> passes;

        private RodPassModel(BakedModel base, List<BakedModel> overlays) {
            super(base);
            if (overlays.isEmpty()) {
                this.passes = List.of(base);
            } else {
                List<BakedModel> all = new ArrayList<>(overlays.size() + 1);
                all.add(base);
                all.addAll(overlays);
                this.passes = List.copyOf(all);
            }
        }

        @Override
        public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
            return this.passes;
        }

// 应用变换后必须把自己交回去：原版 handleCameraTransforms 会用返回值继续渲染，
// 沿用 BakedModelWrapper 的默认实现会退回原模型、把这层包装丢掉，配件就叠不上去了。
        @Override
        public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
            this.originalModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
            return this;
        }
    }
}
