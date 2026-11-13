package com.flavor_immersed_daily_argo.economy.product;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Reads possible item/tag entries without executing loot, conditions, functions, or modifiers. */
public final class LootProductEntries {
    public record Result(Set<String> items, Set<String> tags) { }

    private LootProductEntries() { }

    public static Result find(Map<String, JsonElement> tables, String root) {
        Set<String> items = new HashSet<>();
        Set<String> tags = new HashSet<>();
        table(tables, normalize(root), new HashSet<>(), items, tags, 0);
        return new Result(Set.copyOf(items), Set.copyOf(tags));
    }

    private static void table(Map<String, JsonElement> tables, String key, Set<String> visited,
                              Set<String> items, Set<String> tags, int references) {
        if (references > 32 || key.isEmpty() || !visited.add(key)) {
            return;
        }
        node(tables, tables.get(key), visited, items, tags, references, 0);
    }

    private static void node(Map<String, JsonElement> tables, JsonElement value, Set<String> visited,
                             Set<String> items, Set<String> tags, int references, int jsonDepth) {
        if (value == null || references > 32 || jsonDepth > 64) {
            return;
        }
        if (value.isJsonArray()) {
            for (JsonElement element : value.getAsJsonArray()) {
                node(tables, element, visited, items, tags, references, jsonDepth + 1);
            }
            return;
        }
        if (!value.isJsonObject()) {
            return;
        }
        JsonObject object = value.getAsJsonObject();
        String type = normalize(string(object.get("type")));
        String name = normalize(string(object.get("name")));
        switch (type) {
            case "minecraft:item" -> { if (!name.isEmpty()) items.add(name); }
            case "minecraft:tag" -> { if (!name.isEmpty()) tags.add(name); }
            case "minecraft:loot_table" -> {
                JsonElement nested = object.get("value");
                if (nested != null && nested.isJsonObject()) {
                    node(tables, nested, visited, items, tags, references + 1, jsonDepth + 1);
                } else {
                    String key = nested != null ? normalize(string(nested)) : name;
                    table(tables, key, visited, items, tags, references + 1);
                }
            }
            default -> { }
        }
        // Deliberately exclude conditions/functions: tools mentioned there are not product drops.
        for (String child : new String[]{"pools", "entries", "children"}) {
            node(tables, object.get(child), visited, items, tags, references, jsonDepth + 1);
        }
    }

    private static String normalize(String key) {
        if (key.isEmpty()) return "";
        String id = key.contains(":") ? key : "minecraft:" + key;
        return id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+") ? id : "";
    }

    private static String string(JsonElement value) {
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                ? value.getAsString() : "";
    }
}
