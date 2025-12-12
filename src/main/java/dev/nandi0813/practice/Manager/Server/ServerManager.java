package dev.nandi0813.practice.Manager.Server;

import dev.nandi0813.practice.Manager.File.BackendManager;
import org.bukkit.Location;

public class ServerManager {

    /**
     * It sets the lobby location to the location passed in
     *
     * @param lobbyLocation The location of the lobby
     */
    public static void setLobby(Location lobbyLocation) {
        BackendManager.getConfig().set("lobby.world", lobbyLocation.getWorld().getName());
        BackendManager.getConfig().set("lobby.x", lobbyLocation.getX());
        BackendManager.getConfig().set("lobby.y", lobbyLocation.getY());
        BackendManager.getConfig().set("lobby.z", lobbyLocation.getZ());
        BackendManager.getConfig().set("lobby.yaw", lobbyLocation.getYaw());
        BackendManager.getConfig().set("lobby.pitch", lobbyLocation.getPitch());
        BackendManager.save();
    }

    /**
     * If the config has a lobby location, return it, otherwise return null
     *
     * @return The lobby location
     */
    public static Location getLobby() {
        if (!BackendManager.getConfig().isSet("lobby.world")) return null;

        String worldName = BackendManager.getConfig().getString("lobby.world");
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = BackendManager.getConfig().getDouble("lobby.x");
        double y = BackendManager.getConfig().getDouble("lobby.y");
        double z = BackendManager.getConfig().getDouble("lobby.z");
        float yaw = (float) BackendManager.getConfig().getDouble("lobby.yaw");
        float pitch = (float) BackendManager.getConfig().getDouble("lobby.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }
}