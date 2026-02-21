package dev.nandi0813.practice.Manager.Match.Util;

import dev.nandi0813.practice.Manager.File.ConfigManager;
import dev.nandi0813.practice.Manager.Ladder.KnockbackType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.logging.Logger;

public class KnockbackUtil {
    private static final double MAX_VELOCITY_COMPONENT = 4.0;

    /**
     * It sets the knockback of a player based on the knockback type
     *
     * @param player        The player to set the knockback for.
     * @param knockbackType The type of knockback you want to use.
     */
    public static void setPlayerKnockback(Entity player, KnockbackType knockbackType) {
        if (!ConfigManager.getConfig().getBoolean("knockback.enabled", true)) return;
        int airhorizontal = ConfigManager.getConfig().getInt("knockback." + knockbackType.toString().toLowerCase() + ".air-horizontal");
        int airvertical = ConfigManager.getConfig().getInt("knockback." + knockbackType.toString().toLowerCase() + ".air-vertical");
        int horizontal = ConfigManager.getConfig().getInt("knockback." + knockbackType.toString().toLowerCase() + ".horizontal");
        int vertical = ConfigManager.getConfig().getInt("knockback." + knockbackType.toString().toLowerCase() + ".vertical");
        Vector vel = player.getVelocity();
        if (player.isOnGround()) {
            vel.setX(vel.getX() * horizontal);
            vel.setZ(vel.getZ() * horizontal);
            vel.setY(vel.getY() * vertical);
        } else {
            vel.setX(vel.getX() * airhorizontal);
            vel.setZ(vel.getZ() * airhorizontal);
            vel.setY(vel.getY() * airvertical);
        }
        if (Math.abs(vel.getX()) > MAX_VELOCITY_COMPONENT
                || Math.abs(vel.getY()) > MAX_VELOCITY_COMPONENT
                || Math.abs(vel.getZ()) > MAX_VELOCITY_COMPONENT) {
            Logger logger = Bukkit.getLogger();
            String name = player.getName() != null ? player.getName() : player.getType().name();
            String message = "Excessive velocity detected for " + name + ": x=" + vel.getX() + ", y=" + vel.getY() + ", z=" + vel.getZ();
            logger.warning(message);
            if (player instanceof Player) {
                Player p = (Player) player;
                p.sendMessage(message);
            }
            return;
        }
        player.setVelocity(vel);
    }
}
