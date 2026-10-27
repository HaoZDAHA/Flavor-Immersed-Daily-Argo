package com.flavor_immersed_daily_argo.client;

import net.minecraft.client.gui.GuiGraphics;

// 经济类界面的统一画风：高级半透明黑底 + 圆角 + 细描边。
//
// 这里只做纯色 / 渐变的 fill 绘制，不依赖任何贴图：
//
// #panel 整块窗口底：外投影 + 上亮下暗的描边 + 半透明主体 + 顶部高光；
// #slot 物品格：内凹的圆角深槽；
// #row 列表行：悬停 / 选中的圆角高亮；
// #field 输入框：圆角底色，聚焦时描边变金色；
// #button 按钮底：普通 / 主操作 / 危险三种画风，见 RoundedButton。
public final class GuiStyle {

// 金色强调（价格、余额、选中的页签）
    public static final int ACCENT = 0xFFFFD24A;
// 正文亮色
    public static final int TEXT = 0xFFE8ECF2;
// 次要文字
    public static final int TEXT_DIM = 0xFF9AA0A8;

    private GuiStyle() {
    }

// 整块界面底板
    public static void panel(GuiGraphics gui, int x, int y, int w, int h) {
// 外投影：两层低透明度圆角，越往外越淡，做出“悬浮”的感觉
        roundedRect(gui, x - 3, y - 2, w + 6, h + 8, 11, 0x2E000000);
        roundedRect(gui, x - 2, y - 1, w + 4, h + 5, 10, 0x42000000);
// 1px 描边：上沿亮、下沿暗
        roundedGradient(gui, x - 1, y - 1, w + 2, h + 2, 9, 0x55FFFFFF, 0x1FFFFFFF);
// 主体：半透明黑，上浅下深
        roundedGradient(gui, x, y, w, h, 8, 0xE81B1E25, 0xE00A0B0E);
// 顶部高光
        roundedRect(gui, x + 8, y + 1, w - 16, 1, 0, 0x18FFFFFF);
    }

// 头部分隔线
    public static void divider(GuiGraphics gui, int x, int y, int w) {
        gui.fill(x, y, x + w, y + 1, 0x18FFFFFF);
    }

// 物品格：内凹圆角深槽，x/y 与槽位物品坐标一致（16×16）
    public static void slot(GuiGraphics gui, int x, int y) {
        roundedRect(gui, x - 1, y - 1, 18, 18, 4, 0xC8000000);
        roundedRect(gui, x, y, 16, 16, 3, 0xF2101216);
// 上沿压暗、下沿提亮，做出凹槽的立体感
        gui.fill(x + 3, y, x + 13, y + 1, 0x70000000);
        gui.fill(x + 3, y + 15, x + 13, y + 16, 0x1AFFFFFF);
    }

// 列表行：未被选中时是一层很淡的底，悬停提亮，选中时金色
    public static void row(GuiGraphics gui, int x, int y, int w, int h, boolean hovered, boolean selected) {
        if (selected) {
            roundedGradient(gui, x, y, w, h, 4, 0x5CFFD24A, 0x2EFFD24A);
        } else if (hovered) {
            roundedGradient(gui, x, y, w, h, 4, 0x3DFFFFFF, 0x1FFFFFFF);
        } else {
            roundedRect(gui, x, y, w, h, 4, 0x14FFFFFF);
        }
    }

// 输入框底：聚焦时描边金色
    public static void field(GuiGraphics gui, int x, int y, int w, int h, boolean focused) {
        int border = focused ? 0xB3FFD24A : 0x33FFFFFF;
        roundedRect(gui, x, y, w, h, 5, border);
        roundedGradient(gui, x + 1, y + 1, w - 2, h - 2, 4, 0xF016191E, 0xF00C0E11);
    }

// 按钮底；style 见 RoundedButton.Style
    public static void button(GuiGraphics gui, int x, int y, int w, int h,
                              RoundedButton.Style style, boolean selected, boolean hovered, boolean active) {
        int radius = Math.min(6, h / 2);
        int border;
        int top;
        int bottom;
        if (!active) {
            border = 0x16FFFFFF;
            top = 0xC014161A;
            bottom = 0xC00A0B0E;
        } else if (selected || style == RoundedButton.Style.ACCENT) {
            border = hovered ? 0xD9FFD24A : 0xA6FFD24A;
            top = hovered ? 0xF23E3421 : 0xF02A2418;
            bottom = 0xF018140C;
        } else if (style == RoundedButton.Style.DANGER) {
            border = hovered ? 0xB3FF8080 : 0x80FF8080;
            top = hovered ? 0xF23A1E1E : 0xF02A1717;
            bottom = 0xF0180D0D;
        } else {
            border = hovered ? 0x66FFFFFF : 0x38FFFFFF;
            top = hovered ? 0xF0333842 : 0xF0262A31;
            bottom = hovered ? 0xF01B1E24 : 0xF014161A;
        }
// 底部投影
        roundedRect(gui, x, y + 1, w, h, radius, 0x45000000);
// 描边 + 主体
        roundedRect(gui, x, y, w, h, radius, border);
        roundedGradient(gui, x + 1, y + 1, w - 2, h - 2, Math.max(0, radius - 1), top, bottom);
// 顶部高光
        gui.fill(x + radius, y + 1, x + w - radius, y + 2, 0x14FFFFFF);
    }

// 按钮文字颜色
    public static int buttonText(RoundedButton.Style style, boolean selected, boolean active) {
        if (!active) {
            return 0x66777D86;
        }
        if (selected || style == RoundedButton.Style.ACCENT) {
            return ACCENT;
        }
        if (style == RoundedButton.Style.DANGER) {
            return 0xFFFF9A9A;
        }
        return TEXT;
    }

// 圆角绘制

// 圆角纯色矩形
    public static void roundedRect(GuiGraphics gui, int x, int y, int w, int h, int radius, int color) {
        int r = clampRadius(x, y, w, h, radius);
        if (w <= 0 || h <= 0) {
            return;
        }
        if (r <= 0) {
            gui.fill(x, y, x + w, y + h, color);
            return;
        }
        for (int row = 0; row < h; row++) {
            int inset = rowInset(row, h, r);
            gui.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
        }
    }

// 圆角竖向渐变矩形（逐行插值颜色）
    public static void roundedGradient(GuiGraphics gui, int x, int y, int w, int h, int radius, int top, int bottom) {
        int r = clampRadius(x, y, w, h, radius);
        if (w <= 0 || h <= 0) {
            return;
        }
        for (int row = 0; row < h; row++) {
            int inset = r <= 0 ? 0 : rowInset(row, h, r);
            float t = h <= 1 ? 0.0F : (float) row / (h - 1);
            gui.fill(x + inset, y + row, x + w - inset, y + row + 1, lerpColor(top, bottom, t));
        }
    }

// 圆角半径不能超过短边的一半
    private static int clampRadius(int x, int y, int w, int h, int radius) {
        if (w <= 0 || h <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(radius, Math.min(w, h) / 2));
    }

// 第 row 行在圆角处的左右缩进
    private static int rowInset(int row, int h, int r) {
        if (row < r) {
            double d = r - row - 0.5;
            return r - (int) Math.round(Math.sqrt(Math.max(0.0, r * r - d * d)));
        }
        if (row >= h - r) {
            double d = row - (h - r) + 0.5;
            return r - (int) Math.round(Math.sqrt(Math.max(0.0, r * r - d * d)));
        }
        return 0;
    }

// ARGB 线性插值
    public static int lerpColor(int from, int to, float t) {
        int fa = from >>> 24;
        int fr = from >> 16 & 0xFF;
        int fg = from >> 8 & 0xFF;
        int fb = from & 0xFF;
        int ta = to >>> 24;
        int tr = to >> 16 & 0xFF;
        int tg = to >> 8 & 0xFF;
        int tb = to & 0xFF;
        int a = Math.round(fa + (ta - fa) * t);
        int r = Math.round(fr + (tr - fr) * t);
        int g = Math.round(fg + (tg - fg) * t);
        int b = Math.round(fb + (tb - fb) * t);
        return a << 24 | r << 16 | g << 8 | b;
    }
}
