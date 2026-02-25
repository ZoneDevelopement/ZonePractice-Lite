package dev.nandi0813.practice.Manager.Match.MatchType.Duel;

import dev.nandi0813.practice.Manager.File.ConfigManager;
import dev.nandi0813.practice.Manager.File.LanguageManager;
import dev.nandi0813.practice.Manager.Match.Enum.TeamEnum;
import dev.nandi0813.practice.Manager.Match.Match;
import dev.nandi0813.practice.Manager.Match.MatchStats.MatchStatEditor;
import dev.nandi0813.practice.Manager.Match.MatchStats.PlayerMatchStat;
import dev.nandi0813.practice.Manager.Match.Util.PlayerUtil;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Practice;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Duel {

    public static void startMatch(Match match) {
        Player player1 = match.getPlayers().get(0);
        Player player2 = match.getPlayers().get(1);

        match.getTeams().put(player1, TeamEnum.TEAM1);
        match.getTeams().put(player2, TeamEnum.TEAM2);

        for (String line : LanguageManager.getList("match.duel.match-start")) {
            match.sendMessage(line
                    .replaceAll("%weightClass%", (match.isRanked() ? "&cRanked" : "&aUnranked"))
                    .replaceAll("%ladder%", match.getLadder().getName())
                    .replaceAll("%map%", match.getArena().getName()), true);
        }
    }

    public static void endMatch(Match match, Player winner, Player loser) {
        for (Player player : match.getPlayers()) {
            PlayerMatchStat matchStat = match.getMatchStats().get(player);
            if (!matchStat.isSet()) matchStat.end();
        }

        List<String> spectators = new ArrayList<>();
        for (Player spectator : match.getSpectators())
            if (!spectator.hasPermission("zonepractice.spectate.silent"))
                spectators.add(spectator.getName());

        if (winner != null && loser != null) {
            List<Player> msgTo = new ArrayList<>();
            msgTo.addAll(match.getPlayers());
            msgTo.addAll(match.getSpectators());

            for (String line : LanguageManager.getList("match.duel.match-end")) {
                for (Player player : msgTo) {

                    if (line.contains("%winner%")) {
                        TextComponent winnerComponent = new TextComponent(line.replaceAll("%winner%", winner.getName()));
                        winnerComponent.setHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder(ChatColor.translateAlternateColorCodes('&',
                                        "&aClick to view inventory of &6" + winner.getName() + "&a.")).create()
                        ));
                        winnerComponent.setClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/matchinv " + match.getMatchID() + " " + winner.getName()
                        ));
                        player.spigot().sendMessage(winnerComponent);

                    } else if (line.contains("%loser%")) {
                        TextComponent loserComponent = new TextComponent(line.replaceAll("%loser%", loser.getName()));
                        loserComponent.setHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder(ChatColor.translateAlternateColorCodes('&',
                                        "&aClick to view inventory of &6" + loser.getName() + "&a.")).create()
                        ));
                        loserComponent.setClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/matchinv " + match.getMatchID() + " " + loser.getName()
                        ));
                        player.spigot().sendMessage(loserComponent);

                    } else if (line.contains("%spectators%")) {
                        if (!spectators.isEmpty()) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    line.replaceAll("%size%", String.valueOf(spectators.size()))
                                            .replaceAll("%spectators%", spectators.toString().replace("[", "").replace("]", ""))));
                        }

                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                    }
                }
            }

            Profile winnerProfile = Practice.getProfileManager().getProfiles().get(winner);
            Profile loserProfile = Practice.getProfileManager().getProfiles().get(loser);

            MatchStatEditor.setDuelStats(winnerProfile, loserProfile, match.getLadder(), match.isRanked());

            if (!match.isRanked() && (winnerProfile.getUnrankedWins() == ConfigManager.getInt("ranked.min-unranked-wins")))
                winner.sendMessage(LanguageManager.getString("match.ranked-limit-reach"));

            if (match.isRanked()) {
                int elochange = ConfigManager.getInt("ranked.elo-change");

                int winnerOldElo = winnerProfile.getElo().getOrDefault(match.getLadder(), 1000);
                int winnerNewElo = winnerOldElo + elochange;
                winnerProfile.getElo().put(match.getLadder(), winnerNewElo);

                int loserOldElo = loserProfile.getElo().getOrDefault(match.getLadder(), 1000);
                int loserNewElo;

                if (loserOldElo >= 100) {
                    loserNewElo = loserOldElo - elochange;
                    loserProfile.getElo().put(match.getLadder(), loserNewElo);
                }
                loserNewElo = loserOldElo;


                for (String line : LanguageManager.getList("match.duel.ranked-extension")) {
                    match.sendMessage(line
                            .replaceAll("%winner%", winnerProfile.getPlayer().getName())
                            .replaceAll("%loser%", loserProfile.getPlayer().getName())
                            .replaceAll("%eloChange%", String.valueOf(elochange))
                            .replaceAll("%winnerNewElo%", String.valueOf(winnerNewElo))
                            .replaceAll("%loserNewElo%", String.valueOf(loserNewElo)), true);
                }
            }

            winnerProfile.saveData();
            loserProfile.saveData();
        } else {
            for (String line : LanguageManager.getList("match.duel.match-end-draw"))
                match.sendMessage(line, true);
        }
    }

    public static void killPlayer(Match match, Player player, boolean teleport) {
        PlayerMatchStat matchStat = match.getMatchStats().get(player);
        if (!matchStat.isSet())
            matchStat.end();
        matchStat.setEndHeart(0.0);

        if (teleport)
            PlayerUtil.teleportPlayer(player, match);
        PlayerUtil.setMatchPlayer(player);

        for (Player matchPlayer : match.getPlayers()) {
            if (!matchPlayer.equals(player)) {
                PlayerUtil.dropPlayerInventory(player, match);
                match.getRoundManager().endRound(matchPlayer);
            }
        }
    }

    public static Player getOppositePlayer(Match match, Player player) {
        for (Player matchPlayer : match.getPlayers())
            if (!matchPlayer.equals(player))
                return matchPlayer;
        return null;
    }

}
