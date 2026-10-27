package com.flavor_immersed_daily_argo.client.render;

import com.flavor_immersed_daily_argo.flavor_immersed_daily_argo;
import com.flavor_immersed_daily_argo.textile.SilkDesign;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 空白丝绸装备用到的 64×32 展开贴图
//
// 玩家是在盔甲模型的各个 cube 的各个面上画的，每个面正好对应展开贴图里的一块固定矩形
// （见 SilkDesign#faceTexU / SilkDesign#faceTexV），所以这里只需要把每个面的
// 像素一格一格抄进对应位置就行，不用做任何缩放。
//
// 整张先铺成纯白：没画过的地方就是空白丝绸原本的底色，画过的地方盖上去。
//
// 生成结果按「部位 + 花纹内容」缓存，花纹没变就直接复用，不重复上传。
public final class SilkArmorTextures {

    private static final int WIDTH = 64;
    private static final int HEIGHT = 32;

// 空白丝绸的底色（不透明纯白），NativeImage 内部按 ABGR 排字节
    private static final int BASE_ABGR = 0xFFFFFFFF;

// 缓存上限，超过就整体释放重来，避免长时间游玩后显存越攒越多
    private static final int MAX_CACHE = 512;

    private static final Map<Key, ResourceLocation> CACHE = new HashMap<>();
    private static int counter;

    private SilkArmorTextures() {
    }

    private record Key(EquipmentSlot slot, int designHash) {
    }

    public static ResourceLocation get(ItemStack stack, EquipmentSlot slot) {
        List<Integer> design = SilkDesign.get(stack);
        Key key = new Key(slot, design.hashCode());
        ResourceLocation cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        if (CACHE.size() >= MAX_CACHE) {
            releaseAll();
        }
        ResourceLocation location = build(design, slot);
        CACHE.put(key, location);
        return location;
    }

    private static ResourceLocation build(List<Integer> design, EquipmentSlot slot) {
        DynamicTexture texture = new DynamicTexture(buildImage(design, slot));
        texture.upload();
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                flavor_immersed_daily_argo.MODID, "dynamic/silk_design_" + (counter++));
        Minecraft.getInstance().getTextureManager().register(location, texture);
        return location;
    }

// 生成一张 64×32 的花纹贴图：没画过的像素是纯白底色
    public static NativeImage buildImage(List<Integer> design, EquipmentSlot slot) {
// useCalloc = true：保证初始内存被清零
        NativeImage image = new NativeImage(WIDTH, HEIGHT, true);
        paintInto(image, design, slot);
        return image;
    }

// 把花纹抄进一张已经开好的 64×32 图里（缝纫机预览每帧复用同一张纹理时用）
    public static void paintInto(NativeImage image, List<Integer> design, EquipmentSlot slot) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                image.setPixelRGBA(x, y, BASE_ABGR);
            }
        }

// 把该槽位用到的每个 cube 的每个面，逐像素抄到展开贴图里对应的位置
        for (int cube : SilkDesign.cubesFor(slot)) {
            for (int face = 0; face < SilkDesign.FACE_COUNT; face++) {
                int u = SilkDesign.faceTexU(cube, face);
                int v = SilkDesign.faceTexV(cube, face);
                int w = SilkDesign.faceWidth(cube, face);
                int h = SilkDesign.faceHeight(cube, face);
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int argb = design.get(SilkDesign.index(cube, face, x, y));
                        if (argb == SilkDesign.UNPAINTED) {
// 没画过 → 透出纯白底色（上面已经铺满）
                            continue;
                        }
// 画过 / 擦过：擦过的是 alpha=0 的透明色，会在这里盖出一个透明的洞
                        image.setPixelRGBA(u + x, v + y, toAbgr(argb));
                    }
                }
            }
        }
    }

// 花纹存的是 ARGB，NativeImage 内部按 ABGR 排字节
    private static int toAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    private static void releaseAll() {
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        for (ResourceLocation location : CACHE.values()) {
            manager.release(location);
        }
        CACHE.clear();
    }
}
