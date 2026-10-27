package com.flavor_immersed_daily_argo.forestry;

import com.flavor_immersed_daily_argo.all.ModBlockEntities;
import com.flavor_immersed_daily_argo.recipe.InsectBreedingInput;
import com.flavor_immersed_daily_argo.recipe.InsectBreedingRecipe;
import com.flavor_immersed_daily_argo.recipe.ModRecipes;
import com.flavor_immersed_daily_argo.util.InventoryNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 昆虫培育箱方块实体。
//
// 内部分成四个分区：饲料（1 格，每次转化消耗一份）、父代（2 格，每次转化消耗其中一只）、载体（1 格，不消耗）、
// 子代（6 格）。子代格既能放东西也能取东西——把要培育的昆虫（如蚕卵 / 蚕）放进去，
// 匹配到配方后按作物生长那样累积进度，进度跑满就地转成产物。
public class InsectIncubatorBlockEntity extends BlockEntity implements MenuProvider {

// 饲料槽：每次转化消耗一份
    public static final int FEED_SLOT = 0;
// 父代槽：两个
    public static final int PARENT_SLOT_1 = 1;
    public static final int PARENT_SLOT_2 = 2;
// 载体槽：不消耗
    public static final int CARRIER_SLOT = 3;
// 子代槽：起始下标与数量（原来只出不进的产出槽，现在也能放东西）
    public static final int OFFSPRING_START = 4;
    public static final int OFFSPRING_SLOTS = 6;
// 槽位总数
    public static final int SLOT_COUNT = OFFSPRING_START + OFFSPRING_SLOTS;

    private static final String INVENTORY_TAG = "Inventory";
    private static final String PROGRESS_TAG = "Progress";

// 四个分区的内容；玩家改动后写回方块实体，存档才能拿走
    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            InsectIncubatorBlockEntity.this.setChanged();
        }
    };

// 培育进度：0 = 当前进度，1 = 本次转化总时长（同步给客户端画进度条）
    private int progress;
    private int maxProgress;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? InsectIncubatorBlockEntity.this.progress : InsectIncubatorBlockEntity.this.maxProgress;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                InsectIncubatorBlockEntity.this.progress = value;
            } else {
                InsectIncubatorBlockEntity.this.maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public InsectIncubatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INSECT_INCUBATOR_ENTITY.get(), pos, state);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public ContainerData getData() {
        return this.data;
    }

// 培育逻辑

// 每刻推进一次：匹配到配方就累积进度，进度跑满就地转化（表现上类似作物生长）
    public static void serverTick(Level level, BlockPos pos, BlockState state, InsectIncubatorBlockEntity incubator) {
        Optional<InsectBreedingRecipe> match = level.getRecipeManager()
                .getRecipeFor(ModRecipes.INSECT_BREEDING_TYPE.get(), incubator.buildInput(), level);
        if (match.isEmpty()) {
            if (incubator.progress != 0) {
                incubator.progress = 0;
                incubator.setChanged();
            }
            return;
        }
        InsectBreedingRecipe recipe = match.get();
        if (incubator.maxProgress != recipe.getTime()) {
// 换配方了，进度从头开始
            incubator.maxProgress = recipe.getTime();
            incubator.progress = 0;
        }
        incubator.progress++;
        if (incubator.progress >= incubator.maxProgress) {
            incubator.craft(recipe);
            incubator.progress = 0;
        }
        incubator.setChanged();
    }

    private InsectBreedingInput buildInput() {
        List<ItemStack> offspring = new ArrayList<>(OFFSPRING_SLOTS);
        for (int i = 0; i < OFFSPRING_SLOTS; i++) {
            offspring.add(this.inventory.getItem(OFFSPRING_START + i));
        }
        return new InsectBreedingInput(this.inventory.getItem(FEED_SLOT), this.inventory.getItem(PARENT_SLOT_1),
                this.inventory.getItem(PARENT_SLOT_2), this.inventory.getItem(CARRIER_SLOT), offspring);
    }

// 完成一次转化：扣一份饲料与一只父代，子代格就地变成产物；载体不消耗
    private void craft(InsectBreedingRecipe recipe) {
        ItemStack feed = this.inventory.getItem(FEED_SLOT);
        if (!feed.isEmpty()) {
            feed.shrink(1);
        }
// 配方要求两只父代时，转化完只拿走其中一只（先扣第一格，空了再扣第二格）
        if (recipe.getParents().isPresent()) {
            ItemStack parent = this.inventory.getItem(PARENT_SLOT_1);
            if (parent.isEmpty()) {
                parent = this.inventory.getItem(PARENT_SLOT_2);
            }
            if (!parent.isEmpty()) {
                parent.shrink(1);
            }
        }
        if (recipe.hasOffspringInput()) {
// 逐个把装着输入的子代格替换成产物
            for (int i = 0; i < OFFSPRING_SLOTS; i++) {
                if (!this.inventory.getItem(OFFSPRING_START + i).isEmpty()) {
                    this.inventory.setItem(OFFSPRING_START + i, recipe.getOffspringOutput().copy());
                }
            }
        } else {
// 六个子代格铺满产物
            for (int i = 0; i < OFFSPRING_SLOTS; i++) {
                this.inventory.setItem(OFFSPRING_START + i, recipe.getOffspringOutput().copy());
            }
        }
        this.setChanged();
    }

// 存档

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(INVENTORY_TAG, InventoryNbt.save(this.inventory));
        tag.putInt(PROGRESS_TAG, this.progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        InventoryNbt.load(this.inventory, tag, INVENTORY_TAG);
        this.progress = tag.getInt(PROGRESS_TAG);
    }

// 界面

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.flavor_immersed_daily_argo.insect_incubator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new InsectIncubatorMenu(containerId, playerInventory, this);
    }
}
