package com.flavor_immersed_daily_argo.fishing;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import com.flavor_immersed_daily_argo.item.BaitItem;
import com.flavor_immersed_daily_argo.item.FishingNetItem;
import com.flavor_immersed_daily_argo.item.ModFishingRodItem;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModFishingHook extends FishingHook {

    private static final Logger LOGGER = LogUtils.getLogger();

// 遛鱼阶段参数
// 判定窗口长度（tick），约 1.2 秒评估一次
    private static final int WINDOW_TICKS = 24;
    private static final int PROGRESS_START = 50;
    private static final int PROGRESS_MAX = 100;
    private static final int PROGRESS_MIN = 0;
// 单个窗口内正确操作 tick 占比达到该值即判定成功（宽松判定，给玩家反应时间）
    private static final double PASS_RATIO = 0.55D;
// 单窗口失败时的进度损失
    private static final int PROGRESS_LOSS = 8;
// 由配件推算出的容错时长换算成 tick 后的下限，避免极端配件数值导致瞬间超时
    private static final int MIN_FIGHT_TIMEOUT_TICKS = 100;

// 岸边收纳
// 以玩家为中心搜索鱼护 / 钓鱼箱的半径（格）
    private static final double CONTAINER_RADIUS = 8.0D;

// 遛鱼阶段状态（仅服务端使用）
    private boolean fighting = false;
    private int fightTicks = 0;
    private int windowTicks = 0;
    private int correctTicks = 0;
    private int progress = PROGRESS_START;
// 本次遛鱼的超时 tick 数 = 容错时长 / 鱼进食速度 / 抗流能力
    private int timeoutTicks = 600;
// 单窗口进度增幅：由收杆动作次数反推，进度从 50 到 100 恰好需要配置的动作次数
    private int progressGain = 8;
// 本次遛鱼累计成功拉扯次数（仅用于钓获后的技术统计）
    private int pullCount = 0;
// 本次遛鱼累计消耗的临界耐久（仅用于钓获后的技术统计）
    private int durabilitySpent = 0;
// 本次咬钩锁定的战利品（手册条目；战利品生成与图鉴记账都以它为准）
    private FishingHandbookEntry targetEntry;
// 本次钓鱼使用的钓竿实际参数
    private FishingRodStats rodStats;
// 鱼的最大拉力（未收录取默认）
    private double fishTension = FishingRodStats.DEFAULT_MAX_TENSION;
// 拉力比 = ceil(鱼最大拉力 / 钓竿最大拉力)，下限 1；等于每次成功拉扯消耗的临界耐久
    private int tensionRatio = 1;
    private double anchorX;
    private double anchorY;
    private double anchorZ;

// 遛鱼阶段的结束方式，决定给玩家的失败提示
    private enum FightOutcome {
        SUCCESS, TIMEOUT, ESCAPED, GIVE_UP
    }

// 构造器

    public ModFishingHook(EntityType<? extends FishingHook> entityType, Level level) {
        super(entityType, level);
    }

    public ModFishingHook(Player player, Level level, int luck, int lureSpeed) {
        super(player, level, luck, lureSpeed);
    }

// 计时：驱动遛鱼阶段

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.fighting && !this.isRemoved()) {
            this.tickFight();
        }
    }

// 遛鱼阶段

// 右键时由钓竿调用。返回 true 表示本次右键已被遛鱼阶段接管
// （进入阶段 / 阶段中主动放弃），调用方不应再执行默认收杆逻辑。
    public boolean handleRightClick(Player player) {
        if (this.level().isClientSide) {
            return false;
        }
        if (this.fighting) {
// 阶段中再按右键 → 主动放弃，鱼逃走
            this.finishFight(player, FightOutcome.GIVE_UP);
            return true;
        }
// 只有真的有鱼咬钩时才进入遛鱼阶段，否则交给普通收杆（把空钩拉回来）
        if (!this.isFishBiting()) {
            notify(player, Component.translatable("msg.flavor_immersed_daily_argo.no_bite"));
            return false;
        }
// 咬钩判定失败（鱼吐钩）或没有匹配的鱼种时，空钩收回
        if (!this.tryStartFight(player)) {
            this.discard();
        }
        return true;
    }

// 是否正处在遛鱼阶段（竿架借此判断这一竿还能不能架上）
    public boolean isFighting() {
        return this.fighting;
    }

// 竿架把这一竿交给玩家时直接开打：咬钩已经在竿架上等到了，这里不再走 handleRightClick，
// 直接按钓竿参数锁定战利品并进入遛鱼阶段。
//
// 返回 false 表示没有匹配的鱼种或鱼在最后一刻吐钩逃走，调用方应丢弃这条鱼钩。
    public boolean startFightFromPod(Player player) {
        if (this.level().isClientSide || this.fighting) {
            return false;
        }
        return this.tryStartFight(player);
    }

// 浮漂上是否真的有鱼咬钩。
//
// 原版把咬钩状态存在私有字段里，同时同步到 DATA_BITING。浮漂身上只有这一个布尔型
// 同步数据会被置为 true（Entity 自带的三个布尔数据默认都是 false，Projectile
// 没有同步数据），所以扫描「非默认值」里是否存在 true 的布尔项即可判断，不必用反射。
    private boolean isFishBiting() {
        List<SynchedEntityData.DataValue<?>> values = this.getEntityData().getNonDefaultValues();
        if (values == null) {
            return false;
        }
        for (SynchedEntityData.DataValue<?> value : values) {
            if (value.value() instanceof Boolean flag && flag) {
                return true;
            }
        }
        return false;
    }

// 客户端每 tick 上报一次操作结果，服务端只累加正确 tick 数
    public void acceptInput(boolean correct) {
        if (this.fighting && !this.level().isClientSide && correct) {
            this.correctTicks++;
        }
    }

// 咬钩结算：先按钓竿参数与鱼的生活习性挑出咬钩的战利品，再判定有效咬钩率。
//
// 返回 false 表示鱼吐钩逃走（或没有匹配的鱼种），本次咬钩作废。
    private boolean tryStartFight(Player player) {
        FishingRodStats stats = rodStatsOf(player);
        FishingHandbookEntry entry = selectTarget(player, stats);
        if (entry == null) {
            notifyChat(player, Component.translatable("msg.flavor_immersed_daily_argo.no_species"));
            return false;
        }
        double biteChance = stats.biteChance(entry.alertnessFactor());
        if (this.random.nextDouble() > biteChance) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.NEUTRAL, 0.7F, 0.5F);
// 咬钩率只受「鱼警惕性 × 钓组显眼度」影响，显眼度偏高时点名鱼线与浮漂
            notifyChat(player, Component.translatable("msg.flavor_immersed_daily_argo.bite_failed",
                    entry.displayNameComponent(),
                    percent(biteChance),
                    trim(stats.conspicuousness),
                    partName(player, FishingRodStats.SLOT_LINE),
                    partName(player, FishingRodStats.SLOT_FLOAT)));
            LOGGER.info("[FIDA钓鱼] 咬钩失败（咬钩率 {}，显眼度 {}），{} 吐钩逃走",
                    String.format("%.2f", biteChance), stats.conspicuousness, entry.displayName());
            return false;
        }

        this.targetEntry = entry;
        this.rodStats = stats;
        this.fishTension = entry.fishTension();
        this.tensionRatio = stats.tensionRatioTo(this.fishTension);

        this.fighting = true;
        this.fightTicks = 0;
        this.windowTicks = 0;
        this.correctTicks = 0;
        this.progress = PROGRESS_START;
        this.pullCount = 0;
        this.durabilitySpent = 0;
// 留给玩家拖拽收杆的有效反应时间换算为 tick
        this.timeoutTicks = Math.max(MIN_FIGHT_TIMEOUT_TICKS,
                (int) Math.round(stats.reactionSeconds(entry.feedingFactor()) * 20.0D));
// 收杆动作次数反推单窗口进度增幅：达到 100 恰好需要配置的动作次数
        this.progressGain = Math.max(1,
                (int) Math.ceil((double) (PROGRESS_MAX - PROGRESS_START) / stats.reelActions));

// 记录入水锚点：鱼的一切发力都朝这个点，客户端也以它为浮漂抖动的中心
        this.anchorX = this.getX();
        this.anchorY = this.getY();
        this.anchorZ = this.getZ();
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.NEUTRAL, 0.8F, 1.0F);
        LOGGER.info("[FIDA钓鱼] 进入遛鱼阶段：{}（拉力 {} / 钓竿 {}，拉力比 {}，容错 {} tick，动作 {} 次）",
                entry.displayName(), this.fishTension, stats.maxTension, this.tensionRatio,
                this.timeoutTicks, stats.reelActions);
        this.sendFightSync(true);
        return true;
    }

// 从钓鱼手册条目里挑出咬钩的战利品，并与钓竿参数对接：
// 水域习性相符、鱼饵食性相符、生活水深落在钓竿深度区间内、生物群系相符、活动时段与当前昼夜 / 天气相符。
//
// 手册条目全部由数据包驱动，因此任何附属模组在 data/<命名空间>/fishing_handbook/
// 下写的鱼都会自动进入候选池；物品 / 实体都不存在的条目（未安装对应模组）会被跳过。
    private FishingHandbookEntry selectTarget(Player player, FishingRodStats stats) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        ResourceLocation biome = serverLevel.getBiome(this.blockPosition()).getKey().location();
        boolean isOcean = isOceanPath(biome.getPath());
// 纯实体条目（没有物品形态）必须拿抄网兜住；有物品形态的条目拿不拿都行
        boolean hasNet = netStackOf(player) != null;
        boolean isDay = serverLevel.isDay();
        boolean isRaining = serverLevel.isRaining();
// 投入鱼饵槽的鱼饵对应的食性；没装鱼饵或装的不是鱼饵时为 null
        FishingHandbookEntry.Diet baitDiet = baitDietOf(stats);

        List<FishingHandbookEntry> matched = new ArrayList<>();
        List<FishingHandbookEntry> relaxed = new ArrayList<>();
        for (FishingHandbookEntry entry : FishingHandbookData.getInstance().all()) {
            if (!entry.available() || (!entry.hasItem() && !hasNet)) {
                continue;
            }
            if (!entry.habitatMatches(isOcean)) {
                continue;
            }
// 鱼饵是硬条件：非杂食鱼必须用对应食性的鱼饵，因此不参与下面的兜底
            if (!baitMatches(entry, baitDiet)) {
                continue;
            }
            relaxed.add(entry);
            if (entry.depthMatches(stats.depthMin, stats.depthMax)
                    && entry.biomesMatch(biome)
                    && entry.activityMatches(isDay, isRaining)) {
                matched.add(entry);
            }
        }
// 兜底：水深 / 群系 / 时段把候选滤空时退回「只按水域与鱼饵」的结果，避免完全钓不到鱼
        List<FishingHandbookEntry> candidates = matched.isEmpty() ? relaxed : matched;
        return candidates.isEmpty() ? null : candidates.get(this.random.nextInt(candidates.size()));
    }

// 当前装配鱼饵对应的食性；未装鱼饵或装的不是鱼饵时返回 null
    private static FishingHandbookEntry.Diet baitDietOf(FishingRodStats stats) {
        if (stats.baitId == null) {
            return null;
        }
        Item bait = BuiltInRegistries.ITEM.getOptional(stats.baitId).orElse(null);
        return bait instanceof BaitItem baitItem ? baitItem.diet() : null;
    }

// 鱼饵是否对这条鱼有效：杂食鱼不装饵或装任意饵都可能咬钩，其余必须是对应食性的鱼饵
    private static boolean baitMatches(FishingHandbookEntry entry, FishingHandbookEntry.Diet baitDiet) {
        return entry.diet == FishingHandbookEntry.Diet.OMNIVORE || entry.diet == baitDiet;
    }

// 钓竿主人当前手持的南山钓竿；不在手上返回空栈
    private static ItemStack rodStackOf(Player player) {
        if (player.getMainHandItem().getItem() instanceof ModFishingRodItem) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof ModFishingRodItem) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

// 玩家手持的抄网（副手优先，拿着竿子的那只手一般是主手）；没拿返回 null
    @Nullable
    private static ItemStack netStackOf(Player player) {
        if (player.getOffhandItem().getItem() instanceof FishingNetItem) {
            return player.getOffhandItem();
        }
        if (player.getMainHandItem().getItem() instanceof FishingNetItem) {
            return player.getMainHandItem();
        }
        return null;
    }

// 由钓竿改装槽位实时推算的实际参数
    private static FishingRodStats rodStatsOf(Player player) {
        ItemStack rod = rodStackOf(player);
        return FishingRodStats.of(rod.isEmpty() ? FishingRodModData.EMPTY.mods() : FishingRodModMenu.getData(rod).mods());
    }

// 把遛鱼阶段的开始 / 结束同步给钓竿主人
    private void sendFightSync(boolean active) {
        if (this.getPlayerOwner() instanceof ServerPlayer serverPlayer) {
// 客户端据此决定拖拽与抖动幅度：拉力比越大拽得越狠，警惕性 × 显眼度决定抖动
            double tensionFactor = active ? this.fishTension / Math.max(0.01D, this.rodStats.maxTension) : 0.0D;
            double shake = active && this.targetEntry != null
                    ? this.targetEntry.alertnessFactor() * this.rodStats.conspicuousness
                    : 0.0D;
            PacketDistributor.sendToPlayer(serverPlayer, new FishingFightSyncPacket(
                    this.getId(), this.anchorX, this.anchorY, this.anchorZ, active,
                    new FishingFightSyncPacket.FightParams(tensionFactor, shake)));
        }
    }

    private void tickFight() {
        Player player = this.getPlayerOwner();
        if (player == null) {
            this.discard();
            return;
        }

        this.fightTicks++;
        this.windowTicks++;

        if (this.fightTicks >= this.timeoutTicks) {
            this.finishFight(player, FightOutcome.TIMEOUT);
            return;
        }

// 每个窗口结算一次：正确操作时长占比达标就加进度，否则减进度
        if (this.windowTicks >= WINDOW_TICKS) {
            boolean windowOk = this.correctTicks >= Math.ceil(WINDOW_TICKS * PASS_RATIO);
            this.windowTicks = 0;
            this.correctTicks = 0;
            this.progress += windowOk ? this.progressGain : -PROGRESS_LOSS;
            if (windowOk) {
// 每次拉扯成功消耗「拉力比」点临界耐久
                this.pullCount++;
                this.durabilitySpent += consumeDurability(player, this.tensionRatio);
            }
            if (this.progress >= PROGRESS_MAX) {
                this.finishFight(player, FightOutcome.SUCCESS);
                return;
            }
            if (this.progress <= PROGRESS_MIN) {
                this.finishFight(player, FightOutcome.ESCAPED);
                return;
            }
        }
    }

// 每次拉扯成功消耗「拉力比」点临界耐久，从竿体 / 鱼线中最脆弱的一件上扣；
// 扣满即该配件损毁（从改装槽位移除）。都没装可扣时不做任何事。
    private int consumeDurability(Player player, int amount) {
        if (amount <= 0) {
            return 0;
        }
        ItemStack rod = rodStackOf(player);
        if (rod.isEmpty()) {
            return 0;
        }
        FishingRodModData data = FishingRodModMenu.getData(rod);
        List<ItemStack> mods = new ArrayList<>(data.mods());
        int slot = weakestDurableSlot(mods);
        if (slot < 0) {
            return 0;
        }
        ItemStack part = mods.get(slot);
        part.setDamageValue(part.getDamageValue() + amount);
        if (part.getDamageValue() >= part.getMaxDamage()) {
            mods.set(slot, ItemStack.EMPTY);
            notify(player, Component.translatable("msg.flavor_immersed_daily_argo.part_worn_out"));
        }
        rod.set(ModDataComponents.ROD_MOD_DATA.get(), new FishingRodModData(data.customName(), List.copyOf(mods)));
        return amount;
    }

// 竿体 / 鱼线中剩余耐久最少的一件的槽位；都不可扣返回 -1
    private static int weakestDurableSlot(List<ItemStack> mods) {
        int best = -1;
        int bestLeft = Integer.MAX_VALUE;
        for (int slot : new int[]{FishingRodStats.SLOT_BODY, FishingRodStats.SLOT_LINE}) {
            ItemStack part = mods.get(slot);
            if (part.isEmpty() || !part.isDamageableItem()) {
                continue;
            }
            int left = part.getMaxDamage() - part.getDamageValue();
            if (left < bestLeft) {
                bestLeft = left;
                best = slot;
            }
        }
        return best;
    }

// 鱼的最大拉力超过钓竿最大拉力时，按 鱼最大拉力/100 的概率分别断线、断杆
    private void checkTensionBreak(Player player) {
        if (this.rodStats == null || this.fishTension <= this.rodStats.maxTension) {
            return;
        }
        double chance = this.fishTension / 100.0D;
        if (this.random.nextDouble() < chance) {
            breakPart(player, FishingRodStats.SLOT_LINE, "断线");
        }
        if (this.random.nextDouble() < chance) {
            breakPart(player, FishingRodStats.SLOT_BODY, "断杆");
        }
    }

// 损毁指定槽位的配件并从改装数据中移除
    private void breakPart(Player player, int slot, String reason) {
        ItemStack rod = rodStackOf(player);
        if (rod.isEmpty()) {
            return;
        }
        FishingRodModData data = FishingRodModMenu.getData(rod);
        if (slot >= data.mods().size() || data.mods().get(slot).isEmpty()) {
            return;
        }
        List<ItemStack> mods = new ArrayList<>(data.mods());
        mods.set(slot, ItemStack.EMPTY);
        rod.set(ModDataComponents.ROD_MOD_DATA.get(), new FishingRodModData(data.customName(), List.copyOf(mods)));
        this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
        notify(player, Component.translatable("msg.flavor_immersed_daily_argo."
                + (slot == FishingRodStats.SLOT_LINE ? "line_broken" : "body_broken")));
        LOGGER.info("[FIDA钓鱼] {}", reason);
    }

    private static void notify(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(message, true);
        }
    }

// 失败原因 / 技术统计这类长文案发到聊天栏，actionbar 显示不下
    private static void notifyChat(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message);
        }
    }

// 改装槽位里配件的显示名；空槽位返回「无」
    private static Component partName(Player player, int slot) {
        ItemStack rod = rodStackOf(player);
        if (!rod.isEmpty()) {
            List<ItemStack> mods = FishingRodModMenu.getData(rod).mods();
            if (slot >= 0 && slot < mods.size() && !mods.get(slot).isEmpty()) {
                return mods.get(slot).getHoverName();
            }
        }
        return Component.translatable("tooltip.flavor_immersed_daily_argo.rod_none");
    }

// 去掉整数的小数尾巴：30.0 → 30，30.5 → 30.5
    private static String trim(double value) {
        return value == Math.floor(value) && !Double.isInfinite(value)
                ? String.valueOf((long) value)
                : String.format(Locale.ROOT, "%.1f", value);
    }

// 把 0~1 的概率写成百分数文本；百分号放在这里，翻译模板里就不必写转义符
    private static String percent(double ratio) {
        return Math.round(ratio * 100.0D) + "%";
    }

    private void finishFight(Player player, FightOutcome outcome) {
        this.fighting = false;
        this.sendFightSync(false);
        if (outcome == FightOutcome.SUCCESS) {
// 鱼的最大拉力超过钓竿最大拉力时，钓上来的瞬间有断线 / 断杆风险
            this.checkTensionBreak(player);
            LOGGER.info("[FIDA钓鱼] 遛鱼成功，收获");
            this.giveCatch(player);
            if (this.targetEntry != null) {
                notifyChat(player, this.catchStats(this.targetEntry));
            }
        } else {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.NEUTRAL, 0.8F, 0.6F);
            LOGGER.info("[FIDA钓鱼] 遛鱼失败（{}），鱼逃走了", outcome);
            notifyChat(player, this.failureMessage(player, outcome));
        }
        this.discard();
    }

// 遛鱼失败的原因说明：点出是哪项参数不够、由哪些配件决定
    private Component failureMessage(Player player, FightOutcome outcome) {
        return switch (outcome) {
            case TIMEOUT ->
// 容错时长 = 基准 30s + 线轮泄力×2 + 鱼线延展×1.5 - 竿体硬度
                    Component.translatable("msg.flavor_immersed_daily_argo.fight_timeout",
                            trim(this.rodStats == null ? 0.0D : this.rodStats.toleranceSeconds),
                            partName(player, FishingRodStats.SLOT_REEL),
                            partName(player, FishingRodStats.SLOT_LINE),
                            partName(player, FishingRodStats.SLOT_BODY));
            case ESCAPED -> Component.translatable("msg.flavor_immersed_daily_argo.fight_escaped");
            default -> Component.translatable("msg.flavor_immersed_daily_argo.fight_give_up");
        };
    }

// 成功钓获后的技术统计：用时 / 拉扯次数 / 耐久消耗 / 双方拉力
    private Component catchStats(FishingHandbookEntry entry) {
        return Component.translatable("msg.flavor_immersed_daily_argo.fight_stats",
                entry.displayNameComponent(),
                trim(this.fightTicks / 20.0D),
                String.valueOf(this.pullCount),
                String.valueOf(this.durabilitySpent),
                trim(this.fishTension),
                trim(this.rodStats == null ? this.fishTension : this.rodStats.maxTension));
    }

// 核心：覆盖收杆逻辑

    @Override
    public int retrieve(ItemStack rodStack) {
        if (this.level().isClientSide) {
            return 0;
        }

// 鱼只能从遛鱼阶段获得：普通收杆只是把空钩拉回来，不给鱼
        this.discard();
        return 0;
    }

// 成功钓到鱼：掉落物品 / 生成活鱼（战利品已在咬钩时按钓竿参数锁定）
    private void giveCatch(Player player) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        FishingHandbookEntry entry = this.targetEntry;
        if (entry == null) {
            return;
        }

// 只有手上拿着抄网才给活鱼：没拿网就把钓获当物品掉落，否则活鱼飞过来玩家根本收不了

// 图鉴记录：本玩家已钓获该水产物（物品形态记物品 id，纯实体条目记实体 id）
        if (player instanceof ServerPlayer serverPlayer) {
            FishingCaughtRecords.markCaught(serverPlayer, entry.caughtKey());
        }

// 鱼饵消耗：钓到对应食性的鱼后从鱼饵槽扣掉一个（杂食鱼本来就不需要鱼饵，不消耗）
        consumeBait(player, entry);

        EntityType<?> type = entry.hasEntity() && netStackOf(player) != null
                ? BuiltInRegistries.ENTITY_TYPE.getOptional(entry.entityId).orElse(null)
                : null;
// 活鱼已经进了鱼护 / 已经作为实体生成，就不要再掉落物品
        boolean captured = false;
        if (type != null) {
// 生成活体战利品 —— 从钩点飞向玩家
            Entity fishEntity = type.create(serverLevel);
            if (fishEntity != null) {
                fishEntity.setPos(this.getX(), this.getY(), this.getZ());
                if (fishEntity instanceof AbstractFish af) {
                    af.setFromBucket(true);
                }
                bindFishVariant(fishEntity, entry);
// 优先把活鱼直接放进附近鱼护的笼子里（纯物理：生成成实体待在里面）
                BlockPos protection = findProtection(serverLevel, player);
                if (protection != null) {
                    Vec3 center = Vec3.atCenterOf(protection);
                    fishEntity.moveTo(center.x, center.y, center.z, this.random.nextFloat() * 360.0F, 0.0F);
// 养在笼里的鱼不该因为玩家走远而被刷掉
                    if (fishEntity instanceof Mob mob) {
                        mob.setPersistenceRequired();
                    }
                    serverLevel.addFreshEntity(fishEntity);
                    captured = true;
                    LOGGER.info("[FIDA钓鱼] 钓获 → 放进鱼护: {}", entry.displayName());
                } else {
// 没有鱼护就作为活鱼飞向玩家，让玩家用抄网自己捞
                    double dx = player.getX() - this.getX();
                    double dy = player.getY() - this.getY();
                    double dz = player.getZ() - this.getZ();
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    fishEntity.setDeltaMovement(dx * 0.1, dy * 0.1 + Math.sqrt(dist) * 0.08, dz * 0.1);
                    serverLevel.addFreshEntity(fishEntity);
                    captured = true;
                    LOGGER.info("[FIDA钓鱼] 钓获 → 生成活体战利品: {}", entry.displayName());
                }
            }
        }
        if (!captured) {
// 没有实体形态的条目：掉落物品（优先塞进附近的钓鱼箱）
            ItemStack drop = entry.itemId == null ? ItemStack.EMPTY
                    : BuiltInRegistries.ITEM.getOptional(entry.itemId)
                            .map(Item::getDefaultInstance).orElse(ItemStack.EMPTY);
            if (drop.isEmpty()) {
                return;
            }
            FishingBoxBlockEntity box = findBox(serverLevel, player);
            if (box != null && box.add(drop)) {
                LOGGER.info("[FIDA钓鱼] 钓获 → 送入钓鱼箱: {}", drop.getHoverName().getString());
            } else {
                ItemEntity itemEntity = new ItemEntity(serverLevel,
                        this.getX(), this.getY(), this.getZ(), drop);
                double dx = player.getX() - this.getX();
                double dy = player.getY() - this.getY();
                double dz = player.getZ() - this.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                itemEntity.setDeltaMovement(dx * 0.1, dy * 0.1 + Math.sqrt(dist) * 0.08, dz * 0.1);
                serverLevel.addFreshEntity(itemEntity);
                LOGGER.info("[FIDA钓鱼] {} → 掉落物品: {}",
                        isOceanPath(serverLevel.getBiome(this.blockPosition()).getKey().location().getPath()) ? "海洋" : "淡水",
                        drop.getHoverName().getString());
            }
        }

// 播放收杆音效
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F,
                0.4F / (this.random.nextFloat() * 0.4F + 0.8F));
    }

// 找玩家附近最近的鱼护方块；没有则返回 null
    @Nullable
    private static BlockPos findProtection(ServerLevel level, Player player) {
        BlockPos center = player.blockPosition();
        int r = Mth.ceil(CONTAINER_RADIUS);
        double limit = CONTAINER_RADIUS * CONTAINER_RADIUS;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            BlockState state = level.getBlockState(pos);
// 干放的鱼护里没水，鱼进去会直接窒息，不算有效鱼护
            if (state.getBlock() instanceof FishProtectionBlock && state.getValue(FishProtectionBlock.WATERLOGGED)) {
                double d = pos.distSqr(center);
                if (d <= limit && d < bestDist) {
                    bestDist = d;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }

// 找玩家附近最近的钓鱼箱；没有则返回 null
    @Nullable
    private static FishingBoxBlockEntity findBox(ServerLevel level, Player player) {
        BlockPos center = player.blockPosition();
        int r = Mth.ceil(CONTAINER_RADIUS);
        double limit = CONTAINER_RADIUS * CONTAINER_RADIUS;
        FishingBoxBlockEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            if (level.getBlockEntity(pos) instanceof FishingBoxBlockEntity box) {
                double d = pos.distSqr(center);
                if (d <= limit && d < bestDist) {
                    bestDist = d;
                    best = box;
                }
            }
        }
        return best;
    }

// 钓到鱼后从钓竿的鱼饵槽扣掉一个鱼饵。
//
// 杂食鱼不消耗鱼饵（它们不装饵也能咬钩）；其余条目在咬钩时已按食性过滤，
// 因此这里只要装的是鱼饵即可直接扣减。槽位写回钓竿组件，数量归零时清空该槽。
    private static void consumeBait(Player player, FishingHandbookEntry entry) {
        if (entry.diet == FishingHandbookEntry.Diet.OMNIVORE) {
            return;
        }
        ItemStack rod = rodStackOf(player);
        if (rod.isEmpty()) {
            return;
        }
        FishingRodModData data = FishingRodModMenu.getData(rod);
        List<ItemStack> mods = new ArrayList<>(data.mods());
        ItemStack bait = mods.get(FishingRodStats.SLOT_BAIT);
        if (bait.isEmpty()) {
            return;
        }
        ItemStack left = bait.copy();
        left.shrink(1);
        mods.set(FishingRodStats.SLOT_BAIT, left.isEmpty() ? ItemStack.EMPTY : left);
        rod.set(ModDataComponents.ROD_MOD_DATA.get(), new FishingRodModData(data.customName(), mods));
    }

// 部分模组把所有鱼共用一个实体，靠实体里携带的鱼物品来区分种类（如捕星者的 starcatcher:fish），
// 不写入鱼物品的话实体渲染为空白。这里尝试把鱼物品写进实体，实体没有该方法时保持原样。
    private static void bindFishVariant(Entity entity, FishingHandbookEntry entry) {
        if (entry.itemId == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(entry.itemId).orElse(null);
        if (item == null) {
            return;
        }
        try {
            entity.getClass().getMethod("setFish", ItemStack.class).invoke(entity, item.getDefaultInstance());
        } catch (ReflectiveOperationException ignored) {
// 该实体不是靠物品区分种类的通用鱼，无需处理
        }
    }

    private static boolean isOceanPath(String path) {
        return path.equals("ocean") || path.equals("deep_ocean")
                || path.equals("cold_ocean") || path.equals("deep_cold_ocean")
                || path.equals("frozen_ocean") || path.equals("deep_frozen_ocean")
                || path.equals("lukewarm_ocean") || path.equals("deep_lukewarm_ocean")
                || path.equals("warm_ocean");
    }
}
