package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.common.item.PatchouliDataComponents;

import java.util.List;

// 帕秋莉手册物品：南山-教程卷轴（guide_book）与南山开发者指导手册（developer_guide_book）。
//
// 以帕秋莉手册（Patchouli）为可选依赖：已安装时物品携带 patchouli:book 组件并打开对应手册；
// 未安装时游戏照常运行，右键只会提示缺少帕秋莉手册。
public class GuideBookItem extends Item {

// 对应 data/flavor_immersed_daily_argo/patchouli_books/guide
    public static final ResourceLocation BOOK_ID =
            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "guide");

// 对应 data/flavor_immersed_daily_argo/patchouli_books/developer
    public static final ResourceLocation DEVELOPER_BOOK_ID =
            ResourceLocation.fromNamespaceAndPath(flavor_immersed_daily_argo.MODID, "developer");

// 是否已安装帕秋莉手册
    private static final boolean HAS_PATCHOULI = ModList.get().isLoaded("patchouli");

// 帕秋莉的书绑定组件，仅在已安装时引用，避免未安装时触发类加载崩溃
    @Nullable
    private static final DataComponentType<ResourceLocation> BOOK_COMPONENT = HAS_PATCHOULI
            ? PatchouliDataComponents.BOOK
            : null;

// 这本书打开哪一本手册
    private final ResourceLocation bookId;

// 教程卷轴（默认那本 guide）
    public GuideBookItem() {
        this(BOOK_ID);
    }

    public GuideBookItem(ResourceLocation bookId) {
        super(buildProperties(bookId));
        this.bookId = bookId;
    }

    private static Item.Properties buildProperties(ResourceLocation bookId) {
        Item.Properties props = new Item.Properties().stacksTo(1);
        if (BOOK_COMPONENT != null) {
            props.component(BOOK_COMPONENT, bookId);
        }
        return props;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!HAS_PATCHOULI) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.flavor_immersed_daily_argo.need_patchouli"), true);
            }
            return InteractionResultHolder.success(stack);
        }
// 与 Patchouli 自带的 ItemModBook 行为一致：仅在服务端发送打开手册的封包
        if (player instanceof ServerPlayer serverPlayer) {
            PatchouliAPI.get().openBookGUI(serverPlayer, this.bookId);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (DEVELOPER_BOOK_ID.equals(this.bookId)) {
            tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.developer_guide_book"));
            tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.developer_guide_book.only_creative"));
        }
    }
}
