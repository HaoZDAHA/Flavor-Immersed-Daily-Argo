package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
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
import net.minecraftforge.fml.ModList;

import java.util.List;

// 帕秋莉手册物品：南山-教程卷轴（guide_book）与南山开发者指导手册（developer_guide_book）。
//
// 以帕秋莉手册（Patchouli）为可选依赖：已安装时物品携带 patchouli:book 组件并打开对应手册；
// 未安装时游戏照常运行，右键只会提示缺少帕秋莉手册。
public class GuideBookItem extends Item {

// 对应 data/flavor_immersed_daily_argo/patchouli_books/guide
    public static final ResourceLocation BOOK_ID =
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "guide");

// 对应 data/flavor_immersed_daily_argo/patchouli_books/developer
    public static final ResourceLocation DEVELOPER_BOOK_ID =
            new ResourceLocation(flavor_immersed_daily_argo.MODID, "developer");

// 是否已安装帕秋莉手册
    private static final boolean HAS_PATCHOULI = ModList.get().isLoaded("patchouli");

// 帕秋莉的书绑定组件：1.20.1 没有数据组件，由 Patchouli 的物品自行处理，这里不再设置。

// 这本书打开哪一本手册
    private final ResourceLocation bookId;

// 教程卷轴（默认那本 guide）
    public GuideBookItem() {
        this(BOOK_ID);
    }

    public GuideBookItem(ResourceLocation bookId) {
        super(new Item.Properties().stacksTo(1));
        this.bookId = bookId;
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
// 与 Patchouli 自带的 ItemModBook 行为一致：仅在服务端发送打开手册的封包。
// Patchouli 是可选依赖，编译期不引用它的类，运行时用反射调用。
        if (player instanceof ServerPlayer serverPlayer) {
            openBook(serverPlayer, this.bookId);
        }
        return InteractionResultHolder.success(stack);
    }

    private static void openBook(ServerPlayer player, ResourceLocation bookId) {
        try {
            Class<?> apiClass = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            Object api = apiClass.getMethod("get").invoke(null);
            apiClass.getMethod("openBookGUI", ServerPlayer.class, ResourceLocation.class).invoke(api, player, bookId);
        } catch (ReflectiveOperationException ignored) {
// 没装 Patchouli 或版本不匹配时静默跳过
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (DEVELOPER_BOOK_ID.equals(this.bookId)) {
            tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.developer_guide_book"));
            tooltip.add(Component.translatable("tooltip.flavor_immersed_daily_argo.developer_guide_book.only_creative"));
        }
    }
}
