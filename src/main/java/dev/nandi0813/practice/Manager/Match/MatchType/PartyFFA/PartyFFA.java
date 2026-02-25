package dev.nandi0813.practice.Manager.Match.MatchType.PartyFFA;

import dev.nandi0813.practice.Manager.File.LanguageManager;
import dev.nandi0813.practice.Manager.Match.Enum.TeamEnum;
import dev.nandi0813.practice.Manager.Match.Match;
import dev.nandi0813.practice.Manager.Match.Util.PlayerUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PartyFFA {

    // Starting a match.
    public static void startMatch(Match match) {
        ArrayList<String> playerNames = new ArrayList<>();

        for (Player player : match.getPlayers()) {
            match.getTeams().put(player, TeamEnum.TEAM1);
            playerNames.add(player.getName());
        }

        for (String line : LanguageManager.getList("match.partyffa.match-start")) {
            match.sendMessage(line
                    .replaceAll("%ladder%", match.getLadder().getName())
                    .replaceAll("%map%", match.getArena().getName())
                    .replaceAll("%players%", playerNames.toString().replace("[", "").replace("]", "")), true);
        }
    }

    /**
     * It sends a message to the players in the match, telling them who won
     *
     * @param match  The match that is ending.
     * @param winner The winner of the match. If the match ended in a draw, this will be null.
     */
    public static void endMatch(Match match, Player winner) {
        List<String> spectators = new ArrayList<>();
        for (Player spectator : match.getSpectators())
            if (!spectator.hasPermission("zonepractice.spectate.silent"))
                spectators.add(spectator.getName());

        if (winner != null) {
            ArrayList<String> losers = new ArrayList<>();
            for (Player player : match.getPlayers())
                if (!player.equals(winner)) losers.add(player.getName());

            for (String line : LanguageManager.getList("match.partyffa.match-end")) {

                if (line.contains("%spectators%")) {
                    if (!spectators.isEmpty()) {
                        match.sendMessage(line
                                .replaceAll("%size%", String.valueOf(spectators.size()))
                                .replaceAll("%spectators%", spectators.toString().replace("[", "").replace("]", "")), true);
                    }
                    continue;
                }

                match.sendMessage(line
                        .replaceAll("%winner%", winner.getName())
                        .replaceAll("%losers%", losers.toString().replace("[", "").replace("]", "")), true);
            }
        } else {
            for (String line : LanguageManager.getList("match.partyffa.match-end-draw")) {

                if (line.contains("%spectators%")) {
                    if (!spectators.isEmpty()) {
                        match.sendMessage(line
                                .replaceAll("%size%", String.valueOf(spectators.size()))
                                .replaceAll("%spectators%", spectators.toString().replace("[", "").replace("]", "")), true);
                    }
                } else {
                    match.sendMessage(line, true);
                }
            }
        }
    }

    /**
     * It kills a player, removes them from the alive players list, hides them from other players, and ends the round if
     * there's only one player left
     *
     * @param match   The match that the player is in
     * @param player  The player who died
     * @param message Whether or not to send a message to the players in the match.
     */
    public static void killPlayer(Match match, Player player, boolean message) {
        if (message)
            match.sendMessage(LanguageManager.getString("match.partyffa.player-die")
                    .replaceAll("%player%", player.getName()), true);

        match.getAlivePlayers().remove(player);

        if (player.isOnline())
            PlayerUtil.hidePlayerPartyGames(match, player);

        if (match.getAlivePlayers().size() == 1) {
            match.getRoundManager().endRound(match.getAlivePlayers().stream().findAny().get());
        }
    }
}