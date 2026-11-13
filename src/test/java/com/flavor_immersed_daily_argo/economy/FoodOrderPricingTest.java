package com.flavor_immersed_daily_argo.economy;

/** Standalone pure-policy regression checks; no game bootstrap or external test library required. */
public final class FoodOrderPricingTest {
    private static int checks;

    public static void main(String[] args) {
        var limits = FoodOrderPricing.Limits.defaults();
        var rawChestnut = profile(3, 1.8, 0, 0, 0, false, true, 0);
        var roastChestnut = profile(3, 3.6, 1, 1, 5, true, false, 2);
        var sugarChestnut = profile(5, 8, 2, 2, 8, true, false, 4);
        var soup = profile(8, 12.8, 4, 4, 6, true, false, 10);
        var burger = profile(15, 24, 2, 2, 0, true, false, 20);
        var goose = profile(10, 16, 6, 6, 8, true, false, 30);
        expect(FoodOrderPricing.unitPrice(rawChestnut, limits) == 3, "raw chestnut remains low value");
        expect(FoodOrderPricing.unitPrice(roastChestnut, limits) == 18, "smoked chestnut cooking premium");
        expect(FoodOrderPricing.unitPrice(sugarChestnut, limits) == 24, "simple prepared snack band");
        expect(FoodOrderPricing.unitPrice(soup, limits) == 52, "soup nutrition/ingredients/time");
        expect(FoodOrderPricing.unitPrice(burger, limits) == 61, "high-nutrition two-stage ingredients");
        expect(FoodOrderPricing.unitPrice(goose, limits) == 64, "whole goose dish premium");
        expect(FoodOrderPricing.unitPrice(profile(8, 12, 1, 1, 10, true, false, 100), limits) == 139,
                "expensive whole fish opportunity cost may exceed normal complex cap");
        expect(FoodOrderPricing.unitPrice(profile(20, 20, 0, 0, 100, true, false, 0), limits) == 0,
                "known zero-material recipe cannot print money");
        expect(FoodOrderPricing.unitPrice(profile(0, 0, 4, 4, 10, true, false, 10), limits) == 0,
                "zero nutrition is not a cooking order payday");
        for (int count = 1; count <= 64; count++) {
            expect(FoodOrderPricing.total(soup, count, limits) == 52L * count, "portion count is exactly linear");
            expect(FoodOrderPricing.total(burger, count, limits) == 61L * count, "complex portions remain linear");
        }
        expect(FoodOrderPricing.total(soup, 0, limits) == 0, "empty order");
        expect(FoodOrderPricing.total(soup, -1, limits) == 0, "invalid order quantity");
        var batch = profile(8, 12.8, 1, 4, 1.5, true, false, 2.5);
        expect(FoodOrderPricing.unitPrice(batch, limits) < FoodOrderPricing.unitPrice(soup, limits),
                "recipe output portions divide materials, processing time and opportunity cost");
        System.out.println("Cooking-order pricing checks passed: " + checks);
        System.out.println("Sample unit quotes: raw chestnut=3, roasted chestnut=18, sugar-roasted chestnut=24, "
                + "duck blood soup=52, donkey burger=61, pot-stew goose=64; costs above samples may raise quotes.");
    }

    private static FoodOrderPricing.Profile profile(int nutrition, double saturation, double materials,
            int kinds, double seconds, boolean known, boolean raw, double cost) {
        return new FoodOrderPricing.Profile(nutrition, saturation, materials, kinds, seconds, known, raw, cost);
    }

    private static void expect(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }
}
