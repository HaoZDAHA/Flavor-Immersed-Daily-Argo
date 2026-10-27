package com.flavor_immersed_daily_argo.uav;

import com.flavor_immersed_daily_argo.all.ModItems;
import com.flavor_immersed_daily_argo.entity.Uav;
import com.flavor_immersed_daily_argo.item.UavControllerItem;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

// 无人机遥控器的客户端部分：开界面、切相机、收键位并上报。
//
// 一次点击的信号链：遥控器还没绑过时右键无人机 → Uav#interact 在服务端完成绑定，并把绑定信息写进
// 遥控器的数据组件；绑定之后，#onInteractionKey 会在右键按下的最前面把事件拦下来直接开操作界面，
// 所以「随便右键都行」，不必再对着无人机点。
//
// 点「第一人称操作」后 #beginOperating 把相机挂到无人机身上（Minecraft#setCameraEntity），
// 同时关掉原版的第一人称手部渲染。玩家本人不骑上去：#onMovementInputUpdate 把移动输入清零，
// 人就钉在原地不会跟着跑。
//
// 之后每 tick 把六个方向键读成一个位掩码、连同玩家视角的朝向一起发给服务端，同时在本机给无人机装上
// 同一份输入——它自己会照着走一遍，所以画面是即时的，不用等服务端回包。
//
// 注意：鼠标转的一直是玩家（原版 MouseHandler 只动 player），而相机看的是
// 相机实体的朝向，所以每 tick 都要把玩家的视角抄到无人机身上，镜头才跟手。
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class UavClient {

    private static final String CATEGORY = "category.flavor_immersed_daily_argo.uav";

// 这几个键位是 MOD 总线上的 RegisterKeyMappingsEvent 注册的，处理器在
// flavor_immersed_daily_argoClient#registerKeyMappings（RegisterKeyMappingsEvent 只走 MOD 总线）
    public static final KeyMapping UAV_FORWARD = new KeyMapping(
            "key.flavor_immersed_daily_argo.uav_forward", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_W, CATEGORY);
    public static final KeyMapping UAV_BACK = new KeyMapping(
            "key.flavor_immersed_daily_argo.uav_back", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_S, CATEGORY);
    public static final KeyMapping UAV_LEFT = new KeyMapping(
            "key.flavor_immersed_daily_argo.uav_left", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_A, CATEGORY);
    public static final KeyMapping UAV_RIGHT = new KeyMapping(
            "key.flavor_immersed_daily_argo.uav_right", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_D, CATEGORY);
    public static final KeyMapping UAV_UP = new KeyMapping(
            "key.flavor_immersed_daily_argo.uav_up", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, CATEGORY);
// 下降不能用左 Shift：那是原版的潜行键，手持物品 / 站在方块上时容易误触
    public static final KeyMapping UAV_DOWN = new KeyMapping(
            "key.flavor_immersed_daily_argo.uav_down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, CATEGORY);
    public static final KeyMapping UAV_EXIT = new KeyMapping(
            "key.flavor_immersed_daily_argo.uav_exit", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY);

// 刚点下「第一人称操作」后，服务端把操控者写进同步数据、再发回客户端要跑一个来回。
// 这几 tick 里 isOperatedBy 还是 false，不能据此判定「操作已结束」，否则刚上机就被踢下来。
    private static final int OPERATE_ACK_GRACE = 40;

// 正在操作的无人机实体 id；0 表示没在操作
    private static int operatingId;

// 还剩几 tick 的「等服务端确认操控者」宽限
    private static int operateGrace;

    private UavClient() {
    }

// 开始第一人称操作：把相机挂到这架无人机上、收起玩家自己的手
    public static void beginOperating(Uav uav) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        operatingId = uav.getId();
        operateGrace = OPERATE_ACK_GRACE;
        minecraft.setCameraEntity(uav);
// 相机不在玩家身上，原版照样会把玩家自己的手画在屏幕下方，这里关掉
        minecraft.gameRenderer.setRenderHand(false);
    }

// 客户端侧退出操作：还原本机状态（服务端那边由 STOP_OPERATE 包负责）
    private static void stopOperatingLocally() {
        Minecraft minecraft = Minecraft.getInstance();
        if (operatingId != 0 && minecraft.level != null
                && minecraft.level.getEntity(operatingId) instanceof Uav uav) {
            uav.clientOperated = false;
        }
        operatingId = 0;
        operateGrace = 0;
        minecraft.gameRenderer.setRenderHand(true);
        if (minecraft.getCameraEntity() instanceof Uav) {
            minecraft.setCameraEntity(null);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (operatingId == 0) {
            return;
        }
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            stopOperatingLocally();
            return;
        }

        Entity entity = minecraft.level.getEntity(operatingId);
        if (!(entity instanceof Uav uav)) {
// 无人机没了
            stopOperatingLocally();
            return;
        }
        if (!uav.isOperatedBy(player)) {
// 服务端的确认还没回来时先按宽限顶着；宽限用完说明服务端那边确实结束操作了
            if (operateGrace > 0) {
                operateGrace--;
            } else {
                stopOperatingLocally();
                return;
            }
        } else {
            operateGrace = 0;
        }

        int id = operatingId;

// 退出键：先在本地收摊，再让服务端把操控者清掉
        if (UAV_EXIT.isDown()) {
            uav.clientOperated = false;
            PacketDistributor.sendToServer(new UavActionPacket(id, UavActionPacket.STOP_OPERATE, 0, 0, 0, 0, 0, 0));
            stopOperatingLocally();
            return;
        }

// 相机钉在无人机上；鼠标转的是玩家，所以每 tick 把玩家的视角抄过去
        uav.clientOperated = true;
        minecraft.setCameraEntity(uav);

// 附身时按原版背包键：要开的是无人机的物品栏，不能让它落到原版去开玩家自己的背包。
// ClientTickEvent.Pre 比 Minecraft#handleKeybinds 早，这里把点击消费掉，原版就看不见了
        while (minecraft.options.keyInventory.consumeClick()) {
            PacketDistributor.sendToServer(new UavActionPacket(id, UavActionPacket.OPEN_INVENTORY, 0, 0, 0, 0, 0, 0));
        }

        int keys = readKeys();
        uav.setFlightInput(keys, player.getYRot(), player.getXRot());
        PacketDistributor.sendToServer(new UavFlightInputPacket(id, keys, player.getYRot(), player.getXRot()));
    }

// 操作无人机期间把玩家的移动输入清零。玩家不骑在无人机上，不拦的话 WASD 会让人一边开飞机一边满地跑，
// 退出操作时人都不知道飘到哪儿去了。
    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (operatingId == 0) {
            return;
        }
        Input input = event.getInput();
        input.leftImpulse = 0.0F;
        input.forwardImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

// 拿着已经绑好的遥控器右键：不管准星对着空气、方块还是别的什么，一律拦下来开操作界面。
//
// 还没绑的时候不拦，那一下要让给 Uav#interact 去完成绑定。
//
// 正在附身操作时按键都归无人机：左键走战斗模块发射；右键看模组槽——装了高亮标记模块就把准星方块
// 标记成高亮（见 UavHighlightRenderer），其余情况走战斗模块食用使用槽的物品。都由服务端校验
// 模块与操控者，这一下不能落到原版的攻击 / 使用上，否则会顺手砍到或者用掉玩家自己手里的东西。
    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

// 附身中：左右键都归无人机，别落到原版
        if (operatingId != 0 && (event.isAttack() || event.isUseItem())) {
            event.setSwingHand(false);
            event.setCanceled(true);
            if (event.isAttack()) {
                PacketDistributor.sendToServer(new UavActionPacket(
                        operatingId, UavActionPacket.COMBAT_FIRE, 0, 0, 0, 0, 0, 0));
            } else {
                sendUseAction();
            }
            return;
        }

        if (!event.isUseItem()) {
            return;
        }
        ItemStack stack = player.getItemInHand(event.getHand());
        if (!stack.is(ModItems.UAV_CONTROLLER.get())) {
            return;
        }
        Uav uav = findBoundDrone(player, stack);
        if (uav == null) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
        minecraft.setScreen(new UavControllerScreen(uav));
    }

// 附身时右键：模组槽里装的是高亮标记模块、且准星正对着方块，就把那格发给服务端高亮；
// 其余情况照旧交给战斗模块（食用使用槽的物品，不行就发射）。
    private static void sendUseAction() {
        Uav uav = getOperatingUav();
        if (uav != null && uav.hasHighlightModule()
                && Minecraft.getInstance().hitResult instanceof BlockHitResult hit
                && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            PacketDistributor.sendToServer(new UavActionPacket(operatingId, UavActionPacket.MARK_BLOCK,
                    pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0));
            return;
        }
        PacketDistributor.sendToServer(new UavActionPacket(
                operatingId, UavActionPacket.COMBAT_USE, 0, 0, 0, 0, 0, 0));
    }

// 本机正在操作的无人机；没在操作返回 null。
// 渲染高亮方块的 UavHighlightRenderer 也用它来判断「现在是不是附身视角」。
    @Nullable
    public static Uav getOperatingUav() {
        Minecraft minecraft = Minecraft.getInstance();
        if (operatingId == 0 || minecraft.level == null) {
            return null;
        }
        return minecraft.level.getEntity(operatingId) instanceof Uav uav ? uav : null;
    }

// 在已加载的实体里按 UUID 找出遥控器绑着的那架无人机；没绑 / 不在这附近就返回 null
    @Nullable
    private static Uav findBoundDrone(Player player, ItemStack stack) {
        UUID bound = UavControllerItem.getBoundDrone(stack);
        if (bound == null || !(player.level() instanceof ClientLevel level)) {
            return null;
        }
// 不能按距离找：无人机飞自动航线会跑出几十格外，限制范围会让面板打不开。
// 已加载的实体本来就有限，直接扫一遍按 UUID 对即可
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof Uav uav && uav.getUUID().equals(bound)) {
                return uav;
            }
        }
        return null;
    }

    private static int readKeys() {
        int keys = 0;
        if (UAV_FORWARD.isDown()) {
            keys |= Uav.KEY_FORWARD;
        }
        if (UAV_BACK.isDown()) {
            keys |= Uav.KEY_BACK;
        }
        if (UAV_LEFT.isDown()) {
            keys |= Uav.KEY_LEFT;
        }
        if (UAV_RIGHT.isDown()) {
            keys |= Uav.KEY_RIGHT;
        }
        if (UAV_UP.isDown()) {
            keys |= Uav.KEY_UP;
        }
        if (UAV_DOWN.isDown()) {
            keys |= Uav.KEY_DOWN;
        }
        return keys;
    }
}
