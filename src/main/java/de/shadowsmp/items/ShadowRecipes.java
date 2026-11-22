package de.shadowsmp.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.RecipeChoice;
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
        scythe.shape("EHE", "ESE", "NXN");
        scythe.setIngredient('E', Material.ECHO_SHARD);
        scythe.setIngredient('H', Material.SCULK_SHRIEKER);
        scythe.setIngredient('S', Material.NETHERITE_SWORD);
        scythe.setIngredient('N', Material.NETHERITE_BLOCK);
        scythe.setIngredient('X', Material.NETHER_STAR);
        Bukkit.addRecipe(scythe);

        // 8. SCHATTEN-BOGEN
        ShapedRecipe bow = new ShapedRecipe(new NamespacedKey(plugin, "recipe_bow"), plugin.buildItem("BOW"));
        bow.shape("CNC", "SBS", "CNC");
        bow.setIngredient('C', Material.CRYING_OBSIDIAN);
        bow.setIngredient('N', Material.NETHER_STAR);
        bow.setIngredient('S', Material.SHULKER_SHELL);
        bow.setIngredient('B', Material.BOW);
        Bukkit.addRecipe(bow);

        // 9. SCHATTEN-STIEFEL
        ShapedRecipe boots = new ShapedRecipe(new NamespacedKey(plugin, "recipe_boots"), plugin.buildItem("BOOTS"));
        boots.shape("WFW", "NBN", "ASA");
        boots.setIngredient('W', Material.WIND_CHARGE);
        boots.setIngredient('F', Material.FEATHER);
        boots.setIngredient('N', Material.NETHERITE_BLOCK);
        boots.setIngredient('B', Material.NETHERITE_BOOTS);
        boots.setIngredient('A', Material.AMETHYST_SHARD);
        boots.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(boots);

        // 10. INFINITE SHADOW GAP
        ShapedRecipe gap = new ShapedRecipe(new NamespacedKey(plugin, "recipe_gap"), plugin.buildItem("GAP"));
        gap.shape("GNG", "NSN", "GNG");
        gap.setIngredient('G', Material.ENCHANTED_GOLDEN_APPLE);
        gap.setIngredient('N', Material.NETHERITE_BLOCK);
        gap.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(gap);
        // 11. SCHATTEN-LEGGINGS
        ShapedRecipe leggings = new ShapedRecipe(new NamespacedKey(plugin, "recipe_leggings"), plugin.buildItem("LEGGINGS"));
        leggings.shape("CPC", "NLN", "ASA");
        leggings.setIngredient('C', Material.CRYING_OBSIDIAN);
        leggings.setIngredient('P', Material.PHANTOM_MEMBRANE);
        leggings.setIngredient('N', Material.NETHERITE_INGOT);
        leggings.setIngredient('L', Material.NETHERITE_LEGGINGS);
        leggings.setIngredient('A', Material.AMETHYST_SHARD);
        leggings.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(leggings);

        // 12. SEELEN-SCHILD
        ShapedRecipe shield = new ShapedRecipe(new NamespacedKey(plugin, "recipe_shield"), plugin.buildItem("SHIELD"));
        shield.shape("NSN", "BHB", "NSN");
        shield.setIngredient('N', Material.NETHERITE_INGOT);
        shield.setIngredient('S', Material.SOUL_LANTERN);
        shield.setIngredient('B', Material.BLUE_ICE);
        shield.setIngredient('H', Material.SHIELD);
        Bukkit.addRecipe(shield);

        // 13. VAMPIR-KLINGE
        ShapedRecipe vampire = new ShapedRecipe(new NamespacedKey(plugin, "recipe_vampire"), plugin.buildItem("VAMPIRE"));
        vampire.shape("RNR", "GSG", "BNB");
        vampire.setIngredient('R', Material.REDSTONE_BLOCK);
        vampire.setIngredient('N', Material.NETHER_STAR);
        vampire.setIngredient('G', Material.GHAST_TEAR);
        vampire.setIngredient('S', Material.NETHERITE_SWORD);
        vampire.setIngredient('B', Material.NETHERITE_BLOCK);
        Bukkit.addRecipe(vampire);

        // 14. SCHATTEN-ARMBRUST
        ShapedRecipe crossbow = new ShapedRecipe(new NamespacedKey(plugin, "recipe_crossbow"), plugin.buildItem("CROSSBOW"));
        crossbow.shape("FNF", "TCT", "ASA");
        crossbow.setIngredient('F', Material.FIRE_CHARGE);
        crossbow.setIngredient('N', Material.NETHER_STAR);
        crossbow.setIngredient('T', Material.TNT);
        crossbow.setIngredient('C', Material.CROSSBOW);
        crossbow.setIngredient('A', Material.AMETHYST_SHARD);
        crossbow.setIngredient('S', Material.SHULKER_SHELL);
        Bukkit.addRecipe(crossbow);

        // 15. PHASEN-HELM
        ShapedRecipe voidhelm = new ShapedRecipe(new NamespacedKey(plugin, "recipe_voidhelm"), plugin.buildItem("VOIDHELM"));
        voidhelm.shape("ENE", "CHC", "ASA");
        voidhelm.setIngredient('E', Material.ENDER_EYE);
        voidhelm.setIngredient('N', Material.NETHERITE_BLOCK);
        voidhelm.setIngredient('C', Material.CHORUS_FRUIT);
        voidhelm.setIngredient('H', Material.NETHERITE_HELMET);
        voidhelm.setIngredient('A', Material.AMETHYST_SHARD);
        voidhelm.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(voidhelm);

        // 16. GRAVITATIONS-AXT
        ShapedRecipe axe = new ShapedRecipe(new NamespacedKey(plugin, "recipe_axe"), plugin.buildItem("AXE"));
        axe.shape("NNH", "NA ", " S ");
        axe.setIngredient('N', Material.NETHERITE_BLOCK);
        axe.setIngredient('H', Material.HEAVY_CORE);
        axe.setIngredient('A', Material.NETHERITE_AXE);
        axe.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(axe);

        // 17. INFINITY ENDERPERLE
        ShapedRecipe pearl = new ShapedRecipe(new NamespacedKey(plugin, "recipe_pearl"), plugin.buildItem("PEARL"));
        pearl.shape("EPE", "NSN", "EPE");
        pearl.setIngredient('E', Material.ENDER_EYE);
        pearl.setIngredient('P', Material.ENDER_PEARL);
        pearl.setIngredient('N', Material.NETHERITE_INGOT);
        pearl.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(pearl);

        // 18. TOTEM-HOSE
        ShapedRecipe undying = new ShapedRecipe(new NamespacedKey(plugin, "recipe_undying"), plugin.buildItem("UNDYING"));
        undying.shape("TNT", "GLG", "NSN");
        undying.setIngredient('T', Material.TOTEM_OF_UNDYING);
        undying.setIngredient('N', Material.NETHERITE_BLOCK);
        undying.setIngredient('G', Material.ENCHANTED_GOLDEN_APPLE);
        undying.setIngredient('L', Material.NETHERITE_LEGGINGS);
        undying.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(undying);

        // 19. SCHATTEN-STAB
        ShapedRecipe staff = new ShapedRecipe(new NamespacedKey(plugin, "recipe_staff"), plugin.buildItem("STAFF"));
        staff.shape(" W ", "NSN", " B ");
        staff.setIngredient('W', Material.WITHER_SKELETON_SKULL);
        staff.setIngredient('N', Material.NETHER_STAR);
        staff.setIngredient('S', Material.BLAZE_ROD);
        staff.setIngredient('B', Material.NETHERITE_BLOCK);
        Bukkit.addRecipe(staff);

        // 20. ★ DIE SCHATTEN-KRONE (AUS DEN 9 STÄRKSTEN ITEMS!) ★
        ShapedRecipe crown = new ShapedRecipe(new NamespacedKey(plugin, "recipe_crown"), plugin.buildItem("CROWN"));
        crown.shape("123", "456", "789");
        crown.setIngredient('1', new RecipeChoice.ExactChoice(plugin.buildItem("SCYTHE")));
        crown.setIngredient('2', new RecipeChoice.ExactChoice(plugin.buildItem("GAP")));
        crown.setIngredient('3', new RecipeChoice.ExactChoice(plugin.buildItem("VAMPIRE")));
        crown.setIngredient('4', new RecipeChoice.ExactChoice(plugin.
