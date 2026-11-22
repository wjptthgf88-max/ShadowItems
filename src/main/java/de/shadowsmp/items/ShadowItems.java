package de.shadowsmp.items;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShadowItems extends JavaPlugin implements CommandExecutor, TabCompleter, Listener {
    private final MiniMessage mm = MiniMessage.miniMessage();
    private NamespacedKey itemKey;
    private NamespacedKey shadowArrowKey;
    private NamespacedKey infinityPearlKey;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Long> noFallDamageUntil = new HashMap<>();
    private final Map<UUID, Integer> scytheHits = new HashMap<>();
    private final Set<UUID> drillingPlayers = new HashSet<>();
    private final Map<UUID, Set<String>> unlockedRecipes = new HashMap<>();
    private boolean crownCrafted = false;
    private File dataFile;

    private static final List<String> ALL_RECIPE_IDS = List.of(
            "recipe_dagger", "recipe_drill", "recipe_berserker", "recipe_thor", "recipe_spear",
            "recipe_phasemace", "recipe_scythe", "recipe_bow", "recipe_boots", "recipe_gap",
            "recipe_leggings", "recipe_shield", "recipe_vampire", "recipe_crossbow", "recipe_voidhelm",
            "recipe_axe", "recipe_pearl", "recipe_undying", "recipe_staff", "recipe_crown"
    );

    @Override
    public void onEnable() {
        itemKey = new NamespacedKey(this, "shadow_item_id");
        shadowArrowKey = new NamespacedKey(this, "shadow_arrow");
        infinityPearlKey = new NamespacedKey(this, "infinity_pearl");
        loadData();
        getCommand("shadowitems").setExecutor(this);
        getCommand("shadowitems").setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);
        ShadowRecipes.registerAll(this);

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.getScheduler().run(this, playerTask -> applyArmorPassives(p), null);
            }
        }, 20L, 40L);
    }

    private void applyArmorPassives(Player p) {
        boolean hasBoots = "BOOTS".equals(getItemId(p.getInventory().getBoots()));
        boolean hasVoidHelm = "VOIDHELM".equals(getItemId(p.getInventory().getHelmet()));
        boolean hasCrown = "CROWN".equals(getItemId(p.getInventory().getHelmet()));

        if (hasBoots && !hasCrown) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, true, false));
        }

        if (hasVoidHelm) {
            p.removePotionEffect(PotionEffectType.BLINDNESS);
            p.removePotionEffect(PotionEffectType.DARKNESS);
            p.removePotionEffect(PotionEffectType.WITHER);
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 260, 0, true, false));
        }

        if (hasCrown) {
            p.removePotionEffect(PotionEffectType.BLINDNESS);
            p.removePotionEffect(PotionEffectType.DARKNESS);
            p.removePotionEffect(PotionEffectType.WITHER);
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 80, 2, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 2, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 80, 1, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 260, 0, true, false));
            if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                p.setAllowFlight(true);
            }
            p.getWorld().spawnParticle(Particle.WITCH, p.getLocation().add(0, 2.1, 0), 4, 0.25, 0.05, 0.25, 0.01);
        } else if (!hasBoots && (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE)) {
            if (p.getAllowFlight()) {
                p.setFlying(false);
                p.setAllowFlight(false);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Set<String> unlocked = unlockedRecipes.getOrDefault(p.getUniqueId(), Set.of());
        List<NamespacedKey> toHide = new ArrayList<>();
        List<NamespacedKey> toShow = new ArrayList<>();
        for (String rec : ALL_RECIPE_IDS) {
            NamespacedKey key = new NamespacedKey(this, rec);
            if (unlocked.contains(rec)) toShow.add(key);
            else toHide.add(key);
        }
        p.undiscoverRecipes(toHide);
        p.discoverRecipes(toShow);
    }

    private boolean checkCooldown(Player p, String ability, int seconds) {
        String key = p.getUniqueId() + ":" + ability;
        long now = System.currentTimeMillis();
        long end = cooldowns.getOrDefault(key, 0L);
        if (now < end) {
            double left = (end - now) / 1000.0;
            p.sendActionBar(mm.deserialize("<red>Cooldown: <light_purple>" + String.format("%.1f", left) + "s"));
            return false;
        }
        cooldowns.put(key, now + (seconds * 1000L));
        return true;
    }

    private String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
    }
    public ItemStack buildItem(String id) {
        return switch (id.toUpperCase()) {
            case "DAGGER" -> {
                ItemStack item = createItem(Material.NETHERITE_SWORD, 2001, "DAGGER",
                        "<gradient:#9d4edd:#e0aaff><bold>SCHATTEN-DOLCH</bold></gradient>",
                        List.of("<gray>Rechtsklick: <light_purple>8 Blöcke Schatten-Teleport", "<gray>+ 3s Speed II & Unsichtbarkeit", "<dark_gray>Unzerstörbar | Cooldown: 12s | CMD: 2001"));
                enchantSword(item);
                yield item;
            }
            case "DRILL" -> {
                ItemStack item = createItem(Material.NETHERITE_PICKAXE, 2002, "DRILL",
                        "<gradient:#9d4edd:#e0aaff><bold>SCHATTEN-BOHRER</bold></gradient>",
                        List.of("<gray>Passiv: <light_purple>Baut 3x3 Blöcke gleichzeitig ab", "<dark_gray>Unzerstörbar | CMD: 2002"));
                item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 5);
                item.addUnsafeEnchantment(Enchantment.FORTUNE, 3);
                yield item;
            }
            case "BERSERKER" -> {
                ItemStack item = createItem(Material.NETHERITE_CHESTPLATE, 2003, "BERSERKER",
                        "<gradient:#9d4edd:#e0aaff><bold>BERSERKER-BRUSTPLATTE</bold></gradient>",
                        List.of("<gray>Passiv: <light_purple>Unter 5 Herzen -> Stärke II & Resistenz", "<dark_gray>Unzerstörbar | Cooldown: 20s | CMD: 2003"));
                item.addUnsafeEnchantment(Enchantment.PROTECTION, 4);
                item.addUnsafeEnchantment(Enchantment.THORNS, 3);
                yield item;
            }
            case "THOR" -> {
                ItemStack item = createItem(Material.MACE, 2004, "THOR",
                        "<gradient:#9d4edd:#e0aaff><bold>THORS HAMMER</bold></gradient>",
                        List.of("<gray>Rechtsklick: <light_purple>Ruft einen Blitzschlag herab", "<gray>& schleudert Gegner zurück", "<dark_gray>Unzerstörbar | Cooldown: 10s | CMD: 2004"));
                item.addUnsafeEnchantment(Enchantment.BREACH, 4);
                item.addUnsafeEnchantment(Enchantment.WIND_BURST, 3);
                item.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
                yield item;
            }
            case "SPEAR" -> {
                ItemStack item = createItem(Material.TRIDENT, 2005, "SPEAR",
                        "<gradient:#9d4edd:#e0aaff><bold>SPÄHER-SPEER</bold></gradient>",
                        List.of("<gray>Rechtsklick: <light_purple>Riptide-Dash nach vorne", "<gray>Verleiht <light_purple>No Fall Damage <gray>(30s)", "<dark_gray>Unzerstörbar | Cooldown: 6s | CMD: 2005"));
                item.addUnsafeEnchantment(Enchantment.IMPALING, 5);
                yield item;
            }
            case "PHASEMACE" -> {
                ItemStack item = createItem(Material.MACE, 2006, "PHASEMACE",
                        "<gradient:#9d4edd:#e0aaff><bold>PHASEN-MACE</bold></gradient>",
                        List.of("<gray>Rechtsklick: <light_purple>Katapultiert dich 14 Blöcke hoch", "<gray>für einen tödlichen Mace-Smash", "<dark_gray>Unzerstörbar | Cooldown: 15s | CMD: 2006"));
                item.addUnsafeEnchantment(Enchantment.DENSITY, 5);
                item.addUnsafeEnchantment(Enchantment.WIND_BURST, 3);
                item.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
                yield item;
            }
            case "SCYTHE" -> {
                ItemStack item = createItem(Material.NETHERITE_SWORD, 2007, "SCYTHE",
                        "<gradient:#9d4edd:#e0aaff><bold>WARDEN-SENSE</bold></gradient>",
                        List.of("<gray>Rechtsklick: <light_purple>Warden Sonic Boom (15 Blöcke)", "<gray>Passiv: <light_purple>Jeder 3. Schlag -> Regeneration & Darkness", "<dark_gray>Unzerstörbar | Cooldown: 14s | CMD: 2007"));
                enchantSword(item);
                yield item;
            }
            case "BOW" -> {
                ItemStack item = createItem(Material.BOW, 2008, "BOW",
                        "<gradient:#9d4edd:#e0aaff><bold>SCHATTEN-BOGEN</bold></gradient>",
                        List.of("<gray>Schuss: <light_purple>Explosiver Schatten-Pfeil", "<gray>Gibt Gegnern Wither & Blindness", "<dark_gray>Unzerstörbar | Cooldown: 8s | CMD: 2008"));
                item.addUnsafeEnchantment(Enchantment.POWER, 5);
                item.addUnsafeEnchantment(Enchantment.PUNCH, 2);
                item.addUnsafeEnchantment(Enchantment.FLAME, 1);
                item.addUnsafeEnchantment(Enchantment.INFINITY, 1);
                yield item;
            }
            case "BOOTS" -> {
                ItemStack item = createItem(Material.NETHERITE_BOOTS, 2009, "BOOTS",
                        "<gradient:#9d4edd:#e0aaff><bold>SCHATTEN-STIEFEL</bold></gradient>",
                        List.of("<gray>Passiv: <light_purple>Dauerhaft Speed I", "<gray>In der Luft: <light_purple>Doppelsprung (Leertaste)", "<dark_gray>Unzerstörbar | Cooldown: 5s | CMD: 2009"));
                item.addUnsafeEnchantment(Enchantment.PROTECTION, 4);
                item.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, 4);
                item.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3);
                item.addUnsafeEnchantment(Enchantment.SOUL_SPEED, 3);
                yield item;
            }
            case "GAP" -> createItem(Material.ENCHANTED_GOLDEN_APPLE, 2011, "GAP",
                    "<gradient:#ffd166:#9d4edd><bold>INFINITE SHADOW GAP</bold></gradient>",
                    List.of("<gray>Essen: <light_purple>Wird nicht verbraucht!", "<gray>Regen III, Absorption IV & Resistenz II", "<dark_gray>Cooldown: 25s | CMD: 2011"));
            case "LEGGINGS" -> {
                ItemStack item = createItem(Material.NETHERITE_LEGGINGS, 2012, "LEGGINGS",
                        "<gradient:#9d4edd:#e0aaff><bold>SCHATTEN-LEGGINGS</bold></gradient>",
                        List.of("<gray>Sneak: <light_purple>4s Unsichtbarkeit & Speed II", "<dark_gray>Unzerstörbar | Cooldown: 15s | CMD: 2012"));
                item.addUnsafeEnchantment(Enchantment.PROTECTION, 4);
                item.addUnsafeEnchantment(Enchantment.SWIFT_SNEAK, 3);
                yield item;
            }
            case "SHIELD" -> createItem(Material.SHIELD, 2013, "SHIELD",
                    "<gradient:#9d4edd:#e0aaff><bold>SEELEN-SCHILD</bold></gradient>",
                    List.of("<gray>Blocken: <light_purple>Schleudert Angreifer weg, friert ein", "<gray>& heilt dich um 2 Herzen", "<dark_gray>Unzerstörbar | Cooldown: 8s | CMD: 2013"));
            case "VAMPIRE" -> {
                ItemStack item = createItem(Material.NETHERITE_SWORD, 2014, "VAMPIRE",
                        "<gradient:#9d4edd:#e0aaff><bold>VAMPIR-KLINGE</bold></gradient>",
                        List.of("<gray>Passiv: <light_purple>25% Lifesteal bei jedem Treffer", "<gray>& belegt Gegner mit Wither I", "<dark_gray>Unzerstörbar | CMD: 2014"));
                enchantSword(item);
                yield item;
            }
            case "CROSSBOW" -> {
                ItemStack item = createItem(Material.CROSSBOW, 2015, "CROSSBOW",
                        "<gradient:#9d4edd:#e0aaff><bold>SCHATTEN-ARMBRUST</bold></gradient>",
                        List.of("<gray>Rechtsklick: <light_purple>Feuert 3 explosive Schatten-Bolzen", "<dark_gray>Unzerstörbar | Cooldown: 10s | CMD: 2015"));
                item.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 3);
                item.addUnsafeEnchantment(Enchantment.MULTISHOT, 1);
                item.addUnsafeEnchantment(Enchantment.PIERCING, 4);
                yield item;
            }
            case "VOIDHELM" -> {
                ItemStack item = createItem(Material.NETHERITE_HELMET, 2016, "VOIDHELM",
                        "<gradient:#9d4edd:#e0aaff><bold>PHASEN-HELM</bold></gradient>",
                        List.of("<gray>Passiv: <light_purple>Immun gegen Wither, Blindness & Darkness", "<gray>Rechtsklick: <light_purple>Phased durch 3 Blöcke Wände", "<dark_gray>Unzerstörbar | Cooldown: 18s | CMD: 2016"));
                item.addUnsafeEnchantment(Enchantment.PROTECTION, 4);
                item.addUnsafeEnchantment(Enchantment.RESPIRATION, 3);
                item.addUnsafeEnchantment(Enchantment.AQUA_AFFINITY, 1);
                yield item;
            }
            case "AXE" -> {
                ItemStack item = createItem(Material.NETHERITE_AXE, 2017, "AXE",
                        "<gradient:#9d4edd:#e0aaff><bold>GRAVITATIONS-AXT</bold></gradient>",
                        List.of("<gray>Rechtsklick: <light_purple>Zieht alle Gegner (8 Blöcke) heran", "<gray>& verlangsamt sie stark", "<dark_gray>Unzerstörbar | Cooldown: 12s | CMD: 2017"));
                item.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
                item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 5);
                item.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
                yield item;
            }
            case "PEARL" -> createItem(Material.ENDER_PEARL, 2018, "PEARL",
                    "<gradient:#9d4edd:#e0aaff><bold>INFINITY ENDERPERLE</bold></gradient>",
                    List.of("<gray>Werfen: <light_purple>Unendliche Enderperle ohne Fallschaden", "<gray>+ 3s Resistenz I nach Teleport", "<dark_gray>Cooldown: 10s | CMD: 2018"));
            case "UNDYING" -> {
                ItemStack item = createItem(Material.NETHERITE_LEGGINGS, 2019, "UNDYING",
                        "<gradient:#ffd166:#9d4edd><bold>TOTEM-HOSE</bold></gradient>",
                        List.of("<gray>Passiv: <light_purple>Rettet dich automatisch vor dem Tod", "<gray>(wie ein eingebautes Totem)", "<dark_gray>Unzerstörbar | Cooldown: 60s | CMD: 2019"));
                item.addUnsafeEnchantment(Enchantment.PROTECTION, 4);
                yield item;
            }
            case "STAFF" -> createItem(Material.BLAZE_ROD, 2020, "STAFF",
                    "<gradient:#9d4edd:#e0aaff><bold>SCHATTEN-STAB</bold></gradient>",
                    List.of("<gray>Rechtsklick: <light_purple>Feuert einen tödlichen Wither-Schädel", "<dark_gray>Unzerstörbar | Cooldown: 9s | CMD: 2020"));
            case "SCROLL" -> createItem(Material.MOJANG_BANNER_PATTERN, 2021, "SCROLL",
                    "<gradient:#ffd166:#9d4edd><bold>ALTE SCHATTEN-SCHRIFTROLLE</bold></gradient>",
                    List.of("<gray>Rechtsklick: <light_purple>Enthüllt 1 zufälliges geheimes Rezept", "<gray>im grünen Rezeptbuch!", "<dark_gray>Einmalig verbrauchbar | CMD: 2021"));
            case "CROWN" -> {
                ItemStack item = createItem(Material.GOLDEN_HELMET, 2010, "CROWN",
                        "<gradient:#ffd166:#9d4edd><bold>★ DIE SCHATTEN-KRONE ★</bold></gradient>",
                        List.of("<gold>Einzigartiges Server-Artefakt (1x)", "<light_purple>+10 Extraherzen (20 Herzen) & +60% Schaden", "<light_purple>Dauerhaft: Stärke III, Regen III, Resistenz II & Flug", "<gray>Sneak: <light_purple>Schatten-Dominanz (15 Blöcke Schockwelle)", "<dark_gray>God-Artefakt | Cooldown: 18s | CMD: 2010"));
                item.addUnsafeEnchantment(Enchantment.PROTECTION, 5);
                item.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 4);
                item.addUnsafeEnchantment(Enchantment.FIRE_PROTECTION, 4);
                item.addUnsafeEnchantment(Enchantment.PROJECTILE_PROTECTION, 4);
                item.addUnsafeEnchantment(Enchantment.THORNS, 3);
                item.addUnsafeEnchantment(Enchantment.RESPIRATION, 3);
                item.addUnsafeEnchantment(Enchantment.AQUA_AFFINITY, 1);
                if (item.getItemMeta() instanceof ArmorMeta am) {
                    am.setTrim(new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.VEX));
                    am.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(new NamespacedKey(this, "crown_armor"), 6.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));
                    am.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(new NamespacedKey(this, "crown_tough"), 4.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));
                    am.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, new AttributeModifier(new NamespacedKey(this, "crown_kb"), 0.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));
                    am.addAttributeModifier(Attribute.MAX_HEALTH, new AttributeModifier(new NamespacedKey(this, "crown_hp"), 20.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));
                    am.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(new NamespacedKey(this, "crown_dmg"), 0.6, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlotGroup.HEAD));
                    item.setItemMeta(am);
                }
                yield item;
            }
            default -> null;
        };
    }

    private void enchantSword(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
        item.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
        item.addUnsafeEnchantment(Enchantment.LOOTING, 3);
        item.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, 3);
    }

    private ItemStack createItem(Material mat, int cmd, String id, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(name));
        meta.lore(loreLines.stream().map(mm::deserialize).toList());
        meta.setCustomModelData(cmd);
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] rawArgs) {
        List<String> args = Arrays.asList(rawArgs);
        if (args.isEmpty()) {
            sender.sendMessage(mm.deserialize("<light_purple>Items: <white>dagger, drill, berserker, thor, spear, phasemace, scythe, bow, boots, gap, leggings, shield, vampire, crossbow, voidhelm, axe, pearl, undying, staff, scroll, crown"));
            return true;
        }
        if (args.get(0).equalsIgnoreCase("resetcrown")) {
            crownCrafted = false;
            saveData();
            sender.sendMessage(mm.deserialize("<green>Die Schatten-Krone ist wieder 1x craftbar!"));
            return true;
        }
        ItemStack item = buildItem(args.get(0));
        if (item == null) {
            sender.sendMessage(mm.deserialize("<red>Unbekanntes Item!"));
            return true;
        }
        Player target = (args.size() >= 2) ? Bukkit.getPlayer(args.get(1)) : (sender instanceof Player p ? p : null);
        if (target == null) {
            sender.sendMessage(mm.deserialize("<red>Spieler nicht gefunden!"));
            return true;
        }
        target.getInventory().addItem(item);
        target.sendMessage(mm.deserialize("<light_purple>Du hast ein <bold>SHADOW ITEM</bold> <light_purple>erhalten!"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("dagger", "drill", "berserker", "thor", "spear", "phasemace", "scythe", "bow", "boots",
                    "gap", "leggings", "shield", "vampire", "crossbow", "voidhelm", "axe", "pearl", "undying", "staff", "scroll", "crown", "resetcrown");
        }
        return null;
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        EntityType type = entity.getType();
        double maxHp = entity.getAttribute(Attribute.MAX_HEALTH) != null ? entity.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0;

        boolean isBoss = (type == EntityType.WARDEN || type == EntityType.WITHER || type == EntityType.ENDER_DRAGON || maxHp >= 150.0);
        if (isBoss) {
            e.getDrops().add(buildItem("SCROLL"));
        }
    }

    @EventHandler
    public void onPrepareCrownCraft(PrepareItemCraftEvent e) {
        if (e.getRecipe() == null) return;
        if (!"CROWN".equals(getItemId(e.getRecipe().getResult()))) return;

        List<ItemStack> matrix = Arrays.asList(e.getInventory().getMatrix());
        List<String> required = List.of("SCYTHE", "GAP", "VAMPIRE", "BERSERKER", "THOR", "UNDYING", "DAGGER", "SPEAR", "PHASEMACE");
        if (matrix.size() < 9) {
            e.getInventory().setResult(null);
            return;
        }
        for (int i = 0; i < 9; i++) {
            if (!required.get(i).equals(getItemId(matrix.get(i)))) {
                e.getInventory().setResult(null);
                return;
            }
        }
    }

    @EventHandler
    public void onCrownCraft(CraftItemEvent e) {
        if (!"CROWN".equals(getItemId(e.getRecipe().getResult()))) return;
        if (crownCrafted) {
            e.setCancelled(true);
            e.getWhoClicked().sendMessage(mm.deserialize("<red>Die Schatten-Krone wurde auf diesem Server bereits gecraftet!"));
            return;
        }
        if (e.isShiftClick()) {
            e.setCancelled(true);
            e.getWhoClicked().sendMessage(mm.deserialize("<red>Bitte klicke die Krone normal an (ohne Shift)!"));
            return;
        }
        crownCrafted = true;
        saveData();
        Bukkit.broadcast(mm.deserialize("<dark_purple><bold>[SHADOW SMP]</bold> <gold><bold>" + e.getWhoClicked().getName() + "</bold> <light_purple>hat soeben <gold><bold>DIE SCHATTEN-KRONE</bold> <light_purple>geschmiedet!"));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        ItemStack handItem = p.getInventory().getItemInMainHand();
        String id = getItemId(handItem);
        if (id == null) return;

        switch (id) {
            case "SCROLL" -> {
                e.setCancelled(true);
                Set<String> unlocked = unlockedRecipes.computeIfAbsent(p.getUniqueId(), k -> new HashSet<>());
                List<String> locked = new ArrayList<>();
                for (String rec : ALL_RECIPE_IDS) {
                    if (!unlocked.contains(rec)) locked.add(rec);
                }
                if (locked.isEmpty()) {
                    p.sendMessage(mm.deserialize("<gold>Du hast bereits alle geheimen Shadow-Rezepte freigeschaltet!"));
                    return;
                }
                String chosen = locked.get(new Random().nextInt(locked.size()));
                unlocked.add(chosen);
                saveData();
                p.discoverRecipe(new NamespacedKey(this, chosen));
                handItem.setAmount(handItem.getAmount() - 1);
                p.getWorld().spawnParticle(Particle.WITCH, p.getLocation().add(0, 1, 0), 50, 0.5, 0.8, 0.5, 0.15);
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
                String cleanName = chosen.replace("recipe_", "").toUpperCase();
                p.sendMessage(mm.deserialize("<dark_purple><bold>[SHADOW SMP]</bold> <light_purple>Du hast ein neues Rezept entschlüsselt: <gold><bold>" + cleanName + "</bold> <gray>(Schau in dein Rezeptbuch!)"));
            }
            case "DAGGER" -> {
                if (!checkCooldown(p, "DAGGER", 12)) return;
                Location start = p.getLocation();
                Vector dir = start.getDirection().normalize();
                Location targetLoc = start.clone();
                for (int i = 1; i <= 8; i++) {
                    Location foot = start.clone().add(dir.clone().multiply(i));
                    Location head = foot.clone().add(0, 1, 0);
                    if (foot.getBlock().getType().isOccluding() || head.getBlock().getType().isOccluding()) break;
                    targetLoc = foot;
                }
                final Location finalDest = targetLoc.clone();
                finalDest.setYaw(start.getYaw());
                finalDest.setPitch(start.getPitch());
                p.getWorld().spawnParticle(Particle.PORTAL, start.clone().add(0, 1, 0), 40, 0.4, 0.5, 0.4, 0.15);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
                p.getScheduler().runDelayed(this, task -> p.teleportAsync(finalDest).thenRun(() -> {
                    p.getWorld().spawnParticle(Particle.PORTAL, finalDest.clone().add(0, 1, 0), 40, 0.4, 0.5, 0.4, 0.15);
                    p.playSound(finalDest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
                }), null, 1L);
            }
            case "THOR" -> {
                if (!checkCooldown(p, "THOR", 10)) return;
                Block targetBlock = p.getTargetBlockExact(25);
                Location strikeLoc = (targetBlock != null) ? targetBlock.getLocation().add(0.5, 1, 0.5) : p.getLocation().add(p.getLocation().getDirection().multiply(8));
                p.getWorld().strikeLightning(strikeLoc);
                p.getWorld().spawnParticle(Particle.WITCH, strikeLoc, 40, 1.5, 1, 1.5, 0.1);
                for (Entity ent : strikeLoc.getWorld().getNearbyEntities(strikeLoc, 4, 4, 4)) {
                    if (ent instanceof LivingEntity le && !ent.equals(p)) {
                        le.damage(6.0, p);
                        le.setVelocity(le.getLocation().toVector().subtract(strikeLoc.toVector()).normalize().multiply(1.2).setY(0.6));
                    }
                }
            }
            case "SPEAR" -> {
                e.setCancelled(true);
                if (!checkCooldown(p, "SPEAR", 6)) return;
                p.setVelocity(p.getLocation().getDirection().normalize().multiply(1.85).setY(0.75));
                p.startRiptideAttack(20, 8.0f, handItem);
                noFallDamageUntil.put(p.getUniqueId(), System.currentTimeMillis() + 30_000L);
                p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0, 1, 0), 35, 0.3, 0.3, 0.3, 0.2);
                p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_2, 1f, 1.2f);
                p.sendActionBar(mm.deserialize("<light_purple><bold>NO FALL DAMAGE</bold> <gray>aktiviert (30s)!"));
            }
            case "PHASEMACE" -> {
                if (!checkCooldown(p, "PHASEMACE", 15)) return;
                p.setVelocity(new Vector(0, 1.45, 0));
                p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation(), 40, 0.6, 0.2, 0.6, 0.15);
                p.playSound(p.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.2f, 0.8f);
            }
            case "SCYTHE" -> {
                if (!checkCooldown(p, "SCYTHE", 14)) return;
                Location eye = p.getEyeLocation();
                Vector dir = eye.getDirection().normalize();
                p.getWorld().playSound(eye, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.2f, 1.0f);
                Set<UUID> hitEntities = new HashSet<>();
                for (int i = 1; i <= 15; i++) {
                    Location point = eye.clone().add(dir.clone().multiply(i));
                    if (point.getBlock().getType().isSolid()) break;
                    p.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1, 0, 0, 0, 0);
                    for (Entity ent : point.getWorld().getNearbyEntities(point, 1.3, 1.3, 1.3)) {
                        if (ent instanceof LivingEntity le && !ent.equals(p) && hitEntities.add(ent.getUniqueId())) {
                            le.damage(10.0, p);
                            le.setVelocity(dir.clone().multiply(1.4).setY(0.4));
                        }
                    }
                }
            }
            case "VOIDHELM" -> {
                if (!checkCooldown(p, "VOIDHELM", 18)) return;
                Location dest = p.getLocation().add(p.getLocation().getDirection().normalize().multiply(4));
                if (dest.getBlock().getType().isOccluding() || dest.clone().add(0, 1, 0).getBlock().getType().isOccluding()) {
                    p.sendActionBar(mm.deserialize("<red>Wand zu dick zum Hindurchphasen!"));
                    return;
                }
                p.getScheduler().runDelayed(this, task -> p.teleportAsync(dest), null, 1L);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.7f);
            }
            case "AXE" -> {
                if (!checkCooldown(p, "AXE", 12)) return;
                Location center = p.getLocation();
                p.getWorld().spawnParticle(Particle.WITCH, center.clone().add(0, 1, 0), 50, 2, 1, 2, 0.1);
                p.playSound(center, Sound.ENTITY_IRON_GOLEM_ATTACK, 1f, 0.6f);
                for (Entity ent : center.getWorld().getNearbyEntities(center, 8, 5, 8)) {
                    if (ent instanceof LivingEntity le && !ent.equals(p)) {
                        Vector pull = center.toVector().subtract(le.getLocation().toVector()).normalize().multiply(1.3).setY(0.35);
                        le.setVelocity(pull);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                    }
                }
            }
            case "PEARL" -> {
                e.setCancelled(true);
                if (!checkCooldown(p, "PEARL", 10)) return;
                EnderPearl pearl = p.launchProjectile(EnderPearl.class);
                pearl.getPersistentDataContainer().set(infinityPearlKey, PersistentDataType.BYTE, (byte) 1);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1f, 1f);
            }
            case "STAFF" -> {
                if (!checkCooldown(p, "STAFF", 9)) return;
                WitherSkull skull = p.launchProjectile(WitherSkull.class);
                skull.setCharged(true);
                skull.setVelocity(p.getLocation().getDirection().normalize().multiply(1.8));
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1f, 1.1f);
            }
            case "CROSSBOW" -> {
                e.setCancelled(true);
                if (!checkCooldown(p, "CROSSBOW", 10)) return;
                Vector base = p.getLocation().getDirection().normalize();
                for (double angle : new double[]{-0.12, 0.0, 0.12}) {
                    Vector spread = base.clone().rotateAroundY(angle).multiply(2.2);
                    Arrow arrow = p.launchProjectile(Arrow.class, spread);
                    arrow.getPersistentDataContainer().set(shadowArrowKey, PersistentDataType.BYTE, (byte) 1);
                }
                p.playSound(p.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1f, 0.9f);
            }
        }
    }
    @EventHandler
    public void onInfiniteGapConsume(PlayerItemConsumeEvent e) {
        if (!"GAP".equals(getItemId(e.getItem()))) return;
        e.setCancelled(true);
        Player p = e.getPlayer();
        if (!checkCooldown(p, "GAP", 25)) return;

        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1200, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1200, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1f, 1.2f);
        p.sendActionBar(mm.deserialize("<gold><bold>INFINITE SHADOW GAP</bold> <light_purple>aktiviert!"));
    }

    @EventHandler
    public void onSneakAbilities(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        if (!e.isSneaking()) return;

        if ("CROWN".equals(getItemId(p.getInventory().getHelmet()))) {
            if (!checkCooldown(p, "CROWN", 18)) return;
            Location center = p.getLocation();
            p.getWorld().spawnParticle(Particle.WITCH, center.clone().add(0, 1, 0), 120, 4.0, 1.2, 4.0, 0.2);
            p.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, 0.9f, 1.4f);
            p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 300, 3));
            p.setHealth(p.getAttribute(Attribute.MAX_HEALTH).getValue());
            for (Entity ent : center.getWorld().getNearbyEntities(center, 15, 8, 15)) {
                if (ent instanceof LivingEntity le && !ent.equals(p)) {
                    le.getWorld().strikeLightningEffect(le.getLocation());
                    le.damage(14.0, p);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
                    le.setVelocity(le.getLocation().toVector().subtract(center.toVector()).normalize().multiply(2.0).setY(0.85));
                }
            }
            p.sendActionBar(mm.deserialize("<gold><bold>★ SCHATTEN-DOMINANZ ENTFESSELT! ★</bold>"));
            return;
        }

        if ("LEGGINGS".equals(getItemId(p.getInventory().getLeggings()))) {
            if (!checkCooldown(p, "LEGGINGS", 15)) return;
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 80, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1));
            p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.15);
            p.sendActionBar(mm.deserialize("<light_purple><bold>SCHATTEN-TARNUNG</bold> <gray>aktiviert (4s)!"));
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!"BOW".equals(getItemId(e.getBow()))) return;
        if (!checkCooldown(p, "BOW", 8)) return;
        e.getProjectile().getPersistentDataContainer().set(shadowArrowKey, PersistentDataType.BYTE, (byte) 1);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Arrow arrow && arrow.getPersistentDataContainer().has(shadowArrowKey, PersistentDataType.BYTE)) {
            Location loc = arrow.getLocation();
            loc.getWorld().createExplosion(loc, 2.2f, false, false, arrow.getShooter() instanceof Entity ent ? ent : null);
            loc.getWorld().spawnParticle(Particle.WITCH, loc, 40, 1.5, 1.5, 1.5, 0.1);
            for (Entity ent : loc.getWorld().getNearbyEntities(loc, 3.5, 3.5, 3.5)) {
                if (ent instanceof LivingEntity le && !ent.equals(arrow.getShooter())) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                }
            }
            arrow.remove();
        } else if (e.getEntity() instanceof EnderPearl pearl && pearl.getPersistentDataContainer().has(infinityPearlKey, PersistentDataType.BYTE)) {
            if (pearl.getShooter() instanceof Player p) {
                noFallDamageUntil.put(p.getUniqueId(), System.currentTimeMillis() + 3_000L);
                p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0));
            }
        }
    }

    @EventHandler
    public void onBootsDoubleJump(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;
        if ("CROWN".equals(getItemId(p.getInventory().getHelmet()))) return;
        if (!"BOOTS".equals(getItemId(p.getInventory().getBoots()))) return;

        e.setCancelled(true);
        p.setAllowFlight(false);
        if (!checkCooldown(p, "BOOTS", 5)) return;

        p.setVelocity(p.getLocation().getDirection().normalize().multiply(1.1).setY(0.85));
        noFallDamageUntil.put(p.getUniqueId(), System.currentTimeMillis() + 6_000L);
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation(), 25, 0.3, 0.1, 0.3, 0.15);
        p.playSound(p.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1f, 1.3f);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;
        if ("BOOTS".equals(getItemId(p.getInventory().getBoots())) && ((Entity) p).isOnGround()) {
            p.setAllowFlight(true);
        }
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWeaponHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        if (!(e.getEntity() instanceof LivingEntity victim)) return;
        String id = getItemId(p.getInventory().getItemInMainHand());

        if ("SCYTHE".equals(id)) {
            int count = scytheHits.getOrDefault(p.getUniqueId(), 0) + 1;
            if (count >= 3) {
                scytheHits.put(p.getUniqueId(), 0);
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
                p.getWorld().spawnParticle(Particle.SCULK_SOUL, victim.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
                p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1f, 1.2f);
            } else {
                scytheHits.put(p.getUniqueId(), count);
            }
        } else if ("VAMPIRE".equals(id)) {
            double heal = e.getFinalDamage() * 0.25;
            double maxHp = p.getAttribute(Attribute.MAX_HEALTH).getValue();
            p.setHealth(Math.min(maxHp, p.getHealth() + heal));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if ("CROWN".equals(getItemId(p.getInventory().getHelmet())) || System.currentTimeMillis() < noFallDamageUntil.getOrDefault(p.getUniqueId(), 0L)) {
                e.setCancelled(true);
                return;
            }
        }

        if (e instanceof EntityDamageByEntityEvent byEnt && p.isBlocking()) {
            boolean hasSoulShield = "SHIELD".equals(getItemId(p.getInventory().getItemInMainHand())) || "SHIELD".equals(getItemId(p.getInventory().getItemInOffHand()));
            if (hasSoulShield && byEnt.getDamager() instanceof LivingEntity attacker && checkCooldown(p, "SHIELD", 8)) {
                attacker.setVelocity(attacker.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.4).setY(0.5));
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                p.setHealth(Math.min(p.getAttribute(Attribute.MAX_HEALTH).getValue(), p.getHealth() + 4.0));
                p.playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 0.6f);
            }
        }

        double afterHealth = p.getHealth() - e.getFinalDamage();
        if (afterHealth <= 0 && "UNDYING".equals(getItemId(p.getInventory().getLeggings())) && checkCooldown(p, "UNDYING", 60)) {
            e.setCancelled(true);
            p.setHealth(8.0);
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 160, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1));
            p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation().add(0, 1, 0), 60, 0.5, 0.8, 0.5, 0.2);
            p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 1f);
            p.sendActionBar(mm.deserialize("<gold><bold>TOTEM-HOSE HAT DICH GERETTET!</bold>"));
            return;
        }

        if (afterHealth > 0 && afterHealth <= 10.0 && "BERSERKER".equals(getItemId(p.getInventory().getChestplate())) && checkCooldown(p, "BERSERKER", 20)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 0));
            p.getWorld().spawnParticle(Particle.WITCH, p.getLocation().add(0, 1, 0), 40, 0.5, 0.8, 0.5, 0.1);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1.4f);
            p.sendActionBar(mm.deserialize("<dark_purple><bold>BERSERKER-WUT AKTIVIERT!</bold>"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrillBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (drillingPlayers.contains(p.getUniqueId())) return;
        if (!"DRILL".equals(getItemId(p.getInventory().getItemInMainHand()))) return;

        Block center = e.getBlock();
        BlockFace face = getTargetBlockFace(p);
        drillingPlayers.add(p.getUniqueId());
        try {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    Block rel = switch (face) {
                        case UP, DOWN -> center.getRelative(dx, 0, dy);
                        case NORTH, SOUTH -> center.getRelative(dx, dy, 0);
                        case EAST, WEST -> center.getRelative(0, dy, dx);
                        default -> center;
                    };
                    if (rel.equals(center)) continue;
                    Material type = rel.getType();
                    if (type.isAir() || type == Material.BEDROCK || type == Material.BARRIER || type.getHardness() < 0) continue;
                    p.breakBlock(rel);
                }
            }
        } finally {
            drillingPlayers.remove(p.getUniqueId());
        }
    }

    private BlockFace getTargetBlockFace(Player p) {
        List<Block> lastTwo = p.getLastTwoTargetBlocks(null, 6);
        if (lastTwo.size() == 2) {
            BlockFace face = lastTwo.get(1).getFace(lastTwo.get(0));
            if (face != null) return face;
        }
        return p.getFacing();
    }

    private void saveData() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        dataFile = new File(getDataFolder(), "data.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("crown-crafted", crownCrafted);
        for (Map.Entry<UUID, Set<String>> entry : unlockedRecipes.entrySet()) {
            cfg.set("unlocked." + entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }
        try { cfg.save(dataFile); } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void loadData() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        crownCrafted = cfg.getBoolean("crown-crafted", false);
        if (cfg.isConfigurationSection("unlocked")) {
            for (String uuidStr : cfg.getConfigurationSection("unlocked").getKeys(false)) {
                unlockedRecipes.put(UUID.fromString(uuidStr), new HashSet<>(cfg.getStringList("unlocked." + uuidStr)));
            }
        }
    }
}
