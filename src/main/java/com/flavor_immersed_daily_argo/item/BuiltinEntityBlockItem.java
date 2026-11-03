package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.client.block.BraisedPrawnsItemRenderer;
import com.flavor_immersed_daily_argo.client.block.DonkeyFeastItemRenderer;
import com.flavor_immersed_daily_argo.client.block.PotStewGooseItemRenderer;
import com.flavor_immersed_daily_argo.client.block.RawSwordfishItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

// 物品模型父模型是 builtin/entity 的方块物品。
//
// 1.20.1 没有 NeoForge 的 RegisterClientExtensionsEvent，客户端扩展只能由物品类自己
// 在 initializeClient 里交出去；这里按 rendererKind 选一个复用方块实体渲染器模型的自定义渲染器。
public class BuiltinEntityBlockItem extends BlockItem {

    private final String rendererKind;
    @Nullable
    private final String tooltipKey;

    public BuiltinEntityBlockItem(Block block, Properties properties, String rendererKind) {
        this(block, properties, null, rendererKind);
    }

    public BuiltinEntityBlockItem(Block block, Properties properties, @Nullable String tooltipKey, String rendererKind) {
        super(block, properties);
        this.rendererKind = rendererKind;
        this.tooltipKey = tooltipKey;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    Minecraft minecraft = Minecraft.getInstance();
// 这里必须现读外层字段：Forge 可能在物品构造完成前就调用 initializeClient，
// 提前捕获 rendererKind 会拿到 null，渲染时直接崩（渲染掉落的 raw_swordfish 就会触发）
                    String kind = BuiltinEntityBlockItem.this.rendererKind;
                    if ("swordfish".equals(kind)) {
                        this.renderer = new RawSwordfishItemRenderer(
                                minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                    } else if ("pot_stew_goose".equals(kind)) {
                        this.renderer = new PotStewGooseItemRenderer(
                                minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                    } else if ("donkey_feast".equals(kind)) {
                        this.renderer = new DonkeyFeastItemRenderer(
                                minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                    } else {
                        this.renderer = new BraisedPrawnsItemRenderer(
                                minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                    }
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (this.tooltipKey != null) {
            tooltip.add(Component.translatable(this.tooltipKey));
        }
    }
}
