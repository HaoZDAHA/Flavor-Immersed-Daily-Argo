package com.flavor_immersed_daily_argo.economy.product;

/** Pure policy functions shared by discovery and regression checks. */
public final class AutomaticProductRules {
    private AutomaticProductRules() { }

    public static boolean isOtherMod(String namespace) {
        return !namespace.equals("minecraft") && !namespace.startsWith("flavor_immersed_daily");
    }

    public static boolean isFarmTag(String namespace, String path) {
        if (namespace.equals("minecraft")) {
            return path.equals("wool");
        }
        if (!namespace.equals("c") && !namespace.equals("forge")) {
            return false;
        }
        for (String root : new String[]{"crops", "seeds", "eggs", "feathers", "leathers", "wools",
                "foods/vegetables", "foods/fruits", "foods/berries", "foods/grains", "foods/nuts",
                "foods/mushrooms", "foods/raw_meats", "foods/raw_fishes", "foods/raw_fish",
                "raw_meats", "raw_fishes", "raw_fish"}) {
            if (path.equals(root) || path.startsWith(root + "/")) {
                return true;
            }
        }
        return false;
    }

    public static int price(String itemId, int firstBound, int secondBound) {
        int min = Math.max(1, Math.min(firstBound, secondBound));
        int max = Math.max(min, Math.max(firstBound, secondBound));
        long range = (long) max - min + 1;
        return min + (int) Math.floorMod((long) itemId.hashCode() * 341873128712L + 132897987541L, range);
    }

    public static boolean idBlocked(String itemId, String rule) {
        String normalized = rule.trim();
        if (normalized.endsWith(":*")) {
            return itemId.startsWith(normalized.substring(0, normalized.length() - 1));
        }
        return itemId.equals(normalized);
    }
}
