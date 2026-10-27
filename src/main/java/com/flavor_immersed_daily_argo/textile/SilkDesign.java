package com.flavor_immersed_daily_argo.textile;

import com.flavor_immersed_daily_argo.all.ModDataComponents;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 空白丝绸装备上的花纹数据
//
// 花纹不是一张平面画布，而是直接画在盔甲模型的 cube 上：一件装备按槽位对应若干个 cube
// （比如胸甲 = 身体 + 左右臂），每个 cube 有 6 个面（下、上、北、南、西、东），每个面单独存
// 一张和原版贴图等大的小画布，所以画上去的每一个像素都是真正会显示出来的贴图像素。
//
// 所有 cube、所有面的像素按固定顺序拼成一个 List<Integer>（ARGB），值 0 表示这个点
// 还没画（渲染时透出纯白底）。固定顺序保证同一件装备的组件在任何槽位下都能正确解读。
public final class SilkDesign {

// cube
    public static final int CUBE_HEAD = 0;
    public static final int CUBE_BODY = 1;
    public static final int CUBE_LEFT_ARM = 2;
    public static final int CUBE_RIGHT_ARM = 3;
    public static final int CUBE_LEFT_LEG = 4;
    public static final int CUBE_RIGHT_LEG = 5;
    public static final int CUBE_COUNT = 6;

// 面
    public static final int FACE_DOWN = 0;
    public static final int FACE_UP = 1;
    public static final int FACE_NORTH = 2;
    public static final int FACE_SOUTH = 3;
    public static final int FACE_WEST = 4;
    public static final int FACE_EAST = 5;
    public static final int FACE_COUNT = 6;

// 面的排列顺序，和 #FACE_DOWN 等常量一一对应
    public static final Direction[] FACE_DIRECTIONS = {
            Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

// 每个 cube 的模型尺寸（Minecraft 模型像素）：{宽 w, 高 h, 深 d}，与 HumanoidModel.createMesh 一致
    private static final int[][] CUBE_SIZE = {
            {8, 8, 8},   // 头
            {8, 12, 4},  // 身体
            {4, 12, 4},  // 左臂
            {4, 12, 4},  // 右臂
            {4, 12, 4},  // 左腿
            {4, 12, 4},  // 右腿
    };

// 每个 cube 在 64×32 盔甲贴图里的 texOffs
    private static final int[][] CUBE_TEX_OFFS = {
            {0, 0},    // 头
            {16, 16},  // 身体
            {40, 16},  // 左臂
            {40, 16},  // 右臂
            {0, 16},   // 左腿
            {0, 16},   // 右腿
    };

// 每个 cube 在「未膨胀」的人形模型里的原点（模型像素，相对该部件）。
//
// 体素坐标 (i,j,k) 就是从原点往 +X / +Y / +Z 数的格子编号：[0, 尺寸) 是贴着身体的那一层，
// 小于 0 或大于等于尺寸就是往身体外面堆出去的小方块。渲染层用的是膨胀过 0.5 的模型，但体素一律
// 按这份未膨胀的原点对齐，界面预览和穿在身上才对得上。
    private static final float[][] CUBE_ORIGIN = {
            {-4, -8, -4},  // 头
            {-4, 0, -2},   // 身体
            {-1, -2, -2},  // 左臂（原版是镜像出来的，x 原点为 -1）
            {-3, -2, -2},  // 右臂
            {-2, 0, -2},   // 左腿
            {-2, 0, -2},   // 右腿
    };

// 每个 cube / 每个面在展开贴图里的尺寸（宽 × 高），由 cube 尺寸推出来
    private static final int[][][] FACE_SIZE = new int[CUBE_COUNT][FACE_COUNT][2];

// 每个 cube / 每个面在整份数据里的起始下标
    private static final int[][] FACE_OFFSET = new int[CUBE_COUNT][FACE_COUNT];

// 件装备所有 cube 所有面的像素总数
    public static final int PIXELS;

// 「没画过」的像素：值 0，渲染时透出纯白底色。
//
// 橡皮擦擦出来的不是它，而是 #TRANSPARENT，两者要分开：没画过是空白丝绸的白，
// 擦过是透明。
    public static final int UNPAINTED = 0;

// 擦除后的像素：alpha = 0 的完全透明色。
//
// 取值特意不是 0，好和「没画过」（#UNPAINTED）区分开——擦掉白布上的颜色要露出
// 透明的洞，而不是又变回白布。
    public static final int TRANSPARENT = 0x00FFFFFF;

// 没画过时返回的空画布（不可修改，改之前请自己 copy 一份）
    private static final List<Integer> EMPTY;

// 槽位 → 这个槽位会用到的 cube
    private static final int[] CUBES_HEAD = {CUBE_HEAD};
    private static final int[] CUBES_CHEST = {CUBE_BODY, CUBE_LEFT_ARM, CUBE_RIGHT_ARM};
    private static final int[] CUBES_LEGS = {CUBE_BODY, CUBE_LEFT_LEG, CUBE_RIGHT_LEG};
    private static final int[] CUBES_FEET = {CUBE_LEFT_LEG, CUBE_RIGHT_LEG};
    private static final int[] CUBES_NONE = {};

    static {
        int offset = 0;
        for (int cube = 0; cube < CUBE_COUNT; cube++) {
            int w = CUBE_SIZE[cube][0];
            int h = CUBE_SIZE[cube][1];
            int d = CUBE_SIZE[cube][2];
            for (int face = 0; face < FACE_COUNT; face++) {
// 展开贴图里：下/上是 w×d，北/南是 w×h，西/东是 d×h
                int fw = face <= FACE_UP || face <= FACE_SOUTH ? w : d;
                int fh = face <= FACE_UP ? d : h;
                FACE_SIZE[cube][face][0] = fw;
                FACE_SIZE[cube][face][1] = fh;
                FACE_OFFSET[cube][face] = offset;
                offset += fw * fh;
            }
        }
        PIXELS = offset;
        EMPTY = Collections.nCopies(PIXELS, Integer.valueOf(UNPAINTED));
    }

    private SilkDesign() {
    }

// cube / 面 的尺寸与位置

// 这个面的画布宽（= 它在展开贴图里的宽度）
    public static int faceWidth(int cube, int face) {
        return FACE_SIZE[cube][face][0];
    }

// 这个面的画布高
    public static int faceHeight(int cube, int face) {
        return FACE_SIZE[cube][face][1];
    }

// 这个面的第一个像素在整份数据里的下标
    public static int faceOffset(int cube, int face) {
        return FACE_OFFSET[cube][face];
    }

// 这个面在 64×32 展开贴图里的左上角 u
    public static int faceTexU(int cube, int face) {
        int u = CUBE_TEX_OFFS[cube][0];
        int w = CUBE_SIZE[cube][0];
        int d = CUBE_SIZE[cube][2];
        return switch (face) {
            case FACE_DOWN -> u + d;
            case FACE_UP -> u + d + w;
            case FACE_NORTH -> u + d;
            case FACE_SOUTH -> u + d + w + d;
            case FACE_WEST -> u;
            default -> u + d + w; // FACE_EAST
        };
    }

// 这个面在 64×32 展开贴图里的左上角 v
    public static int faceTexV(int cube, int face) {
        int v = CUBE_TEX_OFFS[cube][1];
        int d = CUBE_SIZE[cube][2];
        return face <= FACE_UP ? v : v + d;
    }

// 这个槽位会用到的 cube（不要修改返回的数组）
    public static int[] cubesFor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> CUBES_HEAD;
            case CHEST -> CUBES_CHEST;
            case LEGS -> CUBES_LEGS;
            case FEET -> CUBES_FEET;
            default -> CUBES_NONE;
        };
    }

// 这个 cube 的宽 / 高 / 深（模型像素）
    public static int cubeWidth(int cube) {
        return CUBE_SIZE[cube][0];
    }

    public static int cubeHeight(int cube) {
        return CUBE_SIZE[cube][1];
    }

    public static int cubeDepth(int cube) {
        return CUBE_SIZE[cube][2];
    }

// 左臂 / 左腿在原版里是镜像出来的，贴图与体素方向因此相反
    public static boolean isMirrored(int cube) {
        return cube == CUBE_LEFT_ARM || cube == CUBE_LEFT_LEG;
    }

// 这个 cube 在未膨胀模型里的原点 X（模型像素）
    public static float originX(int cube) {
        return CUBE_ORIGIN[cube][0];
    }

    public static float originY(int cube) {
        return CUBE_ORIGIN[cube][1];
    }

    public static float originZ(int cube) {
        return CUBE_ORIGIN[cube][2];
    }

// 某个面朝外（往身体外）的方向，写进 out：out[0]=di、out[1]=dj、out[2]=dk。
//
// 镜像的左臂 / 左腿在网格里东西两个面是反过来的，所以这两个面的法线要跟着镜像翻一下，
// 这样「面的体素 + 法线」得到的才是真正朝外的那个格子。
    public static void faceNormal(int cube, int face, int[] out) {
        boolean mirror = isMirrored(cube);
        switch (face) {
            case FACE_DOWN -> {
                out[0] = 0;
                out[1] = -1;
                out[2] = 0;
            }
            case FACE_UP -> {
                out[0] = 0;
                out[1] = 1;
                out[2] = 0;
            }
            case FACE_NORTH -> {
                out[0] = 0;
                out[1] = 0;
                out[2] = -1;
            }
            case FACE_SOUTH -> {
                out[0] = 0;
                out[1] = 0;
                out[2] = 1;
            }
            case FACE_WEST -> {
                out[0] = mirror ? 1 : -1;
                out[1] = 0;
                out[2] = 0;
            }
            default -> { // FACE_EAST
                out[0] = mirror ? -1 : 1;
                out[1] = 0;
                out[2] = 0;
            }
        }
    }

// 把某个面上的像素坐标反解成这个 cube 里的体素坐标，写进 out：
// out[0]=i（沿 +X）、out[1]=j（沿 +Y）、out[2]=k（沿 +Z）。
//
// 和界面里拾取用的正向换算必须严格互逆，否则方块工具刷出来的位置会和看到的不一致。
    public static void facePixelToVoxel(int cube, int face, boolean mirror, int col, int row, int[] out) {
        int w = CUBE_SIZE[cube][0];
        int h = CUBE_SIZE[cube][1];
        int d = CUBE_SIZE[cube][2];
        switch (face) {
            case FACE_DOWN, FACE_UP -> {
                out[0] = mirror ? w - 1 - col : col;
                out[1] = face == FACE_DOWN ? 0 : h - 1;
                out[2] = d - 1 - row;
            }
            case FACE_NORTH -> {
                out[0] = mirror ? w - 1 - col : col;
                out[1] = row;
                out[2] = 0;
            }
            case FACE_SOUTH -> {
                out[0] = mirror ? col : w - 1 - col;
                out[1] = row;
                out[2] = d - 1;
            }
            case FACE_WEST -> {
                out[0] = mirror ? w - 1 : 0;
                out[1] = row;
                out[2] = d - 1 - col;
            }
            default -> { // FACE_EAST
                out[0] = mirror ? 0 : w - 1;
                out[1] = row;
                out[2] = col;
            }
        }
    }

// 面编号 → Direction
    public static Direction directionOf(int face) {
        return FACE_DIRECTIONS[face];
    }

// Direction → 面编号
    public static int faceOf(Direction direction) {
        return switch (direction) {
            case DOWN -> FACE_DOWN;
            case UP -> FACE_UP;
            case NORTH -> FACE_NORTH;
            case SOUTH -> FACE_SOUTH;
            case WEST -> FACE_WEST;
            case EAST -> FACE_EAST;
        };
    }

// 存 / 取

// 整份花纹；没画过或尺寸对不上就还你一张空画布
    public static List<Integer> get(ItemStack stack) {
        List<Integer> design = ModDataComponents.SILK_DESIGN.get().get(stack);
        if (design == null || design.size() != PIXELS) {
            return EMPTY;
        }
        return design;
    }

// 是否已经画过（组件存在而且尺寸正确）
    public static boolean hasDesign(ItemStack stack) {
        List<Integer> design = ModDataComponents.SILK_DESIGN.get().get(stack);
        return design != null && design.size() == PIXELS;
    }

// 写入整份花纹
    public static void set(ItemStack stack, List<Integer> pixels) {
        ModDataComponents.SILK_DESIGN.get().set(stack, new ArrayList<>(pixels));
    }

// 一张还没画过的空画布（不可修改）
    public static List<Integer> emptyCanvas() {
        return EMPTY;
    }

// 某个面里某个像素在整份数据里的下标
    public static int index(int cube, int face, int x, int y) {
        return FACE_OFFSET[cube][face] + y * FACE_SIZE[cube][face][0] + x;
    }

// 「着装」进来的那件真护甲；没有就返回空的
    public static ItemStack attachedArmor(ItemStack stack) {
        return ModDataComponents.SILK_ARMOR.get().getOrDefault(stack, ItemStack.EMPTY);
    }

// 3D 体素

// 一个体素在数据里占几个 int：cube, i, j, k, ARGB
    public static final int VOXEL_STRIDE = 5;

// 体素坐标的允许范围（往身体外最多堆这么多层）
    public static final int VOXEL_MIN = -16;
    public static final int VOXEL_MAX = 47;

    private static final List<Integer> EMPTY_VOXELS = List.of();

// 没建模过时返回的空体素表（不可修改，改之前请自己 copy 一份）
    public static List<Integer> emptyVoxels() {
        return EMPTY_VOXELS;
    }

// 已经堆出来的所有体素；没建模过就还你一张空表
    public static List<Integer> getVoxels(ItemStack stack) {
        List<Integer> voxels = ModDataComponents.SILK_VOXELS.get().get(stack);
        if (voxels == null || voxels.size() % VOXEL_STRIDE != 0) {
            return EMPTY_VOXELS;
        }
        return voxels;
    }

// 是否已经建模过（组件存在而且长度正确）
    public static boolean hasVoxels(ItemStack stack) {
        List<Integer> voxels = ModDataComponents.SILK_VOXELS.get().get(stack);
        return voxels != null && !voxels.isEmpty() && voxels.size() % VOXEL_STRIDE == 0;
    }

// 写入整份体素表
    public static void setVoxels(ItemStack stack, List<Integer> voxels) {
        ModDataComponents.SILK_VOXELS.get().set(stack, new ArrayList<>(voxels));
    }

// 体素个数
    public static int voxelCount(List<Integer> voxels) {
        return voxels.size() / VOXEL_STRIDE;
    }

    public static int voxelCube(List<Integer> voxels, int index) {
        return voxels.get(index * VOXEL_STRIDE);
    }

    public static int voxelI(List<Integer> voxels, int index) {
        return voxels.get(index * VOXEL_STRIDE + 1);
    }

    public static int voxelJ(List<Integer> voxels, int index) {
        return voxels.get(index * VOXEL_STRIDE + 2);
    }

    public static int voxelK(List<Integer> voxels, int index) {
        return voxels.get(index * VOXEL_STRIDE + 3);
    }

    public static int voxelColor(List<Integer> voxels, int index) {
        return voxels.get(index * VOXEL_STRIDE + 4);
    }

// 坐标是否在允许范围内
    public static boolean inVoxelRange(int i, int j, int k) {
        return i >= VOXEL_MIN && i <= VOXEL_MAX
                && j >= VOXEL_MIN && j <= VOXEL_MAX
                && k >= VOXEL_MIN && k <= VOXEL_MAX;
    }

// 找到某个格子上的体素在体素表里的下标；没有就是 -1
    public static int voxelAt(List<Integer> voxels, int cube, int i, int j, int k) {
        int count = voxelCount(voxels);
        for (int index = 0; index < count; index++) {
            if (voxelCube(voxels, index) == cube
                    && voxelI(voxels, index) == i
                    && voxelJ(voxels, index) == j
                    && voxelK(voxels, index) == k) {
                return index;
            }
        }
        return -1;
    }

// 在某个格子上放一个体素（同一格重复放就是改颜色）。返回是否真的改到了东西。
    public static boolean addVoxel(List<Integer> voxels, int cube, int i, int j, int k, int color) {
        if (!inVoxelRange(i, j, k)) {
            return false;
        }
        int index = voxelAt(voxels, cube, i, j, k);
        if (index >= 0) {
            int slot = index * VOXEL_STRIDE + 4;
            if (voxels.get(slot) == color) {
                return false;
            }
            voxels.set(slot, color);
            return true;
        }
        voxels.add(cube);
        voxels.add(i);
        voxels.add(j);
        voxels.add(k);
        voxels.add(color);
        return true;
    }

// 拆掉某个格子上的体素。返回是否真的拆掉了。
    public static boolean removeVoxel(List<Integer> voxels, int cube, int i, int j, int k) {
        int index = voxelAt(voxels, cube, i, j, k);
        if (index < 0) {
            return false;
        }
        int from = index * VOXEL_STRIDE;
        for (int n = 0; n < VOXEL_STRIDE; n++) {
            voxels.remove(from);
        }
        return true;
    }
}
