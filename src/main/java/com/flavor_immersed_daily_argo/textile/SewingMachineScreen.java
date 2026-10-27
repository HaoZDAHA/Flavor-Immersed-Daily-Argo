package com.flavor_immersed_daily_argo.textile;

import com.flavor_immersed_daily_argo.client.GuiStyle;
import com.flavor_immersed_daily_argo.client.RoundedButton;
import com.flavor_immersed_daily_argo.client.render.SilkArmorTextures;
import com.flavor_immersed_daily_argo.client.render.SilkDesignLayer;
import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

// 缝纫机界面
//
// 界面分两页，顶部的两个页签来回切：
//
// 装备页：只放护甲槽、丝绸槽和物品栏，以及「着装 / 拆卸」两个按钮；
// 绘制页：一块可以自由旋转的 3D 视口（里面立着台面装备对应的几个 cube，六个面都能直接
// 涂色），右边是一张 RGB 色板 + 明度条、当前色、四个工具和「保存 / 取出」。这页不显示任何槽位。
//
// 模型用的就是原版 HumanoidModel.createMesh，贴图由 SilkArmorTextures 按花纹现算，
// 所以这里看到的样子和穿在身上时一模一样。
public class SewingMachineScreen extends AbstractContainerScreen<SewingMachineMenu> {

// 配色
// 视口 / 色板的细描边（画在黑底上，用半透明白）
    private static final int BORDER_COLOR = 0x4DFFFFFF;
    private static final int TEXT_DIM_COLOR = GuiStyle.TEXT_DIM;
// 鼠标选中面的高亮（最前面那层）
    private static final int HIGHLIGHT_COLOR = 0xC0FFFFFF;
// 鼠标选中的是被挡住的那层时的高亮
    private static final int HIGHLIGHT_HIDDEN_COLOR = 0xC0FFC24A;
// 3D 体素工具光标（要落的那个小方块）的线框
    private static final int VOXEL_COLOR = 0xE0FFE24A;
// 丝绸不够时体素光标的线框
    private static final int VOXEL_COLOR_EMPTY = 0xE0FF6A6A;

// 工具
    private static final int TOOL_PAINT = 0;
    private static final int TOOL_ERASE = 1;
// 3D 体素建模：左键放方块（每个耗 1 丝绸）、右键拆方块
    private static final int TOOL_VOXEL = 2;
    private static final int TOOL_PICK = 3;

// 3D 视口
// 缩放下限 / 上限
    private static final double MIN_ZOOM = 0.5D;
    private static final double MAX_ZOOM = 6.0D;
// 每拖一个屏幕像素转多少度
    private static final float ROTATE_SENSITIVITY = 0.6F;
// 俯仰角限制，别翻过头
    private static final float MAX_PITCH = 89.0F;
// 视口里留出的边距（屏幕像素），免得模型贴着边框
    private static final double FIT_MARGIN = 14.0D;
// 光照：GUI 里给全亮
    private static final int FULL_BRIGHT = 0xF000F0;
// 视口里那块模型写进深度缓冲时才用得上的 z，和原版物品栏里的实体一致
    private static final float VIEW_Z = 50.0F;

// 预览专用贴图，整个界面复用同一张，花纹变了就重画再上传
    private static final ResourceLocation PREVIEW_TEXTURE = new ResourceLocation(
            flavor_immersed_daily_argo.MODID, "dynamic/sewing_machine_preview");

// 按原版人形模型烘一份骨架，只用来画预览与做拾取
    private final ModelPart root;
    private final HumanoidModel<LivingEntity> model;
// 立在线框里的 Steve 模特：让玩家看清正在画的是哪个部位（衣物那几块由花纹贴图盖掉）
    private static final ResourceLocation STEVE_TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");
    private final ModelPart mannequinRoot;
    private final PlayerModel<Player> mannequin;

// 当前可拾取的面（随着台面装备的槽位变化重建）
    private final List<Face> faces = new ArrayList<>();
// 可拾取几何体的中心（模型坐标，单位格），视角就是绕着它转的
    private final Vector3f center = new Vector3f();
// 这些面总共多大，用来决定默认缩放
    private float spanX = 1.0F;
    private float spanY = 1.0F;
// 已经为哪个槽位建过几何体
    private EquipmentSlot builtSlot;
// 建几何体时那份体素表的指纹，体素一变（自己放了 / 服务端同步了）就重建
    private int builtVoxelHash = -1;

// 视角：绕竖直轴 / 绕水平轴，单位度
    private float yaw;
    private float pitch;
    private double zoom = 1.0D;
    private double baseFit = 40.0D;

// 鼠标当前压在可见面上的命中，由近到远（每帧刷新，用来画高亮框、决定落笔）
    private List<Hit> hoverStack = List.of();
// 在 hoverStack 里选中的那一层（右键往下钻，好画到被胳膊 / 腿挡住的面）
    private int hoverLayer;
// 被玩家用「隐藏 / 显示」按钮扣下去的 cube，藏起来的既不再画也不再被拾取
    private final boolean[] hiddenCubes = new boolean[SilkDesign.CUBE_COUNT];

    private DynamicTexture previewTexture;
    private NativeImage previewImage;
    private EquipmentSlot previewSlot;
    private int previewHash;

// 工具状态
    private int tool = TOOL_PAINT;
// 当前颜色（HSV，都是 0~1），可以点色板取，也可以吸取画布上的像素
    private float hue;
    private float sat = 1.0F;
    private float val = 1.0F;

    private boolean rotating;
    private double rotateMouseX;
    private double rotateMouseY;

// 正在色板 / 明度条上拖动取色
    private boolean pickingColor;

    private boolean painting;
    private int paintCube = -1;
    private int paintFace = -1;
    private int lastCol = Integer.MIN_VALUE;
    private int lastRow = Integer.MIN_VALUE;

// 体素工具：正按着左键连续摆放
    private boolean building;
// 上一格已经落过方块的位置，拖动时同一格不重复扣丝绸
    private int builtCube = -1;
    private int builtI;
    private int builtJ;
    private int builtK;

// 体素光标（要落的那个小方块）当前的位置
    private boolean ghostValid;
    private int ghostCube = -1;
    private int ghostI;
    private int ghostJ;
    private int ghostK;

// 这一笔（从按下到松开）还没有向服务端落过子。
//
// 落笔时带上这个标记，服务端就趁这一笔还没开始时记一个回退点；之后的连续拖动不再重复记，
// 于是「回退」一次撤回的是完整的一笔，而不是一个像素。
    private boolean strokePending;

    public SewingMachineScreen(SewingMachineMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = SewingMachineMenu.IMAGE_WIDTH;
        this.imageHeight = SewingMachineMenu.IMAGE_HEIGHT;
        this.inventoryLabelY = SewingMachineMenu.INVENTORY_LABEL_Y;
        this.inventoryLabelX = SewingMachineMenu.PLAYER_INV_X;

        this.root = LayerDefinition
                .create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32)
                .bakeRoot();
        this.model = new HumanoidModel<>(this.root);
// 默认 young = true 会让模型按婴儿缩放，预览要原尺寸
        this.model.young = false;

// 模特用的是玩家皮肤的 64×64 布局（和上面那套盔甲 64×32 布局不一样），单独烘一份。
// 缩进去 0.25px：盔甲本来就比史蒂夫厚，这样整具模特都塞在衣物的壳里，
// 衣物擦成透明的地方就能看见里面的身体，不会是个空壳。
        this.mannequinRoot = LayerDefinition
                .create(PlayerModel.createMesh(new CubeDeformation(-0.25F), false), 64, 64)
                .bakeRoot();
        this.mannequin = new PlayerModel<>(this.mannequinRoot, false);
        this.mannequin.young = false;
    }

    @Override
    protected void init() {
        super.init();
        this.applyPageLayout();
    }

// 两页各用一套界面尺寸：装备页没有画布，收紧成一个标准物品栏大小；
// 绘制页才是那块宽画布。尺寸变了要顺手把界面重新居中。
    private void applyPageLayout() {
        boolean armor = this.page() == SewingMachineMenu.PAGE_ARMOR;
        this.imageWidth = armor ? SewingMachineMenu.ARMOR_IMAGE_WIDTH : SewingMachineMenu.IMAGE_WIDTH;
        this.imageHeight = armor ? SewingMachineMenu.ARMOR_IMAGE_HEIGHT : SewingMachineMenu.IMAGE_HEIGHT;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

// 几何体 / 视角

// 台面装备的槽位或体素变了就重建可拾取的面
    private void rebuildGeometry() {
        EquipmentSlot slot = this.menu.garmentSlot();
        List<Integer> voxels = this.menu.displayVoxels();
        int voxelHash = voxels.hashCode();
        boolean slotChanged = slot != this.builtSlot;
        if (!slotChanged && voxelHash == this.builtVoxelHash) {
            return;
        }
        this.builtSlot = slot;
        this.builtVoxelHash = voxelHash;
        this.faces.clear();
        if (slotChanged) {
// 换了件装备，之前藏起来的 cube 就不作数了
            java.util.Arrays.fill(this.hiddenCubes, false);
            this.hoverLayer = 0;
            this.hoverStack = List.of();
            this.ghostValid = false;
        }
        configureVisibility(slot);
        if (slot != null) {
            boolean[] wanted = new boolean[SilkDesign.CUBE_COUNT];
            for (int id : SilkDesign.cubesFor(slot)) {
                wanted[id] = true;
            }
// 走一遍骨架，把每个部件的变换和 cube 拿出来（根部件没有平移，所以直接就是部件自己的变换）
            this.root.visit(new PoseStack(), (pose, path, index, cube) -> {
                int id = cubeIdOf(path);
                if (id < 0 || !wanted[id]) {
                    return;
                }
                addFaces(id, cube, pose.pose(), isMirrored(path));
            });
// 已经堆出来的小方块也要能被拾取，这样可以从它们身上继续往外长
            addVoxelFaces(voxels, wanted);
        }
        computeBounds();
    }

// 只显示当前槽位会用到的部件（和穿戴渲染层一致），再扣掉玩家手动藏起来的 cube
    private void configureVisibility(EquipmentSlot slot) {
        this.model.setAllVisible(false);
        if (slot == null) {
            return;
        }
        switch (slot) {
            case HEAD -> this.model.head.visible = !this.hiddenCubes[SilkDesign.CUBE_HEAD];
            case CHEST -> {
                this.model.body.visible = !this.hiddenCubes[SilkDesign.CUBE_BODY];
                this.model.rightArm.visible = !this.hiddenCubes[SilkDesign.CUBE_RIGHT_ARM];
                this.model.leftArm.visible = !this.hiddenCubes[SilkDesign.CUBE_LEFT_ARM];
            }
            case LEGS -> {
                this.model.body.visible = !this.hiddenCubes[SilkDesign.CUBE_BODY];
                this.model.rightLeg.visible = !this.hiddenCubes[SilkDesign.CUBE_RIGHT_LEG];
                this.model.leftLeg.visible = !this.hiddenCubes[SilkDesign.CUBE_LEFT_LEG];
            }
            case FEET -> {
                this.model.rightLeg.visible = !this.hiddenCubes[SilkDesign.CUBE_RIGHT_LEG];
                this.model.leftLeg.visible = !this.hiddenCubes[SilkDesign.CUBE_LEFT_LEG];
            }
            default -> {
            }
        }
    }

// 把某个 cube 的六个面摊成可拾取的四边形
    private void addFaces(int cube, ModelPart.Cube box, Matrix4f part, boolean mirror) {
        for (int face = 0; face < SilkDesign.FACE_COUNT; face++) {
            addFace(cube, face, mirror, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ,
                    false, 0, 0, 0, part);
        }
    }

// 堆出来的体素也摊成可拾取的面（一个体素就是一个 1×1×1 的格子），这样能从它身上继续往外长
    private void addVoxelFaces(List<Integer> voxels, boolean[] wanted) {
        int count = SilkDesign.voxelCount(voxels);
        for (int index = 0; index < count; index++) {
            int cube = SilkDesign.voxelCube(voxels, index);
            if (cube < 0 || cube >= SilkDesign.CUBE_COUNT || !wanted[cube]) {
                continue;
            }
            Face base = firstFaceOfCube(cube);
            if (base == null) {
                continue;
            }
            int vi = SilkDesign.voxelI(voxels, index);
            int vj = SilkDesign.voxelJ(voxels, index);
            int vk = SilkDesign.voxelK(voxels, index);
            float x = SilkDesign.originX(cube) + vi;
            float y = SilkDesign.originY(cube) + vj;
            float z = SilkDesign.originZ(cube) + vk;
            for (int face = 0; face < SilkDesign.FACE_COUNT; face++) {
                addFace(cube, face, SilkDesign.isMirrored(cube), x, y, z, x + 1.0F, y + 1.0F, z + 1.0F,
                        true, vi, vj, vk, base.part());
            }
        }
    }

    private void addFace(int cube, int face, boolean mirror,
                         float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                         boolean voxel, int vi, int vj, int vk, Matrix4f part) {
        Vector3f[] local = faceCorners(face, mirror, minX, minY, minZ, maxX, maxY, maxZ);
        Vector3f[] world = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            world[i] = toWorld(part, local[i].x, local[i].y, local[i].z);
        }
        this.faces.add(new Face(cube, face, mirror, minX, minY, minZ, maxX, maxY, maxZ,
                voxel, vi, vj, vk, part, world, local));
    }

// cube 局部坐标（模型像素）→ 模型坐标（格）
    private static Vector3f toWorld(Matrix4f part, float x, float y, float z) {
        return part.transformPosition(x / 16.0F, y / 16.0F, z / 16.0F, new Vector3f());
    }

// 一个面的四个角（局部坐标，单位模型像素），按一圈的顺序给。
//
// 镜像的 cube（左臂 / 左腿）在网格里，原版把 Direction.WEST 那个多边形放在了
// +X 侧、Direction.EAST 放在了 -X 侧，所以这两个面的平面要按镜像与否互换。
    private static Vector3f[] faceCorners(int face, boolean mirror,
                                          float minX, float minY, float minZ,
                                          float maxX, float maxY, float maxZ) {
        return switch (face) {
            case SilkDesign.FACE_DOWN, SilkDesign.FACE_UP -> {
                float y = face == SilkDesign.FACE_DOWN ? minY : maxY;
                yield new Vector3f[]{
                        new Vector3f(minX, y, minZ),
                        new Vector3f(maxX, y, minZ),
                        new Vector3f(maxX, y, maxZ),
                        new Vector3f(minX, y, maxZ)};
            }
            case SilkDesign.FACE_NORTH, SilkDesign.FACE_SOUTH -> {
                float z = face == SilkDesign.FACE_NORTH ? minZ : maxZ;
                yield new Vector3f[]{
                        new Vector3f(minX, minY, z),
                        new Vector3f(maxX, minY, z),
                        new Vector3f(maxX, maxY, z),
                        new Vector3f(minX, maxY, z)};
            }
            default -> {
                float x = face == SilkDesign.FACE_WEST
                        ? (mirror ? maxX : minX)
                        : (mirror ? minX : maxX);
                yield new Vector3f[]{
                        new Vector3f(x, minY, minZ),
                        new Vector3f(x, minY, maxZ),
                        new Vector3f(x, maxY, maxZ),
                        new Vector3f(x, maxY, minZ)};
            }
        };
    }

    private void computeBounds() {
// 画布中央立着整个模特，取景按全身来算，保证头到脚都在画面里；
// 衣物部位只是全身的一部分，取景跟着全身走玩家才好对照着看。
        float[] b = {
                Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        this.root.visit(new PoseStack(), (pose, path, index, cube) -> {
// 头 / 身 / 双臂 / 双腿都算进来；原版的 hat 之类不算
            if (cubeIdOf(path) < 0) {
                return;
            }
            Matrix4f matrix = pose.pose();
            for (int xi = 0; xi < 2; xi++) {
                for (int yi = 0; yi < 2; yi++) {
                    for (int zi = 0; zi < 2; zi++) {
                        Vector3f p = matrix.transformPosition(
                                (xi == 0 ? cube.minX : cube.maxX) / 16.0F,
                                (yi == 0 ? cube.minY : cube.maxY) / 16.0F,
                                (zi == 0 ? cube.minZ : cube.maxZ) / 16.0F,
                                new Vector3f());
                        b[0] = Math.min(b[0], p.x);
                        b[1] = Math.min(b[1], p.y);
                        b[2] = Math.min(b[2], p.z);
                        b[3] = Math.max(b[3], p.x);
                        b[4] = Math.max(b[4], p.y);
                        b[5] = Math.max(b[5], p.z);
                    }
                }
            }
        });
        this.center.set((b[0] + b[3]) / 2.0F, (b[1] + b[4]) / 2.0F, (b[2] + b[5]) / 2.0F);
        this.spanX = b[3] - b[0];
        this.spanY = b[4] - b[1];
// 全身比单个部位大得多，默认缩放按最长边自适应
        double fit = Math.max(Math.max(this.spanX, this.spanY), b[5] - b[2]);
        this.baseFit = (SewingMachineMenu.CANVAS_SIZE - 2.0 * FIT_MARGIN) / Math.max(fit, 0.25F);
    }

// 3D 视口的变换：把模型坐标（格）映射到 GUI 屏幕坐标。
//
// 渲染和拾取都用它，所以「点到哪」和「看到哪」永远是同一套。
    private Matrix4f viewMatrix() {
        float cx = this.leftPos + SewingMachineMenu.CANVAS_X + SewingMachineMenu.CANVAS_SIZE / 2.0F;
        float cy = this.topPos + SewingMachineMenu.CANVAS_Y + SewingMachineMenu.CANVAS_SIZE / 2.0F;
        float scale = (float) (this.baseFit * this.zoom);
// 模型空间是 +Y 向下的，z 取反让镜头落在模型正面（-Z）那一侧；靠 z 的符号决定前后遮挡
        return new Matrix4f()
                .translate(cx, cy, VIEW_Z)
                .scale(scale, scale, -scale)
                .rotateX((float) Math.toRadians(this.pitch))
                .rotateY((float) Math.toRadians(this.yaw))
                .translate(-this.center.x, -this.center.y, -this.center.z);
    }

// 把这套视角逐项套到 poseStack 上（1.20.1 的 PoseStack 只接受四元数）。
// 顺序与 viewMatrix() 完全一致：平移 → 缩放 → 俯仰 → 偏航 → 回到模型中心，
// 这样法线矩阵也会跟着转，模型受光方向才对。
    private void applyView(PoseStack pose) {
        float cx = this.leftPos + SewingMachineMenu.CANVAS_X + SewingMachineMenu.CANVAS_SIZE / 2.0F;
        float cy = this.topPos + SewingMachineMenu.CANVAS_Y + SewingMachineMenu.CANVAS_SIZE / 2.0F;
        float scale = (float) (this.baseFit * this.zoom);
        pose.translate(cx, cy, VIEW_Z);
        pose.scale(scale, scale, -scale);
        pose.mulPose(Axis.XP.rotationDegrees(this.pitch));
        pose.mulPose(Axis.YP.rotationDegrees(this.yaw));
        pose.translate(-this.center.x, -this.center.y, -this.center.z);
    }

// 渲染

    private int page() {
        return this.menu.page();
    }

    private static Component trans(String key) {
        return Component.translatable("gui." + flavor_immersed_daily_argo.MODID + "." + key);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
// 与其他界面同一套半透明黑圆角底板；绘制页不显示任何槽位，也就不画格子
        GuiStyle.panel(gui, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        if (this.page() == SewingMachineMenu.PAGE_ARMOR) {
            for (Slot slot : this.menu.slots) {
                GuiStyle.slot(gui, this.leftPos + slot.x, this.topPos + slot.y);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
// 绘制页一个槽位都不显示，连标题和物品栏标签也省掉
        if (this.page() != SewingMachineMenu.PAGE_ARMOR) {
            return;
        }
        gui.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, TEXT_DIM_COLOR);
        caption(gui, trans("sewing_machine_armor"), SewingMachineMenu.ARMOR_SLOT_X, SewingMachineMenu.ARMOR_SLOT_Y);
        caption(gui, trans("sewing_machine_silk"), SewingMachineMenu.SILK_SLOT_X, SewingMachineMenu.SILK_SLOT_Y);
    }

// 槽位上方居中的小标题
    private void caption(GuiGraphics gui, Component label, int slotX, int slotY) {
        gui.drawString(this.font, label, slotX + 8 - this.font.width(label) / 2, slotY - 11, TEXT_DIM_COLOR);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        if (this.page() == SewingMachineMenu.PAGE_PAINT) {
            rebuildGeometry();
            refreshHover(mouseX, mouseY);
            updateGhost();
        }
        super.render(gui, mouseX, mouseY, partialTick);
        renderTabs(gui, mouseX, mouseY);
        if (this.page() == SewingMachineMenu.PAGE_ARMOR) {
            renderArmorButtons(gui, mouseX, mouseY);
        } else {
            renderViewport(gui);
            renderPalette(gui, mouseX, mouseY);
            renderPaintButtons(gui, mouseX, mouseY);
        }
        renderTooltip(gui, mouseX, mouseY);
    }

// 顶部两个页签
    private void renderTabs(GuiGraphics gui, int mouseX, int mouseY) {
        int x0 = this.leftPos;
        int y0 = this.topPos;
        tab(gui, mouseX, mouseY, x0 + SewingMachineMenu.TAB_ARMOR_X,
                trans("sewing_machine_page_armor"), this.page() == SewingMachineMenu.PAGE_ARMOR);
        tab(gui, mouseX, mouseY, x0 + SewingMachineMenu.TAB_PAINT_X,
                trans("sewing_machine_page_paint"), this.page() == SewingMachineMenu.PAGE_PAINT);
    }

    private void tab(GuiGraphics gui, int mouseX, int mouseY, int x, Component label, boolean active) {
        int y = this.topPos + SewingMachineMenu.TAB_Y;
        int w = SewingMachineMenu.TAB_W;
        int h = SewingMachineMenu.TAB_H;
        boolean hovered = inRect(mouseX, mouseY, x, y, w, h);
        GuiStyle.button(gui, x, y, w, h, RoundedButton.Style.NORMAL, active, hovered, true);
        gui.drawString(this.font, label, x + (w - this.font.width(label)) / 2, y + (h - 8) / 2,
                GuiStyle.buttonText(RoundedButton.Style.NORMAL, active, true));
    }

    private void renderViewport(GuiGraphics gui) {
        int viewX = this.leftPos + SewingMachineMenu.CANVAS_X;
        int viewY = this.topPos + SewingMachineMenu.CANVAS_Y;
        int viewEndX = viewX + SewingMachineMenu.CANVAS_SIZE;
        int viewEndY = viewY + SewingMachineMenu.CANVAS_SIZE;

        gui.enableScissor(viewX, viewY, viewEndX, viewEndY);

        EquipmentSlot slot = this.menu.garmentSlot();
        if (slot == null) {
            gui.drawString(this.font, trans("sewing_machine_no_garment"),
                    viewX + 4, viewY + SewingMachineMenu.CANVAS_SIZE / 2 - 4, TEXT_DIM_COLOR);
        } else {
            renderModel(gui, slot);
            renderVoxels(gui);
            renderVoxelGhost(gui);
            renderHoverHighlight(gui);
            String hint = this.tool == TOOL_VOXEL ? "sewing_machine_hint_voxel" : "sewing_machine_hint";
            gui.drawString(this.font, trans(hint), viewX + 3, viewEndY - 10, TEXT_DIM_COLOR);
        }

        gui.disableScissor();

        gui.fill(viewX - 1, viewY - 1, viewEndX + 1, viewY, BORDER_COLOR);
        gui.fill(viewX - 1, viewEndY, viewEndX + 1, viewEndY + 1, BORDER_COLOR);
        gui.fill(viewX - 1, viewY - 1, viewX, viewEndY + 1, BORDER_COLOR);
        gui.fill(viewEndX, viewY - 1, viewEndX + 1, viewEndY + 1, BORDER_COLOR);
    }

    private void renderModel(GuiGraphics gui, EquipmentSlot slot) {
        updatePreviewTexture(slot);
        if (this.previewTexture == null) {
            return;
        }
        gui.pose().pushPose();
        applyView(gui.pose());
        Lighting.setupForEntityInInventory();
        MultiBufferSource.BufferSource buffers = gui.bufferSource();
// 先把整具模特垫在底下，再把花纹贴图的衣物盖上去
        renderMannequin(gui, slot);
        VertexConsumer consumer = buffers.getBuffer(RenderType.armorCutoutNoCull(PREVIEW_TEXTURE));
        this.model.renderToBuffer(gui.pose(), consumer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        gui.flush();
        Lighting.setupFor3DItems();
        gui.pose().popPose();
    }

// 整具 Steve 都垫在衣物下面。衣物盖住的部位只画最里层（皮肤第二层会凸出去，得让给花纹），
// 其余部位整个 Steve 都露出来，玩家一眼能看出正在画哪块。
    private void renderMannequin(GuiGraphics gui, EquipmentSlot slot) {
        this.mannequin.setAllVisible(false);
        int[] garment = SilkDesign.cubesFor(slot);
        for (int cube = 0; cube < SilkDesign.CUBE_COUNT; cube++) {
            showMannequinCube(cube, !SilkDesignLayer.contains(garment, cube));
        }
        VertexConsumer consumer = gui.bufferSource().getBuffer(RenderType.entityCutoutNoCull(STEVE_TEXTURE));
        this.mannequin.renderToBuffer(gui.pose(), consumer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        gui.flush();
    }

// 让模特露出某个 cube 的部件；withOverlay=false 时只画最里层，免得皮肤第二层凸到衣物外面
    private void showMannequinCube(int cube, boolean withOverlay) {
        switch (cube) {
            case SilkDesign.CUBE_HEAD -> {
                this.mannequin.head.visible = true;
                this.mannequin.hat.visible = withOverlay;
            }
            case SilkDesign.CUBE_BODY -> {
                this.mannequin.body.visible = true;
                this.mannequin.jacket.visible = withOverlay;
            }
            case SilkDesign.CUBE_LEFT_ARM -> {
                this.mannequin.leftArm.visible = true;
                this.mannequin.leftSleeve.visible = withOverlay;
            }
            case SilkDesign.CUBE_RIGHT_ARM -> {
                this.mannequin.rightArm.visible = true;
                this.mannequin.rightSleeve.visible = withOverlay;
            }
            case SilkDesign.CUBE_LEFT_LEG -> {
                this.mannequin.leftLeg.visible = true;
                this.mannequin.leftPants.visible = withOverlay;
            }
            case SilkDesign.CUBE_RIGHT_LEG -> {
                this.mannequin.rightLeg.visible = true;
                this.mannequin.rightPants.visible = withOverlay;
            }
            default -> {
            }
        }
    }

// 花纹变了才重画贴图；整块界面从头到尾只用一张纹理，不反复注册
    private void updatePreviewTexture(EquipmentSlot slot) {
        if (this.previewTexture == null || this.previewSlot != slot) {
            NativeImage image = new NativeImage(64, 32, true);
            DynamicTexture texture = new DynamicTexture(image);
            texture.setFilter(false, false);
            Minecraft.getInstance().getTextureManager().register(PREVIEW_TEXTURE, texture);
            this.previewImage = image;
            this.previewTexture = texture;
            this.previewSlot = slot;
            this.previewHash = 0;
        }
        List<Integer> design = this.menu.displayDesign();
        int hash = design.hashCode();
        if (hash != this.previewHash) {
            SilkArmorTextures.paintInto(this.previewImage, design, slot);
            this.previewTexture.upload();
            this.previewHash = hash;
        }
    }

// 把当前选中的那个面沿边描一圈，玩家就能看清落笔落在哪一层上
    private void renderHoverHighlight(GuiGraphics gui) {
        if (this.hoverStack.isEmpty()) {
            return;
        }
        int layer = Mth.clamp(this.hoverLayer, 0, this.hoverStack.size() - 1);
        Face face = this.hoverStack.get(layer).face();
        Matrix4f matrix = viewMatrix();
        Vector3f[] pts = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            pts[i] = matrix.transformPosition(face.world()[i], new Vector3f());
        }
// 最前面那层用白色，被别的面挡住的层用金黄，提示「再右键可以换回去」
        int color = this.hoverStack.size() > 1 && layer > 0
                ? HIGHLIGHT_HIDDEN_COLOR : HIGHLIGHT_COLOR;
        for (int i = 0; i < 4; i++) {
            Vector3f from = pts[i];
            Vector3f to = pts[(i + 1) % 4];
            drawLine(gui, from.x, from.y, to.x, to.y, color);
        }
    }

// 把堆出来的体素画成一个个上色的真立方体（和穿在身上用的是同一套画法）
    private void renderVoxels(GuiGraphics gui) {
        List<Integer> voxels = this.menu.displayVoxels();
        int count = SilkDesign.voxelCount(voxels);
        if (count == 0) {
            return;
        }
        Matrix4f view = viewMatrix();
        VertexConsumer consumer = gui.bufferSource().getBuffer(RenderType.debugQuads());
        for (int index = 0; index < count; index++) {
            int cube = SilkDesign.voxelCube(voxels, index);
            if (this.hiddenCubes[cube]) {
                continue;
            }
            Face base = firstFaceOfCube(cube);
            if (base == null) {
                continue;
            }
            Matrix4f matrix = new Matrix4f(view).mul(base.part());
            SilkDesignLayer.addVoxelCube(consumer, matrix,
                    SilkDesign.originX(cube) + SilkDesign.voxelI(voxels, index),
                    SilkDesign.originY(cube) + SilkDesign.voxelJ(voxels, index),
                    SilkDesign.originZ(cube) + SilkDesign.voxelK(voxels, index),
                    SilkDesign.voxelColor(voxels, index));
        }
        gui.flush();
    }

// 体素工具的光标：要在哪一格落方块，就在那一格描一个线框
    private void renderVoxelGhost(GuiGraphics gui) {
        if (!this.ghostValid) {
            return;
        }
        Face base = firstFaceOfCube(this.ghostCube);
        if (base == null) {
            return;
        }
        float x = SilkDesign.originX(this.ghostCube) + this.ghostI;
        float y = SilkDesign.originY(this.ghostCube) + this.ghostJ;
        float z = SilkDesign.originZ(this.ghostCube) + this.ghostK;
        int color = this.menu.hasSilk() ? VOXEL_COLOR : VOXEL_COLOR_EMPTY;
        drawBoxEdges(gui, base.part(), x, y, z, x + 1.0F, y + 1.0F, z + 1.0F, color);
    }

// 把一个盒子（局部坐标，模型像素）的 12 条棱投到屏幕上描出来
    private void drawBoxEdges(GuiGraphics gui, Matrix4f part,
                              float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        Matrix4f matrix = viewMatrix();
        Vector3f[] corners = new Vector3f[8];
        for (int xi = 0; xi < 2; xi++) {
            for (int yi = 0; yi < 2; yi++) {
                for (int zi = 0; zi < 2; zi++) {
                    Vector3f world = toWorld(part, xi == 0 ? x1 : x2, yi == 0 ? y1 : y2, zi == 0 ? z1 : z2);
                    corners[xi * 4 + yi * 2 + zi] = matrix.transformPosition(world, new Vector3f());
                }
            }
        }
// 12 条棱：相邻两个顶点只差一位
        int[][] edges = {
                {0, 1}, {2, 3}, {4, 5}, {6, 7},
                {0, 2}, {1, 3}, {4, 6}, {5, 7},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        for (int[] e : edges) {
            Vector3f a = corners[e[0]];
            Vector3f b = corners[e[1]];
            drawLine(gui, a.x, a.y, b.x, b.y, color);
        }
    }

// GuiGraphics 没有画线，这里用小方块沿线段铺出来
    private static void drawLine(GuiGraphics gui, double x0, double y0, double x1, double y1, int color) {
        int steps = (int) Math.ceil(Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0)));
        if (steps <= 0) {
            gui.fill((int) Math.round(x0), (int) Math.round(y0),
                    (int) Math.round(x0) + 1, (int) Math.round(y0) + 1, color);
            return;
        }
        for (int s = 0; s <= steps; s++) {
            int px = (int) Math.round(x0 + (x1 - x0) * s / steps);
            int py = (int) Math.round(y0 + (y1 - y0) * s / steps);
            gui.fill(px, py, px + 1, py + 1, color);
        }
    }

// 色板 / 明度条

// 一张真正的 RGB 色板：纵向是色相（一圈彩虹），横向是饱和度（左边白、右边纯色），
// 点哪就是哪的颜色。右边那条竖条是明度，把当前色往黑里拉。
    private void renderPalette(GuiGraphics gui, int mouseX, int mouseY) {
        int px = this.leftPos + SewingMachineMenu.PALETTE_X;
        int py = this.topPos + SewingMachineMenu.PALETTE_Y;
        int pw = SewingMachineMenu.PALETTE_W;
        int ph = SewingMachineMenu.PALETTE_H;

// 每一行一个色相，从白到纯色线性插值（正好等于 HSV 的饱和度渐变）
        for (int row = 0; row < ph; row++) {
            float h = (row + 0.5F) / ph;
            int full = (0xFF000000 | Mth.hsvToRgb(h, 1.0F, 1.0F));
            gui.fillGradient(px, py + row, px + pw, py + row + 1, 0xFFFFFFFF, full);
        }

// 明度条
        int vx = this.leftPos + SewingMachineMenu.VALUE_X;
        int vy = this.topPos + SewingMachineMenu.VALUE_Y;
        int vw = SewingMachineMenu.VALUE_W;
        int vh = SewingMachineMenu.VALUE_H;
        for (int row = 0; row < vh; row++) {
            float v = 1.0F - (row + 0.5F) / vh;
            gui.fill(vx, vy + row, vx + vw, vy + row + 1, (0xFF000000 | Mth.hsvToRgb(this.hue, this.sat, v)));
        }

        border(gui, px, py, pw, ph, mouseX, mouseY);
        border(gui, vx, vy, vw, vh, mouseX, mouseY);

// 色板 / 明度条上的位置标记
        int markX = px + Math.min(pw - 5, Math.max(0, (int) (this.sat * pw) - 2));
        int markY = py + Math.min(ph - 5, Math.max(0, (int) (this.hue * ph) - 2));
        outline(gui, markX, markY, 5, 5);
        int vMarkY = vy + Math.min(vh - 3, Math.max(0, (int) ((1.0F - this.val) * vh) - 1));
        outline(gui, vx, vMarkY, vw, 3);

// 当前色预览
        int sx = this.leftPos + SewingMachineMenu.SWATCH_X;
        int sy = this.topPos + SewingMachineMenu.SWATCH_Y;
        int ss = SewingMachineMenu.SWATCH_SIZE;
        gui.fill(sx - 1, sy - 1, sx + ss + 1, sy + ss + 1, 0xFF000000);
        gui.fill(sx, sy, sx + ss, sy + ss, this.selectedColor());
    }

    private static void border(GuiGraphics gui, int x, int y, int w, int h, int mouseX, int mouseY) {
        int color = inRect(mouseX, mouseY, x, y, w, h) ? 0xFFFFFFFF : BORDER_COLOR;
        gui.fill(x - 1, y - 1, x + w + 1, y, color);
        gui.fill(x - 1, y + h, x + w + 1, y + h + 1, color);
        gui.fill(x - 1, y - 1, x, y + h + 1, color);
        gui.fill(x + w, y - 1, x + w + 1, y + h + 1, color);
    }

    private static void outline(GuiGraphics gui, int x, int y, int w, int h) {
        gui.fill(x - 1, y - 1, x + w + 1, y, 0xFF000000);
        gui.fill(x - 1, y + h, x + w + 1, y + h + 1, 0xFF000000);
        gui.fill(x - 1, y - 1, x, y + h + 1, 0xFF000000);
        gui.fill(x + w, y - 1, x + w + 1, y + h + 1, 0xFF000000);
    }

// 当前 HSV 对应的 ARGB
    private int selectedColor() {
        return (0xFF000000 | Mth.hsvToRgb(this.hue, this.sat, this.val));
    }

// 按钮

    private void renderArmorButtons(GuiGraphics gui, int mouseX, int mouseY) {
        int x0 = this.leftPos;
        int y0 = this.topPos;
        button(gui, mouseX, mouseY, x0 + SewingMachineMenu.BTN_LEFT_X, y0 + SewingMachineMenu.BTN_DRESS_Y,
                SewingMachineMenu.BTN_LEFT_W, SewingMachineMenu.BTN_LEFT_H,
                trans("sewing_machine_dress"), false, this.menu.hasGarment());
        button(gui, mouseX, mouseY, x0 + SewingMachineMenu.BTN_LEFT_X, y0 + SewingMachineMenu.BTN_UNDRESS_Y,
                SewingMachineMenu.BTN_LEFT_W, SewingMachineMenu.BTN_LEFT_H,
                trans("sewing_machine_undress"), false, this.menu.hasGarment());
    }

    private void renderPaintButtons(GuiGraphics gui, int mouseX, int mouseY) {
        int x0 = this.leftPos;
        int y0 = this.topPos;
        int w = SewingMachineMenu.TOOL_W;
        int h = SewingMachineMenu.TOOL_H;
        boolean hasGarment = this.menu.garmentSlot() != null;

        button(gui, mouseX, mouseY, x0 + SewingMachineMenu.TOOL_X1, y0 + SewingMachineMenu.TOOL_ROW1_Y, w, h,
                trans("sewing_machine_paint"), this.tool == TOOL_PAINT, true);
        button(gui, mouseX, mouseY, x0 + SewingMachineMenu.TOOL_X2, y0 + SewingMachineMenu.TOOL_ROW1_Y, w, h,
                trans("sewing_machine_erase"), this.tool == TOOL_ERASE, true);
        button(gui, mouseX, mouseY, x0 + SewingMachineMenu.TOOL_X1, y0 + SewingMachineMenu.TOOL_ROW2_Y, w, h,
                trans("sewing_machine_stamp"), this.tool == TOOL_VOXEL, true);
        button(gui, mouseX, mouseY, x0 + SewingMachineMenu.TOOL_X2, y0 + SewingMachineMenu.TOOL_ROW2_Y, w, h,
                trans("sewing_machine_pick"), this.tool == TOOL_PICK, true);
        button(gui, mouseX, mouseY, x0 + SewingMachineMenu.TOOL_X1, y0 + SewingMachineMenu.TOOL_ROW3_Y,
                SewingMachineMenu.TOOL_WIDE_W, h, trans("sewing_machine_hide"), false, hasGarment);
// 画布正下方的空位：回退上一步（不退丝绸）
        button(gui, mouseX, mouseY, x0 + SewingMachineMenu.BTN_UNDO_X, y0 + SewingMachineMenu.BTN_UNDO_Y,
                SewingMachineMenu.BTN_UNDO_W, SewingMachineMenu.BTN_UNDO_H,
                trans("sewing_machine_undo"), false, hasGarment);
        button(gui, mouseX, mouseY, x0 + SewingMachineMenu.BTN_SAVE_X, y0 + SewingMachineMenu.BTN_SAVE_Y,
                SewingMachineMenu.BTN_BOTTOM_W, SewingMachineMenu.BTN_BOTTOM_H,
                trans("sewing_machine_save"), false, true);
        button(gui, mouseX, mouseY, x0 + SewingMachineMenu.BTN_TAKE_X, y0 + SewingMachineMenu.BTN_TAKE_Y,
                SewingMachineMenu.BTN_BOTTOM_W, SewingMachineMenu.BTN_BOTTOM_H,
                trans("sewing_machine_take"), false, true);
    }

    private void button(GuiGraphics gui, int mouseX, int mouseY, int x, int y, int w, int h,
                        Component label, boolean selected, boolean enabled) {
        boolean hovered = enabled && inRect(mouseX, mouseY, x, y, w, h);
        GuiStyle.button(gui, x, y, w, h, RoundedButton.Style.NORMAL, selected, hovered, enabled);
        gui.drawString(this.font, label, x + (w - this.font.width(label)) / 2, y + (h - 8) / 2,
                GuiStyle.buttonText(RoundedButton.Style.NORMAL, selected, enabled));
    }

// 拾取

// 刷新鼠标当前压在可见面上的命中
    private void refreshHover(double mouseX, double mouseY) {
        if (!inViewport(mouseX, mouseY) || this.menu.garmentSlot() == null) {
            this.hoverStack = List.of();
            return;
        }
        this.hoverStack = pickStack(mouseX, mouseY);
    }

// 把鼠标位置投到模型上，返回它压着的所有可见面，按离镜头从近到远排好
    private List<Hit> pickStack(double mouseX, double mouseY) {
        List<Hit> hits = new ArrayList<>();
        if (this.faces.isEmpty()) {
            return hits;
        }
        Matrix4f matrix = viewMatrix();
        Vector3f[] screen = new Vector3f[4];
        boolean voxelTool = this.tool == TOOL_VOXEL;
        for (Face face : this.faces) {
            if (this.hiddenCubes[face.cube()]) {
                continue;
            }
// 只有体素工具才把已经堆出来的方块当成拾取目标；画笔 / 橡皮仍然直接画在身体那层上
            if (face.voxel() && !voxelTool) {
                continue;
            }
            Vector3f[] world = face.world();
            for (int i = 0; i < 4; i++) {
                screen[i] = matrix.transformPosition(world[i], new Vector3f());
            }
// 四边形拆成两个三角形逐个判包含
            for (int t = 0; t < 2; t++) {
                int a = 0;
                int b = t + 1;
                int c = t + 2;
                double area = cross(screen[a], screen[b], screen[c].x, screen[c].y);
                if (Math.abs(area) < 1.0E-4D) {
                    continue;
                }
                double wa = cross(screen[b], screen[c], mouseX, mouseY) / area;
                double wb = cross(screen[c], screen[a], mouseX, mouseY) / area;
                double wc = cross(screen[a], screen[b], mouseX, mouseY) / area;
                if (wa < 0.0D || wb < 0.0D || wc < 0.0D) {
                    continue;
                }
                Hit hit = hitAt(face, screen, a, b, c, wa, wb, wc);
                if (hit != null) {
                    hits.add(hit);
                }
            }
        }
        hits.sort((p, q) -> Double.compare(q.depth(), p.depth()));
        return hits;
    }

// 用重心坐标把一次三角形命中换算成「cube / 面 / 像素 / 深度」
    private Hit hitAt(Face face, Vector3f[] screen, int a, int b, int c,
                      double wa, double wb, double wc) {
        Vector3f[] local = face.local();
        float lx = (float) (wa * local[a].x + wb * local[b].x + wc * local[c].x);
        float ly = (float) (wa * local[a].y + wb * local[b].y + wc * local[c].y);
        float lz = (float) (wa * local[a].z + wb * local[b].z + wc * local[c].z);

        int w = Math.round(face.maxX() - face.minX());
        int d = Math.round(face.maxZ() - face.minZ());
        int i = (int) Math.floor(lx - face.minX());
        int j = (int) Math.floor(ly - face.minY());
        int k = (int) Math.floor(lz - face.minZ());
        int col;
        int row;
        switch (face.face()) {
            case SilkDesign.FACE_DOWN, SilkDesign.FACE_UP -> {
                col = face.mirror() ? w - 1 - i : i;
                row = d - 1 - k;
            }
            case SilkDesign.FACE_NORTH -> {
                col = face.mirror() ? w - 1 - i : i;
                row = j;
            }
            case SilkDesign.FACE_SOUTH -> {
                col = face.mirror() ? i : w - 1 - i;
                row = j;
            }
            case SilkDesign.FACE_WEST -> {
                col = d - 1 - k;
                row = j;
            }
            default -> {
                col = k;
                row = j;
            }
        }
        if (col < 0 || col >= SilkDesign.faceWidth(face.cube(), face.face())
                || row < 0 || row >= SilkDesign.faceHeight(face.cube(), face.face())) {
            return null;
        }
        double depth = wa * screen[a].z + wb * screen[b].z + wc * screen[c].z;
        return new Hit(face, col, row, depth);
    }

// 按当前选中的穿透层取一个命中面
    private Hit pickSelected(double mouseX, double mouseY) {
        List<Hit> stack = pickStack(mouseX, mouseY);
        if (stack.isEmpty()) {
            return null;
        }
        int layer = Mth.clamp(this.hoverLayer, 0, stack.size() - 1);
        return stack.get(layer);
    }

// 找到某个 cube 在身体上的那一个面（用来拿它的部件变换）
    private Face firstFaceOfCube(int cube) {
        for (Face candidate : this.faces) {
            if (candidate.cube() == cube && !candidate.voxel()) {
                return candidate;
            }
        }
        return null;
    }

    private static double cross(Vector3f a, Vector3f b, double px, double py) {
        return (b.x - a.x) * (py - a.y) - (b.y - a.y) * (px - a.x);
    }

// 输入

    private boolean inViewport(double mouseX, double mouseY) {
        return mouseX >= this.leftPos + SewingMachineMenu.CANVAS_X
                && mouseX < this.leftPos + SewingMachineMenu.CANVAS_X + SewingMachineMenu.CANVAS_SIZE
                && mouseY >= this.topPos + SewingMachineMenu.CANVAS_Y
                && mouseY < this.topPos + SewingMachineMenu.CANVAS_Y + SewingMachineMenu.CANVAS_SIZE;
    }

    private static boolean inRect(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x0 = this.leftPos;
        int y0 = this.topPos;

// 顶部页签
        if (button == 0) {
            if (inRect(mouseX, mouseY, x0 + SewingMachineMenu.TAB_ARMOR_X, y0 + SewingMachineMenu.TAB_Y,
                    SewingMachineMenu.TAB_W, SewingMachineMenu.TAB_H)) {
                this.menu.setPage(SewingMachineMenu.PAGE_ARMOR);
                this.applyPageLayout();
                return true;
            }
            if (inRect(mouseX, mouseY, x0 + SewingMachineMenu.TAB_PAINT_X, y0 + SewingMachineMenu.TAB_Y,
                    SewingMachineMenu.TAB_W, SewingMachineMenu.TAB_H)) {
                this.menu.setPage(SewingMachineMenu.PAGE_PAINT);
                this.applyPageLayout();
                return true;
            }
        }

        return this.page() == SewingMachineMenu.PAGE_ARMOR
                ? clickArmorPage(mouseX, mouseY, button, x0, y0)
                : clickPaintPage(mouseX, mouseY, button, x0, y0);
    }

    private boolean clickArmorPage(double mouseX, double mouseY, int button, int x0, int y0) {
        if (button == 0) {
            if (inRect(mouseX, mouseY, x0 + SewingMachineMenu.BTN_LEFT_X, y0 + SewingMachineMenu.BTN_DRESS_Y,
                    SewingMachineMenu.BTN_LEFT_W, SewingMachineMenu.BTN_LEFT_H)) {
                sendButton(SewingMachineMenu.BUTTON_DRESS);
                return true;
            }
            if (inRect(mouseX, mouseY, x0 + SewingMachineMenu.BTN_LEFT_X, y0 + SewingMachineMenu.BTN_UNDRESS_Y,
                    SewingMachineMenu.BTN_LEFT_W, SewingMachineMenu.BTN_LEFT_H)) {
                sendButton(SewingMachineMenu.BUTTON_UNDRESS);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean clickPaintPage(double mouseX, double mouseY, int button, int x0, int y0) {
        int w = SewingMachineMenu.TOOL_W;
        int h = SewingMachineMenu.TOOL_H;

// 色板 / 明度条要能一路拖着取色，所以点下去后 return true 让 mouseDragged 跟上
        if (button == 0) {
            if (pickFromPalette(mouseX, mouseY)) {
                this.pickingColor = true;
                return true;
            }
            if (inRect(mouseX, mouseY, x0 + SewingMachineMenu.TOOL_X1, y0 + SewingMachineMenu.TOOL_ROW1_Y, w, h)) {
                this.tool = TOOL_PAINT;
                return true;
            }
            if (inRect(mouseX, mouseY, x0 + SewingMachineMenu.TOOL_X2, y0 + SewingMachineMenu.TOOL_ROW1_Y, w, h)) {
                this.tool = TOOL_ERASE;
                return true;
            }
            if (inRect(mouseX, mouseY, x0 + SewingMachineMenu.TOOL_X1, y0 + SewingMachineMenu.TOOL_ROW2_Y, w, h)) {
                this.tool = TOOL_VOXEL;
                return true;
            }
            if (inRect(mouseX, mouseY, x0 + SewingMachineMenu.TOOL_X2, y0 + SewingMachineMenu.TOOL_ROW2_Y, w, h)) {
                this.tool = TOOL_PICK;
                return true;
            }
            if (inRect(mouseX, mouseY, x0 + SewingMachineMenu.TOOL_X1, y0 + SewingMachineMenu.TOOL_ROW3_Y,
                    SewingMachineMenu.TOOL_WIDE_W, h)) {
                toggleHide();
                return true;
            }
            if (inRect(mouseX, mouseY, x0 + SewingMachineMenu.BTN_UNDO_X, y0 + SewingMachineMenu.BTN_UNDO_Y,
                    SewingMachineMenu.BTN_UNDO_W, SewingMachineMenu.BTN_UNDO_H)) {
                sendButton(SewingMachineMenu.BUTTON_UNDO);
                return true;
            }
            if (inRect(mouseX, mouseY, x0 + SewingMachineMenu.BTN_SAVE_X, y0 + SewingMachineMenu.BTN_SAVE_Y,
                    SewingMachineMenu.BTN_BOTTOM_W, SewingMachineMenu.BTN_BOTTOM_H)) {
                sendButton(SewingMachineMenu.BUTTON_SAVE);
                return true;
            }
            if (inRect(mouseX, mouseY, x0 + SewingMachineMenu.BTN_TAKE_X, y0 + SewingMachineMenu.BTN_TAKE_Y,
                    SewingMachineMenu.BTN_BOTTOM_W, SewingMachineMenu.BTN_BOTTOM_H)) {
                sendButton(SewingMachineMenu.BUTTON_TAKE);
                return true;
            }
        }

// 3D 视口
        if (inViewport(mouseX, mouseY) && this.menu.garmentSlot() != null) {
            List<Hit> visible = pickStack(mouseX, mouseY);
            if (!visible.isEmpty()) {
                if (button == 1) {
                    if (this.tool == TOOL_VOXEL) {
// 体素工具右键：拆掉鼠标下面那个小方块
                        this.strokePending = true;
                        removeVoxelUnder(visible);
                        return true;
                    }
// 其他工具右键：往下钻一层，好把被胳膊 / 腿挡住的那些面也画到
                    this.hoverStack = visible;
                    this.hoverLayer = (Mth.clamp(this.hoverLayer, 0, visible.size() - 1) + 1) % visible.size();
                    return true;
                }
                if (button == 0) {
                    this.hoverStack = visible;
                    this.hoverLayer = Mth.clamp(this.hoverLayer, 0, visible.size() - 1);
                    Hit hit = visible.get(this.hoverLayer);
                    if (this.tool == TOOL_PICK) {
                        pickFromCanvas(hit);
                        return true;
                    }
                    if (this.tool == TOOL_VOXEL) {
                        this.building = true;
                        this.builtCube = -1;
                        this.strokePending = true;
                        placeVoxel(hit);
                        return true;
                    }
                    this.painting = true;
                    this.paintCube = -1;
                    this.paintFace = -1;
                    this.lastCol = Integer.MIN_VALUE;
                    this.lastRow = Integer.MIN_VALUE;
                    this.strokePending = true;
                    strokeTo(hit);
                    return true;
                }
            }
            if (button == 0) {
// 左键点在空白处 → 拖拽旋转视角
                this.rotating = true;
                this.rotateMouseX = mouseX;
                this.rotateMouseY = mouseY;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.pickingColor) {
            pickFromPalette(mouseX, mouseY);
            return true;
        }
        if (this.rotating) {
            this.yaw += (float) ((mouseX - this.rotateMouseX) * ROTATE_SENSITIVITY);
            this.pitch = Mth.clamp(
                    this.pitch + (float) ((mouseY - this.rotateMouseY) * ROTATE_SENSITIVITY),
                    -MAX_PITCH, MAX_PITCH);
            this.rotateMouseX = mouseX;
            this.rotateMouseY = mouseY;
            return true;
        }
        if (this.building) {
// 拖着不放就沿着鼠标划过的面连续摆放，等于用方块「画」出一条线
            Hit hit = pickSelected(mouseX, mouseY);
            if (hit != null) {
                placeVoxel(hit);
            }
            return true;
        }
        if (this.painting) {
            Hit hit = pickSelected(mouseX, mouseY);
            if (hit != null) {
                strokeTo(hit);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasBuilding = this.building;
        this.building = false;
        this.builtCube = -1;
        this.painting = false;
        this.rotating = false;
        this.pickingColor = false;
        if (wasBuilding) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (this.page() == SewingMachineMenu.PAGE_PAINT && inViewport(mouseX, mouseY)) {
            this.zoom = Mth.clamp(this.zoom * Math.pow(1.15D, scrollY), MIN_ZOOM, MAX_ZOOM);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    private void sendButton(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

// 取色 / 隐藏

// 点色板或明度条；命中就更新当前 HSV 并返回 true
    private boolean pickFromPalette(double mouseX, double mouseY) {
        int px = this.leftPos + SewingMachineMenu.PALETTE_X;
        int py = this.topPos + SewingMachineMenu.PALETTE_Y;
        int pw = SewingMachineMenu.PALETTE_W;
        int ph = SewingMachineMenu.PALETTE_H;
        if (inRect(mouseX, mouseY, px, py, pw, ph)) {
            this.hue = Mth.clamp((float) ((mouseY - py) / (double) ph), 0.0F, 1.0F - 1.0E-4F);
            this.sat = Mth.clamp((float) ((mouseX - px) / (double) pw), 0.0F, 1.0F);
            return true;
        }
        int vx = this.leftPos + SewingMachineMenu.VALUE_X;
        int vy = this.topPos + SewingMachineMenu.VALUE_Y;
        if (inRect(mouseX, mouseY, vx, vy, SewingMachineMenu.VALUE_W, SewingMachineMenu.VALUE_H)) {
            this.val = Mth.clamp(1.0F - (float) ((mouseY - vy) / (double) SewingMachineMenu.VALUE_H), 0.0F, 1.0F);
            return true;
        }
        return false;
    }

// 取色工具：把画布上这个像素的颜色吸到当前色里
    private void pickFromCanvas(Hit hit) {
        int index = SilkDesign.index(hit.face().cube(), hit.face().face(), hit.col(), hit.row());
        int argb = this.menu.displayDesign().get(index);
// 没画过和擦出来的透明洞都不算颜色
        if (argb == SilkDesign.UNPAINTED || (argb >>> 24) == 0) {
            return;
        }
        float[] hsv = rgbToHsv(argb);
        this.hue = hsv[0];
        this.sat = hsv[1];
        this.val = hsv[2];
    }

// ARGB → {hue, sat, val}，都是 0~1
    private static float[] rgbToHsv(int argb) {
        float r = ((argb >> 16) & 0xFF) / 255.0F;
        float g = ((argb >> 8) & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float h;
        if (delta == 0.0F) {
            h = 0.0F;
        } else if (max == r) {
            h = ((g - b) / delta) % 6.0F;
        } else if (max == g) {
            h = (b - r) / delta + 2.0F;
        } else {
            h = (r - g) / delta + 4.0F;
        }
        h /= 6.0F;
        if (h < 0.0F) {
            h += 1.0F;
        }
        float s = max == 0.0F ? 0.0F : delta / max;
        return new float[]{h, s, max};
    }

// 「隐藏 / 显示」按钮：把当前槽位用到的 cube 依次扣掉，全扣完再点就全部放回来。
// 这样手上总有按钮能把藏起来的部分再显示出来。
    private void toggleHide() {
        EquipmentSlot slot = this.menu.garmentSlot();
        if (slot == null) {
            return;
        }
        int[] cubes = SilkDesign.cubesFor(slot);
        int next = -1;
        for (int cube : cubes) {
            if (!this.hiddenCubes[cube]) {
                next = cube;
                break;
            }
        }
        if (next < 0) {
            for (int cube : cubes) {
                this.hiddenCubes[cube] = false;
            }
        } else {
            this.hiddenCubes[next] = true;
        }
        configureVisibility(slot);
        this.hoverLayer = 0;
        this.hoverStack = List.of();
    }

// 落笔

// 沿着上一笔到这一笔的连线补点，快速拖动时不会漏格
    private void strokeTo(Hit hit) {
        int cube = hit.face().cube();
        int face = hit.face().face();
        int col = Mth.clamp(hit.col(), 0, SilkDesign.faceWidth(cube, face) - 1);
        int row = Mth.clamp(hit.row(), 0, SilkDesign.faceHeight(cube, face) - 1);
        if (cube != this.paintCube || face != this.paintFace) {
// 换了个面，之前的连线就不算了
            this.paintCube = cube;
            this.paintFace = face;
            this.lastCol = Integer.MIN_VALUE;
        }
        if (this.lastCol == Integer.MIN_VALUE) {
            drawPixel(cube, face, col, row);
        } else {
            int dx = Math.abs(col - this.lastCol);
            int dy = Math.abs(row - this.lastRow);
            int steps = Math.max(dx, dy);
            for (int i = 1; i <= steps; i++) {
                drawPixel(cube, face,
                        this.lastCol + Math.round((float) (col - this.lastCol) * i / steps),
                        this.lastRow + Math.round((float) (row - this.lastRow) * i / steps));
            }
        }
        this.lastCol = col;
        this.lastRow = row;
    }

    private void drawPixel(int cube, int face, int col, int row) {
        int mode = this.tool == TOOL_ERASE
                ? SewingMachineEditPacket.MODE_ERASE : SewingMachineEditPacket.MODE_PAINT;
        int index = SilkDesign.index(cube, face, col, row);
        List<Integer> design = this.menu.displayDesign();
        int color = this.selectedColor();
        boolean already = mode == SewingMachineEditPacket.MODE_ERASE
                ? design.get(index) == SilkDesign.TRANSPARENT
                : design.get(index) == color;
        if (already) {
            return;
        }
        this.menu.applyLocalEdit(mode, cube, face, col, row, 0, col, row, 0, color);
        boolean beginStroke = this.consumeStrokeStart();
        PacketDistributor.sendToServer(
                new SewingMachineEditPacket(mode, cube, face, col, row, 0, col, row, 0, color, beginStroke));
    }

// 3D 体素

// 每帧刷新体素光标：落在鼠标选中的那个面再往外一格上
    private void updateGhost() {
        this.ghostValid = false;
        if (this.tool != TOOL_VOXEL || this.hoverStack.isEmpty()) {
            return;
        }
        int layer = Mth.clamp(this.hoverLayer, 0, this.hoverStack.size() - 1);
        Hit hit = this.hoverStack.get(layer);
        int[] cell = targetCell(hit);
        if (cell == null || !SilkDesign.inVoxelRange(cell[0], cell[1], cell[2])) {
            return;
        }
        this.ghostValid = true;
        this.ghostCube = hit.face().cube();
        this.ghostI = cell[0];
        this.ghostJ = cell[1];
        this.ghostK = cell[2];
    }

// 一次命中的「往外一格」：体素面就是该方块 + 法线，身体面就是这个像素反解出的体素 + 法线。
// 于是不管从身上还是从已经堆出来的方块上起手，新方块都长在外侧那一格。
    private int[] targetCell(Hit hit) {
        Face face = hit.face();
        int[] cell = new int[3];
        if (face.voxel()) {
            cell[0] = face.vi();
            cell[1] = face.vj();
            cell[2] = face.vk();
        } else {
            SilkDesign.facePixelToVoxel(face.cube(), face.face(), face.mirror(), hit.col(), hit.row(), cell);
        }
        int[] normal = new int[3];
        SilkDesign.faceNormal(face.cube(), face.face(), normal);
        cell[0] += normal[0];
        cell[1] += normal[1];
        cell[2] += normal[2];
        return cell;
    }

// 在命中的外侧格放一个体素（本地先改一份好立刻看到，随后把同一笔发给服务端扣丝绸）
    private void placeVoxel(Hit hit) {
        if (!this.menu.hasSilk()) {
            return;
        }
        int[] cell = targetCell(hit);
        if (cell == null || !SilkDesign.inVoxelRange(cell[0], cell[1], cell[2])) {
            return;
        }
        int cube = hit.face().cube();
        if (this.building && cube == this.builtCube
                && cell[0] == this.builtI && cell[1] == this.builtJ && cell[2] == this.builtK) {
// 拖动时同一格只算一次，免得一个位置反复扣丝绸
            return;
        }
        int color = this.selectedColor();
        this.menu.applyLocalEdit(SewingMachineEditPacket.MODE_VOXEL, cube, 0,
                cell[0], cell[1], cell[2], 0, 0, 0, color);
        boolean beginStroke = this.consumeStrokeStart();
        PacketDistributor.sendToServer(new SewingMachineEditPacket(
                SewingMachineEditPacket.MODE_VOXEL, cube, 0,
                cell[0], cell[1], cell[2], 0, 0, 0, color, beginStroke));
        this.builtCube = cube;
        this.builtI = cell[0];
        this.builtJ = cell[1];
        this.builtK = cell[2];
    }

// 右键拆掉鼠标正下方的那个体素（只在点到已经堆出来的方块时有效）
    private void removeVoxelUnder(List<Hit> visible) {
        for (Hit hit : visible) {
            Face face = hit.face();
            if (!face.voxel()) {
                continue;
            }
            this.menu.applyLocalEdit(SewingMachineEditPacket.MODE_VOXEL_REMOVE, face.cube(), 0,
                    face.vi(), face.vj(), face.vk(), 0, 0, 0, 0);
            boolean beginStroke = this.consumeStrokeStart();
            PacketDistributor.sendToServer(new SewingMachineEditPacket(
                    SewingMachineEditPacket.MODE_VOXEL_REMOVE, face.cube(), 0,
                    face.vi(), face.vj(), face.vk(), 0, 0, 0, 0, beginStroke));
            return;
        }
    }

// 取走「这一笔是不是刚开始」的标记：第一次真正发出去的那一包返回 true，之后都返回 false
    private boolean consumeStrokeStart() {
        boolean begin = this.strokePending;
        this.strokePending = false;
        return begin;
    }

    @Override
    public void removed() {
        super.removed();
        Minecraft.getInstance().getTextureManager().release(PREVIEW_TEXTURE);
        this.previewTexture = null;
        this.previewImage = null;
        this.previewSlot = null;
    }

// 小工具

// 骨架里的部件路径 → cube 编号；用不到的部件（帽子）返回 -1
    private static int cubeIdOf(String path) {
        return switch (path) {
            case "/head" -> SilkDesign.CUBE_HEAD;
            case "/body" -> SilkDesign.CUBE_BODY;
            case "/left_arm" -> SilkDesign.CUBE_LEFT_ARM;
            case "/right_arm" -> SilkDesign.CUBE_RIGHT_ARM;
            case "/left_leg" -> SilkDesign.CUBE_LEFT_LEG;
            case "/right_leg" -> SilkDesign.CUBE_RIGHT_LEG;
            default -> -1;
        };
    }

// 原版里左臂和左腿是镜像出来的，贴图方向因此相反
    private static boolean isMirrored(String path) {
        return "/left_arm".equals(path) || "/left_leg".equals(path);
    }

// 可拾取的一个面。
//
// 身体上的面用 cube 自己的尺寸；已经堆出来的体素也当成一个 1×1×1 的盒子加进这个表，
// 这时 voxel 为真、vi/vj/vk 记下它是哪一格，拆方块和继续往外长都靠它。
    private record Face(int cube, int face, boolean mirror,
                        float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                        boolean voxel, int vi, int vj, int vk,
                        Matrix4f part, Vector3f[] world, Vector3f[] local) {
    }

// 一次拾取结果：命中了哪张面、面上哪个像素；depth 越大越靠近镜头
    private record Hit(Face face, int col, int row, double depth) {
    }
}
