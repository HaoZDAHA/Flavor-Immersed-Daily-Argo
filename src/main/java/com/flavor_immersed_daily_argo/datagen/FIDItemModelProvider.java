package com.flavor_immersed_daily_argo.datagen;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

// 物品模型数据生成
//
// 说明：普通的平面贴图物品统一走 #basicItem 自动生成；
// 立体（3D）生鲜物品直接继承同名方块模型；钓竿 / 抄网 / 图标等特殊模型仍保留手写文件。
//
// 南山钓竿的贴图不再按配件组合预生成，只生成两类模型：竿体模型
// （fishing_rod_body_<竿体>，由 fishing_rod.json 的 body 谓词选中）
// 与每个配件各一个的叠加层模型（item/<配件>_overlay）。
// 配件叠加层不挂在任何物品上，由
// com.flavor_immersed_daily_argo.fishing.FishingRodModelOverrides 侧载后，
// 在渲染时按槽位实际装的配件动态叠在竿体模型之上。
public class FIDItemModelProvider extends ItemModelProvider {

    public FIDItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, flavor_immersed_daily_argo.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
// 平面贴图的生鲜海产
        basicItem(ModItems.RAW_GRASS_CARP.get());
        basicItem(ModItems.RAW_TENCH.get());
        basicItem(ModItems.RAW_MANDARIN_FISH.get());
        basicItem(ModItems.RAW_BLACK_FISH.get());
        basicItem(ModItems.RAW_RECTUM.get());
        basicItem(ModItems.RAW_CRUCIAN_CARP.get());
        basicItem(ModItems.RAW_CHINESE_CARP.get());
        basicItem(ModItems.RAW_SILVER_CARP.get());
        basicItem(ModItems.RAW_BASS.get());
        basicItem(ModItems.RAW_TILAPIA.get());
        basicItem(ModItems.RAW_LOACH.get());
        basicItem(ModItems.RAW_CATFISH.get());
        basicItem(ModItems.RAW_GOBY.get());
        basicItem(ModItems.RAW_BLACK_CARP.get());
        basicItem(ModItems.RAW_REDFIN.get());
        basicItem(ModItems.RAW_PLECO.get());
        basicItem(ModItems.RAW_BIGHEAD_CARP.get());
        basicItem(ModItems.RAW_HAIRY_CRAB.get());
        basicItem(ModItems.RAW_PRAWN.get());
        basicItem(ModItems.RAW_CLAM.get());
        basicItem(ModItems.RAW_TURTLE.get());
        basicItem(ModItems.RAW_MUD_CRAB.get());
        basicItem(ModItems.RAW_SWIMMING_CRAB.get());
        basicItem(ModItems.RAW_RIVER_SNAIL.get());
        basicItem(ModItems.RAW_CRAYFISH.get());
        basicItem(ModItems.RAW_SILVERY_POMFRET.get());
        basicItem(ModItems.RAW_PACIFIC_SAURY.get());
        basicItem(ModItems.RAW_SARDINE.get());
        basicItem(ModItems.RAW_GROUPER.get());
        basicItem(ModItems.RAW_JAPANESE_HALFBEAK.get());
        basicItem(ModItems.RAW_RAZOR_CLAM.get());
        basicItem(ModItems.RAW_SEA_CUCUMBER.get());
        basicItem(ModItems.RAW_SPOON_WORM.get());
        basicItem(ModItems.RAW_SEA_URCHIN.get());
        basicItem(ModItems.RAW_SEA_SNAIL.get());
        basicItem(ModItems.RAW_JELLYFISH.get());
        basicItem(ModItems.RAW_AKIAMI_SHRIMP.get());
        basicItem(ModItems.RAW_MANTIS_SHRIMP.get());
        basicItem(ModItems.RAW_SCALLOP.get());
        basicItem(ModItems.RAW_OYSTER.get());
        basicItem(ModItems.RAW_MUD_SHRIMP.get());
        basicItem(ModItems.RAW_ABALONE.get());
// 大型鱼的生肉与部位
        basicItem(ModItems.RAW_ANGLERFISH_LIVER.get());
        basicItem(ModItems.RAW_MACKEREL_MEAT.get());
        basicItem(ModItems.RAW_HAIRTAIL_SEGMENT.get());
        basicItem(ModItems.RAW_SWORDFISH_MEAT.get());
        basicItem(ModItems.RAW_TUNA_MEAT.get());
        basicItem(ModItems.RAW_EEL_SEGMENT.get());
// 鱿鱼：生鱿鱼肉 / 生鱿鱼须是平面图标；整只生鱿鱼（raw_squid）用砧板屠宰的立体模型，
// 手写在 models/item/raw_squid.json 里
        basicItem(ModItems.RAW_SQUID_MEAT.get());
        basicItem(ModItems.RAW_SQUID_TENTACLE.get());

// 桶装水产
        basicItem(ModItems.GRASS_CARP_BUCKET.get());
        basicItem(ModItems.TENCH_BUCKET.get());
        basicItem(ModItems.MANDARIN_FISH_BUCKET.get());
        basicItem(ModItems.BLACK_FISH_BUCKET.get());
        basicItem(ModItems.RECTUM_BUCKET.get());
        basicItem(ModItems.CRUCIAN_CARP_BUCKET.get());
        basicItem(ModItems.CHINESE_CARP_BUCKET.get());
        basicItem(ModItems.SILVER_CARP_BUCKET.get());
        basicItem(ModItems.BASS_BUCKET.get());
        basicItem(ModItems.TILAPIA_BUCKET.get());
        basicItem(ModItems.LOACH_BUCKET.get());
        basicItem(ModItems.CATFISH_BUCKET.get());
        basicItem(ModItems.GOBY_BUCKET.get());
        basicItem(ModItems.REDFIN_BUCKET.get());
        basicItem(ModItems.BLACK_CARP_BUCKET.get());
        basicItem(ModItems.PLECO_BUCKET.get());
        basicItem(ModItems.BIGHEAD_CARP_BUCKET.get());
        basicItem(ModItems.HAIRY_CRAB_BUCKET.get());
        basicItem(ModItems.PRAWN_BUCKET.get());
        basicItem(ModItems.TURTLE_BUCKET.get());
        basicItem(ModItems.MUD_CRAB_BUCKET.get());
        basicItem(ModItems.SWIMMING_CRAB_BUCKET.get());
        basicItem(ModItems.SILVERY_POMFRET_BUCKET.get());
        basicItem(ModItems.PACIFIC_SAURY_BUCKET.get());
        basicItem(ModItems.SARDINE_BUCKET.get());
        basicItem(ModItems.GROUPER_BUCKET.get());
        basicItem(ModItems.JAPANESE_HALFBEAK_BUCKET.get());

// 杂项物品
        basicItem(ModItems.JINKELA.get());
        basicItem(ModItems.ORGANIC_FERTILIZER.get());
        basicItem(ModItems.SICKLE.get());
        basicItem(ModItems.WATERING_CAN.get());
        basicItem(ModItems.HARROW.get());
// 草帽（straw_hat）不在这里生成：它的手持模型要带 worn 谓词（戴到头上时切到 item/straw_hat_head），
// 手写在 models/item/straw_hat.json 里
// 肥沃耕地：没有单独的 16×16 手持贴图，直接继承方块模型（与 vanilla 耕地物品一致）
        withExistingParent("fertile_farmland", modLoc("block/fertile_farmland"));
// 灌溉水车：手持用独立的 16×16 平面贴图，与方块实体模型分开
        basicItem(ModItems.WATERWHEEL.get());
// 水管：手持用独立的 16×16 平面贴图，与方块模型分开
        basicItem(ModItems.WATER_PIPE.get());
// 水泵：同样手持用独立的 16×16 平面贴图
        basicItem(ModItems.WATER_PUMP.get());
// 加料阀：同样手持用独立的 16×16 平面贴图，与方块模型分开
        basicItem(ModItems.DOSING_TUBE.get());
// 旋转水管 / 扇形水管：手持同样用独立的 16×16 平面贴图，与方块模型分开
        basicItem(ModItems.ROTATING_WATER_PIPE.get());
        basicItem(ModItems.SECTOR_WATER_PIPE.get());

// 畜禽饲养
// 五种禽类的蛋：机制同原版鸡蛋，平面贴图
        basicItem(ModItems.DUCK_EGG.get());
        basicItem(ModItems.GOOSE_EGG.get());
        basicItem(ModItems.QUAIL_EGG.get());
        basicItem(ModItems.PIGEON_EGG.get());
        basicItem(ModItems.TURKEY_EGG.get());
// 奶类：平面贴图
        basicItem(ModItems.FRESH_GOAT_MILK.get());
        basicItem(ModItems.CAMEL_MILK.get());
        basicItem(ModItems.WILD_DOG_MILK.get());
// 小型鱼屠宰产物
        basicItem(ModItems.RAW_FISH_HEAD.get());
        basicItem(ModItems.RAW_FISH_SWIM_BLADDER.get());
        basicItem(ModItems.RAW_FISH_SKIN.get());
        basicItem(ModItems.RAW_FISH_ROE.get());
        basicItem(ModItems.RAW_FISH_LIVER.get());
        basicItem(ModItems.FISH_BONE.get());
        basicItem(ModItems.FISH_SCALE.get());
// 鸡窝：手持用独立的 16×16 平面贴图，与方块模型（32×32）分开
        basicItem(ModItems.CHICKEN_COOP.get());
        basicItem(ModItems.FEEDING_TROUGH.get());
// 化粪发酵桶：同样手持用独立的 16×16 平面贴图，与方块模型（128×128）分开
        basicItem(ModItems.SEPTIC_TANK.get());
// 沼气罐：手持用独立平面贴图，方块模型跨两格
        basicItem(ModItems.METHANE_TANK.get());
// 宠物窝：手持用独立的 16×16 平面贴图，与方块模型分开
        basicItem(ModItems.PET_NEST.get());
        basicItem(ModItems.MARSH_GAS.get());
        basicItem(ModItems.FEED_GRASS.get());
        basicItem(ModItems.FEED_SEED.get());
        basicItem(ModItems.FEED_AQUATIC.get());
        basicItem(ModItems.FEED_MEAT.get());
        basicItem(ModItems.MILKER.get());
        basicItem(ModItems.POOP.get());
// 针管：空针管与「筒装生物遗传液」两套贴图，用 dna 谓词切换
        getBuilder("syringe")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/syringe"))
                .override()
                .predicate(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "dna"), 1.0F)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/syringe_dna")))
                .end();
        getBuilder("syringe_dna")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/syringe_dna"));
// 刷毛梳子 / 牧鞭 / 哨子 / 体外驱虫药：手持 16×16 平面贴图
        basicItem(ModItems.LIVESTOCK_BRUSH.get());
        basicItem(ModItems.STOCK_WHIP.get());
        basicItem(ModItems.WHISTLE.get());
        basicItem(ModItems.FLEA_TREATMENT.get());
// 捕捞渔网：手持用独立的 16×16 平面贴图，与方块模型分开
        basicItem(ModItems.FISH_NET.get());
// 稻草人：手持 16×16 平面贴图，与实体模型分开
        basicItem(ModItems.SCARECROW.get());
// 毒饵站：同样手持用独立的 16×16 平面贴图，与实体模型分开
        basicItem(ModItems.POISON_BAIT_STATION.get());
// 拉力木车：同样手持用独立的 16×16 平面贴图，与实体模型分开
        basicItem(ModItems.HANDCART.get());
// 充气钓鱼浮台：同样手持用独立的 16×16 平面贴图，与实体模型分开
        basicItem(ModItems.FISHING_FLOATING_PLATFORM.get());
// 水产三件套：手持用独立的 16×16 平面贴图，与方块模型分开
        basicItem(ModItems.FISH_PROTECTION.get());
        basicItem(ModItems.FISHING_BOX.get());
        basicItem(ModItems.ROD_POD.get());
// 钓鱼展示架：手持用独立的 16×16 平面贴图，与方块模型（64×64）分开
        basicItem(ModItems.FISH_DISPLAY_BOARD.get());
// 鱼缸：手持用独立的 16×16 平面贴图，与方块模型（32×32）分开
        basicItem(ModItems.FISH_BOWL.get());
// 杆秤：立体手持模型（Blockbench 导出），保留手写 models/item/steelyard.json，不走 basicItem
// 抄网（fishing_net）同样是手写的 3D 模型，不在这里生成
// 多功能无人机与遥控器：同样手持用独立的 16×16 平面贴图
        basicItem(ModItems.UAV.get());
        basicItem(ModItems.UAV_CONTROLLER.get());
// 无人机模块（五选一）：同样手持用独立的 16×16 平面贴图
        basicItem(ModItems.UAV_MODULE_CARE.get());
        basicItem(ModItems.UAV_MODULE_COMBAT.get());
        basicItem(ModItems.UAV_MODULE_HARVEST.get());
        basicItem(ModItems.UAV_MODULE_TRANSPORT.get());
        basicItem(ModItems.UAV_MODULE_HIGHLIGHT.get());
// 纸箱：运输模式要用的东西，同样手持用独立的 16×16 平面贴图
        basicItem(ModItems.CARTON.get());

// 编制麻袋：空袋与装袋共两个模型，装袋后由 filled 谓词切换过去
        getBuilder("poultry_catching_net")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/poultry_catching_net"))
                .override()
                .predicate(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "filled"), 1.0F)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/poultry_catching_net_full")))
                .end();
        getBuilder("poultry_catching_net_full")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/poultry_catching_net_full"));

// 南山钓竿配件：杆体
        basicItem(ModItems.ROD_BAMBOO.get());
        basicItem(ModItems.ROD_FIBERGLASS.get());
        basicItem(ModItems.ROD_CARBON.get());
        basicItem(ModItems.ROD_RESIN.get());
        basicItem(ModItems.ROD_HARDWOOD.get());
        basicItem(ModItems.ROD_COMPOSITE.get());

// 南山钓竿配件：线轮
        basicItem(ModItems.REEL_HAND.get());
        basicItem(ModItems.REEL_SPINNING.get());
        basicItem(ModItems.REEL_DRUM.get());
        basicItem(ModItems.REEL_BAITCAST.get());
        basicItem(ModItems.REEL_DRAG.get());
        basicItem(ModItems.REEL_JIGGING.get());

// 南山钓竿配件：鱼线
        basicItem(ModItems.LINE_COTTON.get());
        basicItem(ModItems.LINE_NYLON.get());
        basicItem(ModItems.LINE_FLUOROCARBON.get());
        basicItem(ModItems.LINE_BRAID.get());
        basicItem(ModItems.LINE_STEEL.get());
        basicItem(ModItems.LINE_GLOW.get());

// 南山钓竿配件：浮漂
        basicItem(ModItems.FLOAT_REED.get());
        basicItem(ModItems.FLOAT_BALSA.get());
        basicItem(ModItems.FLOAT_NANO.get());
        basicItem(ModItems.FLOAT_PEACOCK.get());
        basicItem(ModItems.FLOAT_GLOW.get());
        basicItem(ModItems.FLOAT_ISO.get());

// 南山钓竿配件：铅坠
        basicItem(ModItems.SINKER_SPLIT.get());
        basicItem(ModItems.SINKER_BULLET.get());
        basicItem(ModItems.SINKER_DROP.get());
        basicItem(ModItems.SINKER_SHEET.get());
        basicItem(ModItems.SINKER_BOTTOM.get());

// 南山钓竿配件：鱼饵
        basicItem(ModItems.BAIT_GRASS.get());
        basicItem(ModItems.BAIT_MEAT.get());
        basicItem(ModItems.BAIT_ROT.get());
        basicItem(ModItems.BAIT_WORM.get());

// 配件叠加层：线轮 / 鱼线 / 浮漂 / 铅坠的渲染贴图都是与钓竿同尺寸的整物品覆盖图，几何完全一致，
// 所以不必按「竿体 × 线轮 × 鱼线 × 浮漂 × 铅坠」为每种组合各生成一份模型（组合数会到 6480），
// 而是每个配件只生成一个独立模型；它不挂在任何物品上，由 FishingRodModelOverrides 用
// ModelEvent.RegisterAdditional 侧载后，按槽位实际装的配件动态叠在解析出的钓竿模型之上。
        for (List<DeferredHolder<Item, Item>> parts : List.of(
                ModItems.ROD_REELS, ModItems.ROD_LINES, ModItems.ROD_FLOATS, ModItems.ROD_SINKERS)) {
            for (DeferredHolder<Item, Item> part : parts) {
                String partPath = part.getId().getPath();
                getBuilder("item/" + partPath + "_overlay")
                        .parent(getExistingFile(mcLoc("item/generated")))
                        .texture("layer0", modLoc("item/" + partPath + "_overlay"));
            }
        }

// 南山钓竿：竿体模型
// 竿体是「底座」，不是叠加层，所以仍走 body 谓词：fishing_rod.json 用 body 谓词选中
// fishing_rod_body_<竿体>，把原版钓竿贴图换成竿体贴图；线轮 / 鱼线 / 浮漂 / 铅坠
// 则由上面生成的独立叠加层模型在渲染时叠上去。
        for (int b = 0; b < ModItems.ROD_BODIES.size(); b++) {
            String bodyPath = ModItems.ROD_BODIES.get(b).getId().getPath();
            getBuilder("fishing_rod_body_" + bodyPath.substring("rod_".length()))
                    .parent(getExistingFile(mcLoc("item/handheld_rod")))
                    .texture("layer0", modLoc("item/" + bodyPath));
        }

// 刷怪蛋
        basicItem(ModItems.DUCK_SPAWN_EGG.get());
        basicItem(ModItems.GOOSE_SPAWN_EGG.get());
        basicItem(ModItems.QUAIL_SPAWN_EGG.get());
        basicItem(ModItems.PIGEON_SPAWN_EGG.get());
        basicItem(ModItems.TURKEY_SPAWN_EGG.get());
        basicItem(ModItems.BULLFROG_SPAWN_EGG.get());
        basicItem(ModItems.BAMBOORAT_SPAWN_EGG.get());
        basicItem(ModItems.MOUSE_SPAWN_EGG.get());
        basicItem(ModItems.GRASS_CARP_SPAWN_EGG.get());
        basicItem(ModItems.TENCH_SPAWN_EGG.get());
        basicItem(ModItems.MANDARIN_FISH_SPAWN_EGG.get());
        basicItem(ModItems.LOACH_SPAWN_EGG.get());
        basicItem(ModItems.PLECO_SPAWN_EGG.get());
        basicItem(ModItems.GOBY_SPAWN_EGG.get());
        basicItem(ModItems.SILVER_CARP_SPAWN_EGG.get());
        basicItem(ModItems.TILAPIA_SPAWN_EGG.get());
        basicItem(ModItems.REDFIN_SPAWN_EGG.get());
        basicItem(ModItems.BLACK_CARP_SPAWN_EGG.get());
        basicItem(ModItems.RECTUM_SPAWN_EGG.get());
        basicItem(ModItems.BASS_SPAWN_EGG.get());
        basicItem(ModItems.CHINESE_CARP_SPAWN_EGG.get());
        basicItem(ModItems.CRUCIAN_CARP_SPAWN_EGG.get());
        basicItem(ModItems.CATFISH_SPAWN_EGG.get());
        basicItem(ModItems.BIGHEAD_CARP_SPAWN_EGG.get());
        basicItem(ModItems.BLACK_FISH_SPAWN_EGG.get());
        basicItem(ModItems.KING_CRAB_SPAWN_EGG.get());
        basicItem(ModItems.SWORDFISH_SPAWN_EGG.get());
        basicItem(ModItems.HAIRTAIL_SPAWN_EGG.get());
        basicItem(ModItems.TUNA_SPAWN_EGG.get());
        basicItem(ModItems.BROWN_CRAB_SPAWN_EGG.get());
        basicItem(ModItems.ANGLERFISH_SPAWN_EGG.get());
        basicItem(ModItems.CHINESE_MACKEREL_SPAWN_EGG.get());
        basicItem(ModItems.EEL_SPAWN_EGG.get());
        basicItem(ModItems.LOBSTER_SPAWN_EGG.get());
        basicItem(ModItems.TURTLE_SPAWN_EGG.get());
        basicItem(ModItems.HAIRY_CRAB_SPAWN_EGG.get());
        basicItem(ModItems.PRAWN_SPAWN_EGG.get());
        basicItem(ModItems.SWIMMING_CRAB_SPAWN_EGG.get());
        basicItem(ModItems.MUD_CRAB_SPAWN_EGG.get());
        basicItem(ModItems.SARDINE_SPAWN_EGG.get());
        basicItem(ModItems.GROUPER_SPAWN_EGG.get());
        basicItem(ModItems.PACIFIC_SAURY_SPAWN_EGG.get());
        basicItem(ModItems.SILVERY_POMFRET_SPAWN_EGG.get());
        basicItem(ModItems.JAPANESE_HALFBEAK_SPAWN_EGG.get());
        basicItem(ModItems.HARVESTER_SPAWN_EGG.get());

// 立体（3D）生鲜海产：继承同名方块模型
        withExistingParent("raw_anglerfish", modLoc("block/raw_anglerfish"));
        withExistingParent("raw_brown_crab", modLoc("block/raw_brown_crab"));
        withExistingParent("raw_chinese_mackerel", modLoc("block/raw_chinese_mackerel"));
        withExistingParent("raw_eel", modLoc("block/raw_eel"));
        withExistingParent("raw_hairtail", modLoc("block/raw_hairtail"));
        withExistingParent("raw_king_crab", modLoc("block/raw_king_crab"));
        withExistingParent("raw_lobster", modLoc("block/raw_lobster"));
        withExistingParent("raw_tuna", modLoc("block/raw_tuna"));

// 生鸭子
        basicItem(ModItems.BLED_DUCK.get());
        basicItem(ModItems.RAW_DRESSED_DUCK.get());
        basicItem(ModItems.RAW_DUCK_THIGH.get());
        basicItem(ModItems.RAW_DUCK_RUMP.get());
        basicItem(ModItems.RAW_DUCK_HEART.get());
        basicItem(ModItems.RAW_DUCK_WEB.get());
        basicItem(ModItems.RAW_DUCK_WING.get());
        basicItem(ModItems.RAW_DUCK_MEAT.get());
        basicItem(ModItems.RAW_DUCK_LIVER.get());
        basicItem(ModItems.RAW_DUCK_INTESTINE.get());
        basicItem(ModItems.RAW_DUCK_GIZZARD.get());
        basicItem(ModItems.RAW_DUCK_NECK.get());
        basicItem(ModItems.RAW_DUCK_LEG.get());
        basicItem(ModItems.RAW_DUCK_TONGUE.get());
        basicItem(ModItems.RAW_DUCK_BLOOD.get());
        basicItem(ModItems.RAW_DUCK_HEAD.get());

// 生鹅
        basicItem(ModItems.BLED_GOOSE.get());
        basicItem(ModItems.RAW_DRESSED_GOOSE.get());
        basicItem(ModItems.RAW_GOOSE_MEAT.get());
        basicItem(ModItems.RAW_GOOSE_LIVER.get());
        basicItem(ModItems.RAW_GOOSE_INTESTINE.get());
        basicItem(ModItems.RAW_GOOSE_LEG.get());

// 熟食：烤鸭 / 烤鹅
        basicItem(ModItems.ROAST_DUCK_HEAD.get());
        basicItem(ModItems.ROAST_DUCK_GIZZARD.get());
        basicItem(ModItems.ROAST_DUCK_HEART.get());
        basicItem(ModItems.ROAST_DUCK_WEB.get());
        basicItem(ModItems.ROAST_DUCK_BLOOD_CAKE.get());
        basicItem(ModItems.ROAST_DUCK_LEG.get());
        basicItem(ModItems.ROAST_DUCK_RUMP.get());
        basicItem(ModItems.ROAST_DUCK_TONGUE.get());
        basicItem(ModItems.ROAST_DUCK_MEAT.get());
        basicItem(ModItems.ROAST_DUCK_THIGH.get());
        basicItem(ModItems.ROAST_DUCK_LIVER.get());
        basicItem(ModItems.ROAST_DUCK_WING.get());
        basicItem(ModItems.ROAST_DUCK_INTESTINE.get());
        basicItem(ModItems.ROAST_DUCK_NECK.get());
        basicItem(ModItems.ROAST_GOOSE_INTESTINE.get());
        basicItem(ModItems.ROAST_GOOSE_LIVER.get());
        basicItem(ModItems.ROAST_GOOSE_MEAT.get());
        basicItem(ModItems.ROAST_GOOSE_LEG.get());

// 熟食：动物内脏 / 原版动物肉 / 水产
        basicItem(ModItems.ROAST_ANIMAL_INTESTINE.get());
        basicItem(ModItems.ROAST_ANIMAL_LUNG.get());
        basicItem(ModItems.ROAST_ANIMAL_LIVER.get());
        basicItem(ModItems.ROAST_ANIMAL_STOMACH.get());
        basicItem(ModItems.ROAST_ANIMAL_HEART.get());
        basicItem(ModItems.ROAST_ANIMAL_BLOOD_CAKE.get());
        basicItem(ModItems.ROAST_RABBIT_HEAD.get());
        basicItem(ModItems.ROAST_DOG_MEAT.get());
        basicItem(ModItems.ROAST_LLAMA_MEAT.get());
        basicItem(ModItems.ROAST_DONKEY_MEAT.get());
        basicItem(ModItems.ROAST_CAMEL_HUMP.get());
        basicItem(ModItems.ROAST_CAMEL_MEAT.get());
        basicItem(ModItems.ROAST_SWIMMING_CRAB.get());
        basicItem(ModItems.ROAST_SPOON_WORM.get());
        basicItem(ModItems.ROAST_SILVERY_POMFRET.get());
        basicItem(ModItems.ROAST_SEA_URCHIN.get());
        basicItem(ModItems.ROAST_SEA_SNAIL.get());
        basicItem(ModItems.ROAST_SEA_CUCUMBER.get());
        basicItem(ModItems.ROAST_SCALLOP.get());
        basicItem(ModItems.ROAST_SARDINE.get());
        basicItem(ModItems.ROAST_RIVER_SNAIL.get());
        basicItem(ModItems.ROAST_RAZOR_CLAM.get());
        basicItem(ModItems.SHRIMP_MEAT.get());
        basicItem(ModItems.ROAST_PACIFIC_SAURY.get());
        basicItem(ModItems.ROAST_MUD_CRAB.get());
        basicItem(ModItems.ROAST_MANTIS_SHRIMP.get());
        basicItem(ModItems.ROAST_JELLYFISH.get());
        basicItem(ModItems.ROAST_JAPANESE_HALFBEAK.get());
        basicItem(ModItems.ROAST_HAIRY_CRAB.get());
        basicItem(ModItems.ROAST_MUD_SHRIMP.get());
        basicItem(ModItems.ROAST_GROUPER.get());
        basicItem(ModItems.ROAST_OYSTER.get());
        basicItem(ModItems.ROAST_CRAYFISH.get());
        basicItem(ModItems.ROAST_CLAM.get());
        basicItem(ModItems.DRIED_SHRIMP.get());
        basicItem(ModItems.ROAST_ABALONE.get());

// 烤鱼（整鱼）
        basicItem(ModItems.ROAST_GRASS_CARP.get());
        basicItem(ModItems.ROAST_TENCH.get());
        basicItem(ModItems.ROAST_MANDARIN_FISH.get());
        basicItem(ModItems.ROAST_BLACK_FISH.get());
        basicItem(ModItems.ROAST_RECTUM.get());
        basicItem(ModItems.ROAST_CRUCIAN_CARP.get());
        basicItem(ModItems.ROAST_CHINESE_CARP.get());
        basicItem(ModItems.ROAST_SILVER_CARP.get());
        basicItem(ModItems.ROAST_BASS.get());
        basicItem(ModItems.ROAST_TILAPIA.get());
        basicItem(ModItems.ROAST_LOACH.get());
        basicItem(ModItems.ROAST_CATFISH.get());
        basicItem(ModItems.ROAST_GOBY.get());
        basicItem(ModItems.ROAST_REDFIN.get());
        basicItem(ModItems.ROAST_BLACK_CARP.get());
        basicItem(ModItems.ROAST_PLECO.get());
        basicItem(ModItems.ROAST_BIGHEAD_CARP.get());

// 生禽类（鹌鹑 / 鸽子 / 火鸡）
        basicItem(ModItems.BLED_QUAIL.get());
        basicItem(ModItems.RAW_DRESSED_QUAIL.get());
        basicItem(ModItems.BLED_PIGEON.get());
        basicItem(ModItems.RAW_DRESSED_PIGEON.get());
        basicItem(ModItems.BLED_TURKEY.get());
        basicItem(ModItems.RAW_DRESSED_TURKEY.get());

// 生肉与动物内脏
        basicItem(ModItems.RAW_RABBIT_HEAD.get());
        basicItem(ModItems.RAW_DOG_MEAT.get());
        basicItem(ModItems.RAW_LLAMA_MEAT.get());
        basicItem(ModItems.RAW_DONKEY_MEAT.get());
        basicItem(ModItems.RAW_CAMEL_HUMP.get());
        basicItem(ModItems.RAW_CAMEL_MEAT.get());
        basicItem(ModItems.RAW_ANIMAL_HEART.get());
        basicItem(ModItems.RAW_ANIMAL_LIVER.get());
        basicItem(ModItems.RAW_ANIMAL_INTESTINE.get());
        basicItem(ModItems.RAW_ANIMAL_LUNG.get());
        basicItem(ModItems.RAW_ANIMAL_STOMACH.get());
        basicItem(ModItems.RAW_ANIMAL_BLOOD.get());

// 死动物：手持模型直接继承同名方块模型
// 用 flavor_immersed_daily 的斩骨刀攻击对应动物即可获得，挂到大挂钩上按阶段屠宰
// 五种禽类
        withExistingParent("dead_duck", modLoc("block/dead_duck"));
        withExistingParent("dead_goose", modLoc("block/dead_goose"));
        withExistingParent("dead_quail", modLoc("block/dead_quail"));
        withExistingParent("dead_pigeon", modLoc("block/dead_pigeon"));
        withExistingParent("dead_turkey", modLoc("block/dead_turkey"));
// 原版动物
        withExistingParent("dead_dog", modLoc("block/dead_dog"));
        withExistingParent("dead_donkey", modLoc("block/dead_donkey"));
        withExistingParent("dead_camel", modLoc("block/dead_camel"));
        withExistingParent("dead_rabbit", modLoc("block/dead_rabbit"));
        withExistingParent("dead_llama", modLoc("block/dead_llama"));

// 林业生态
        basicItem(ModItems.RANGER_CLIPBOARD.get());
        basicItem(ModItems.TREE_PAINT.get());
        basicItem(ModItems.FIRE_EXTINGUISHER.get());
// 气象仪器：手持用独立 16×16 平面贴图，方块实体模型由渲染器绘制
        basicItem(ModItems.WEATHER_INSTRUMENT.get());
        basicItem(ModItems.HERBICIDE.get());
        basicItem(ModItems.RANGING_ROPE.get());
        basicItem(ModItems.WATER_LEVEL_GAUGE.get());
        basicItem(ModItems.WATER_QUALITY_DETECTOR.get());
// 昆虫培育箱：没有单独的 16×16 手持贴图，手持模型直接继承方块模型（与立体生鲜一致）
        withExistingParent("insect_incubator", modLoc("block/insect_incubator"));
        basicItem(ModItems.CARDBOARD_HONEYCOMB.get());
        basicItem(ModItems.INSECT_BOARD.get());
        basicItem(ModItems.INSECT_NET.get());
// 昆虫系列：四种昆虫卵（蜜蜂卵 / 蝉卵 / 蚂蚱卵 / 蚕卵）
        basicItem(ModItems.BEE_ROE.get());
        basicItem(ModItems.CICADA_ROE.get());
        basicItem(ModItems.LOCUST_ROE.get());
        basicItem(ModItems.SILKWORM_ROE.get());
// 昆虫系列：四种成虫（蜜蜂 / 知了猴 / 蚂蚱 / 蚕）
        basicItem(ModItems.BEE.get());
        basicItem(ModItems.CICADA.get());
        basicItem(ModItems.CICADA_SLOUGH.get());
        basicItem(ModItems.LOCUST.get());
        basicItem(ModItems.SILKWORM.get());
        basicItem(ModItems.HIGH_PROTEIN_SUBSTANCE.get());
// 水质处理系列：五项投放式道具，均为 16×16 平面贴图
        basicItem(ModItems.ACTIVATED_CARBON_PARTICLES.get());
        basicItem(ModItems.PROTOCATECHUIC_ACID.get());
        basicItem(ModItems.OXYPHYLLINE.get());
        basicItem(ModItems.WATER_OXYGEN_MACHINE.get());
        basicItem(ModItems.MICROBE_POWDER.get());

// 农作物（果树）
// 三种果实：平面贴图
        basicItem(ModItems.POMELO.get());
        basicItem(ModItems.CHESTNUT.get());
        basicItem(ModItems.GUAVA.get());
// 荠菜与荠菜种子：平面贴图（作物方块模型另见 blockstates / models/block）
        basicItem(ModItems.SHEPHERDS_PURSE.get());
        basicItem(ModItems.SHEPHERDS_PURSE_SEEDS.get());
// 三种树苗：物品手持为平面贴图，方块模型另见 blockstates / models/block
        basicItem(ModItems.POMELO_SAPLING.get());
        basicItem(ModItems.CHESTNUT_SAPLING.get());
        basicItem(ModItems.GUAVA_SAPLING.get());
// 果树叶 / 悬挂的果子：手持与放置共用同一份 3D 方块模型
        withExistingParent("pomelo_fruiting_leaves", modLoc("block/pomelo_fruiting_leaves"));
        withExistingParent("chestnut_fruiting_leaves", modLoc("block/chestnut_fruiting_leaves"));
        withExistingParent("guava_fruiting_leaves", modLoc("block/guava_fruiting_leaves"));
        withExistingParent("hanging_pomelo", modLoc("block/hanging_pomelo"));
        withExistingParent("hanging_chestnut", modLoc("block/hanging_chestnut"));
        withExistingParent("hanging_guava", modLoc("block/hanging_guava"));

// 果食（果树果实的加工品）：全部平面贴图
        basicItem(ModItems.POMELO_JAM.get());
        basicItem(ModItems.GUAVA_JAM.get());
        basicItem(ModItems.POMELO_ICECREAM.get());
        basicItem(ModItems.GUAVA_ICECREAM.get());
        basicItem(ModItems.POMELO_POPSICLE.get());
        basicItem(ModItems.GUAVA_POPSICLE.get());
        basicItem(ModItems.POMELO_JUICE.get());
        basicItem(ModItems.GUAVA_JUICE.get());
// 板栗的加工品
        basicItem(ModItems.CHESTNUT_FLOUR.get());
        basicItem(ModItems.CHESTNUT_DOUGH.get());
        basicItem(ModItems.ROASTED_CHESTNUT.get());
        basicItem(ModItems.SUGAR_ROASTED_CHESTNUT.get());
        basicItem(ModItems.CHESTNUT_CAKE.get());

// 食物佳肴：鸭菜
// 卤味 / 鸭蛋 / 汤：都是平面贴图
        basicItem(ModItems.BRAISED_DUCK_HEAD.get());
        basicItem(ModItems.BRAISED_DUCK_WEB.get());
        basicItem(ModItems.BRAISED_DUCK_INTESTINE.get());
        basicItem(ModItems.BRAISED_DUCK_GIZZARD.get());
        basicItem(ModItems.BRAISED_DUCK_NECK.get());
        basicItem(ModItems.BRAISED_DUCK_LEG.get());
        basicItem(ModItems.BRAISED_DUCK_TONGUE.get());
        basicItem(ModItems.OOZING_SALTED_DUCK_EGG.get());
        basicItem(ModItems.BOILED_DUCK_EGG.get());
        basicItem(ModItems.DUCK_BLOOD_VERMICELLI_SOUP.get());
// 可放置食物：物品一律用 16×16 平面图标（GUI / 手持 / 掉落物都是这张图），
// 方块本体仍然是 3D 模型，摆上桌后按阶段换模型
        basicItem(ModItems.PEKING_ROAST_DUCK.get());
        basicItem(ModItems.BRAISED_DUCK_PLATTER.get());
        basicItem(ModItems.ROAST_DUCK.get());
        basicItem(ModItems.FOIE_GRAS.get());
        basicItem(ModItems.ROASTED_GOOSE.get());
        basicItem(ModItems.PEIXIAN_DOG_MEAT.get());
        basicItem(ModItems.STEAMED_HAIRY_CRAB.get());
        basicItem(ModItems.SCALDED_PRAWNS.get());
        basicItem(ModItems.TURTLE_PIGEON_SOUP.get());
        basicItem(ModItems.SPICY_MACKEREL_CRUMBLE.get());
        basicItem(ModItems.STIR_FRIED_CLAMS.get());
        basicItem(ModItems.TYPHOON_SHELTER_PRAWN.get());
        basicItem(ModItems.CLAM_CHIVE_DOUGH_DROP_SOUP.get());
        basicItem(ModItems.SPICY_CRAYFISH.get());
// 铁锅炖大鹅 / 全驴宴 / 油焖大虾的方块由方块实体渲染器绘制，物品同样用平面图标
        basicItem(ModItems.POT_STEW_GOOSE.get());
        basicItem(ModItems.DONKEY_FEAST.get());
        basicItem(ModItems.BRAISED_PRAWNS.get());
// 蒸龙虾 / 蒸帝王蟹 / 蒸面包蟹 / 春秋剑鱼：还没有 16×16 图标，手持继承第一阶段的方块模型
        withExistingParent("steamed_lobster", modLoc("block/steamed_lobster_stage0"));
        withExistingParent("steamed_king_crab", modLoc("block/steamed_king_crab_stage0"));
        withExistingParent("steamed_brown_crab", modLoc("block/steamed_brown_crab_stage0"));
        withExistingParent("stinky_swordfish", modLoc("block/stinky_swordfish_stage0"));
// 调味料（不可食用）：平面贴图
        basicItem(ModItems.OYSTER_SAUCE.get());
        basicItem(ModItems.GARLIC_MINCE.get());
        basicItem(ModItems.SHRIMP_PASTE.get());
        basicItem(ModItems.CRAB_ROE.get());
        basicItem(ModItems.FISH_LIVER_OIL.get());
        basicItem(ModItems.DEEP_SEA_FISH_LIVER_OIL.get());
// 水产熟食：平面贴图
        basicItem(ModItems.PICKLED_SWORDFISH.get());
        basicItem(ModItems.MACKEREL_PANCAKE_ROLL.get());
        basicItem(ModItems.RAZOR_CLAM_SCRAMBLED_EGGS.get());
        basicItem(ModItems.SCALLION_SEA_CUCUMBER.get());
        basicItem(ModItems.SPOON_WORM_RICE.get());
        basicItem(ModItems.SEA_URCHIN_LUFFA_SOUP.get());
        basicItem(ModItems.SEA_URCHIN_STEAMED_EGG.get());
        basicItem(ModItems.BRAISED_ABALONE.get());
        basicItem(ModItems.BRAISED_HAIRTAIL.get());
        basicItem(ModItems.BRAISED_FISH_ROE.get());
        basicItem(ModItems.SALT_PEPPER_MANTIS_SHRIMP.get());
        basicItem(ModItems.TUNA_SUSHI.get());
        basicItem(ModItems.ROAST_MACKEREL.get());
        basicItem(ModItems.ROAST_SILKWORM_CHRYSALIS.get());
        basicItem(ModItems.ROAST_HAIRTAIL_SEGMENT.get());
        basicItem(ModItems.ROAST_SWORDFISH.get());
        basicItem(ModItems.ROAST_TUNA.get());
        basicItem(ModItems.ROAST_LOCUST.get());
        basicItem(ModItems.ROAST_EEL_SEGMENT.get());
        basicItem(ModItems.ROAST_CICADA.get());
        basicItem(ModItems.CHILI_FRIED_MACKEREL_CRUMBLE.get());
        basicItem(ModItems.VINEGAR_JELLYFISH_HEAD.get());
        basicItem(ModItems.COLD_FISH_SKIN.get());
        basicItem(ModItems.SPICY_RIVER_SNAIL.get());
        basicItem(ModItems.EEL_RICE.get());
        basicItem(ModItems.STEAMED_GROUPER.get());
        basicItem(ModItems.STEAMED_MUD_CRAB.get());
        basicItem(ModItems.STEAMED_SWIMMING_CRAB.get());
        basicItem(ModItems.COOKED_KING_CRAB_LEG.get());
        basicItem(ModItems.COOKED_LOBSTER_MEAT.get());
        basicItem(ModItems.COOKED_SQUID_MEAT.get());
        basicItem(ModItems.COOKED_SQUID_TENTACLE.get());
        basicItem(ModItems.COOKED_BROWN_CRAB_MEAT.get());
        basicItem(ModItems.BOILED_PRAWN.get());
        basicItem(ModItems.GARLIC_VERMICELLI_SCALLOP.get());
        basicItem(ModItems.GARLIC_VERMICELLI_OYSTER.get());
        basicItem(ModItems.OIL_SOAKED_SNAIL_SLICES.get());
        basicItem(ModItems.FISH_MAW_TOFU_SOUP.get());
        basicItem(ModItems.FRIED_SILKWORM_CHRYSALIS.get());
        basicItem(ModItems.FRIED_HAIRTAIL_SEGMENT.get());
        basicItem(ModItems.FRIED_LOCUST.get());
        basicItem(ModItems.FRIED_CICADA.get());
// 禽畜熟食：平面贴图
        basicItem(ModItems.PIGEON_SOUP.get());
        basicItem(ModItems.BRAISED_DONKEY_MEAT.get());
        basicItem(ModItems.ROAST_QUAIL.get());
        basicItem(ModItems.ROAST_PIGEON.get());
        basicItem(ModItems.ROAST_TURKEY.get());
        basicItem(ModItems.BRAISED_QUAIL_EGG.get());
        basicItem(ModItems.BRAISED_CAMEL_MEAT.get());
        basicItem(ModItems.DONKEY_MEAT_BURGER.get());
        basicItem(ModItems.SPICY_RABBIT_HEAD.get());
        basicItem(ModItems.BOILED_QUAIL_EGG.get());
        basicItem(ModItems.BOILED_GOOSE_EGG.get());
        basicItem(ModItems.BOILED_PIGEON_EGG.get());
        basicItem(ModItems.BOILED_TURKEY_EGG.get());

// 纺织系列
// 蚕茧 / 生蚕蛹 / 丝绸：平面贴图
        basicItem(ModItems.SILKWORM_COCOON.get());
        basicItem(ModItems.RAW_SILKWORM_CHRYSALIS.get());
        basicItem(ModItems.SILK.get());
// 缝纫机：没有单独的 16×16 手持贴图，手持模型直接继承方块模型
        withExistingParent("sewing_machine", modLoc("block/sewing_machine"));
// 空白丝绸装备：平面贴图（就是那四张纯白模板）
        basicItem(ModItems.BLANK_SILK_HAT.get());
        basicItem(ModItems.BLANK_SILK_TUNIC.get());
        basicItem(ModItems.BLANK_SILK_LEGGINGS.get());
        basicItem(ModItems.BLANK_SILK_BOOTS.get());

// 经济与生活
// 自助取款机：没有单独的 16×16 手持贴图，手持模型直接继承方块模型
        withExistingParent("atm", modLoc("block/atm"));
// 小票打印机：同样没有单独的手持贴图，手持模型继承常态方块模型
        withExistingParent("receipt_printer", modLoc("block/receipt_printer"));
// 餐饮营业执照：没有单独的手持贴图，手持模型继承方块模型
        withExistingParent("catering_business_license", modLoc("block/catering_business_license"));
// 商业电脑：没有单独的手持贴图，手持模型继承方块模型
        withExistingParent("shopping_computer", modLoc("block/shopping_computer"));
// 银行卡与六种纸币：平面贴图
        basicItem(ModItems.CREDIT_CARD.get());
        basicItem(ModItems.MONEY_1.get());
        basicItem(ModItems.MONEY_5.get());
        basicItem(ModItems.MONEY_10.get());
        basicItem(ModItems.MONEY_20.get());
        basicItem(ModItems.MONEY_50.get());
        basicItem(ModItems.MONEY_100.get());
// 导航手机：展开 / 收起两套贴图，用 open 谓词切换
        getBuilder("phone")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/phone"))
                .override()
                .predicate(ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "open"), 1.0F)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/phone_open")))
                .end();
        getBuilder("phone_open")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/phone_open"));
// 打包好的外卖：平面贴图
        basicItem(ModItems.TAKEAWAY_BAG.get());
    }
}
