package dev.nandi0813.practice.Manager.Match.Util;

import dev.nandi0813.practice.Manager.File.ConfigManager;
import dev.nandi0813.practice.Manager.Ladder.KnockbackType;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class KnockbackUtil {

    /**
     * It sets the knockback of a player based on the knockback type
     *
     * @param player        The player to set the knockback for.
     * @param knockbackType The type of knockback you want to use.
     */
    public static void setPlayerKnockback(Entity player, KnockbackType knockbackType) {
        // Skip knockback modification if disabled
        if (!ConfigManager.getBoolean("knockback.enabled")) {
            return;
        }

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
        player.setVelocity(vel);
    }

}
