package dev.nandi0813.practice.Manager.Inventory;

import dev.nandi0813.practice.Manager.File.ConfigManager;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Manager.Profile.ProfileManager;
import dev.nandi0813.practice.Manager.Profile.ProfileStatus;
import dev.nandi0813.practice.Practice;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ProfileManager profileManager = Practice.getProfileManager();
        Profile profile = profileManager.getProfiles().get(player);

        if (ConfigManager.getBoolean("protection.allow-lobby-inventory")) {
            return;
        }

        if (!player.hasPermission("zonepractice.admin") && profile.getStatus().equals(ProfileStatus.LOBBY))
            e.setCancelled(true);
        else if (profile.getStatus().equals(ProfileStatus.QUEUE))
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        ProfileManager profileManager = Practice.getProfileManager();
        Profile profile = profileManager.getProfiles().get(player);

        if (ConfigManager.getBoolean("protection.allow-lobby-drop")) {
            return;
        }

        if (!player.hasPermission("zonepractice.admin") && profile.getStatus().equals(ProfileStatus.LOBBY))
            e.setCancelled(true);
        else if (profile.getStatus().equals(ProfileStatus.QUEUE))
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        ProfileManager profileManager = Practice.getProfileManager();
        Profile profile = profileManager.getProfiles().get(player);

        if (ConfigManager.getBoolean("protection.allow-lobby-pickup")) {
            return;
        }

        if (!player.hasPermission("zonepractice.admin") && profile.getStatus().equals(ProfileStatus.LOBBY))
            e.setCancelled(true);
        else if (profile.getStatus().equals(ProfileStatus.QUEUE))
            e.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            Profile profile = Practice.getProfileManager().getProfiles().get(player);

            if (ConfigManager.getBoolean("protection.allow-lobby-hunger")) {
                return;
            }

            if (profile.getStatus().equals(ProfileStatus.LOBBY) || profile.getStatus().equals(ProfileStatus.QUEUE)) {
                e.setCancelled(true);
                e.setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        Profile profile = Practice.getProfileManager().getProfiles().get(player);

        if (profile == null) return;

        if (ConfigManager.getBoolean("protection.allow-lobby-damage")) {
            return;
        }

        if (profile.getStatus().equals(ProfileStatus.LOBBY) || profile.getStatus().equals(ProfileStatus.QUEUE))
            e.setCancelled(true);
    }
}
