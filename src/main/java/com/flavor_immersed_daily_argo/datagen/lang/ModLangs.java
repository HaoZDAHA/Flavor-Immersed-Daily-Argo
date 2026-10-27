package com.flavor_immersed_daily_argo.datagen.lang;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

// 英文语言文件（en_us）数据生成。
//
// 中文仍保留手写的 zh_cn.json，方便直接校对文案；英文统一由此生成，
// 避免在 json 与 Java 之间来回同步。修改后运行 gradlew runData 刷新产物。
public class ModLangs extends LanguageProvider {

    private static final String MODID = flavor_immersed_daily_argo.MODID;

    public ModLangs(PackOutput output, String locale) {
        super(output, MODID, locale);
    }

    @Override
    protected void addTranslations() {
        addCreativeTabs();
        addEntities();
        addItemNames();
        addTooltips();
        addJei();
        addScreenTexts();
        addBookTexts();
    }

// 创造模式物品栏

    private void addCreativeTabs() {
        add("itemGroup." + MODID + ".sundries", "Dongli Nanshan Ju");
// 标签页里的分区横幅
        gui("creative_section.primary_products", "Primary Farm Products");
        gui("creative_section.farming", "Farming");
        gui("creative_section.herding", "Herding");
        gui("creative_section.fishing", "Fishery");
        gui("creative_section.forestry", "Forestry");
        gui("creative_section.food_delicacies", "Delicacies");
        gui("creative_section.economy_life", "Economy & Life");
// 家养动物的状态栏与取名界面
        gui("stat.satiety", "Satiety");
        gui("stat.thirst", "Thirst");
        gui("stat.cleanliness", "Cleanliness");
        gui("stat.mood", "Mood");
        gui("stat.weight", "Weight");
        gui("stat.weight.value", "%s kg");
        gui("stat.weight.standard", "Std %s");
        gui("rename.title", "Give It a Name");
        gui("rename.hint", "Leave empty to restore the default name");
        gui("rename.confirm", "Confirm");
    }

// 实体

    private void addEntities() {
        entity("duck", "Duck");
        entity("goose", "Goose");
        entity("turkey", "Turkey");
        entity("pigeon", "Pigeon");
        entity("quail", "Quail");
        entity("bullfrog", "Bullfrog");
        entity("bamboorat", "Bamboo Rat");
        entity("mouse", "Mouse");
        entity("poop", "Feces");
        entity("thrown_poop", "Thrown Feces");
        entity("thrown_poultry_egg", "Thrown Poultry Egg");
        entity("scarecrow", "Scarecrow");
        entity("handcart", "Handcart");
        entity("fishing_floating_platform", "Inflatable Fishing Platform");
        entity("uav", "Multifunctional Drone");
        entity("falling_fruit", "Falling Fruit");
        entity("carton", "Cardboard Box");
        entity("grass_carp", "Grass Carp");
        entity("tench", "Tench");
        entity("mandarin_fish", "Mandarin Fish");
        entity("loach", "Loach");
        entity("pleco", "Pleco");
        entity("goby", "Goby");
        entity("silver_carp", "Silver Carp");
        entity("tilapia", "Tilapia");
        entity("redfin", "Redfin");
        entity("black_carp", "Black Carp");
        entity("rectum", "Longsnout Catfish");
        entity("bass", "Bass");
        entity("chinese_carp", "Chinese Carp");
        entity("crucian_carp", "Crucian Carp");
        entity("catfish", "Catfish");
        entity("bighead_carp", "Bighead Carp");
        entity("black_fish", "Black Fish");
        entity("harvester", "Harvester");
        entity("king_crab", "King Crab");
        entity("swordfish", "Swordfish");
        entity("hairtail", "Hairtail");
        entity("tuna", "Tuna");
        entity("brown_crab", "Brown Crab");
        entity("anglerfish", "Anglerfish");
        entity("chinese_mackerel", "Chinese Mackerel");
        entity("eel", "Eel");
        entity("lobster", "Lobster");
        entity("turtle", "Turtle");
        entity("hairy_crab", "Hairy Crab");
        entity("prawn", "Prawn");
        entity("swimming_crab", "Swimming Crab");
        entity("mud_crab", "Mud Crab");
        entity("sardine", "Sardine");
        entity("grouper", "Grouper");
        entity("pacific_saury", "Pacific Saury");
        entity("silvery_pomfret", "Silvery Pomfret");
        entity("japanese_halfbeak", "Japanese Halfbeak");
        entity("takeaway_bag", "Takeaway Bag");
        entity("delivery_point", "Delivery Point");
        entity("delivery_rider", "Delivery Rider");
        entity("customer", "Customer");
// 音效字幕
        add("subtitles." + MODID + ".harvester_attack", "Harvester strikes");
// 成就：开局赠书
        add("advancements." + MODID + ".guide_book.title", "Nanshan Residence");
        add("advancements." + MODID + ".guide_book.description",
                "Take the Nanshan Tutorial Scroll - from fishing and farming to market day, settle into your own life.");
    }

// 物品名称

    private void addItemNames() {
// 刷怪蛋
        item("duck_spawn_egg", "Duck Spawn Egg");
        item("goose_spawn_egg", "Goose Spawn Egg");
        item("quail_spawn_egg", "Quail Spawn Egg");
        item("pigeon_spawn_egg", "Pigeon Spawn Egg");
        item("turkey_spawn_egg", "Turkey Spawn Egg");
        item("bullfrog_spawn_egg", "Bullfrog Spawn Egg");
        item("bamboorat_spawn_egg", "Bamboo Rat Spawn Egg");
        item("mouse_spawn_egg", "Mouse Spawn Egg");
        item("grass_carp_spawn_egg", "Grass Carp Spawn Egg");
        item("tench_spawn_egg", "Tench Spawn Egg");
        item("mandarin_fish_spawn_egg", "Mandarin Fish Spawn Egg");
        item("loach_spawn_egg", "Loach Spawn Egg");
        item("pleco_spawn_egg", "Pleco Spawn Egg");
        item("goby_spawn_egg", "Goby Spawn Egg");
        item("silver_carp_spawn_egg", "Silver Carp Spawn Egg");
        item("tilapia_spawn_egg", "Tilapia Spawn Egg");
        item("redfin_spawn_egg", "Redfin Spawn Egg");
        item("black_carp_spawn_egg", "Black Carp Spawn Egg");
        item("rectum_spawn_egg", "Longsnout Catfish Spawn Egg");
        item("bass_spawn_egg", "Bass Spawn Egg");
        item("chinese_carp_spawn_egg", "Chinese Carp Spawn Egg");
        item("crucian_carp_spawn_egg", "Crucian Carp Spawn Egg");
        item("catfish_spawn_egg", "Catfish Spawn Egg");
        item("bighead_carp_spawn_egg", "Bighead Carp Spawn Egg");
        item("black_fish_spawn_egg", "Black Fish Spawn Egg");
        item("harvester_spawn_egg", "Harvester Spawn Egg");
        item("king_crab_spawn_egg", "King Crab Spawn Egg");
        item("swordfish_spawn_egg", "Swordfish Spawn Egg");
        item("hairtail_spawn_egg", "Hairtail Spawn Egg");
        item("tuna_spawn_egg", "Tuna Spawn Egg");
        item("brown_crab_spawn_egg", "Brown Crab Spawn Egg");
        item("anglerfish_spawn_egg", "Anglerfish Spawn Egg");
        item("chinese_mackerel_spawn_egg", "Chinese Mackerel Spawn Egg");
        item("eel_spawn_egg", "Eel Spawn Egg");
        item("lobster_spawn_egg", "Lobster Spawn Egg");
        item("turtle_spawn_egg", "Turtle Spawn Egg");
        item("hairy_crab_spawn_egg", "Hairy Crab Spawn Egg");
        item("prawn_spawn_egg", "Prawn Spawn Egg");
        item("swimming_crab_spawn_egg", "Swimming Crab Spawn Egg");
        item("mud_crab_spawn_egg", "Mud Crab Spawn Egg");
        item("sardine_spawn_egg", "Sardine Spawn Egg");
        item("grouper_spawn_egg", "Grouper Spawn Egg");
        item("pacific_saury_spawn_egg", "Pacific Saury Spawn Egg");
        item("silvery_pomfret_spawn_egg", "Silvery Pomfret Spawn Egg");
        item("japanese_halfbeak_spawn_egg", "Japanese Halfbeak Spawn Egg");

// 淡水生鲜
        item("raw_grass_carp", "Raw Grass Carp");
        item("raw_tench", "Raw Tench");
        item("raw_mandarin_fish", "Raw Mandarin Fish");
        item("raw_black_fish", "Raw Black Fish");
        item("raw_rectum", "Raw Recutum");
        item("raw_crucian_carp", "Raw Crucian Carp");
        item("raw_chinese_carp", "Raw Chinese Carp");
        item("raw_silver_carp", "Raw Silver Carp");
        item("raw_bass", "Raw Bass");
        item("raw_tilapia", "Raw Tilapia");
        item("raw_loach", "Raw Loach");
        item("raw_catfish", "Raw Catfish");
        item("raw_goby", "Raw Goby");
        item("raw_black_carp", "Raw Black Carp");
        item("raw_redfin", "Raw Redfin");
        item("raw_pleco", "Raw Pleco");
        item("raw_bighead_carp", "Raw Bighead Carp");
        item("raw_hairy_crab", "Raw Hairy Crab");
        item("raw_prawn", "Raw Prawn");
        item("raw_clam", "Raw Clam");
        item("raw_turtle", "Raw Turtle");
        item("raw_mud_crab", "Raw Mud Crab");
        item("raw_swimming_crab", "Raw Swimming Crab");
        item("raw_river_snail", "Raw River Snail");
        item("raw_crayfish", "Raw Crayfish");

// 海产生鲜
// 以下 8 种是立体模型，物品由方块承载，名称键走 block.*
        block("raw_chinese_mackerel", "Raw Chinese Mackerel");
        block("raw_tuna", "Raw Tuna");
        block("raw_hairtail", "Raw Hairtail");
        block("raw_eel", "Raw Eel");
        block("raw_anglerfish", "Raw Anglerfish");
        block("raw_king_crab", "Raw King Crab");
        block("raw_lobster", "Raw Lobster");
        block("raw_brown_crab", "Raw Brown Crab");
        block("raw_swordfish", "Raw Swordfish");
        item("raw_silvery_pomfret", "Raw Silvery Pomfret");
        item("raw_pacific_saury", "Raw Pacific Saury");
        item("raw_sardine", "Raw Sardine");
        item("raw_grouper", "Raw Grouper");
        item("raw_japanese_halfbeak", "Raw Japanese Halfbeak");
        item("raw_razor_clam", "Raw Razor Clam");
        item("raw_sea_cucumber", "Raw Sea Cucumber");
        item("raw_spoon_worm", "Raw Spoon Worm");
        item("raw_sea_urchin", "Raw Sea Urchin");
        item("raw_sea_snail", "Raw Sea Snail");
        item("raw_jellyfish", "Raw Jellyfish");
        item("raw_akiami_shrimp", "Raw Akiami Shrimp");
        item("raw_mantis_shrimp", "Raw Mantis Shrimp");
        item("raw_scallop", "Raw Scallop");
        item("raw_oyster", "Raw Oyster");
        item("raw_mud_shrimp", "Raw Mud Shrimp");
        item("raw_abalone", "Raw Abalone");
        item("raw_squid", "Raw Squid");
        item("raw_squid_meat", "Raw Squid Meat");
        item("raw_squid_tentacle", "Raw Squid Tentacle");
// 大型鱼的生肉与部位
        item("raw_anglerfish_liver", "Raw Anglerfish Liver");
        item("raw_mackerel_meat", "Raw Mackerel Meat");
        item("raw_hairtail_segment", "Raw Hairtail Segment");
        item("raw_swordfish_meat", "Raw Swordfish Meat");
        item("raw_tuna_meat", "Raw Tuna Meat");
        item("raw_eel_segment", "Raw Eel Segment");
// 桶装水产
        item("grass_carp_bucket", "Bucket of Grass Carp");
        item("tench_bucket", "Bucket of Tench");
        item("mandarin_fish_bucket", "Bucket of Mandarin Fish");
        item("black_fish_bucket", "Bucket of Snakehead");
        item("rectum_bucket", "Bucket of Longsnout Catfish");
        item("crucian_carp_bucket", "Bucket of Crucian Carp");
        item("chinese_carp_bucket", "Bucket of Chinese Carp");
        item("silver_carp_bucket", "Bucket of Silver Carp");
        item("bass_bucket", "Bucket of Bass");
        item("tilapia_bucket", "Bucket of Tilapia");
        item("loach_bucket", "Bucket of Loach");
        item("catfish_bucket", "Bucket of Catfish");
        item("goby_bucket", "Bucket of Goby");
        item("redfin_bucket", "Bucket of Redfin");
        item("black_carp_bucket", "Bucket of Black Carp");
        item("pleco_bucket", "Bucket of Pleco");
        item("bighead_carp_bucket", "Bucket of Bighead Carp");
        item("hairy_crab_bucket", "Bucket of Hairy Crab");
        item("prawn_bucket", "Bucket of Prawn");
        item("turtle_bucket", "Bucket of Softshell Turtle");
        item("mud_crab_bucket", "Bucket of Mud Crab");
        item("swimming_crab_bucket", "Bucket of Swimming Crab");
        item("silvery_pomfret_bucket", "Bucket of Silvery Pomfret");
        item("pacific_saury_bucket", "Bucket of Pacific Saury");
        item("sardine_bucket", "Bucket of Sardine");
        item("grouper_bucket", "Bucket of Grouper");
        item("japanese_halfbeak_bucket", "Bucket of Japanese Halfbeak");

// 生鸭子
        item("bled_duck", "Bled Duck");
        item("raw_dressed_duck", "Raw Dressed Duck");
        item("raw_duck_thigh", "Raw Duck Thigh");
        item("raw_duck_rump", "Raw Duck Rump");
        item("raw_duck_heart", "Raw Duck Heart");
        item("raw_duck_web", "Raw Duck Web");
        item("raw_duck_wing", "Raw Duck Wing");
        item("raw_duck_meat", "Raw Duck Meat");
        item("raw_duck_liver", "Raw Duck Liver");
        item("raw_duck_intestine", "Raw Duck Intestine");
        item("raw_duck_gizzard", "Raw Duck Gizzard");
        item("raw_duck_neck", "Raw Duck Neck");
        item("raw_duck_leg", "Raw Duck Leg");
        item("raw_duck_tongue", "Raw Duck Tongue");
        item("raw_duck_blood", "Raw Duck Blood");
        item("raw_duck_head", "Raw Duck Head");
        item("bled_goose", "Bled Goose");
        item("raw_dressed_goose", "Raw Dressed Goose");
        item("raw_goose_meat", "Raw Goose Meat");
        item("raw_goose_liver", "Raw Goose Liver");
        item("raw_goose_intestine", "Raw Goose Intestine");
        item("raw_goose_leg", "Raw Goose Leg");

// 熟食：烤鸭 / 烤鹅（生鸭子 / 生鹅的各部位烤熟）
        item("roast_duck_head", "Roast Duck Head");
        item("roast_duck_gizzard", "Roast Duck Gizzard");
        item("roast_duck_heart", "Roast Duck Heart");
        item("roast_duck_web", "Roast Duck Web");
        item("roast_duck_blood_cake", "Roast Duck Blood Cake");
        item("roast_duck_leg", "Roast Duck Leg");
        item("roast_duck_rump", "Roast Duck Rump");
        item("roast_duck_tongue", "Roast Duck Tongue");
        item("roast_duck_meat", "Roast Duck Meat");
        item("roast_duck_thigh", "Roast Duck Thigh");
        item("roast_duck_liver", "Roast Duck Liver");
        item("roast_duck_wing", "Roast Duck Wing");
        item("roast_duck_intestine", "Roast Duck Intestine");
        item("roast_duck_neck", "Roast Duck Neck");
        item("roast_goose_intestine", "Roast Goose Intestine");
        item("roast_goose_liver", "Roast Goose Liver");
        item("roast_goose_meat", "Roast Goose Meat");
        item("roast_goose_leg", "Roast Goose Leg");

// 熟食：鸭菜（卤味 / 鸭蛋 / 鸭血粉丝汤 / 三道可摆上桌的整鸭菜）
        item("braised_duck_head", "Braised Duck Head");
        item("braised_duck_web", "Braised Duck Web");
        item("braised_duck_intestine", "Braised Duck Intestine");
        item("braised_duck_gizzard", "Braised Duck Gizzard");
        item("braised_duck_neck", "Braised Duck Neck");
        item("braised_duck_leg", "Braised Duck Leg");
        item("braised_duck_tongue", "Braised Duck Tongue");
        item("oozing_salted_duck_egg", "Oozing Salted Duck Egg");
        item("boiled_duck_egg", "Boiled Duck Egg");
        item("duck_blood_vermicelli_soup", "Duck Blood Vermicelli Soup");
// 三道整鸭菜是方块物品，名字挂在方块上（BlockItem 描述键取方块名）
        block("peking_roast_duck", "Peking Roast Duck");
        block("braised_duck_platter", "Braised Duck Platter");
        block("roast_duck", "Hand-Shredded Roast Duck");
        block("pot_stew_goose", "Iron Pot Stewed Goose");
        block("foie_gras", "Foie Gras");
        block("donkey_feast", "Whole Donkey Feast");
// 广式烧鹅 / 沛县狗肉同样是方块物品
        block("roasted_goose", "Cantonese Roast Goose");
        block("peixian_dog_meat", "Peixian Dog Meat");
// 清蒸大闸蟹 / 白灼虾 / 油焖大虾同样是方块物品
        block("steamed_hairy_crab", "Steamed Hairy Crab");
        block("scalded_prawns", "Scalded Prawns");
        block("braised_prawns", "Braised Prawns");
// 本批新增的可摆放水产菜
        block("turtle_pigeon_soup", "Turtle and Pigeon Soup");
        block("spicy_mackerel_crumble", "Mackerel Pancake Set");
        block("stir_fried_clams", "Stir-Fried Clams");
        block("typhoon_shelter_prawn", "Typhoon Shelter Prawns");
        block("stinky_swordfish", "Spring and Autumn Swordfish");
        block("clam_chive_dough_drop_soup", "Clam and Chive Dough Drop Soup");
        block("spicy_crayfish", "Spicy Crayfish");
// 蒸龙虾 / 蒸帝王蟹 / 蒸面包蟹同样是方块物品
        block("steamed_lobster", "Steamed Lobster");
        block("steamed_king_crab", "Steamed King Crab");
        block("steamed_brown_crab", "Steamed Brown Crab");
// 调味料（不可食用）
        item("oyster_sauce", "Oyster Sauce");
        item("garlic_mince", "Minced Garlic");
        item("shrimp_paste", "Shrimp Paste");
        item("crab_roe", "Crab Roe");
        item("fish_liver_oil", "Fish Liver Oil");
        item("deep_sea_fish_liver_oil", "Deep Sea Fish Liver Oil");
// 水产熟食
        item("pickled_swordfish", "Pickled Swordfish");
        item("mackerel_pancake_roll", "Mackerel Pancake Roll");
        item("razor_clam_scrambled_eggs", "Razor Clam Scrambled Eggs");
        item("scallion_sea_cucumber", "Scallion Braised Sea Cucumber");
        item("spoon_worm_rice", "Spoon Worm Rice");
        item("sea_urchin_luffa_soup", "Sea Urchin Luffa Soup");
        item("sea_urchin_steamed_egg", "Sea Urchin Steamed Egg");
        item("braised_abalone", "Braised Abalone");
        item("braised_hairtail", "Braised Hairtail");
        item("braised_fish_roe", "Braised Fish Roe");
        item("salt_pepper_mantis_shrimp", "Salt and Pepper Mantis Shrimp");
        item("tuna_sushi", "Tuna Sushi");
        item("roast_mackerel", "Roast Mackerel");
        item("roast_silkworm_chrysalis", "Roast Silkworm Chrysalis");
        item("roast_hairtail_segment", "Roast Hairtail Segment");
        item("roast_swordfish", "Roast Swordfish");
        item("roast_tuna", "Roast Tuna");
        item("roast_locust", "Roast Locust");
        item("roast_eel_segment", "Roast Eel Segment");
        item("roast_cicada", "Roast Cicada");
        item("chili_fried_mackerel_crumble", "Chili Fried Mackerel Crumble");
        item("vinegar_jellyfish_head", "Jellyfish Head in Aged Vinegar");
        item("cold_fish_skin", "Cold Fish Skin");
        item("spicy_river_snail", "Spicy River Snail");
        item("eel_rice", "Eel Rice");
        item("steamed_grouper", "Steamed Grouper");
        item("steamed_mud_crab", "Steamed Mud Crab");
        item("steamed_swimming_crab", "Steamed Swimming Crab");
        item("cooked_king_crab_leg", "Cooked King Crab Leg");
        item("cooked_lobster_meat", "Cooked Lobster Meat");
        item("cooked_squid_meat", "Cooked Squid Meat");
        item("cooked_squid_tentacle", "Cooked Squid Tentacle");
        item("cooked_brown_crab_meat", "Cooked Brown Crab Meat");
        item("boiled_prawn", "Boiled Prawn");
        item("garlic_vermicelli_scallop", "Garlic Vermicelli Scallop");
        item("garlic_vermicelli_oyster", "Garlic Vermicelli Oyster");
        item("oil_soaked_snail_slices", "Oil-Soaked Snail Slices");
        item("fish_maw_tofu_soup", "Fish Maw Tofu Soup");
        item("fried_silkworm_chrysalis", "Fried Silkworm Chrysalis");
        item("fried_hairtail_segment", "Fried Hairtail Segment");
        item("fried_locust", "Fried Locust");
        item("fried_cicada", "Fried Cicada");
// 禽畜熟食
        item("pigeon_soup", "Pigeon Soup");
        item("braised_donkey_meat", "Braised Donkey Meat");
        item("roast_quail", "Roast Quail");
        item("roast_pigeon", "Roast Pigeon");
        item("roast_turkey", "Roast Turkey");
        item("braised_quail_egg", "Braised Quail Eggs");
        item("braised_camel_meat", "Braised Camel Meat");
        item("donkey_meat_burger", "Donkey Meat Sandwich");
        item("spicy_rabbit_head", "Spicy Rabbit Head");
        item("boiled_quail_egg", "Boiled Quail Egg");
        item("boiled_goose_egg", "Boiled Goose Egg");
        item("boiled_pigeon_egg", "Boiled Pigeon Egg");
        item("boiled_turkey_egg", "Boiled Turkey Egg");

// 熟食：动物内脏 / 原版动物肉 / 水产
        item("roast_animal_intestine", "Roast Animal Intestine");
        item("roast_animal_lung", "Roast Animal Lung");
        item("roast_animal_liver", "Roast Animal Liver");
        item("roast_animal_stomach", "Roast Animal Stomach");
        item("roast_animal_heart", "Roast Animal Heart");
        item("roast_animal_blood_cake", "Roast Animal Blood Cake");
        item("roast_rabbit_head", "Roast Rabbit Head");
        item("roast_dog_meat", "Roast Dog Meat");
        item("roast_llama_meat", "Roast Llama Meat");
        item("roast_donkey_meat", "Roast Donkey Meat");
        item("roast_camel_hump", "Roast Camel Hump");
        item("roast_camel_meat", "Roast Camel Meat");
        item("roast_swimming_crab", "Roast Swimming Crab");
        item("roast_spoon_worm", "Roast Spoon Worm");
        item("roast_silvery_pomfret", "Roast Silvery Pomfret");
        item("roast_sea_urchin", "Roast Sea Urchin");
        item("roast_sea_snail", "Roast Sea Snail");
        item("roast_sea_cucumber", "Roast Sea Cucumber");
        item("roast_scallop", "Roast Scallop");
        item("roast_sardine", "Roast Sardine");
        item("roast_river_snail", "Roast River Snail");
        item("roast_razor_clam", "Roast Razor Clam");
        item("shrimp_meat", "Shrimp Meat");
        item("roast_pacific_saury", "Roast Pacific Saury");
        item("roast_mud_crab", "Roast Mud Crab");
        item("roast_mantis_shrimp", "Roast Mantis Shrimp");
        item("roast_jellyfish", "Roast Jellyfish");
        item("roast_japanese_halfbeak", "Roast Japanese Halfbeak");
        item("roast_hairy_crab", "Roast Hairy Crab");
        item("roast_mud_shrimp", "Roast Mud Shrimp");
        item("roast_grouper", "Roast Grouper");
        item("roast_oyster", "Roast Oyster");
        item("roast_crayfish", "Roast Crayfish");
        item("roast_clam", "Roast Clam");
        item("dried_shrimp", "Dried Shrimp");
        item("roast_abalone", "Roast Abalone");
// 烤鱼（整鱼）
        item("roast_grass_carp", "Roast Grass Carp");
        item("roast_tench", "Roast Tench");
        item("roast_mandarin_fish", "Roast Mandarin Fish");
        item("roast_black_fish", "Roast Snakehead");
        item("roast_rectum", "Roast Longsnout Catfish");
        item("roast_crucian_carp", "Roast Crucian Carp");
        item("roast_chinese_carp", "Roast Chinese Carp");
        item("roast_silver_carp", "Roast Silver Carp");
        item("roast_bass", "Roast Bass");
        item("roast_tilapia", "Roast Tilapia");
        item("roast_loach", "Roast Loach");
        item("roast_catfish", "Roast Catfish");
        item("roast_goby", "Roast Goby");
        item("roast_redfin", "Roast Redfin");
        item("roast_black_carp", "Roast Black Carp");
        item("roast_pleco", "Roast Pleco");
        item("roast_bighead_carp", "Roast Bighead Carp");

// 生禽类（鹌鹑 / 鸽子 / 火鸡）
        item("bled_quail", "Bled Quail");
        item("raw_dressed_quail", "Raw Dressed Quail");
        item("bled_pigeon", "Bled Pigeon");
        item("raw_dressed_pigeon", "Raw Dressed Pigeon");
        item("bled_turkey", "Bled Turkey");
        item("raw_dressed_turkey", "Raw Dressed Turkey");

// 生肉与动物内脏（屠宰动物获得）
        item("raw_rabbit_head", "Raw Rabbit Head");
        item("raw_dog_meat", "Raw Dog Meat");
        item("raw_llama_meat", "Raw Llama Meat");
        item("raw_donkey_meat", "Raw Donkey Meat");
        item("raw_camel_hump", "Raw Camel Hump");
        item("raw_camel_meat", "Raw Camel Meat");
        item("raw_animal_heart", "Animal Heart");
        item("raw_animal_liver", "Animal Liver");
        item("raw_animal_intestine", "Animal Intestine");
        item("raw_animal_lung", "Animal Lung");
        item("raw_animal_stomach", "Animal Stomach");
        item("raw_animal_blood", "Animal Blood");

// 死动物（用 flavor_immersed_daily 的斩骨刀攻击对应动物获得）
// 五种禽类
        item("dead_duck", "Dead Duck");
        item("dead_goose", "Dead Goose");
        item("dead_quail", "Dead Quail");
        item("dead_pigeon", "Dead Pigeon");
        item("dead_turkey", "Dead Turkey");
// 原版动物
        item("dead_dog", "Dead Dog");
        item("dead_donkey", "Dead Donkey");
        item("dead_camel", "Dead Camel");
        item("dead_rabbit", "Dead Rabbit");
        item("dead_llama", "Dead Llama");

// 工具与杂项
        item("fishing_rod", "Nanshan Fishing Rod");
        item("fishing_net", "Fishing Net");
        item("guide_book", "Nanshan Tutorial Scroll");
        item("developer_guide_book", "Nanshan Developer Guide");
        item("fishing_handbook", "Fishing Handbook");
        item("rod_bamboo", "Bamboo Rod");
        item("rod_fiberglass", "Fiberglass Rod");
        item("rod_carbon", "Carbon Rod");
        item("rod_resin", "Resin Rod");
        item("rod_hardwood", "Hardwood Rod");
        item("rod_composite", "Composite Rod");
        item("reel_hand", "Hand Reel");
        item("reel_spinning", "Spinning Reel");
        item("reel_drum", "Drum Reel");
        item("reel_baitcast", "Baitcast Reel");
        item("reel_drag", "Drag Reel");
        item("reel_jigging", "Jigging Reel");
        item("line_cotton", "Cotton Line");
        item("line_nylon", "Nylon Line");
        item("line_fluorocarbon", "Fluorocarbon Line");
        item("line_braid", "Braid Line");
        item("line_steel", "Steel Line");
        item("line_glow", "Glow Line");
        item("float_reed", "Reed Float");
        item("float_balsa", "Balsa Float");
        item("float_nano", "Nano Float");
        item("float_peacock", "Peacock Float");
        item("float_glow", "Glow Float");
        item("float_iso", "Iso Float");
        item("sinker_split", "Split Shot");
        item("sinker_bullet", "Bullet Sinker");
        item("sinker_drop", "Drop Sinker");
        item("sinker_sheet", "Sinker Sheet");
        item("sinker_bottom", "Bottom Sinker");
        item("bait_grass", "Herbivore Bait");
        item("bait_meat", "Carnivore Bait");
        item("bait_rot", "Scavenger Bait");
        item("earthworm", "Earthworm");
        item("jinkela", "§e§lJinkela");
        item("organic_fertilizer", "Organic Fertilizer");
// 林业生态
        item("ranger_clipboard", "Ranger Clipboard");
        item("tree_paint", "Tree Paint");
        item("fire_extinguisher", "Fire Extinguisher");
        item("weather_instrument", "Weather Instrument");
        block("weather_instrument", "Weather Instrument");
        item("herbicide", "Herbicide");
        item("ranging_rope", "Ranging Rope");
        item("water_level_gauge", "Water Level Gauge");
        item("water_quality_detector", "Water Quality Detector");
        item("insect_incubator", "Insect Incubator");
        block("insect_incubator", "Insect Incubator");
        item("cardboard_honeycomb", "Cardboard Honeycomb");
        item("insect_board", "Light Board");
        item("insect_net", "Insect Net");
// 昆虫系列：四种昆虫卵
        item("bee_roe", "Bee Roe");
        item("cicada_roe", "Cicada Roe");
        item("locust_roe", "Locust Roe");
        item("silkworm_roe", "Silkworm Roe");
// 昆虫系列：四种成虫
        item("bee", "Bee");
        item("cicada", "Cicada");
        item("cicada_slough", "Cicada Slough");
        item("locust", "Locust");
        item("silkworm", "Silkworm");
        item("high_protein_substance", "High-Protein Compound");
        item("activated_carbon_particles", "Activated Carbon Granules");
        item("protocatechuic_acid", "Protocatechuic Acid");
        item("oxyphylline", "Oxyphylline");
        item("water_oxygen_machine", "Water Oxygenator");
        item("microbe_powder", "Water Microbe Culture");
        item("sickle", "Sickle");
        item("watering_can", "Watering Can");
        item("harrow", "Harrow");
        item("straw_hat", "Straw Hat");
        block("fertile_farmland", "Fertile Farmland");
        block("waterwheel", "Irrigation Waterwheel");
        block("water_pipe", "Water Pipe");
        block("water_pump", "Water Pump");
        block("rotating_water_pipe", "Rotating Sprinkler");
        block("sector_water_pipe", "Fan Sprinkler");
        item("handcart", "Handcart");
        item("fishing_floating_platform", "Inflatable Fishing Platform");
        block("fish_protection", "Fish Keeper");
        block("fishing_box", "Fishing Box");
        block("rod_pod", "Rod Pod");
        block("fish_display_board", "Fish Display Board");
        block("fish_bowl", "Fish Tank");
        item("steelyard", "Steelyard");
        tooltip("handcart", "§7§oPlace it down and right-click to hitch it behind you; it follows closely. Sneak to let go.");
        item("uav", "Multifunctional Drone");
        tooltip("uav", "§7§oPlace it down, then bind a Drone Controller to it. With power on it hovers; it descends slowly when switched off. With a care module that has recorded corners installed, it flies to the centre of that area and keeps working.");
        item("uav_controller", "Drone Controller");
        tooltip("uav_controller", "§7§oRight-click a drone to bind it, then right-click anywhere to open the control panel: unbind, first-person piloting and power.");
        add("tooltip." + MODID + ".uav_controller.bound", "§7§oBound to a drone — right-click anywhere to open the control panel.");
        add("tooltip." + MODID + ".uav_controller.unbound", "§7§oNot bound yet — right-click a drone to bind it.");
        item("uav_module_care", "Drone Care Module");
        tooltip("uav_module_care", "§7§oA drone care module: installed in the module slot, the drone flies to ripe crops and fruiting leaves within the recorded area and harvests them, takes the harvest to the nearest container, and also fills troughs, fertilizes crops and moves eggs into the coop's hatch. Feed, water and bone meal go into the drone's own slots.");
        add("tooltip." + MODID + ".uav_module_range.how_to", "§7§oRight-click two blocks to record two horizontal corners (height does not matter), then put the module into the drone's module slot and it flies there and keeps working. Right-click the air while holding it to wipe the record.");
        add("tooltip." + MODID + ".uav_module_range.point_a", "§7§oCorner A (x, z): §f%s");
        add("tooltip." + MODID + ".uav_module_range.point_b", "§7§oCorner B (x, z): §f%s");
        add("tooltip." + MODID + ".uav_module_range.point_b_empty", "§7§oRight-click one more block to record corner B.");
        item("uav_module_combat", "Drone Combat Module");
        tooltip("uav_module_combat", "§7§oA drone module for piloting: the drone can eat from the use slot, and both mouse buttons fire. Arrows, ammo and throwables are taken from the drone's backpack, and each weapon fires through its own logic.");
        item("uav_module_harvest", "Drone Harvest Module");
        tooltip("uav_module_harvest", "§7§oA drone harvest module: installed in the module slot, the drone flies to ripe crops and fruiting leaves within the recorded area and harvests them one by one, then carries the harvest to the nearest container in (or just around) that area and drops it if there is none.");
        item("uav_module_transport", "Drone Transport Module");
        tooltip("uav_module_transport", "§7§oA drone module for transport: put a §fCardboard Box§7 in the drone's use slot, and it flies to the centre of the auto range, drops the box with everything in the drone packed inside, then returns to where it took off.");
        item("uav_module_highlight", "Drone Highlight Module");
        tooltip("uav_module_highlight", "§7§oA drone module for piloting: right-click a block to highlight it, right-click it again to clear. The outline shows through blocks and only while you are piloting the drone.");
        item("carton", "Cardboard Box");

// 畜禽饲养
        item("duck_egg", "Duck Egg");
        item("goose_egg", "Goose Egg");
        item("quail_egg", "Quail Egg");
        item("pigeon_egg", "Pigeon Egg");
        item("turkey_egg", "Turkey Egg");
// 奶类
        item("fresh_goat_milk", "Fresh Goat Milk");
        item("camel_milk", "Camel Milk");
        item("wild_dog_milk", "Wild Dog Milk");

// 小型鱼屠宰产物
        item("raw_fish_head", "Raw Fish Head");
        item("raw_fish_swim_bladder", "Raw Fish Swim Bladder");
        item("raw_fish_skin", "Raw Fish Skin");
        item("raw_fish_roe", "Raw Fish Roe");
        item("raw_fish_liver", "Raw Fish Liver");
        item("fish_bone", "Fish Bone");
        item("fish_scale", "Fish Scale");
        block("chicken_coop", "Chicken Coop");
        block("feeding_trough", "Ranch Feeding Trough");
        block("river_silt", "River Silt");
        block("salt_pan", "Salt Pan");
        block("salt_pile", "Salt Pile");
        block("septic_tank", "Septic Fermentation Tank");
        block("methane_tank", "Methane Tank");
        block("pet_nest", "Pet Nest");
        item("feed_grass", "Forage Feed");
        item("feed_seed", "Grain Feed");
        item("feed_aquatic", "Aquatic Feed");
        item("feed_meat", "Fresh Meat Feed");
// 水产饲料是范围生效，和另外三种“对准一只喂”不一样，单独把用法写在 tooltip 上
        tooltip("feed_aquatic", "§7§oRight-click to use: every fish, squid, crab, shrimp and turtle within §f30 blocks§7 is set to spawn - after 5 minutes each drops one of its kind. Babies simply grow up a step.");
        tooltip("feed_aquatic.spawning", "§7Those already spawning or pregnant will not eat, so one item only feeds the ones that need it.");
        item("poultry_catching_net", "Woven Burlap Sack");
        item("milker", "Milker");
        item("poop", "Feces");
        item("marsh_gas", "Pressurized Marsh Gas");
        item("syringe", "Syringe");
// 针管封上遗传液之后自己换的名字
        item("syringe_dna", "Vial of Animal Genetic Fluid");
        item("livestock_brush", "Livestock Brush");
        item("stock_whip", "Stock Whip");
        item("whistle", "Whistle");
        item("flea_treatment", "Flea Treatment");
        block("dosing_tube", "Dosing Valve");
        block("fish_net", "Catching Net");
        item("scarecrow", "Scarecrow");
        tooltip("scarecrow", "§7§oProtects farmland within §f8 blocks§7. Sneak-right-click or hit it §f3§7 times to take it back, right-click to rename; use a §fgenuine player ID§7 as the name to wear their skin.");
        item("poison_bait_station", "Poison Bait Station");
        tooltip("poison_bait_station", "§7§oRight-click a §fScarecrow§7 to set it up at its feet (the scarecrow stays). No mouse can spawn within §f16 blocks§7, and the station itself never breaks.");

// 农作物（大田：荠菜）
        item("shepherds_purse", "Shepherd's Purse");
        item("shepherds_purse_seeds", "Shepherd's Purse Seeds");
        block("shepherds_purse", "Shepherd's Purse");

// 农作物（果树）
        item("pomelo", "Pomelo");
        item("chestnut", "Chestnut");
        item("guava", "Guava");
        block("pomelo_sapling", "Pomelo Sapling");
        block("chestnut_sapling", "Chestnut Sapling");
        block("guava_sapling", "Guava Sapling");
        block("pomelo_fruiting_leaves", "Pomelo Leaves");
        block("chestnut_fruiting_leaves", "Chestnut Leaves");
        block("guava_fruiting_leaves", "Guava Leaves");
        block("hanging_pomelo", "Hanging Pomelo");
        block("hanging_chestnut", "Hanging Chestnut");
        block("hanging_guava", "Hanging Guava");
// 果食（果树果实的加工品）
        item("pomelo_jam", "Pomelo Jam");
        item("guava_jam", "Guava Jam");
        item("pomelo_icecream", "Pomelo Ice Cream");
        item("guava_icecream", "Guava Ice Cream");
        item("pomelo_popsicle", "Pomelo Popsicle");
        item("guava_popsicle", "Guava Popsicle");
        item("pomelo_juice", "Pomelo Juice");
        item("guava_juice", "Guava Juice");
// 板栗的加工品
        item("chestnut_flour", "Chestnut Flour");
        item("chestnut_dough", "Chestnut Dough");
        item("roasted_chestnut", "Roasted Chestnut");
        item("sugar_roasted_chestnut", "Sugar-Roasted Chestnut");
        item("chestnut_cake", "Chestnut Cake");
// 纺织系列
        item("silkworm_cocoon", "Silkworm Cocoon");
        item("raw_silkworm_chrysalis", "Raw Silkworm Chrysalis");
        item("silk", "Silk");
        block("sewing_machine", "Sewing Machine");
        item("blank_silk_hat", "Blank Silk Hat");
        item("blank_silk_tunic", "Blank Silk Tunic");
        item("blank_silk_leggings", "Blank Silk Leggings");
        item("blank_silk_boots", "Blank Silk Boots");
// 经济与生活
        block("atm", "Self-Service ATM");
        block("receipt_printer", "Receipt Printer");
        block("catering_business_license", "Catering Business License");
        block("shopping_computer", "Shopping Computer");
        block("purchasing_box", "Purchasing Box");
        item("credit_card", "Bank Card");
        item("seat_cushion", "Seat Cushion");
        item("money_1", "One-Yuan Note");
        item("money_5", "Five-Yuan Note");
        item("money_10", "Ten-Yuan Note");
        item("money_20", "Twenty-Yuan Note");
        item("money_50", "Fifty-Yuan Note");
        item("money_100", "One-Hundred-Yuan Note");
        item("phone", "Navigation Phone");
        item("takeaway_bag", "Packed Takeout");
// 状态效果
        add("effect." + MODID + ".spawning", "Spawning");
        add("effect." + MODID + ".pregnant", "Pregnant");
        add("effect." + MODID + ".fly_bite", "Fly Bite");
    }

// 物品提示

    private void addTooltips() {
        tooltip("harvester_spawn_egg", "§7§oPlace and ride to harvest nearby crops and collect drops. (Has its own inventory)");
        tooltip("jinkela", "§o§cWithout §eJinkela§c, how can you farm §acrops§c? §fWe §dneed §eJinkela§c!");

// 畜禽饲养
        tooltip("poultry_catching_net", "§7§oRight-click any animal (young or adult) to stuff it into the sack; right-click again to let it out.");
        tooltip("poultry_catching_net.stored", "§7§oInside: %s");
        tooltip("poultry_catching_net.weight", "§7§oWeight: %s kg");
        tooltip("milker", "§7§oRight-click an adult animal to milk it: cows give milk, goats give fresh goat milk, camels give camel milk, and untamed wolves give wild dog milk (the wolf is angered afterwards). §f2~5§7 bottles each time, costing §f1§7 mood.");
        tooltip("feeding_trough", "§7§oOne slot for feed (§fwheat§7, §fcarrot§7 or this mod's feeds) or §fTidy Water§7. Hungry or thirsty animals nearby will walk over and help themselves.");
        tooltip("river_silt", "§7§oLay it on the riverbed, then right-click with §fAquatic Feed§7 to turn it wet. Plant a sea cucumber, clam, oyster, scallop, river snail, crayfish, spoon worm, sea urchin, razor clam, mantis shrimp, jellyfish, Akiami shrimp or abalone; once grown, right-click to harvest three. Right-click with §fAquatic Feed§7 while it grows to speed it up.");
        tooltip("salt_pan", "§7§oPlace it by the water: when water touches its top it drinks the water above and turns wet; once dry, a separate §fSalt Pile§7 forms above it and grows from stage 1 to 3 on its own. Breaking the pile only drops salt (§f1 / 2 / 3§7 by stage, with Fortune bonus) - the pan itself is never destroyed.");
        tooltip("poop", "§7§oRight-click to pick up. Edible — fills you a little but leaves you queasy; sneak + right-click to throw it at someone.");
        tooltip("septic_tank", "§7§oTwo blocks wide. Gathers every §ffeces§7 within 8 blocks into the §6left half§7; stored feces slowly ferment into §aOrganic Fertilizer§7 in the right half. Right-click the left half to load or take feces (it also takes §fHigh-Protein Compound§7), the right half to take fertilizer.");
        tooltip("organic_fertilizer", "§7§oFermented out of feces. Acts exactly like §fbonemeal§7.");
        tooltip("sickle", "§7§oRight-click a crop to harvest every mature crop in a §f3×3§7 area and replant it. Benefits from §bFortune§7. Costs §f1§7 durability per crop.");
        tooltip("watering_can", "§7§oFill it with water, then hold right-click on crops to water and ripen them.");
        tooltip("harrow", "§7§oRight-click to till a §f3×3§7 patch of ground into farmland at once — it reaches ground a block higher or lower too, and clears away the grass and weeds on top. Costs §f1§7 durability.");
        tooltip("straw_hat", "§7§oWear it on your head. When you harvest a crop, it sometimes grants §bFortune +1§7.");
        tooltip("waterwheel", "§7§oStanding on a water source, it runs and turns into a §fthree-block-tall water source§7: it irrigates farmland and counts as water for anything else that needs it (§fother mods included§7), and an empty bucket can scoop water straight out of it. A splash and a trickle play whenever there is water below or in its own block.");
        tooltip("water_pipe", "§7§oPlace it down and it links up with any adjacent water pipe on its own, whatever the direction or how you build.");
        tooltip("water_pump", "§7§oBury it in water and every water pipe linked to it fills up. The open end of a pipe at the very edge of the network splashes water.");
        tooltip("rotating_water_pipe", "§7§oFits on the end of a water pipe network: once water reaches it, its arm spins and sprays water in a circle. It still works with §fone solid block§7 between it and the pipe.");
        tooltip("sector_water_pipe", "§7§oFits on the end of a water pipe network: once water reaches it, it sprays water in a fan over the ground ahead. It still works with §fone solid block§7 between it and the pipe.");
        tooltip("methane_tank", "§7§oStacks on top of the septic tank. Every time the tank ferments, a unit of §fPressurized Marsh Gas§7 is stored inside. Right-click to take some out.");
        tooltip("marsh_gas", "§7§oBurns like fuel (§f8 items§7), or right-click to throw — it blows up, hurting mobs but never blocks.");
        tooltip("pet_nest", "§7§oTamed cats and dogs nearby will walk over and settle here — one pet per nest. Should they pass away, they come back on it the next day.");
        tooltip("syringe", "§7§oAttack a farm animal to draw its genes into the needle; then attack an animal in §fgood shape§7 (satiety, thirst, cleanliness and mood all §f7§7 or more) to make it pregnant with §f3~5§7 young of that species — eight minutes later they are born right there.");
        tooltip("syringe_dna.source", "§7§oGenetic fluid of: §f%s");
        tooltip("livestock_brush", "§7§oRight-click any farm animal to brush it, slowly raising its §fmood§7 and §fcleanliness§7. It can only take one brushing every 20 seconds.");
        tooltip("stock_whip", "§7§oRight-click a farm animal to crack the whip, driving every animal of that kind within §f12 blocks§7 forward in a herd.");
        tooltip("whistle", "§7§oRight-click to blow: nearby wandering pets switch to following, and nearby tamed mounts make their way over to you.");
        tooltip("flea_treatment", "§7§oRight-click an animal to use one: cleanliness goes straight to full and the §fFly Bite§7 debuff is washed off.");
        tooltip("dosing_tube", "§7§oA fitting in the water pipe network: right-click with §fJinkela§7 to load one, and for §f5 minutes§7 every sprinkler in the network waters crops with a little random fertilizing.");
        tooltip("fish_net", "§7§oDrop it in water and it becomes a catching net: every so often an aquatic creature spawns right above it — saltwater fare in ocean biomes, freshwater elsewhere (§fother mods' fish included§7). Players can walk right through; animals cannot.");
        add("message." + MODID + ".syringe.not_healthy", "The animal is not in good enough shape to conceive");
        add("message." + MODID + ".pet_nest.mode_follow", "Now in Follow mode");
        add("message." + MODID + ".pet_nest.mode_sit", "Now in Sit mode");
        add("message." + MODID + ".pet_nest.mode_wander", "Now in Wander mode");
        add("message." + MODID + ".pet_nest.mode_need_nest", "It needs a Pet Nest to settle into first");
        add("message." + MODID + ".pet_nest.bound", "A pet has settled into this nest");
        add("message." + MODID + ".whistle.called", "§aWhistle blown: %s pet(s) and %s mount(s) answered");
        add("message." + MODID + ".whistle.none", "§eNo pets or mounts nearby answered the whistle");
// 死动物：用 flavor_immersed_daily 的斩骨刀击杀对应动物获得，挂到大挂钩上屠宰
        tooltip("dead_animal", "§7§oFinish the animal off with the §fBone Cutter Knife§7 to get its carcass. Hang it on the §fBig Hook§7, then carve it up stage by stage with the required knives.");
        add("message." + MODID + ".watering_can.empty", "Out of water — right-click a water source to refill!");
        add("message." + MODID + ".need_patchouli", "Reading the Nanshan Tutorial Scroll requires the Patchouli mod");
        add("tooltip." + MODID + ".developer_guide_book", "For modpack authors / server owners");
        add("tooltip." + MODID + ".developer_guide_book.only_creative", "Creative mode only");
        add("message." + MODID + ".need_parent", "The required mod \"flavor_immersed_daily\" is not installed: fruit trees cannot grow. Please install it before playing.");
// 父模组 JEI 的木盆漂洗说明写死了鸡的文案，这里覆盖成通用的「需先装纯净水」说明
        add("jei.flavor_immersed_daily.woodbasin_info.wash_desc", "How to use: right-click the basin with \u201cPurified Water\u201d to fill it (a dry basin processes nothing), then right-click it with the item to wash (silkworm cocoon, chicken, etc.); after a moment take out the result and extra drops");
// 蚕茧缫丝的补充说明：父模组的木盆页面只有输入→输出，看不出「必须先装纯净水」
        add("jei.flavor_immersed_daily_argo.basin_silk.info.1", "Reeling silk needs a water-filled wooden basin: right-click the basin with Purified Water first.");
        add("jei.flavor_immersed_daily_argo.basin_silk.info.2", "Then right-click that filled basin with Silkworm Cocoons \u2014 the whole stack is reeled at once, no waiting one by one.");
        add("jei.flavor_immersed_daily_argo.basin_silk.info.3", "Each cocoon yields one Silk and one Raw Silkworm Chrysalis, and the water in the basin is used up, so refill it next time.");
        tooltip("fishing_floating_platform", "§7§oHold right-click on water to inflate it into a §f5×5§7 platform that any creature can stand on. Hit it a few times to take it back.");
        tooltip("fishing_net", "§7§oHold it in either hand and right-click a fish to scoop it into the net (up to §f3§7). A caught fish goes into a nearby §fFish Keeper§7 first and only flies over when there is none. Right-click a §fFish Keeper§7 to empty the net in, or right-click a block to release the fish.");
        tooltip("fishing_net.stored", "§7§oInside: §f%s§7/3 fish");
        tooltip("fish_protection", "§7§oA net cage set in the water. Live fish caught nearby swim right into the cage; fish carried in the §fFishing Net§7 can be emptied in by right-clicking. Breaking the cage sets them free.");
        tooltip("fishing_box", "§7§oCatches without a living form (shrimp, crabs and the like) go into this box's §f36§7 slots; right-click with an empty hand to open it.");
        tooltip("rod_pod", "§7§oCast the hook first, then set the §fNanshan Fishing Rod§7 on it to keep watch. When a fish bites it alerts the §fnearest player§7, who can right-click to take the rod back and pick up the fight exactly where the hook was; it drops the rod after §f5 minutes§7 unattended.");
        tooltip("steelyard", "§7§oRight-click to open the weighing interface: put something on the pan and it is given a weight. Bigger fish weigh more, and a truly huge one is announced server-wide.");
        tooltip("fish_display_board", "§7§oA board to show off your catch. Right-click with any fish in hand to hang it up \u2014 the live model is drawn on the board; right-click with an empty hand to take it back. Point at it to see how much it weighs.");
        tooltip("fish_bowl", "§7§oA bottomless glass fish tank. Set it in water to hold that block of water; neighbouring tanks join up on their own \u2014 tile them sideways or stack them up.");
        tooltip("weight", "§7Weight: §f%s jin");
        add("message." + MODID + ".fishing_floating_platform.no_water", "Aim at the water surface first!");
        add("message." + MODID + ".mouse.spawn_on", "Mice can now spawn near crops");
        add("message." + MODID + ".mouse.spawn_off", "Mice will no longer spawn near crops");

// 林业生态
        tooltip("ranger_clipboard", "§7§oRight-click a log of a tree to mark a forestry station — one per chunk, and the chunk needs at least §f5§7 trees. Sneak + right-click to remove it. Right-click the air to open the station screen with every coordinate, score and status.");
        tooltip("tree_paint", "§7§oRight-click a tree of a station to wipe out an aphid infestation.");
        tooltip("fire_extinguisher", "§7§oRight-click to spray foam and put out fires around you; extinguishing a station's forest fire earns score. Costs §f1§7 durability each use.");
        tooltip("weather_instrument", "§7§oPlace it in a station chunk. The rotor turns slowly in calm weather, but spins fast once the chunk hits abnormal weather (fire, flood, drought, acid rain).");
        tooltip("herbicide", "§7§oRight-click to spray and clear the weeds around you; if the station in range suffers a weed outbreak, it is resolved and earns score.");
        tooltip("ranging_rope", "§7§oHold it to see the border of the chunk you are standing in, handy for sizing up a station.");
        tooltip("water_level_gauge", "§7§oRight-click to measure the station's water level. Normal level is §f60~66§7 m; flood or drought pushes it far off that range.");
        tooltip("water_quality_detector", "§7§oRight-click to measure the station's water quality: dissolved oxygen, pH, organic pollution, conductivity, turbidity and an overall grade. Detected water disasters are logged to the Ranger Clipboard.");
        tooltip("insect_incubator", "§7§oRight-click with an empty hand to open the interface. Four zones — feed, parents, carrier and offspring — where insects are raised much like crops.");
        tooltip("cardboard_honeycomb", "§7§oA honeycomb imitation made of cardboard, a material for insect breeding.");
        tooltip("insect_board", "§7§oA light board, a material for insect breeding.");
        tooltip("insect_net", "§7§oA net for catching insects. Sweep it through grass to catch cicadas, locusts, silkworms or bees.");
        tooltip("high_protein_substance", "§7§oA high-protein compound made from any insect roe or adult. Drop it into the septic tank to ferment it into §aOrganic Fertilizer§7.");
        tooltip("activated_carbon_particles", "§7§oRight-click on water to apply: adsorbs impurities to lower turbidity and balances conductivity.");
        tooltip("protocatechuic_acid", "§7§oRight-click on water to apply: neutralises alkalinity and lowers pH.");
        tooltip("oxyphylline", "§7§oRight-click on water to apply: neutralises acidity and raises pH.");
        tooltip("water_oxygen_machine", "§7§oRight-click on water (not consumed): balances dissolved oxygen to a suitable level.");
        tooltip("microbe_powder", "§7§oRight-click on water to apply: microbes break down organic pollution and lower the organic index.");
// 纺织系列
        tooltip("silk", "§7§oHold right-click briefly to spend one and mend a random piece of your armour that has taken damage.");
        tooltip("sewing_machine", "§7§oRight-click with a blank silk piece to load it onto the table, then right-click again to paint patterns and dress it with a real suit of armor.");
        tooltip("silk_armor_attached", "§7§oDressed with: §f%s");
        tooltip("silk_armor_preview", "§8Hold §7Shift§8 to preview the model");
// 经济与生活
        tooltip("atm", "§7§oPlace it down and right-click with a §fBank Card§7 to open that card's account screen: deposit the notes in its slots into the balance, withdraw them back by denomination, and change the card holder.");
        tooltip("receipt_printer", "§7§oRight-click empty-handed to see today's menu: §f40 foods§7 drawn from every mod, shown but untakeable. Flip the switch in the top-right corner to take orders — only then does the machine now and then spit out a receipt and call over a §fDelivery Rider§7. Hand him the right food to claim the reward.");
        tooltip("receipt_printer_blacklist", "§7Add a blacklist under the §fTakeout§7 section in the §fconfig§7 to keep certain foods out.");
        tooltip("seat_cushion", "§7§oRight-click a block to mark it as a seat customers can sit on. Right-click again, or break that block, to remove the mark.");
        tooltip("catering_business_license", "§7§oHang it on a wall. Right-click empty-handed to open or close for business. When open it calls over up to §f6 customers§7, who will find marked seats and place orders — hand them the right food to collect the reward.");
        tooltip("shopping_computer", "§7§oRight-click empty-handed to open: on the §fShopping§7 tab browse shops, add items to a cart and check out (put a §fBank Card§7 in the card slot to pay); on the §fMy Shop§7 tab create your own shop and list / delist / reprice / restock / withdraw.");
        tooltip("purchasing_box", "§7§oRight-click empty-handed to open a §f27-slot§7 box. Tip your economic produce in and press §fSell§7 to turn the lot into banknotes at today's buy price (per stack of 64); the notes drop straight back into the box's free slots, largest denominations first.");
        tooltip("credit_card", "§7§oRight-click to open its §f9 slots§7 for storing banknotes; take it to a §fSelf-Service ATM§7 to move money in or out of the balance.");
        tooltip("money_value", "§7Financial value: §f%s");
        tooltip("card_owner", "§7Holder: §f%s");
        tooltip("card_owner_none", "Unbound");
        tooltip("card_value", "§7Balance: §f%s");
        tooltip("nav_phone", "§7§oSneak + right-click to flip it open or shut; once open, right-click to browse takeout orders.");
        tooltip("nav_phone_open", "§7State: §aOpen");
        tooltip("nav_phone_closed", "§7State: §7Closed");
        tooltip("takeaway_bag_hint", "§7§oDeliver it to the glowing delivery point and right-click to hand it over.");
        tooltip("takeaway_bag_origin", "§7Pickup: §f%s, %s");
        tooltip("takeaway_bag_dest", "§7Drop-off: §f%s, %s");
        tooltip("takeaway_bag_reward", "§7Reward: §6%s");
        tooltip("takeaway_bag_time", "§7Time left: §f%s");
        add("message." + MODID + ".delivery.done", "Delivered! Earned %s yuan");
        add("message." + MODID + ".delivery.expired", "This order has already expired");
        add("message." + MODID + ".atm.need_card", "Hold a Bank Card to use the ATM");
        add("message." + MODID + ".rider.done", "Delivered! Earned %s yuan");
        add("message." + MODID + ".rider.remaining", "Wrong item — this rider will wait %s more");
        add("message." + MODID + ".customer.done", "Customer served! Earned %s yuan");
        add("message." + MODID + ".customer.remaining", "Wrong item — this customer will wait %s more");
        add("message." + MODID + ".license.open", "Opened for business");
        add("message." + MODID + ".license.closed", "Closed");
        add("message." + MODID + ".seat.marked", "Marked as a seat");
        add("message." + MODID + ".seat.unmarked", "Seat mark removed");
        add("message." + MODID + ".economy.command.added", "Added exchange: %s");
        add("message." + MODID + ".economy.command.removed", "Removed exchange: %s");
        add("message." + MODID + ".economy.command.add_failed", "That exchange entry already exists");
        add("message." + MODID + ".economy.command.remove_failed", "No such exchange entry");
        add("message." + MODID + ".economy.command.empty", "No exchange entries configured");
        add("message." + MODID + ".economy.command.list_header", "Exchange entries (%s):");
        add("message." + MODID + ".forestry.created", "Forestry station marked");
        add("message." + MODID + ".forestry.removed", "Removed the forestry station in this chunk");
        add("message." + MODID + ".forestry.none_here", "You have no forestry station in this chunk");
        add("message." + MODID + ".forestry.chunk_taken", "This chunk already has a station");
        add("message." + MODID + ".forestry.need_trees", "This chunk needs at least %s trees to host a station (only %s right now)");
        add("message." + MODID + ".forestry.new_event", "One of your forestry stations needs attention!");
        add("message." + MODID + ".forestry.decayed", "A neglected forestry station lost some score");
        add("message." + MODID + ".forestry.command.player_only", "This command can only be run by a player");
        add("message." + MODID + ".forestry.command.no_point", "No forestry station to trigger");
        add("message." + MODID + ".forestry.command.triggered", "Triggered on the forestry station: %s");
        add("message." + MODID + ".forestry.herbicide.cleared", "The herbicide cleared %s weeds");
        add("message." + MODID + ".forestry.herbicide.none", "No weeds to clear nearby");
        add("message." + MODID + ".forestry.water.no_point", "No forestry station of yours nearby");
        add("message." + MODID + ".forestry.gauge.level", "Current water level: %s m");
        add("message." + MODID + ".forestry.gauge.normal", "Water level is normal (60~66 m)");
        add("message." + MODID + ".forestry.gauge.flood", "Water level abnormally high — likely a flood!");
        add("message." + MODID + ".forestry.gauge.drought", "Water level abnormally low — likely a drought!");
        add("message." + MODID + ".forestry.detector.header", "Water quality at station %s, %s:");
        add("message." + MODID + ".forestry.detector.oxygen", "Dissolved oxygen: %s");
        add("message." + MODID + ".forestry.detector.ph", "pH: %s");
        add("message." + MODID + ".forestry.detector.organic", "Organic pollution: %s");
        add("message." + MODID + ".forestry.detector.conductivity", "Conductivity: %s");
        add("message." + MODID + ".forestry.detector.turbidity", "Turbidity: %s");
        add("message." + MODID + ".forestry.detector.comprehensive", "Overall water quality: %s (%s)");
        add("message." + MODID + ".forestry.detector.clean", "Water quality is good, no disaster found");
        add("message." + MODID + ".forestry.detector.reported", "Reported water disaster: %s");
        add("message." + MODID + ".forestry.treatment.turbidity", "Activated carbon applied: turbidity lowered to %s");
        add("message." + MODID + ".forestry.treatment.conductivity", "Activated carbon applied: conductivity balanced to %s");
        add("message." + MODID + ".forestry.treatment.ph_down", "Cicada slough acid applied: alkalinity lowered, pH is now %s");
        add("message." + MODID + ".forestry.treatment.ph_up", "Alkaloid applied: acidity lowered, pH is now %s");
        add("message." + MODID + ".forestry.treatment.oxygen", "Water oxygenator applied: dissolved oxygen balanced to %s");
        add("message." + MODID + ".forestry.treatment.organic", "Water microbe culture applied: organic pollution lowered to %s");
        add("message." + MODID + ".forestry.treatment.limit", "That water metric is already at its limit");

// 淡水生鲜
        tooltip("raw_grass_carp", "§7§oGrass carp, also known as white amur, is one of the four major Chinese carps.");
        tooltip("raw_tench", "§7§oTench, also called the doctor fish, is prized for its fine, tender meat.");
        tooltip("raw_mandarin_fish", "§7§oMandarin fish, also known as the peach blossom fish, has tender and delicate flesh and has been praised in verse since ancient times.");
        tooltip("raw_black_fish", "§7§oNorthern snakehead, also known as the black fish, is highly nutritious and has a smooth, slippery body.");
        tooltip("raw_rectum", "§7§oThe longsnout catfish, which originated in the Yangtze River basin of China, is also known as the \"river tuft.\"");
        tooltip("raw_crucian_carp", "§7§oCrucian carp, which grows in freshwater rivers, has many fine bones throughout its body and is well suited for making soup.");
        tooltip("raw_chinese_carp", "§7§oCarp is one of the four major freshwater fish species in China.");
        tooltip("raw_silver_carp", "§7§oSilver carp is a very common type of carp/pomfret.");
        tooltip("raw_bass", "§7§oCalifornia bass (or largemouth bass) is a bass species native to the U.S. state of California, and it is also a familiar sight in wet markets across China.");
        tooltip("raw_tilapia", "§7§oTilapia, native to Africa, is commonly known as Nile perch.");
        tooltip("raw_loach", "§7§oLoach is a type of freshwater fish found in rivers and streams in China. They are often kept as pets and are also eaten in Chinese cuisine.");
        tooltip("raw_catfish", "§7§oIn some Chinese regions, this specifically refers to the clarias catfish (walking catfish), but in general usage, it's a common nickname for catfish.");
        tooltip("raw_goby", "§7§oThe goby, commonly known as the \"climbing tiger,\" is a small bottom-dwelling fish that gives anglers headaches while making gourmands' mouths water.");
        tooltip("raw_black_carp", "§7§oThe black carp, also known as the \"king of the four great carp,\" is the largest of the four major carp species in China. It is renowned for its meaty flesh and is often used in Chinese cuisine.");
        tooltip("raw_redfin", "§7§oThe redfin, also known as the \"beardfish,\" is a freshwater fish native to China. It is prized for its meat and is often used in Chinese cuisine. It is also known for its distinctive red fins.");
        tooltip("raw_pleco", "§7§oThe pleco, also known as the \"armoured catfish\" or \"cleaner fish,\" is a freshwater fish native to South America, now a notorious invasive species in China.");
        tooltip("raw_bighead_carp", "§7§oThe bighead carp, also known as the \"king of the four great carp,\" is the largest of the four major carp species in China. It is renowned for its meaty flesh and is often used in Chinese cuisine.");
        tooltip("raw_hairy_crab", "§7§oAlso known as the hairy crab, it is famous for its two hair-covered claws and its plump, rich crab roe and crab paste in mid-autumn. It is the most iconic freshwater edible crab in China.");
        tooltip("raw_prawn", "§7§oAlso known as the Eastern prawn or jumbo shrimp, it is the most important economic shrimp species in northern China. It is large in size, thin-shelled, and meaty. It is named after the historical practice of counting them in pairs.");
        tooltip("raw_clam", "§7§oIts other names are flower clam and flower shell, and it is actually one of the four major traditional aquaculture mollusks in China, with extremely high economic value.");
        tooltip("raw_turtle", "§7§oCommonly known as the soft-shelled turtle, it is the most widely distributed and common freshwater turtle in China. Its tender meat and thick calipash make it a prized nourishing delicacy on the table.");
        tooltip("raw_mud_crab", "§7§oAlthough it belongs to the crab family, it is so common in its producing regions that it rivals rice and wheat — not a festival-only treat, but an everyday delicacy of daily life.");
        tooltip("raw_swimming_crab", "§7§oThe swimming crab is very common along the Chinese coast and has extremely high economic value. It is the most widely distributed and abundant marine crab species in Chinese waters.");
        tooltip("raw_river_snail", "§7§oIt can be seen along the banks of rivers and lakes, in ponds and water fields from south to north.");
        tooltip("raw_crayfish", "§7§oThe crayfish — native to North America, now one of the top-selling late-night seafood snacks in China with an annual production of over 30 million tons — is a notorious invasive species that has been fished so hard it now depends on artificial breeding to keep its population up.");

// 海产生鲜
        tooltip("raw_chinese_mackerel", "§7§oChinese mackerel, also known as blue mackerel, is a very common economic fish along the Pacific coast.");
        tooltip("raw_silvery_pomfret", "§7§oSilver pomfret, also known as silvery pomfret, is a very common economic fish along the Pacific coast.");
        tooltip("raw_hairtail", "§7§oRibbonfish live in the deep sea and swim to shallow waters at night to feed. They die immediately upon being brought to the surface due to the drastic pressure change, which makes them difficult to farm artificially, so they are almost entirely caught by wild fishing.");
        tooltip("raw_eel", "§7§oThe eel is a catadromous fish: it grows up in freshwater and, once mature, swims to the deep sea to spawn. Its rich, fatty flesh works equally well in Japanese kabayaki or Chinese braised dishes.");
        tooltip("raw_tuna", "§7§oTuna are warm-blooded, oceanic fish that swim continuously at high speed their entire lives to maintain respiration. Prized for their delicious meat and heavily fished, wild tuna stocks face significant pressure.");
        tooltip("raw_anglerfish", "§7§oThe anglerfish is named for the glowing lure on its head. A deep-sea ambush predator, its liver is prized as the \"foie gras of the sea\".");
        tooltip("raw_king_crab", "§7§oThe Alaska king crab is not a true crab but a distant relative of hermit crabs. Its long, meaty legs are a costly delicacy on the dining table.");
        tooltip("raw_lobster", "§7§oThe American lobster, commonly known as the Boston lobster, has a pair of unequally sized claws. Its firm, springy meat makes it a luxurious seafood in Western cuisine.");
        tooltip("raw_brown_crab", "§7§oThe brown crab, also known as the edible crab, is named for its round, plump shell that resembles a loaf of bread. Rich in crab roe, it is a common edible crab in Europe.");
        tooltip("raw_swordfish", "§7§oThe swordfish, also known as the broadbill, has a long, sword-like snout and is one of the fastest fish in the ocean. Its flesh is firm and almost boneless, making it a favorite for steaks and grilling.");
        tooltip("raw_pacific_saury", "§7§oThe Pacific saury is named for its slender, knife-like body and its peak season in autumn. A common coastal food fish, it grills up rich and fragrant.");
        tooltip("raw_sardine", "§7§oThe sardine, also known as the Japanese sillago, is a small schooling pelagic fish often canned or dried.");
        tooltip("raw_grouper", "§7§oThe tiger grouper is a kind of grouper named for the tiger-like markings on its body, with tender and delicious flesh.");
        tooltip("raw_japanese_halfbeak", "§7§oThe Japanese halfbeak has a needle-like extended lower jaw. It is a common small coastal food fish, often dried into snacks.");
        tooltip("raw_razor_clam", "§7§oThe razor clam has a long, bamboo-like shell and tender flesh, commonly found on tidal flats.");
        tooltip("raw_sea_cucumber", "§7§oThe sea cucumber is a prized sea delicacy, long honored as the \"ginseng of the sea\".");
        tooltip("raw_spoon_worm", "§7§oThe spoon worm, commonly known as the sea intestine, is named for its unusual shape and is a specialty seafood of the Jiaodong region.");
        tooltip("raw_sea_urchin", "§7§oThe sea urchin's orange-yellow gonads, known as \"uni\", are regarded as a delicacy of the sea.");
        tooltip("raw_sea_snail", "§7§oThe sea snail has a thick shell and firm flesh, and is a common seafood on coastal tables.");
        tooltip("raw_jellyfish", "§7§oOnce pickled, the jellyfish becomes the crisp \"jellyfish skin\" and \"jellyfish head\" served as a cold dish.");
        tooltip("raw_akiami_shrimp", "§7§oThe akiami shrimp is named for the fine hairs on its body. Tiny in size, it is the main ingredient of dried shrimp and shrimp paste.");
        tooltip("raw_mantis_shrimp", "§7§oThe mantis shrimp is fierce and aggressive, yet its meat is sweet and springy. It is also called the \"pissing shrimp\".");
        tooltip("raw_scallop", "§7§oThe Weihai scallop has a fan-shaped shell; its dried adductor muscle is the prized \"dried scallop\".");
        tooltip("raw_oyster", "§7§oThe Rushan oyster, known as the \"milk of the sea\", is plump and juicy, best eaten raw or charcoal-grilled.");
        tooltip("raw_mud_shrimp", "§7§oThe mud shrimp is not a shrimp, but a collective name for a mixture of marine plankton. It is fairly common in the waters off Fujian, China, and is extremely demanding about water quality.");
        tooltip("raw_abalone", "§7§oThe abalone ranks first among the \"eight treasures of the sea\", with firm, springy meat long regarded as a banquet delicacy.");
        tooltip("raw_squid", "§7§oSquid are soft-bodied cephalopods with eight arms and two long tentacles. Put the whole squid on a chopping board and cut it up for meat and tentacles.");
        tooltip("raw_squid_meat", "§7§oThe thickest cut of the squid, easy to score and quick to stir-fry.");
        tooltip("raw_squid_tentacle", "§7§oThe arms and tentacles of the squid, crisp and springy — best blanched and served cold.");

// 所有可放置食物（生鲜海产 / 可摆上桌的菜）共用的灰色提示行
        tooltip("placeable", "§7Can be placed");

// 南山钓竿改装参数
        tooltip("rod_hint", "§7Press §e[B]§7 to open the mod screen");
        tooltip("rod_section_parts", "Accessories");
        tooltip("rod_section_stats", "Rod Stats");
        tooltip("rod_critical", "Critical Durability");
        tooltip("rod_reel_tolerance", "Reel Tolerance");
        tooltip("rod_reel_actions", "Reel Actions");
        tooltip("rod_current_resistance", "Current Resistance");
        tooltip("rod_depth_range", "Depth Range");
        tooltip("rod_bait_type", "Bait Type");
        tooltip("bait_diet", "Attracts: ");
        tooltip("rod_max_tension", "Max Tension");
        tooltip("rod_conspicuousness", "Rig Conspicuousness");
        tooltip("rod_none", "None");
        tooltip("rod_unit_seconds", "s");
        tooltip("rod_unit_times", "x");
        tooltip("part_category", "Slot: ");
        tooltip("part_section", "Accessory Stats");
        tooltip("part_durability", "Durability");
        tooltip("part_tension", "Tension");
        tooltip("part_hardness", "Hardness");
        tooltip("part_stretch", "Stretch");
        tooltip("part_drag", "Drag");
        tooltip("part_reel_speed", "Reel Speed");
        tooltip("part_thickness", "Thickness");
        tooltip("part_weight", "Weight");
        tooltip("part_anti_current", "Anti-Current");
        tooltip("part_size", "Size");
        tooltip("part_set_depth", "Set Depth");
        tooltip("rod_medium", "Medium");
        tooltip("rod_no_part", "No accessory installed");
        add("msg." + MODID + ".part_worn_out", "§cAn accessory has worn out");
        add("msg." + MODID + ".line_broken", "§cThe line snapped!");
        add("msg." + MODID + ".body_broken", "§cThe rod body snapped!");
    }

// JEI 说明

    private void addJei() {
        jei("harvester_spawn_egg.description", "§7§oPlaces a Harvester. Right-click to ride and drive — it automatically harvests mature crops in a 7×7 area, replants seeds, and collects drops into its own inventory. Sneak + right-click to open the inventory.");
        jei("sewing", "Sewing Machine");
// 昆虫培育（JEI 分类与 EMI 配方页共用这套文案）
        jei("insect_breeding", "Insect Breeding");
        jei("insect_breeding.feed", "Feed");
        jei("insect_breeding.carrier", "Carrier");
        jei("insect_breeding.parents", "Parents");
        jei("insect_breeding.parents_empty", "Leave Empty");
        jei("insect_breeding.offspring", "Offspring");
        jei("insect_breeding.output", "Result");
        add("emi.category." + MODID + ".insect_breeding", "Insect Breeding");
// 捕虫（JEI 分类与 EMI 配方页共用这套文案）
        jei("insect_net", "Bug Catching");
        jei("insect_net.chance", "Chance %s%%");
        add("emi.category." + MODID + ".insect_net", "Bug Catching");
// 河道淤泥养殖（JEI 分类与 EMI 配方页共用这套文案）
        jei("river_silt_farming", "River Silt Farming");
        jei("river_silt_farming.input", "Seed Stock");
        jei("river_silt_farming.output", "Harvest");
        add("emi.category." + MODID + ".river_silt_farming", "River Silt Farming");
        jei("river_silt.info.1", "Lay River Silt on the riverbed (water must be above it) and right-click it with Aquatic Feed to turn it wet.");
        jei("river_silt.info.2", "Then right-click with a sea cucumber, clam, oyster, scallop, river snail, crayfish, spoon worm, sea urchin, razor clam, mantis shrimp, jellyfish, Akiami shrimp or abalone to plant it and let it grow.");
        jei("river_silt.info.3", "When mature it yields three; right-click to harvest, and the silt dries out - feed it again to start the next round.");
// fidworkblock 工作方块（EMI 侧由本模组补登记）
        add("emi.category." + MODID + ".cooking_mortar", "Millstone");
        add("emi.category." + MODID + ".cooking_squeezingmachine", "Juicer");
        add("emi.category." + MODID + ".cooking_steambox", "Steamer");
        add("emi.category." + MODID + ".cooking_wok", "Wok");
        add("emi.category." + MODID + ".cooking_pot", "Cooking Pot");
        add("emi.category." + MODID + ".cooking_jar", "Pickling Jar");
        add("emi.category." + MODID + ".cooking_pressurecooker", "Pressure Cooker");
        add("emi.category." + MODID + ".cooking_fryingpan", "Deep Fryer");
        add("emi.category." + MODID + ".cooking_choppingboard", "Cutting Board");
    }

// 界面与按键

    private void addScreenTexts() {
        add("key." + MODID + ".open_rod_mod", "Open Rod Mod");
        add("category." + MODID + ".fishing", "FIDA Fishing");
        gui("rod_mod", "Nanshan Fishing Rod Mod");
        gui("rod_name", "Custom name…");
        gui("rod_save", "Save & Confirm");
        gui("line", "Line");
        gui("body", "Rod Body");
        gui("reel", "Reel");
        gui("bait", "Bait");
        gui("floater", "Float");
        gui("sinker", "Sinker");
        gui("steelyard", "Steelyard");
        gui("steelyard_empty", "Put something on the pan to weigh");
        gui("steelyard_unweighable", "This cannot be weighed");
        gui("steelyard_weight", "Weight %s jin");
        add("msg." + MODID + ".heavy_catch", "§6%1$s §eweighed §6%2$s §e— a whopping §6%3$s jin§e!");
        add("msg." + MODID + ".rod_pod_bite", "§eThe rod on the pod is rattling — go grab it!");
        add("msg." + MODID + ".rod_pod_need_cast", "§eCast the hook first, then set the rod on it");
        gui("coop_hatch", "Hatching");
        gui("coop_collect", "Collected Eggs");
// 昆虫培育箱：四个分区标签
        gui("insect_incubator_feed", "Feed");
        gui("insect_incubator_carrier", "Carrier");
        gui("insect_incubator_parents", "Parents");
        gui("insect_incubator_offspring", "Offspring");
        gui("sewing_machine_page_armor", "Armor");
        gui("sewing_machine_page_paint", "Paint");
        gui("sewing_machine_armor", "Armor");
        gui("sewing_machine_silk", "Silk");
        gui("sewing_machine_dress", "Dress");
        gui("sewing_machine_undress", "Remove");
        gui("sewing_machine_paint", "Brush");
        gui("sewing_machine_erase", "Eraser");
        gui("sewing_machine_stamp", "Block");
        gui("sewing_machine_pick", "Pick");
        gui("sewing_machine_hide", "Hide / Show");
        gui("sewing_machine_undo", "Undo");
        gui("sewing_machine_save", "Save");
        gui("sewing_machine_take", "Take Out");
        gui("sewing_machine_no_garment", "Right-click with a blank silk piece to load it");
        gui("sewing_machine_hint", "Drag empty area to rotate · Scroll to zoom · Right-click to switch hidden face");
        gui("sewing_machine_hint_voxel", "L-click place voxel (1 silk) · R-click remove · Drag to build");
// 自助取款机（银行卡界面）
        gui("atm", "ATM");
// 准星落在小票打印机外卖小哥身上时的小提示
        gui("rider.order", "Deliver: %s × %s");
        gui("rider.reward", "Reward: %s yuan");
// 小票打印机界面右上角的出单开关
        gui("receipt_printer.switch_on", "Taking Orders: ON");
        gui("receipt_printer.switch_off", "Taking Orders: OFF");
        gui("bank_card", "Bank Card");
        gui("atm_owner", "Holder: %s");
        gui("atm_value", "Balance: %s");
        gui("atm_owner_hint", "Player ID");
        gui("atm_set_owner", "Set");
        gui("atm_deposit", "Deposit");
        gui("atm_withdraw_denom", "Withdraw ↓");
        gui("atm_tab_bank", "Bank");
        gui("atm_tab_exchange", "FX");
        gui("atm_exchange_empty", "No exchange entries configured");
        gui("atm_exchange_price", "§7Cost: §f%s");
// 导航手机接单界面
        gui("nav_phone_title", "Takeout Orders");
        gui("nav_phone_origin", "Pickup: %s, %s");
        gui("nav_phone_dest", "Drop-off: %s, %s");
        gui("nav_phone_reward", "¥%s");
        gui("nav_phone_empty", "No orders available right now");
        gui("nav_phone_accept", "Accept");
        gui("nav_phone_accepted", "Accepted");
        gui("nav_phone_cancel", "Cancel");
        gui("nav_phone_dist_you", "%s blocks away");
        gui("nav_phone_dist_route", "%s blocks total");
        gui("nav_phone_compass_distance", "Distance %s blocks");
        gui("nav_phone_hint", "Click an order to accept it; the takeout bag only appears after you accept. Click an accepted order to track it (click again to stop); accepted orders can be cancelled");
// 商业电脑界面
        gui("market", "Shopping Computer");
        gui("market.news", "News");
        gui("market.shopping", "Shopping");
        gui("market.shop", "My Shop");
        gui("market.prices", "Prices");
        gui("market.prices_title", "Today's prices by weight");
        gui("market.price_animals", "Livestock");
        gui("market.price_fish", "Raw Fish");
        gui("market.price_base", "Standard");
        gui("market.price_per_kg", "Per extra kg");
        gui("market.price_per_jin", "Per jin");
        gui("market.price_empty", "Nothing is bought by weight.");
        gui("market.back", "← Back");
        gui("market.news_placeholder", "No news today.");
        gui("market.news_title", "Today's Economic Products (per stack)");
        gui("market.news_up", "▲ %s/stack");
        gui("market.news_down", "▼ %s/stack");
        gui("purchasing_box.sell", "Sell");
        gui("market.cart_total", "Total: ¥%s");
        gui("market.cart_clear", "Clear Cart");
        gui("market.checkout", "Check Out");
        gui("market.in_stock", "Stock %s");
        gui("market.infinite_stock", "In Stock");
        gui("market.create_shop", "Create Shop");
        gui("market.delete_shop", "Delete Shop");
        gui("market.pick_shop_hint", "Click a shop on the left to browse");
        gui("market.drag_item_hint", "Hold an item in hand, then click \"List Held Item\"");
        gui("market.choose_card", "Choose a Bank Card to Pay");
        gui("market.choose_card_withdraw", "Choose a Bank Card to Withdraw");
        gui("market.cancel", "Cancel");
        gui("market.no_card", "No bank card in your inventory");
        gui("market.card_anonymous", "Bearer Card");
        gui("market.list_item", "List Held Item");
        gui("market.delist", "Delist Selected");
        gui("market.set_price", "Set Selected Price");
        gui("market.restock", "Restock (Held Item)");
        gui("market.withdraw", "Withdraw to Card");
        gui("market.shop_name_hint", "Type a shop name, then click \"Create Shop\"");
        gui("market.price_hint", "Price");
        gui("market.balance", "Revenue: ¥%s");
        gui("shop.config_shop", "Municipal Shop");
        add("message." + MODID + ".shop.created", "Shop created");
        add("message." + MODID + ".shop.deleted", "Shop deleted");
        add("message." + MODID + ".shop.listed", "Item listed");
        add("message." + MODID + ".shop.delisted", "Item delisted");
        add("message." + MODID + ".shop.repriced", "Price updated");
        add("message." + MODID + ".shop.restocked", "Restocked");
        add("message." + MODID + ".shop.withdrawn", "Withdrawn to bank card");
        add("message." + MODID + ".shop.no_card", "No usable bank card found");
        add("message." + MODID + ".shop.no_goods", "You are not holding any item");
        add("message." + MODID + ".shop.checked_out", "Paid ¥%s");
        add("message." + MODID + ".shop.cart_empty", "The cart is empty");
        add("message." + MODID + ".shop.insufficient", "Not enough money on the card");
        add("message." + MODID + ".shop.out_of_stock", "Not enough stock");
        add("message." + MODID + ".shop.limit_reached", "You have reached the shop creation limit");
        add("message." + MODID + ".shop.disabled", "Player shops are disabled on this server");
        add("message." + MODID + ".shop.not_owner", "You do not own this shop");
// 准星落在畜禽饲养方块上时的小提示
        gui("septic_tank.poop", "§6Left half §7· §fFermentables §e%s");
        gui("septic_tank.fertilizer", "§aRight half §7· §fOrganic Fertilizer §e%s");
        gui("trough.empty", "§7Feeding Trough §7· §fEmpty");
        gui("trough.food", "§7Feeding Trough §7· §fFeed §e%s");
        gui("trough.water", "§7Feeding Trough §7· §fWater §e%s");
        gui("methane_tank.gas", "§7Methane Tank §7· §fMarsh Gas §e%s");
// 准星落在钓鱼展示架上时的小提示：挂着的那条与重量
        gui("fish_display_board.weight", "%1$s §7· §f%2$s jin");

// 无人机遥控器：操作界面与飞行按键
        add("category." + MODID + ".uav", "FIDA Drone");
        add("key." + MODID + ".uav_forward", "Drone: Forward");
        add("key." + MODID + ".uav_back", "Drone: Back");
        add("key." + MODID + ".uav_left", "Drone: Strafe Left");
        add("key." + MODID + ".uav_right", "Drone: Strafe Right");
        add("key." + MODID + ".uav_up", "Drone: Ascend");
        add("key." + MODID + ".uav_down", "Drone: Descend");
        add("key." + MODID + ".uav_exit", "Drone: Exit Pilot View");
        gui("uav.title", "Drone Controller");
        gui("uav.unbind", "Unbind Drone");
        gui("uav.operate", "First-Person Piloting");
        gui("uav.power_on", "Power: ON");
        gui("uav.power_off", "Power: OFF");
        gui("uav.inventory", "Drone Items…");
        gui("uav.inventory_title", "Drone Items");
        gui("uav.slot.module", "Module");
        gui("uav.slot.use", "In Use");
        gui("uav.slot.backpack", "Backpack");
        add("message." + MODID + ".uav.bound", "Drone bound. Right-click anywhere with the controller to open the control panel.");
        add("message." + MODID + ".uav.bound_other", "This drone is already bound to someone else.");
        add("message." + MODID + ".uav_module_range.point_a", "Corner A recorded (x, z): %s. Right-click another block for corner B — height does not matter.");
        add("message." + MODID + ".uav_module_range.point_b", "Corner B recorded (x, z): %s. Both corners are set — put the module into the drone's module slot to start.");
        add("message." + MODID + ".uav_module_range.cleared", "The recorded corners on the module have been wiped.");

// 护林员写字板：网点界面
        gui("forestry.title", "Forestry Stations");
        gui("forestry.points", "Stations");
        gui("forestry.empty", "No station marked yet");
        gui("forestry.hint", "Right-click a log of a tree with the clipboard to mark a station; sneak + right-click to remove.");
        gui("forestry.detail", "Station Details");
        gui("forestry.dimension", "Dimension");
        gui("forestry.pos", "Position");
        gui("forestry.score", "Score");
        gui("forestry.status", "Status");
        gui("forestry.remove", "Remove Station");
        gui("forestry.bonus", "Prosperity Bonus");
        gui("forestry.bonus_text", "Faster crops · Happier animals · More fishing treasure · Doubled drops · Cheaper trades · Mining fortune");
        gui("forestry.none", "No station selected");
        gui("forestry.event.none", "Calm");
        gui("forestry.event.fire", "Forest Fire");
        gui("forestry.event.aphids", "Aphid Infestation");
        gui("forestry.event.weeds", "Weed Outbreak");
        gui("forestry.event.flood", "Flood");
        gui("forestry.event.drought", "Drought");
        gui("forestry.event.hypoxia", "Water Hypoxia");
        gui("forestry.event.oxygen_over", "Oxygen Supersaturation");
        gui("forestry.event.acid_rain", "Acid Rain");
        gui("forestry.event.strong_alkali", "Strong Alkalinity");
        gui("forestry.event.barren_water", "Barren Water");
        gui("forestry.event.stench", "Foul Water");
        gui("forestry.event.osmosis", "Osmotic Imbalance");
        gui("forestry.water_status", "Water Condition");
        gui("forestry.water.normal", "Normal");
        gui("forestry.name", "Name");
        gui("forestry.default_name", "Station %s, %s");
        gui("forestry.rename", "Rename");
        gui("forestry.rename.title", "Name This Station");
    }

// 钓鱼手册

    private void addBookTexts() {
        book("title", "Nanshan Rod Catch Codex");
        book("filter.habitat", "Water");
        book("filter.diet", "Diet");
        book("filter.activity", "Activity");
        book("filter.alertness", "Alertness");
        book("filter.feeding", "Feeding Speed");
        book("habitat.fresh", "Freshwater");
        book("habitat.ocean", "Saltwater");
        book("habitat.both", "Fresh & Saltwater");
        book("diet.omnivore", "Omnivore");
        book("diet.herbivore", "Herbivore");
        book("diet.carnivore", "Carnivore");
        book("diet.scavenger", "Scavenger");
        book("diet.live", "Live Prey");
        book("activity.all", "All Day");
        book("activity.day", "Day");
        book("activity.night", "Night");
        book("activity.sunny", "Sunny");
        book("activity.rain", "Rain");
        book("activity.all_time", "All time, all weather");
        book("alertness.low", "Low");
        book("alertness.medium", "Medium");
        book("alertness.high", "High");
        book("feeding.slow", "Slow");
        book("feeding.medium", "Medium");
        book("feeding.fast", "Fast");
        book("attr.habitat", "Habitat");
        book("attr.depth", "Depth Range");
        book("attr.diet", "Diet");
        book("attr.tension", "Struggle Tension");
        book("attr.alertness", "Alertness");
        book("attr.feeding", "Feeding Speed");
        book("attr.activity", "Active Period");
        book("attr.caught", "Caught Status");
        book("caught.yes", "Caught");
        book("caught.no", "Not caught yet");
        book("desc.hairy_crab", "The Chinese mitten crab is omnivorous, forages at night, and migrates between freshwater and estuaries.");
        book("desc.prawn", "The Chinese white shrimp lives on nearshore muddy seabeds and is active at night.");
        book("depth.unlimited", "Unlimited");
        book("hint_none", "← Select a catch on the left");
        book("empty", "No catch matches the filters (%s entries in codex)");
// 帕秋莉手册首页（book.json 的 landing_text）
        add("patchouli." + MODID + ".guide.landing",
                "East of the fence, south of the mountain — where the plough turns the soil and the rod rests over the water. This scroll gathers three chapters: Farming, Livestock, and Fishing. Open the table of contents and pick one to begin.");
        add("patchouli." + MODID + ".developer.landing",
                "This volume is written for modpack authors and server owners: every config option, every data-pack JSON you can customise, the code behind each mechanic, and step-by-step tutorials for changing them. Back up your world and data folder before you begin.");
    }

// 键前缀辅助方法

    private void entity(String path, String name) {
        add("entity." + MODID + "." + path, name);
    }

    private void item(String path, String name) {
        add("item." + MODID + "." + path, name);
    }

    private void block(String path, String name) {
        add("block." + MODID + "." + path, name);
    }

    private void tooltip(String path, String text) {
        add("tooltip." + MODID + "." + path, text);
    }

    private void gui(String path, String text) {
        add("gui." + MODID + "." + path, text);
    }

    private void book(String path, String text) {
        add("book." + MODID + "." + path, text);
    }

    private void jei(String path, String text) {
        add("jei." + MODID + "." + path, text);
    }
}
