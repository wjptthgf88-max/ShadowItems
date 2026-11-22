package de.shadowsmp.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

public class ShadowRecipes {

    public static void registerAll(ShadowItems plugin) {
        // 1. SCHATTEN-DOLCH
        ShapedRecipe dagger = new ShapedRecipe(new NamespacedKey(plugin, "recipe_dagger"), plugin.buildItem("DAGGER"));
        dagger.shape("CNC", "ESE", "DBD");
        dagger.setIngredient('C', Material.CRYING_OBSIDIAN);
        dagger.setIngredient('N', Material.NETHER_STAR);
        dagger.setIngredient('E', Material.ECHO_SHARD);
        dagger.setIngredient('S', Material.NETHERITE_SWORD);
        dagger.setIngredient('D', Material.DRAGON_BREATH);
        dagger.setIngredient('B', Material.NETHERITE_BLOCK);
        Bukkit.addRecipe(dagger);

        // 2. SCHATTEN-BOHRER
        ShapedRecipe drill = new ShapedRecipe(new NamespacedKey(plugin, "recipe_drill"), plugin.buildItem("DRILL"));
        drill.shape("NBN", "DPD", "RSR");
        drill.setIngredient('N', Material.NETHERITE_BLOCK);
        drill.setIngredient('B', Material.BEACON);
        drill.setIngredient('D', Material.DIAMOND_BLOCK);
        drill.setIngredient('P', Material.NETHERITE_PICKAXE);
        drill.setIngredient('R', Material.REDSTONE_BLOCK);
        drill.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(drill);
        // 3. BERSERKER-BRUSTPLATTE
        ShapedRecipe berserker = new ShapedRecipe(new NamespacedKey(plugin, "recipe_berserker"), plugin.buildItem("BERSERKER"));
        berserker.shape("NGN", "TCT", "NSN");
        berserker.setIngredient('N', Material.NETHERITE_BLOCK);
        berserker.setIngredient('G', Material.ENCHANTED_GOLDEN_APPLE);
        berserker.setIngredient('T', Material.TOTEM_OF_UNDYING);
        berserker.setIngredient('C', Material.NETHERITE_CHESTPLATE);
        berserker.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(berserker);

        // 4. THORS HAMMER
        ShapedRecipe thor = new ShapedRecipe(new NamespacedKey(plugin, "recipe_thor"), plugin.buildItem("THOR"));
        thor.shape("NHN", "LML", " S ");
        thor.setIngredient('N', Material.NETHERITE_BLOCK);
        thor.setIngredient('H', Material.HEAVY_CORE);
        thor.setIngredient('L', Material.LIGHTNING_ROD);
        thor.setIngredient('M', Material.MACE);
        thor.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(thor);
        // 5. SPÄHER-SPEER
        ShapedRecipe spear = new ShapedRecipe(new NamespacedKey(plugin, "recipe_spear"), plugin.buildItem("SPEAR"));
        spear.shape("CHC", "WTW", " N ");
        spear.setIngredient('C', Material.PRISMARINE_CRYSTALS);
        spear.setIngredient('H', Material.HEART_OF_THE_SEA);
        spear.setIngredient('W', Material.WIND_CHARGE);
        spear.setIngredient('T', Material.TRIDENT);
        spear.setIngredient('N', Material.NETHERITE_INGOT);
        Bukkit.addRecipe(spear);

        // 6. PHASEN-MACE
        ShapedRecipe phasemace = new ShapedRecipe(new NamespacedKey(plugin, "recipe_phasemace"), plugin.buildItem("PHASEMACE"));
        phasemace.shape("WHW", "EME", " B ");
        phasemace.setIngredient('W', Material.WIND_CHARGE);
        phasemace.setIngredient('H', Material.HEAVY_CORE);
        phasemace.setIngredient('E', Material.ENDER_EYE);
        phasemace.setIngredient('M', Material.MACE);
        phasemace.setIngredient('B', Material.BREEZE_ROD);
        Bukkit.addRecipe(phasemace);

        // 7. WARDEN-SENSE
        ShapedRecipe scythe = new ShapedRecipe(new NamespacedKey(plugin, "recipe_scythe"), plugin.buildItem("SCYTHE"));
        scythe.shape("EHE", "ESE", "NSN");
        scythe.setIngredient('E', Material.ECHO_SHARD);
        scythe.setIngredient('H', Material.SCULK_SHRIEKER);
        scythe.setIngredient('S', Material.NETHERITE_SWORD);
        scythe.setIngredient('N', Material.NETHERITE_BLOCK);
        Bukkit.addRecipe(scythe);
    }
}
