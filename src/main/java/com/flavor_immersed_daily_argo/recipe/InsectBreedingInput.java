package com.flavor_immersed_daily_argo.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

// 昆虫培育箱的配方输入快照。
//
// 把培育箱里四个分区的内容打包成一个不可变的输入对象交给配方匹配：
// 饲料、父代（两格）、载体、子代（六格）。子代槽用列表表示，是为了让配方能
// 表达「任意多少个槽位都行」——匹配时按整批子代一起判断。
//
// feed：饲料槽内容（每次转化消耗一份）
// parent1：父代槽 1
// parent2：父代槽 2
// carrier：载体槽（不消耗）
// offspring：子代槽内容（六个）
public record InsectBreedingInput(ItemStack feed, ItemStack parent1, ItemStack parent2,
                                  ItemStack carrier, List<ItemStack> offspring) implements RecipeInput {

// 子代槽在 #getItem(int) 里的起始下标
    public static final int OFFSPRING_START = 4;

    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> this.feed;
            case 1 -> this.parent1;
            case 2 -> this.parent2;
            case 3 -> this.carrier;
            default -> {
                int i = index - OFFSPRING_START;
                yield (i >= 0 && i < this.offspring.size()) ? this.offspring.get(i) : ItemStack.EMPTY;
            }
        };
    }

    @Override
    public int size() {
        return OFFSPRING_START + this.offspring.size();
    }
}
