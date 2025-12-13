package dev.nandi0813.practice.Manager.Match.MatchType.PartyFFA;

import dev.nandi0813.practice.Manager.Arena.Util.Cuboid;
import dev.nandi0813.practice.Manager.File.LanguageManager;
import dev.nandi0813.practice.Manager.Match.Enum.MatchStatus;
import dev.nandi0813.practice.Manager.Match.Enum.MatchType;
import dev.nandi0813.practice.Manager.Match.Match;
import dev.nandi0813.practice.Manager.Match.MatchType.Duel.Duel;
import dev.nandi0813.practice.Manager.Profile.ProfileStatus;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Practice;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PartyFFAListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        Profile profile = Practice.getProfileManager().getProfiles().get(player);
        Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

        if (profile.getStatus().equals(ProfileStatus.MATCH)
                && match.getType().equals(MatchType.PARTY_FFA)
                && match.getStatus().equals(MatchStatus.LIVE)) {
            Duel.killPlayer(match, player, true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            Profile profile = Practice.getProfileManager().getProfiles().get(player);
            Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

            if (profile.getStatus().equals(ProfileStatus.MATCH) && match.getType().equals(MatchType.PARTY_FFA)) {
                if (match.getStatus().equals(MatchStatus.LIVE) && match.getAlivePlayers().contains(player)) {
                    if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID) || player.getHealth() - e.getFinalDamage() <= 0) {
                        // Apply damage animation to die.
                        player.damage(0);
                        PartyFFA.killPlayer(match, player, true);
                    }
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Profile profile = Practice.getProfileManager().getProfiles().get(player);
        Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

        if (profile.getStatus().equals(ProfileStatus.MATCH) && match.getType().equals(MatchType.PARTY_FFA)) {
            match.removePlayer(player, false);
            match.getPlayers().remove(player);

            PartyFFA.killPlayer(match, player, false);
            match.sendMessage(LanguageManager.getString("match.partyffa.player-left").replaceAll("%player%", player.getName()), true);
            if (match.getPlayers().size() < 2) {
                for (Player winner : match.getPlayers()) {
                    match.getRoundManager().endMatch(winner);
                    return;
                }
            }
        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Profile profile = Practice.getProfileManager().getProfiles().get(player);
        Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

        if (profile.getStatus().equals(ProfileStatus.MATCH) && match.getType().equals(MatchType.PARTY_FFA) && !match.getAlivePlayers().contains(player))
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickItem(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        Profile profile = Practice.getProfileManager().getProfiles().get(player);
        Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

        if (profile.getStatus().equals(ProfileStatus.MATCH) && match.getType().equals(MatchType.PARTY_FFA) && !match.getAlivePlayers().contains(player))
            e.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        Player player = (Player) e.getEntity();
        Profile profile = Practice.getProfileManager().getProfiles().get(player);
        Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

        if (profile.getStatus().equals(ProfileStatus.MATCH) && match.getType().equals(MatchType.PARTY_FFA) && !match.getAlivePlayers().contains(player))
            e.setFoodLevel(20);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            Profile profile = Practice.getProfileManager().getProfiles().get(player);
            Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

            if (profile.getStatus().equals(ProfileStatus.MATCH) && match.getType().equals(MatchType.PARTY_FFA) && !match.getAlivePlayers().contains(player))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Profile profile = Practice.getProfileManager().getProfiles().get(player);
        Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

        if (profile.getStatus().equals(ProfileStatus.MATCH) && match.getType().equals(MatchType.PARTY_FFA) && !match.getAlivePlayers().contains(player)) {
            Cuboid cuboid = match.getGameArena().getCuboid();

            if (!cuboid.contains(e.getTo()))
                player.teleport(match.getGameArena().getPosition3());
        }
    }
}