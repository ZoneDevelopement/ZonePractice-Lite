package dev.nandi0813.practice.Util.EntityHider;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import dev.nandi0813.practice.Manager.Match.Match;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Manager.Profile.ProfileStatus;
import dev.nandi0813.practice.Practice;
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

public class EntityHiderListener implements PacketListener, Listener {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SOUND_EFFECT
                || event.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            handleSoundPacket(event);
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.EFFECT) {
            handleEffectPacket(event);
        }
    }

    private void handleSoundPacket(PacketSendEvent event) {
        WrapperPlayServerSoundEffect wrapper = new WrapperPlayServerSoundEffect(event);
        Player player = event.getPlayer();
        Vector3i position = wrapper.getEffectPosition();

        if (shouldCancelSound(player, position)) {
            event.setCancelled(true);
        }
    }

    private void handleEffectPacket(PacketSendEvent event) {
        WrapperPlayServerEffect wrapper = new WrapperPlayServerEffect(event);
        Player player = event.getPlayer();
        Vector3i position = wrapper.getPosition();
        Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

        if (match != null && match.effectPositions.contains(position)) {
            match.effectPositions.remove(position);
        } else {
            event.setCancelled(true);
        }
    }

    private boolean shouldCancelSound(Player player, Vector3i position) {
        Player nearestPlayer = null;
        double nearestDistance = 5.0;

        Location soundLocation = new Location(
                player.getWorld(),
                position.x / 8.0,
                position.y / 8.0,
                position.z / 8.0
        );

        for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
            if (soundLocation.getWorld() != nearbyPlayer.getWorld()) continue;
            double distance = nearbyPlayer.getLocation().distance(soundLocation);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = nearbyPlayer;
            }
        }

        return nearestPlayer != null && !player.canSee(nearestPlayer);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile instanceof ThrownPotion thrownPotion)) {
            return;
        }

        if (!(projectile.getShooter() instanceof Player player)) {
            return;
        }

        Profile profile = Practice.getProfileManager().getProfiles().get(player);
        Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

        if (profile == null
                || !profile.getStatus().equals(ProfileStatus.MATCH)
                || match == null
                || match.getLadder().isBuild()) {
            return;
        }

        Vector3i convertedLocation = toBlockVector(projectile.getLocation());

        @SuppressWarnings("unused")
        WrapperPlayServerEffect packet = new WrapperPlayServerEffect(
                2002, // Effect ID (potion break)
                convertedLocation,
                thrownPotion.getItem().getDurability(),
                false
        );

        match.effectPositions.add(convertedLocation);
    }

    private Vector3i toBlockVector(Location location) {
        return new Vector3i(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        Profile profile = Practice.getProfileManager().getProfiles().get(player);
        Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

        if (profile == null
                || !profile.getStatus().equals(ProfileStatus.MATCH)
                || match == null
                || match.getLadder().isBuild()) {
            return;
        }

        for (Player online : Bukkit.getServer().getOnlinePlayers()) {
            if (match.getPlayers().contains(online) || match.getSpectators().contains(online)) {
                continue;
            }

            event.getAffectedEntities().removeIf(
                    entity -> !match.getPlayers().contains(Bukkit.getPlayer(entity.getName()))
            );

            event.setCancelled(true);
            for (LivingEntity entity : event.getAffectedEntities()) {
                Player target = Bukkit.getPlayer(entity.getName());
                if (match.getPlayers().contains(target)) {
                    entity.addPotionEffects(event.getEntity().getEffects());
                }
            }
        }
    }
}
