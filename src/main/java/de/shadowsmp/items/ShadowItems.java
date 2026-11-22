package de.shadowsmp.items;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class ShadowItems extends JavaPlugin implements CommandExecutor, TabCompleter, Listener {
    private final MiniMessage mm = MiniMessage.miniMessage();
    private NamespacedKey itemKey;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Long> noFallDamageUntil = new HashMap<>();
    private final Map<UUID, Integer> scytheHits = new HashMap<>();
    private final Set<UUID> drillingPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        itemKey = new NamespacedKey(this, "shadow_item_id");
        getCommand("shadowitems").setExecutor(this);
        getCommand("shadowitems").setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);
        ShadowRecipes.registerAll(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().undiscoverRecipes(List.of(
                new NamespacedKey(this, "recipe_dagger"),
                new NamespacedKey(this, "recipe_drill"),
                new NamespacedKey(this, "recipe_berserker"),
                new NamespacedKey(this, "recipe_thor"),
                new NamespacedKey(this, "recipe_spear"),
                new NamespacedKey(this, "recipe_phasemace"),
                new NamespacedKey(this, "recipe_scythe")
        ));
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
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(mm.deserialize("<light_purple>Verfügbare Items: <white>dagger, drill, berserker, thor, spear, phasemace, scythe"));
            return true;
        }
        ItemStack item = buildItem(args[0]);
        if (item == null) {
            sender.sendMessage(mm.deserialize("<red>Unbekanntes Item! Nutze: dagger, drill, berserker, thor, spear, phasemace, scythe"));
            return true;
        }
        Player target = (args.length >= 2) ? Bukkit.getPlayer(args[1]) : (sender instanceof Player p ? p : null);
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
            return List.of("dagger", "drill", "berserker", "thor", "spear", "phasemace", "scythe");
        }
        return null;
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
            case "DAGGER" -> {
                if (!checkCooldown(p, "DAGGER", 12)) return;
                Location start = p.getLocation();
                Vector dir = start.getDirection().normalize();
                Location targetLoc = start.clone();
                for (int i = 1; i <= 8; i++) {
                    Location foot = start.clone().add(dir.clone().multiply(i));
                    Location head = foot.clone().add(0, 1, 0);
                    if (foot.getBlock().getType().isOccluding() || head.getBlock().getType().isOccluding()) {
                        break;
                    }
                    targetLoc = foot;
                }
                targetLoc.setYaw(start.getYaw());
                targetLoc.setPitch(start.getPitch());
                p.getWorld().spawnParticle(Particle.PORTAL, start.clone().add(0, 1, 0), 40, 0.4, 0.5, 0.4, 0.15);
                p.teleport(targetLoc);
                p.getWorld().spawnParticle(Particle.PORTAL, targetLoc.clone().add(0, 1, 0), 40, 0.4, 0.5, 0.4, 0.15);
                p.playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
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
                        Vector push = le.getLocation().toVector().subtract(strikeLoc.toVector()).normalize().multiply(1.2).setY(0.6);
                        le.setVelocity(push);
                    }
                }
            }
            case "SPEAR" -> {
                e.setCancelled(true);
                if (!checkCooldown(p, "SPEAR", 6)) return;
                Vector boost = p.getLocation().getDirection().normalize().multiply(1.85).setY(0.75);
                p.setVelocity(boost);
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
        }
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onScytheHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        if (!(e.getEntity() instanceof LivingEntity victim)) return;
        if (!"SCYTHE".equals(getItemId(p.getInventory().getItemInMainHand()))) return;

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
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNoFallDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        long until = noFallDamageUntil.getOrDefault(p.getUniqueId(), 0L);
        if (System.currentTimeMillis() < until) {
            e.setCancelled(true);
        } else {
            noFallDamageUntil.remove(p.getUniqueId());
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBerserkerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!"BERSERKER".equals(getItemId(p.getInventory().getChestplate()))) return;

        double afterHealth = p.getHealth() - e.getFinalDamage();
        if (afterHealth > 0 && afterHealth <= 10.0) {
            if (!checkCooldown(p, "BERSERKER", 20)) return;
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 0));
            p.getWorld().spawnParticle(Particle.WITCH, p.getLocation().add(0, 1, 0), 40, 0.5, 0.8, 0.5, 0.1);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1.4f);
            p.sendActionBar(mm.deserialize("<dark_purple><bold>BERSERKER-WUT AKTIVIERT!</bold>"));
        }
    }
}
