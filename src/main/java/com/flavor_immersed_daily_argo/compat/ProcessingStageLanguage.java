package com.flavor_immersed_daily_argo.compat;

import net.minecraft.client.resources.language.I18n;

import java.util.Map;

/** Client display adapter for the bundled parent mod's literal processing-stage labels. */
public final class ProcessingStageLanguage {
    // These are legacy input labels, not new player-facing literals or registry identifiers.
    private static final Map<String, String> STAGES = Map.ofEntries(
            Map.entry("放血", "bleed"),
            Map.entry("剥皮", "skin"),
            Map.entry("剔骨", "debone"),
            Map.entry("掏空", "eviscerate"),
            Map.entry("切肉", "cut_meat"),
            Map.entry("切割", "cut"),
            Map.entry("取下", "remove"),
            Map.entry("刮鳞", "scale"),
            Map.entry("开膛", "gut"),
            Map.entry("切块", "chop"),
            Map.entry("漂洗", "rinse"),
            Map.entry("缫丝", "reel_silk"),
            Map.entry("拔毛", "pluck"));

    private ProcessingStageLanguage() {
    }

    public static String display(String original) {
        if (original == null) {
            return null;
        }
        String stage = STAGES.get(original);
        if (stage == null) {
            return original;
        }
        String key = "compat.flavor_immersed_daily_argo.processing." + stage;
        // Resolve each time: do not cache translated text across language/resource reloads.
        return I18n.exists(key) ? I18n.get(key) : original;
    }
}
