package org.sincerityworld.brickthrower;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class BrickThrower extends JavaPlugin implements Listener, CommandExecutor {
    private static double BRICK_SNOWBALL_DAMAGE = 2.0;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("setbrikdamage").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (!player.hasPermission("admin.permission")) {
            player.sendMessage("У вас нет разрешения на использование этой команды.");
            return true;
        }
        if (command.getName().equalsIgnoreCase("setbrikdamage")) {
            if (sender instanceof Player) {
                if (args.length == 1) {
                    try {
                        double newDamage = Double.parseDouble(args[0]);
                        BRICK_SNOWBALL_DAMAGE = newDamage;
                        player.sendMessage("Урон от кирпича установлен на: " + newDamage);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Неверное значение урона, введите число!");
                    }
                } else {
                    player.sendMessage("Использование: /setbrikdamage <число>");
                }
            } else {
                sender.sendMessage("Эта команда доступна только для игроков.");
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.BRICK && item.getAmount() > 0) {
                Snowball snowball = player.launchProjectile(Snowball.class);
                snowball.setItem(new ItemStack(Material.BRICK));
                player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1.0f, 1.0f);
                item.setAmount(item.getAmount() - 1);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball && ((Snowball) event.getEntity()).getItem().getType() == Material.BRICK) {
            if (event.getHitEntity() != null) {
                event.getHitEntity().getWorld().playSound(event.getHitEntity().getLocation(), Sound.BLOCK_DECORATED_POT_BREAK, 1.0f, 1.0f);
            } else if (event.getHitBlock() != null) {
                Material blockType = event.getHitBlock().getType();
                if (blockType.name().endsWith("_STAINED_GLASS") || blockType == Material.GLASS || blockType == Material.GLASS_PANE || blockType.name().endsWith("_STAINED_GLASS_PANE") || blockType == Material.DECORATED_POT) {
                    event.getHitBlock().getWorld().playSound(event.getHitBlock().getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f); // Воспроизводим звук ломания стекла
                    event.getHitBlock().getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, event.getHitBlock().getLocation().add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, 0.1, event.getHitBlock().getBlockData());
                    event.getHitBlock().setType(Material.AIR);
                }
                event.getHitBlock().getWorld().playSound(event.getHitBlock().getLocation(), Sound.BLOCK_DECORATED_POT_BREAK, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball && ((Snowball) event.getDamager()).getItem().getType() == Material.BRICK) {
            event.setDamage(BRICK_SNOWBALL_DAMAGE);
            if (event.getEntity() instanceof LivingEntity) {
                event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.BLOCK_DECORATED_POT_BREAK, 1.0f, 1.0f);
            }
        }
    }
}
