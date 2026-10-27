package com.flavor_immersed_daily_argo.item;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.InteractionHand;

// 镰刀：右键点击作物时，收割以点击处为中心 3×3 范围内已成熟的作物，并原地补种回幼苗。
//
// 右键优先级：不少作物（含 flavor_immersed_daily 的 FIDCropBlock 等）自带右键收获，
// 而方块自身的右键处理会排在物品 useOn 之前，导致镰刀无从触发。因此真正干活的是
// #onRightClickBlock —— 它监听 PlayerInteractEvent.RightClickBlock，在方块交互之前
// 先行接管并取消事件，从而保证镰刀优先。#useOn 仅作为该事件未触发时的兜底。
//
// 作物兼容性：不写死任何模组的类，只要方块符合下面任一条件就视为可收割的作物——
// 原版 / 各类模组的 CropBlock 子类（用 isMaxAge 判定），
// 或者任意「名字叫 age 的整型生长属性已达最大值」的植物类方块（BushBlock 或带
// waterlogged 的水生作物），从而同时兼容 flavor_immersed_daily 等附属模组的旱地、水生、
// 菌菇、浆果、稻类作物。
//
// 掉落沿用作物自己的战利品表（Block#getDrops 时把本工具传进去），所以附魔了
// 时运 后能按各方块自己的规则多掉种子 / 果实；每收割一株消耗 1 点耐久，耐久耗尽即停手。
// 补种消耗掉落里的一颗种子，其余收进玩家背包，背包塞不下就掉在脚下。
//
// 只要收割到至少一株，就会在每株作物上方播放原版横扫攻击音效与粒子
// （SoundEvents#PLAYER_ATTACK_SWEEP + ParticleTypes#SWEEP_ATTACK）。
// 范围内没有可收割的成熟作物时放行，不影响右键其它方块。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public class SickleItem extends TooltipItem {

// 收割范围：以点击处为中心的水平半径（1 → 3×3）
    private static final int RADIUS = 1;

    public SickleItem(Item.Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

// 给足附魔价值，好让时运之类的挖掘类附魔能附上去
    @Override
    public int getEnchantmentValue() {
        return 14;
    }

// 右键优先：先于方块自身的右键收获

// 在方块自身的右键处理之前接管：当点击的方块本身就是成熟作物时，取消事件并亲自收割，
// 避免作物自带的右键收获（如 FIDCropBlock）抢占而让镰刀失效。
//
// 只在点中成熟作物时才抢先，是为了不误伤旁边方块（例如紧挨着田地的箱子）的正常右键；
// 点击耕地等其它方块时这里放行，交由 #useOn 兜底收割周围作物。
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof SickleItem)) {
            return;
        }
        BlockPos pos = event.getPos();
        Level level = event.getLevel();
        if (!isMatureCrop(level.getBlockState(pos))) {
            return; // 点击的不是成熟作物，交还原版处理（必要时由 useOn 兜底）
        }
        event.setCanceled(true); // 镰刀优先，阻止作物自身的右键收获
        if (level instanceof ServerLevel serverLevel) {
            harvest(serverLevel, event.getEntity(), stack, pos);
        } else {
            event.getEntity().swing(event.getHand());
        }
    }

// 兜底：正常都走上面的右键事件，这里只在事件未被触发时生效
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || !hasMatureCrop(level, context.getClickedPos())) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            harvest(serverLevel, player, context.getItemInHand(), context.getClickedPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

// 范围内只要有一株成熟作物就动手，否则放行给其它交互
    private static boolean hasMatureCrop(Level level, BlockPos center) {
        for (BlockPos pos : area(center)) {
            if (isMatureCrop(level.getBlockState(pos))) {
                return true;
            }
        }
        return false;
    }

// 是否为已成熟、可收割的作物。兼容原版 CropBlock 与各类自定义作物块：
// 后者没有统一的基类，因此以「拥有名为 age 的整型生长属性且已到最大值」为通用判据，
// 并限定为植物类方块（BushBlock 或带 waterlogged 的水生作物），
// 避免误割甘蔗 / 仙人掌 / 竹子这些同样带 age 的非作物方块。
// 公共静态：无人机的收获模块也按同一套判据找成熟作物（见 UavModules）。
    public static boolean isMatureCrop(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        IntegerProperty age = ageProperty(state);
        if (age == null) {
            return false;
        }
        if (!(block instanceof BushBlock) && !state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return false;
        }
        return state.getValue(age) >= maxOf(age);
    }

// 取方块状态里名为 age 的整型生长属性（原版与模组作物的通用命名），没有则返回 null
    private static IntegerProperty ageProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integer && property.getName().equals("age")) {
                return integer;
            }
        }
        return null;
    }

// 该生长属性的最大取值，即作物的成熟阶段
    private static int maxOf(IntegerProperty property) {
        return Collections.max(property.getPossibleValues());
    }

// 收割范围内所有成熟作物，返回实际收割的株数
    private static int harvest(ServerLevel level, Player player, ItemStack tool, BlockPos center) {
        List<BlockPos> harvested = new ArrayList<>();
        for (BlockPos pos : area(center)) {
            if (tool.isEmpty()) {
                break; // 耐久耗尽，停下来
            }
            BlockState state = level.getBlockState(pos);
            if (!isMatureCrop(state)) {
                continue;
            }
// 掉落走作物自己的战利品表并传入本工具，时运附魔由此生效；
// 戴着草帽时按概率临时挂一级时运（见 StrawHatItem.rollFortuneTool）
            ItemStack dropsTool = StrawHatItem.rollFortuneTool(player, tool);
            List<ItemStack> drops = Block.getDrops(state, level, pos,
                    level.getBlockEntity(pos), player, dropsTool != null ? dropsTool : tool);
            level.setBlock(pos, replantedState(state), 3);
            giveDrops(player, state.getBlock(), drops);
            tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));
            harvested.add(pos.immutable());
        }
        if (!harvested.isEmpty()) {
            playSweepEffects(level, center, harvested);
        }
        return harvested.size();
    }

// 收获后要留下的方块状态：回到最初的幼苗阶段，其余属性（如 waterlogged）保持不变
    public static BlockState replantedState(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) {
            return crop.getStateForAge(0);
        }
        IntegerProperty age = ageProperty(state);
        return age != null ? state.setValue(age, 0) : state;
    }

// 生长阶段倒退一级，其余属性（如 waterlogged）保持不变。
// 用于老鼠啃食作物：原作是被毁掉，现改为让作物退回上一阶段。已是幼苗则原样返回。
    public static BlockState regressedState(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) {
            int age = crop.getAge(state);
            return age > 0 ? crop.getStateForAge(age - 1) : state;
        }
        IntegerProperty age = ageProperty(state);
        if (age == null) {
            return state;
        }
        int current = state.getValue(age);
        return current > 0 ? state.setValue(age, current - 1) : state;
    }

// 掉落里留一颗种子补种，其余进背包，背包塞不下就掉在脚下
    private static void giveDrops(Player player, Block block, List<ItemStack> drops) {
        Item seedItem = block.asItem();
        boolean replanted = false;
        for (ItemStack drop : drops) {
            if (!replanted && !drop.isEmpty() && drop.is(seedItem)) {
                drop.shrink(1);
                replanted = true;
            }
            if (!drop.isEmpty() && !player.getInventory().add(drop)) {
                player.drop(drop, false);
            }
        }
    }

// 原版横扫攻击的观感：横扫音效播在点击处正上方，并在每株被收割的作物上方各来一道横扫粒子，
// 让效果出现在玩家视线里的作物上，而不是脚边的玩家自己身上。
    private static void playSweepEffects(ServerLevel level, BlockPos center, List<BlockPos> harvested) {
        level.playSound(null, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
        for (BlockPos pos : harvested) {
            level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

// 以点击处为中心的水平 3×3，并向上多扫一层，方便右键耕地时够到上面的作物
    private static Iterable<BlockPos> area(BlockPos center) {
        return BlockPos.betweenClosed(
                center.offset(-RADIUS, 0, -RADIUS),
                center.offset(RADIUS, 1, RADIUS));
    }
}