package com.flavor_immersed_daily_argo.economy;

/** Pure cooking-order reward policy. No navigation-delivery fees or game registries here. */
public final class FoodOrderPricing {
    public record Profile(int nutrition, double saturation, double ingredientsPerServing,
                          int ingredientKinds, double processingSecondsPerServing,
                          boolean recipeKnown, boolean raw, double ingredientOpportunityCost) { }

    public record Limits(int rawMin, int rawMax, int simpleMin, int simpleMax,
                         int complexMin, int complexMax, double costMultiplier, int processingFee) {
        public static Limits defaults() {
            return new Limits(2, 6, 12, 24, 30, 80, 1.35D, 4);
        }
    }

    private FoodOrderPricing() { }

    public static int unitPrice(Profile profile, Limits limits) {
        if (profile.nutrition <= 0) return 0;
        // A data-pack recipe with no material investment must not become a money printer.
        if (profile.recipeKnown && (!Double.isFinite(profile.ingredientsPerServing)
                || profile.ingredientsPerServing <= 0.0D)) return 0;
        int nutrition = Math.min(20, profile.nutrition);
        double saturation = finiteClamp(profile.saturation, 0, 20);
        if (profile.raw) {
            return bound(Math.round(1.0D + nutrition * 0.5D + saturation * 0.15D), limits.rawMin, limits.rawMax);
        }
        double inputs = finiteClamp(profile.ingredientsPerServing, 0, 32);
        double seconds = finiteClamp(profile.processingSecondsPerServing, 0, 120);
        boolean complex = (profile.ingredientKinds >= 3 && nutrition >= 6 && inputs >= 0.75D)
                || (nutrition >= 10 && inputs >= 1.5D) || (seconds >= 20 && nutrition >= 6);
        double score = complex
                ? 14 + nutrition * 2.0D + saturation * 0.4D + inputs * 3.0D + seconds * 0.3D + 3
                : 8 + nutrition * 1.25D + saturation * 0.2D + inputs * 2.0D + seconds * 0.25D + 2;
        int quoted = complex ? bound(Math.round(score), limits.complexMin, limits.complexMax)
                : bound(Math.round(score), limits.simpleMin, limits.simpleMax);
        double opportunityCost = finiteClamp(profile.ingredientOpportunityCost, 0, 10_000_000);
        double multiplier = finiteClamp(limits.costMultiplier, 1, 10);
        // Expensive whole fish/meat must not pay less after cooking. This floor intentionally
        // may exceed the ordinary complex-food band; it is backed by identified material cost.
        long costFloor = (long) Math.ceil(opportunityCost * multiplier + Math.max(0, limits.processingFee));
        return (int) Math.min(Integer.MAX_VALUE, Math.max(quoted, costFloor));
    }

    public static long total(Profile profile, int portions, Limits limits) {
        return portions <= 0 ? 0 : (long) unitPrice(profile, limits) * portions;
    }

    private static int bound(long value, int first, int second) {
        int low = Math.max(0, Math.min(first, second));
        int high = Math.max(low, Math.max(first, second));
        return (int) Math.max(low, Math.min(high, value));
    }

    private static double finiteClamp(double value, double min, double max) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : min;
    }
}
