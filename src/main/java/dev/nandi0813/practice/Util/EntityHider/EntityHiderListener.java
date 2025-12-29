package dev.nandi0813.practice.Util.EntityHider;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import dev.nandi0813.practice.Manager.Match.Match;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Manager.Profile.ProfileStatus;
import dev.nandi0813.practice.Practice;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;

public class EntityHiderListener extends PacketAdapter implements Listener {

    @Getter
    public static List<BlockPosition> blockPositions = new ArrayList<>();

    public EntityHiderListener() {
        super(Practice.getInstance(), PacketType.Play.Server.NAMED_ENTITY_SPAWN, PacketType.Play.Server.SPAWN_ENTITY, PacketType.Play.Server.NAMED_SOUND_EFFECT, PacketType.Play.Server.WORLD_EVENT);
    }

    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();
        try {
            PacketContainer packet = event.getPacket();
            PacketType type = packet.getType();
            if (type == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                double x = packet.getIntegers().read(0) / 8.0;
                double y = packet.getIntegers().read(1) / 8.0;
                double z = packet.getIntegers().read(2) / 8.0;
                Player nearestPlayer = null;
                double nearestDistance = 5.0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    double deltaX = p.getLocation().getX() - x;
                    double deltaY = p.getLocation().getY() - y;
                    double deltaZ = p.getLocation().getZ() - z;
                    double distance = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                    if (distance < nearestDistance) {
                        nearestPlayer = p;
                        nearestDistance = distance;
                    }
                }
                if (nearestPlayer != null && !player.canSee(nearestPlayer)) {
                    event.setCancelled(true);
                }
            } else if (type == PacketType.Play.Server.WORLD_EVENT) {
                BlockPosition bp = packet.getBlockPositionModifier().getValues().get(0);
                if (getBlockPositions().contains(bp)) {
                    getBlockPositions().remove(bp);
                    return;
                }
                event.setCancelled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();
        if (projectile instanceof ThrownPotion) {
            ThrownPotion potion = (ThrownPotion) projectile;
            if (projectile.getShooter() instanceof Player) {
                Player player = (Player) projectile.getShooter();
                Profile profile = Practice.getProfileManager().getProfiles().get(player);
                Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

                if (profile.getStatus().equals(ProfileStatus.MATCH) && match != null && !match.getLadder().isBuild()) {
                    PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_EVENT);

                    packet.getIntegers().write(0, 2002);
                    Location location = projectile.getLocation();
                    BlockPosition bp = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                    packet.getBlockPositionModifier().write(0, bp);
                    packet.getIntegers().write(1, (int) potion.getItem().getDurability());
                    packet.getBooleans().write(0, false);

                    EntityHiderListener.blockPositions.add(bp);
                    EntityHiderListener.blockPositions.add(bp);

                    for (Player matchPlayer : match.getPlayers())
                        ProtocolLibrary.getProtocolManager().sendServerPacket(matchPlayer, packet);
                }
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            Player player = (Player) e.getEntity().getShooter();
            Profile profile = Practice.getProfileManager().getProfiles().get(player);
            Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

            if (profile.getStatus().equals(ProfileStatus.MATCH) && match != null && !match.getLadder().isBuild()) {
                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                    if (match.getPlayers().contains(online) || match.getSpectators().contains(online))
                        continue;

                    e.getAffectedEntities().removeIf(entity -> !match.getPlayers().contains(Bukkit.getPlayer(entity.getName())));

                    e.setCancelled(true);
                    for (LivingEntity entity : e.getAffectedEntities()) {
                        if (match.getPlayers().contains(Bukkit.getPlayer(entity.getName())))
                            entity.addPotionEffects(e.getEntity().getEffects());
                    }
                }
            }
        }
    }
}