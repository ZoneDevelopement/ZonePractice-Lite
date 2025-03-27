package dev.nandi0813.practice.Util;

import dev.nandi0813.practice.Manager.File.ConfigManager;
import dev.nandi0813.practice.Manager.Match.Match;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Manager.Profile.ProfileStatus;
import dev.nandi0813.practice.Manager.Server.ServerManager;
import dev.nandi0813.practice.Practice;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.EventPriority;

public class PlayerHider implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Profile profile = Practice.getProfileManager().getProfiles().get(player);

        // If the player is in the lobby, they see everyone except vanished players
        if (profile.getStatus().equals(ProfileStatus.LOBBY)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (player != p) {
                    if (p.hasPermission("zonepractice.staff.vanish") && !player.hasPermission("zonepractice.staff.vanish.bypass")) {
                        player.hidePlayer(p); // Hide vanished admins from regular players
                    } else {
                        player.showPlayer(p);
                    }

                    if (player.hasPermission("zonepractice.staff.vanish") && !p.hasPermission("zonepractice.staff.vanish.bypass")) {
                        p.hidePlayer(player); // Hide the player if they are vanished
                    } else {
                        p.showPlayer(player);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        Profile profile = Practice.getProfileManager().getProfiles().get(player);

        if (ServerManager.getLobby() != null) {
            if (e.getFrom().getWorld().equals(ServerManager.getLobby().getWorld()) && e.getTo().getWorld().equals(Practice.getArenaManager().getArenasWorld())) {
                if (profile.getStatus().equals(ProfileStatus.MATCH)) {
                    Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

                    // Hide all non-matching players from this player
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (player != p) {
                            if (!match.getPlayers().contains(p)) {
                                player.hidePlayer(p); // Hide non-match players
                                p.hidePlayer(player); // Hide the player from non-matching players
                            } else {
                                player.showPlayer(p); // Show match players
                                p.showPlayer(player); // Show the player to match players
                            }
                        }
                    }
                }
            }

            if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
                Match match = Practice.getMatchManager().getLiveMatchBySpectator(player);

                if (match != null) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (player != p) {
                            if (match.getPlayers().contains(p)) {
                                player.showPlayer(p);  // Spectator sees players in their match
                                p.hidePlayer(player);  // Players in the match do not see the spectator
                            } else if (match.getSpectators().contains(p)) {
                                if (ConfigManager.getBoolean("match-settings.hide-other-spectators")) {
                                    player.hidePlayer(p);
                                    p.hidePlayer(player);
                                } else {
                                    player.showPlayer(p);
                                    p.showPlayer(player);
                                }
                            } else {
                                player.hidePlayer(p);  // Spectator does not see players from other matches
                                p.hidePlayer(player);  // Players outside the match do not see the spectator
                            }
                        }
                    }
                }
            }
        }

        // Transition from arena to lobby (depends on lobby)
        if (ServerManager.getLobby() != null &&
                e.getFrom().getWorld().equals(Practice.getArenaManager().getArenasWorld()) &&
                e.getTo().getWorld().equals(ServerManager.getLobby().getWorld())) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (player != p) {
                    if (p.hasPermission("zonepractice.staff.vanish") && !player.hasPermission("zonepractice.staff.vanish.bypass")) {
                        player.hidePlayer(p); // Hide vanished admins from regular players
                    } else {
                        player.showPlayer(p);
                    }

                    if (player.hasPermission("zonepractice.staff.vanish") && !p.hasPermission("zonepractice.staff.vanish.bypass")) {
                        p.hidePlayer(player); // Hide the player if they are vanished
                    } else {
                        p.showPlayer(player);
                    }
                }
            }
        }
    }
}

