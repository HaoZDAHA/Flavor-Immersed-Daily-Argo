package com.flavor_immersed_daily_argo.entity;

import com.flavor_immersed_daily_argo.all.ModEntities;
import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.level.BlockEvent;

// 稻草人。
//
// 插在田里的装饰，放下后会自己落到地面上，落地之后就不再移动、也不会自然消失。它守住脚下这一片地，
// 以它为中心 8 格范围内的耕地既不会被踩成泥土（见 #onFarmlandTrample），
// 干涸得也比别处慢，而且永远不会因为干透而退化成泥土（见 #hydrateFarmland）。
//
// 和地上的 Poop 一样是最朴素的 Entity：原版这种实体既没有重力也没有 AI，所以下落是
// 在 #tick 里自己算的。潜行右键或连着打几下都能收回成物品，直接右键改名。
@EventBusSubscriber(modid = flavor_immersed_daily_argo.MODID)
public class Scarecrow extends Entity {

// 连着挨这么多下就拔起来（与充气浮台的手感一致）
    private static final int HITS_TO_RECOVER = 3;

// 已经被玩家打了几下；不算存档，区块卸载后重新计数
    private int hits;

// 保护半径（格）。判定用的是「耕地到稻草人的距离不超过 8 格」，
// 也就是以稻草人为中心、边长 17 格的立方体范围。
    public static final double PROTECT_RADIUS = 8.0D;

// 每 tick 的重力加速度，和原版生物一致
    private static final double GRAVITY = 0.08D;

// 下落速度上限：从高处掉下来时不会越掉越快，也就不会一步跨过地面
    private static final double MAX_FALL_SPEED = 1.2D;

// 每隔这么多刻，检查一次保护范围内的耕地要不要补水（见 #hydrateFarmland）
    private static final int HYDRATE_INTERVAL = 20;

// 检查到耕地偏干时，每回只有这个概率给它补满——于是地“干得慢”，而不是被死死冻住
    private static final float HYDRATE_CHANCE = 0.5F;

// 湿度低到这个数以下就必补：绝不能让它掉到 0，否则随机刻会把它退化成泥土
    private static final int HYDRATE_FLOOR = 1;

// 补水范围：水平 ±8 格，与踩踏保护同宽
    private static final int HYDRATE_RADIUS_H = (int) PROTECT_RADIUS;

// 补水范围：竖直 ±4 格，够照顾到有落差的梯田
    private static final int HYDRATE_RADIUS_V = 4;

// 错开每只稻草人的补水节拍，免得同一刻所有稻草人一齐扫世界
    private final int hydratePhase = this.random.nextInt(HYDRATE_INTERVAL);

    public Scarecrow(EntityType<? extends Scarecrow> type, Level level) {
        super(type, level);
    }

// 必须覆写：Entity#isPickable() 默认为 false，不改成 true 右键事件根本传不到
// #interact，稻草人就拔不下来了。
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

// 准星能选中（右键收回要用），但别让箭矢、投掷物把稻草人当成靶子
    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

// 重力。
//
// Entity#getDefaultGravity() 对普通实体返回 0，也就不会自己下落，所以这里手动来一套：
// 每 tick 往下加一点速度再走一步，落地之后把速度清干净。悬空放下（比如对着墙边右键）时会一路掉到地面上。
    @Override
    public void tick() {
        super.tick();
// 服务端定期给周围的耕地补水，堵住“干涸成泥土”那条路
        if (!this.level().isClientSide
                && (this.tickCount + this.hydratePhase) % HYDRATE_INTERVAL == 0) {
            this.hydrateFarmland((ServerLevel) this.level());
        }
        if (this.onGround()) {
// 已经贴地：清掉速度，免得停在斜坡或者水流边上慢慢滑走。
// 只在真有速度时才写，不然 setDeltaMovement 会让服务端每 tick 都发一次位置包
            if (!this.getDeltaMovement().equals(Vec3.ZERO)) {
                this.setDeltaMovement(Vec3.ZERO);
            }
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, Math.max(motion.y - GRAVITY, -MAX_FALL_SPEED), motion.z);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

// 右键交互：潜行右键收回成物品，直接右键留给客户端打开改名界面。
//
// 收回只在服务端真正执行；客户端返回成功就行（客户端会把这次交互打包发上去，包里带着潜行标记，
// 所以服务端也知道玩家按着潜行键）。
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
// 直接右键：改名，由客户端事件开界面，这里什么都不用做
            return InteractionResult.SUCCESS;
        }
        if (!this.level().isClientSide) {
            this.recover(player);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

// 左键连击：和充气浮台一样，被玩家连续打 #HITS_TO_RECOVER 下就拔起来收回。
//
// 只认玩家本体动手，箭矢与爆炸之类的意外伤害不理会，免得流弹把田里的稻草人拆了；
// 客户端本地那一份也直接返回，真正落地的是服务端这次判定。
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()
                || !(source.getEntity() instanceof Player player)) {
            return false;
        }
        this.hits++;
        if (this.hits >= HITS_TO_RECOVER) {
            this.recover(player);
        }
        return true;
    }

// 拔起来收回成物品：先塞背包，塞不下再丢在脚下
    private void recover(Player player) {
        ItemStack stack = new ItemStack(ModItems.SCARECROW.get());
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 1.4F);
        this.discard();
    }

// 农田保护

// 拦下耕地的踩踏。
//
// 原版 FarmBlock#fallOn 里踩踏那一步会先问 NeoForge，事件被取消就直接跳过「变泥土」，
// 所以这里取消即可，不需要另行动手把方块改回去。
//
// 注意事件只在服务端触发（fallOn 里带了 !level.isClientSide 判断），
// 拿不到服务端世界说明不是该管的情况，直接放行。
    @SubscribeEvent
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        AABB area = new AABB(pos).inflate(PROTECT_RADIUS);
        if (!level.getEntitiesOfClass(Scarecrow.class, area).isEmpty()) {
            event.setCanceled(true);
        }
    }

// 给保护范围内的耕地补水：偏干时按概率补满，等效于大幅降低干涸的概率、把干涸时间拉长；
// 湿度一旦见底则必补，兜住底线，使它永远不会在随机刻里退化成泥土。
//
// 原版耕地只在「湿度归零、上方又没种着作物」时才会 turnToDirt（见
// FarmBlock.randomTick），所以只要湿度掉不到 0，干涸这条路径就走不通
// （踩踏那条另见 #onFarmlandTrample）。
//
// 补不补交给 #HYDRATE_CHANCE 与 #HYDRATE_FLOOR：前者让它“干得慢”而不是被死死冻住，
// 后者保证湿度不会真的掉到 0。
//
// 上方被实心方块盖住导致 canSurvive 失败的那条路不管——耕地本就压不住实心方块，
// 属于原版物理，不是稻草人该拦的。
    private void hydrateFarmland(ServerLevel level) {
        BlockPos center = this.blockPosition();
        int minX = center.getX() - HYDRATE_RADIUS_H;
        int maxX = center.getX() + HYDRATE_RADIUS_H;
        int minZ = center.getZ() - HYDRATE_RADIUS_H;
        int maxZ = center.getZ() + HYDRATE_RADIUS_H;
        int minY = center.getY() - HYDRATE_RADIUS_V;
        int maxY = center.getY() + HYDRATE_RADIUS_V;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
// 一块一块地读：每个区块只查一次，比逐格 getBlockState 少掉几千次区块查找；
// 没加载的区块直接跳过（不去触发同步加载）
        for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                int fromX = Math.max(minX, chunkX << 4);
                int toX = Math.min(maxX, (chunkX << 4) + 15);
                int fromZ = Math.max(minZ, chunkZ << 4);
                int toZ = Math.min(maxZ, (chunkZ << 4) + 15);
                for (int x = fromX; x <= toX; x++) {
                    for (int z = fromZ; z <= toZ; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            cursor.set(x, y, z);
                            BlockState state = chunk.getBlockState(cursor);
                            if (!(state.getBlock() instanceof FarmBlock)) {
                                continue;
                            }
                            int moisture = state.getValue(FarmBlock.MOISTURE);
                            if (moisture >= FarmBlock.MAX_MOISTURE) {
                                continue;
                            }
// 偏干时按概率补：干涸概率被压下去、时间被拉长；见底了则必补，守住不退化成泥土的底线
                            if (moisture > HYDRATE_FLOOR && level.random.nextFloat() >= HYDRATE_CHANCE) {
                                continue;
                            }
// 只发客户端更新，不引起邻块连锁
                            level.setBlock(cursor.immutable(),
                                    state.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }
    }

// 这里没有任何需要同步或存档的状态，下面三个空实现只是满足 Entity 的抽象方法

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
