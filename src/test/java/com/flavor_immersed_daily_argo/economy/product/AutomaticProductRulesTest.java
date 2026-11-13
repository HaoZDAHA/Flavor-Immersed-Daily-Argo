package com.flavor_immersed_daily_argo.economy.product;

public final class AutomaticProductRulesTest {
    private static int checks;

    public static void main(String[] args) {
        lootFixtures();
        check(!AutomaticProductRules.isOtherMod("minecraft"), "vanilla is not an automatic mod");
        check(!AutomaticProductRules.isOtherMod("flavor_immersed_daily"), "parent retains explicit prices");
        check(!AutomaticProductRules.isOtherMod("flavor_immersed_daily_argo"), "addon retains explicit prices");
        check(AutomaticProductRules.isOtherMod("farmersdelight"), "other mod eligible");
        check(AutomaticProductRules.isFarmTag("c", "crops/tomato"), "nested crop tag");
        check(AutomaticProductRules.isFarmTag("c", "foods/raw_meats"), "animal food tag");
        check(AutomaticProductRules.isFarmTag("forge", "leathers"), "legacy common tag");
        check(AutomaticProductRules.isFarmTag("minecraft", "wool"), "mod wool in vanilla tag");
        check(!AutomaticProductRules.isFarmTag("c", "foods/cooked_meats"), "cooked dishes are not raw farm drops");
        check(!AutomaticProductRules.isFarmTag("c", "ingots"), "do not buy arbitrary mod items");
        check(!AutomaticProductRules.isFarmTag("other", "crops"), "unknown metadata is not guessed");
        check(!AutomaticProductRules.isFarmTag("c", "crops_fake"), "tag prefix boundary");
        check(AutomaticProductRules.idBlocked("other:corn", "other:corn"), "exact blacklist");
        check(AutomaticProductRules.idBlocked("other:corn", " other:* "), "namespace blacklist");
        check(!AutomaticProductRules.idBlocked("other_two:corn", "other:*"), "namespace boundary");
        check(!AutomaticProductRules.idBlocked("other:corn", "third:corn"), "unrelated blacklist");
        for (int i = 0; i < 1000; i++) {
            String item = "third:crop_" + i;
            int price = AutomaticProductRules.price(item, 5, 20);
            check(price >= 5 && price <= 20, "configured interval");
            check(price == AutomaticProductRules.price(item, 20, 5), "reversed interval normalized");
            check(price == AutomaticProductRules.price(item, 5, 20), "stable across rebuilds");
            check(AutomaticProductRules.price(item, 7, 7) == 7, "fixed price");
        }
        System.out.println("Automatic product policy checks passed: " + checks);
    }

    private static void lootFixtures() {
        java.util.Map<String, com.google.gson.JsonElement> tables = new java.util.HashMap<>();
        tables.put("farm:root", com.google.gson.JsonParser.parseString("""
                {"pools":[{"entries":[
                  {"type":"item","name":"farm:corn",
                   "conditions":[{"type":"item","name":"tools:knife"}],
                   "functions":[{"type":"item","name":"tools:pan"}]},
                  {"type":"minecraft:tag","name":"c:crops/beans"},
                  {"type":"loot_table","value":"farm:shared"},
                  {"type":"minecraft:loot_table","value":{"pools":[{"entries":[
                    {"type":"item","name":"farm:egg"}]}]}},
                  {"type":"alternatives","children":[{"type":"item","name":"stick"}]}
                ]}]}"""));
        tables.put("farm:shared", com.google.gson.JsonParser.parseString("""
                {"pools":[{"entries":[{"type":"item","name":"farm:fur"},
                  {"type":"loot_table","value":"farm:root"}]}]}"""));
        var result = LootProductEntries.find(tables, "farm:root");
        check(result.items().contains("farm:corn"), "unnamespaced item entry");
        check(result.items().contains("farm:egg"), "inline loot table");
        check(result.items().contains("farm:fur"), "referenced table despite cycle");
        check(result.items().contains("minecraft:stick"), "default item namespace");
        check(result.tags().contains("c:crops/beans"), "tag entry collected separately");
        check(!result.items().contains("tools:knife"), "condition ingredient is not a drop");
        check(!result.items().contains("tools:pan"), "function ingredient is not a drop");
        check(result.items().size() == 4, "no phantom entries from metadata");
        check(LootProductEntries.find(tables, "farm:missing").items().isEmpty(), "missing table is safe");
        for (int i = 0; i < 20; i++) {
            tables.put("farm:chain" + i, com.google.gson.JsonParser.parseString(
                    "{\"pools\":[{\"entries\":[{\"type\":\"loot_table\",\"value\":\"farm:chain" + (i + 1) + "\"}]}]}"));
        }
        tables.put("farm:chain20", com.google.gson.JsonParser.parseString(
                "{\"pools\":[{\"entries\":[{\"type\":\"item\",\"name\":\"farm:deep_crop\"}]}]}"));
        check(LootProductEntries.find(tables, "farm:chain0").items().contains("farm:deep_crop"),
                "table-reference depth must not be confused with JSON node depth");
    }

    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) {
            throw new AssertionError(message);
        }
    }
}
