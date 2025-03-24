package dev.nandi0813.practice.Listener;

import dev.nandi0813.practice.Manager.File.ConfigManager;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileLaunch implements Listener {

    @EventHandler
    public void onProjHit(ProjectileHitEvent e) {
        if (ConfigManager.getBoolean("world-settings.remove-arrows")) {
            if ((e.getEntity() instanceof Arrow))
                e.getEntity().remove();
        }
    }

}
