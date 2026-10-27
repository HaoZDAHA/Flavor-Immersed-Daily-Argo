package com.flavor_immersed_daily_argo.all;

import com.flavor_immersed_daily_argo.economy.AtmMenu;
import com.flavor_immersed_daily_argo.economy.MarketMenu;
import com.flavor_immersed_daily_argo.economy.ReceiptPrinterMenu;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.fishing.FishingBoxMenu;
import com.flavor_immersed_daily_argo.fishing.FishingRodModMenu;
import com.flavor_immersed_daily_argo.fishing.SteelyardMenu;
import com.flavor_immersed_daily_argo.forestry.InsectIncubatorMenu;
import com.flavor_immersed_daily_argo.herding.ChickenCoopMenu;
import com.flavor_immersed_daily_argo.herding.PurchasingBoxMenu;
import com.flavor_immersed_daily_argo.textile.SewingMachineMenu;
import com.flavor_immersed_daily_argo.uav.UavMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 菜单类型注册
public final class ModMenus {

    private ModMenus() {
    }

    public static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(Registries.MENU, flavor_immersed_daily_argo.MODID);

// 钓竿改装菜单
    public static final DeferredHolder<MenuType<?>, MenuType<FishingRodModMenu>> ROD_MOD_MENU =
            REGISTRY.register("rod_mod_menu", () -> IMenuTypeExtension.create(FishingRodModMenu::new));

// 鸡窝菜单
    public static final DeferredHolder<MenuType<?>, MenuType<ChickenCoopMenu>> CHICKEN_COOP_MENU =
            REGISTRY.register("chicken_coop_menu", () -> IMenuTypeExtension.create(ChickenCoopMenu::new));

// 无人机物品栏菜单
    public static final DeferredHolder<MenuType<?>, MenuType<UavMenu>> UAV_MENU =
            REGISTRY.register("uav_menu", () -> IMenuTypeExtension.create(UavMenu::new));

// 昆虫培育箱菜单
    public static final DeferredHolder<MenuType<?>, MenuType<InsectIncubatorMenu>> INSECT_INCUBATOR_MENU =
            REGISTRY.register("insect_incubator_menu", () -> IMenuTypeExtension.create(InsectIncubatorMenu::new));

// 钓鱼箱菜单
    public static final DeferredHolder<MenuType<?>, MenuType<FishingBoxMenu>> FISHING_BOX_MENU =
            REGISTRY.register("fishing_box_menu", () -> IMenuTypeExtension.create(FishingBoxMenu::new));

// 收购箱菜单
    public static final DeferredHolder<MenuType<?>, MenuType<PurchasingBoxMenu>> PURCHASING_BOX_MENU =
            REGISTRY.register("purchasing_box_menu", () -> IMenuTypeExtension.create(PurchasingBoxMenu::new));

// 杆秤菜单
    public static final DeferredHolder<MenuType<?>, MenuType<SteelyardMenu>> STEELYARD_MENU =
            REGISTRY.register("steelyard_menu", () -> IMenuTypeExtension.create(SteelyardMenu::new));

// 缝纫机菜单
    public static final DeferredHolder<MenuType<?>, MenuType<SewingMachineMenu>> SEWING_MACHINE_MENU =
            REGISTRY.register("sewing_machine_menu", () -> IMenuTypeExtension.create(SewingMachineMenu::new));

// 自助取款机（银行卡）菜单
    public static final DeferredHolder<MenuType<?>, MenuType<AtmMenu>> ATM_MENU =
            REGISTRY.register("atm_menu", () -> IMenuTypeExtension.create(AtmMenu::new));

// 商业电脑（市场）菜单
    public static final DeferredHolder<MenuType<?>, MenuType<MarketMenu>> MARKET_MENU =
            REGISTRY.register("market_menu", () -> IMenuTypeExtension.create(MarketMenu::new));

// 小票打印机菜单
    public static final DeferredHolder<MenuType<?>, MenuType<ReceiptPrinterMenu>> RECEIPT_PRINTER_MENU =
            REGISTRY.register("receipt_printer_menu", () -> IMenuTypeExtension.create(ReceiptPrinterMenu::new));

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
