package dev.nandi0813.practice.Manager.Match.MatchType.PartySplit;

import dev.nandi0813.practice.Manager.File.LanguageManager;
import dev.nandi0813.practice.Manager.Match.Enum.TeamEnum;
import dev.nandi0813.practice.Manager.Match.Match;
import dev.nandi0813.practice.Manager.Match.Util.PlayerUtil;
import dev.nandi0813.practice.Manager.Match.Util.TeamUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PartySplit {

    /**
     * It collects all players in the match, shuffles them, splits them into two teams, and then sends a message to the
     * players
     *
     * @param match The match object.
     */
    public static void startMatch(Match match) {
        // Collect and shuffle players
        List<Player> players = new ArrayList<>(match.getPlayers());
        Collections.shuffle(players);

        // Split players to the teams.
        List<Player> team1 = new ArrayList<>();
        List<Player> team2 = new ArrayList<>();
        ArrayList<String> team1Names = new ArrayList<>();
        ArrayList<String> team2Names = new ArrayList<>();

        for (Player player : players) {
            if (team2.size() > team1.size()) {
                team1.add(player);
                team1Names.add(player.getName());
            } else {
                team2.add(player);
                team2Names.add(player.getName());
            }
        }

        // Add players to the teams.
        for (Player player : team1)
            match.getTeams().put(player, TeamEnum.TEAM1);
        for (Player player : team2)
            match.getTeams().put(player, TeamEnum.TEAM2);

        for (String line : LanguageManager.getList("match.partysplit.match-start")) {
            match.sendMessage(line
                    .replaceAll("%ladder%", match.getLadder().getName())
                    .replaceAll("%map%", match.getArena().getName())
                    .replaceAll("%team1name%", TeamEnum.TEAM1.getName())
                    .replaceAll("%team2name%", TeamEnum.TEAM2.getName())
                    .replaceAll("%team1players%", team1Names.toString().replace("[", "").replace("]", ""))
                    .replaceAll("%team2players%", team2Names.toString().replace("[", "").replace("]", "")), true);
        }
    }

    /**
     * It sends a message to the players in the match, telling them who won and who lost
     *
     * @param match  The match that is ending
     * @param winner The player who won the match. If the match ended in a draw, this will be null.
     */
    public static void endMatch(Match match, Player winner) {
        List<String> spectators = new ArrayList<>();
        for (Player spectator : match.getSpectators())
            if (!spectator.hasPermission("zonepractice.spectate.silent"))
                spectators.add(spectator.getName());

        if (winner != null) {
            ArrayList<String> winners = new ArrayList<>();
            ArrayList<String> losers = new ArrayList<>();
            for (Player player : match.getPlayers()) {
                if (match.getTeams().get(player).equals(match.getTeams().get(winner)))
                    winners.add(player.getName());
                else
                    losers.add(player.getName());
            }

            for (String line : LanguageManager.getList("match.partysplit.match-end")) {

                if (line.contains("%spectators%")) {
                    if (!spectators.isEmpty()) {
                        match.sendMessage(line
                                        .replaceAll("%size%", String.valueOf(spectators.size()))
                                        .replaceAll("%spectators%", spectators.toString().replace("[", "").replace("]", ""))
                                        .replaceAll("%winnerTeam%", match.getTeams().get(winner).getName())
                                        .replaceAll("%winnerPlayers%", winners.toString().replace("[", "").replace("]", ""))
                                        .replaceAll("%loserTeam%", TeamUtil.getOppositeTeam(match.getTeams().get(winner)).getName())
                                        .replaceAll("%loserPlayers%", losers.toString().replace("[", "").replace("]", "")),
                                true);
                    }
                    continue;
                }

                match.sendMessage(line
                                .replaceAll("%winnerTeam%", match.getTeams().get(winner).getName())
                                .replaceAll("%winnerPlayers%", winners.toString().replace("[", "").replace("]", ""))
                                .replaceAll("%loserTeam%", TeamUtil.getOppositeTeam(match.getTeams().get(winner)).getName())
                                .replaceAll("%loserPlayers%", losers.toString().replace("[", "").replace("]", "")),
                        true);
            }

        } else {
            for (String line : LanguageManager.getList("match.partysplit.match-end-draw")) {

                if (line.contains("%spectators%")) {
                    if (!spectators.isEmpty()) {
                        match.sendMessage(line
                                        .replaceAll("%size%", String.valueOf(spectators.size()))
                                        .replaceAll("%spectators%", spectators.toString().replace("[", "").replace("]", "")),
                                true);
                    }
                } else {
                    match.sendMessage(line, true);
                }
            }
        }
    }

    /**
     * It removes a player from the alive players list, sends a message to the match, hides the player from the other
     * players, and ends the round if the player's team is empty
     *
     * @param match   The match object
     * @param player  The player that died
     * @param message If true, the message will be sent to the players.
     */
    public static void killPlayer(Match match, Player player, boolean message) {
        match.getAlivePlayers().remove(player);
        List<Player> playerTeam = getTeamAlivePlayers(match, match.getTeams().get(player));

        if (message)
            match.sendMessage(LanguageManager.getString("match.partysplit.player-die").replaceAll("%player%", player.getName()).replaceAll("%playerTeam%", match.getTeams().get(player).getName()).replaceAll("%playerTeamLeft%", String.valueOf(playerTeam.size())), true);

        if (player.isOnline())
            PlayerUtil.hidePlayerPartyGames(match, player);

        if (playerTeam.isEmpty())
            match.getRoundManager().endRound(match.getAlivePlayers().stream().findAny().get());
    }


    /**
     * "Return a list of players on the given team."
     * <p>
     * The first line of the function is the function signature. It tells us the name of the function, the type of the
     * return value, and the types of the parameters
     *
     * @param match The match object
     * @param team  The team you want to get the players from.
     * @return A list of players on a team.
     */
    public static List<Player> getTeamPlayers(Match match, TeamEnum team) {
        List<Player> players = new ArrayList<>();
        for (Player player : match.getPlayers())
            if (match.getTeams().get(player).equals(team))
                players.add(player);
        return players;
    }

    /**
     * Returns a list of all the alive players on a team.
     *
     * @param match The match object
     * @param team  The team you want to get the players from.
     * @return A list of players that are alive and on the team.
     */
    public static List<Player> getTeamAlivePlayers(Match match, TeamEnum team) {
        List<Player> players = new ArrayList<>();
        for (Player player : getTeamPlayers(match, team))
            if (match.getAlivePlayers().contains(player))
                players.add(player);
        return players;
    }

}