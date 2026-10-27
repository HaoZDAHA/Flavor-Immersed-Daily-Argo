package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.fishing.FishingRodPartData;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Locale;

// 南山钓竿配件。
//
// 数值来自数据包 data/<命名空间>/fishing_rod_parts/*.json，tooltip 只显示该配件真正
// 定义了的系数，方便玩家判断这个配件能改哪几项钓竿参数。耐久未在数据包中定义时回落到物品自身耐久。
//
// 第一行标出配件类别（鱼线 / 竿体 / 线轮 / 浮漂 / 铅坠），玩家一眼就知道该装进哪个槽。
public class FishingRodPartItem extends Item {

// 类别名沿用界面上六个槽位的文案，tooltip 与改装界面用词一致
    private static final String SLOT_LANG_PREFIX = "gui.flavor_immersed_daily_argo.";

    public FishingRodPartItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        FishingRodPartData.PartStats stats = FishingRodPartData.getInstance().statsOf(stack);

        addCategory(tooltipComponents, stack);

        tooltipComponents.add(Component.literal("▎").withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("tooltip.flavor_immersed_daily_argo.part_section")
                        .withStyle(ChatFormatting.GOLD)));

// 耐久：数据包未定义时用物品自身耐久兜底
        double durability = stats.durability() > 0 ? stats.durability() : stack.getMaxDamage();
        addAttr(tooltipComponents, "part_durability", trim(durability));
        addAttr(tooltipComponents, "part_tension", stats.tension());
        addAttr(tooltipComponents, "part_hardness", stats.hardness());
        addAttr(tooltipComponents, "part_stretch", stats.stretch());
        addAttr(tooltipComponents, "part_drag", stats.drag());
        addAttr(tooltipComponents, "part_reel_speed", stats.reelSpeed());
        addAttr(tooltipComponents, "part_thickness", stats.thickness());
        addAttr(tooltipComponents, "part_weight", stats.weight());
        addAttr(tooltipComponents, "part_anti_current", stats.antiCurrent());
        addAttr(tooltipComponents, "part_size", stats.size());
        addAttr(tooltipComponents, "part_set_depth", stats.setDepth());
    }

// 只在系数不为 0 时追加一行：灰色标签对齐 + 白色数值
    private static void addAttr(List<Component> tooltip, String key, double value) {
        if (value == 0.0D) {
            return;
        }
        addAttr(tooltip, key, trim(value));
    }

    private static void addAttr(List<Component> tooltip, String key, String value) {
        Component label = Component.translatable("tooltip.flavor_immersed_daily_argo." + key);
        tooltip.add(Component.literal(" ").withStyle(ChatFormatting.RESET)
                .append(Component.literal(pad(label.getString(), 5)).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE)));
    }

// 用全角空格补齐到指定全角字数，让后面的数值对齐
    private static String pad(String text, int width) {
        return text.length() >= width ? text + " " : text + "\u3000".repeat(width - text.length());
    }

// 去掉整数的小数尾巴：80.0 → 80，1.5 → 1.5
    private static String trim(double value) {
        return value == Math.floor(value) && !Double.isInfinite(value)
                ? String.valueOf((long) value)
                : String.format(Locale.ROOT, "%.1f", value);
    }

// 追加「类别：X」一行；名字里认不出槽位时不画
    private static void addCategory(List<Component> tooltip, ItemStack stack) {
        String slotKey = slotKeyOf(stack);
        if (slotKey == null) {
            return;
        }
        tooltip.add(Component.literal(" ").withStyle(ChatFormatting.RESET)
                .append(Component.translatable("tooltip.flavor_immersed_daily_argo.part_category")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.translatable(SLOT_LANG_PREFIX + slotKey).withStyle(ChatFormatting.GOLD)));
    }

// 由注册名前缀推出该配件对应的槽位文案键。
//
// 注册名已经按 rod_ / reel_ / line_ / float_ / sinker_ 分类，与六个改装槽一一对应，
// 这样新加配件只要沿用命名就能自动得到正确的类别 tooltip。
    private static String slotKeyOf(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = id == null ? "" : id.getPath();
        int split = path.indexOf('_');
        String prefix = split > 0 ? path.substring(0, split) : path;
        return switch (prefix) {
            case "rod" -> "body";
            case "reel" -> "reel";
            case "line" -> "line";
            case "float" -> "floater";
            case "sinker" -> "sinker";
            default -> null;
        };
    }
}
