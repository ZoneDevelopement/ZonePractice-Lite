package dev.nandi0813.practice.Manager.Match;

import dev.nandi0813.practice.Event.MatchEndEvent;
import dev.nandi0813.practice.Event.MatchStartEvent;
import dev.nandi0813.practice.Manager.Arena.Arena;
import dev.nandi0813.practice.Manager.Arena.BlockChange.CachedBlock;
import dev.nandi0813.practice.Manager.Arena.BlockChange.Rollback;
import dev.nandi0813.practice.Manager.File.LanguageManager;
import dev.nandi0813.practice.Manager.Ladder.Ladder;
import dev.nandi0813.practice.Manager.Match.Enum.MatchStatus;
import dev.nandi0813.practice.Manager.Match.Enum.TeamEnum;
import dev.nandi0813.practice.Manager.Match.MatchStats.PlayerMatchStat;
import dev.nandi0813.practice.Manager.Match.MatchType.Duel.Duel;
import dev.nandi0813.practice.Manager.Match.Enum.MatchType;
import dev.nandi0813.practice.Manager.Match.MatchType.PartyFFA.PartyFFA;
import dev.nandi0813.practice.Manager.Match.MatchType.PartySplit.PartySplit;
import dev.nandi0813.practice.Manager.Match.Runnable.AfterCountdown;
import dev.nandi0813.practice.Manager.Match.Runnable.DurationCountdown;
import dev.nandi0813.practice.Manager.Match.Runnable.StartCountdown;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Manager.Profile.ProfileStatus;
import dev.nandi0813.practice.Practice;
import dev.nandi0813.practice.Util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.*;

public class Match {

    @Getter
    private final String matchID;
    @Getter
    private final RoundManager roundManager;
    @Getter
    private final MatchType type;
    @Getter
    @Setter
    private MatchStatus status;
    @Getter
    private final Ladder ladder;
    @Getter
    private final boolean ranked;

    @Getter
    private final Arena arena;
    @Getter
    @Setter
    private Arena gameArena;

    @Getter
    private final List<Player> players;
    @Getter
    private final List<Player> spectators = new ArrayList<>();
    @Getter
    private final HashMap<Player, TeamEnum> teams = new HashMap<>();
    // For team matches
    @Getter
    private final List<Player> alivePlayers = new ArrayList<>();

    @Getter
    private final HashSet<CachedBlock> blockChange;
    @Getter
    @Setter
    private HashSet<Item> droppedItems = new HashSet<>();
    @Getter
    private final HashMap<OfflinePlayer, PlayerMatchStat> matchStats = new HashMap<>();

    @Getter
    @Setter
    private StartCountdown startCountdown;
    @Getter
    @Setter
    private DurationCountdown durationCountdown;
    @Getter
    @Setter
    private AfterCountdown afterCountdown;

    public Match(MatchType matchType, List<Player> players, Ladder ladder, boolean ranked, Arena arena) {
        matchID = "match" + System.currentTimeMillis();
        this.type = matchType;
        this.players = players;
        this.ladder = ladder;
        this.ranked = ranked;
        this.arena = arena;
        blockChange = new HashSet<>();
        roundManager = new RoundManager(this);
        startCountdown = new StartCountdown(this);
        durationCountdown = new DurationCountdown();
        afterCountdown = new AfterCountdown(this);
    }

    public void startMatch() {
        MatchStartEvent matchStartEvent = new MatchStartEvent(this);
        Bukkit.getPluginManager().callEvent(matchStartEvent);

        if (matchStartEvent.isCancelled()) {
            sendMessage("&cThe match got cancelled!", false);
            return;
        }

        if (!type.equals(MatchType.PARTY_FFA)) {
            roundManager.getWonRoundsTeam().put(TeamEnum.TEAM1, 0);
            roundManager.getWonRoundsTeam().put(TeamEnum.TEAM2, 0);
        } else {
            for (Player player : players)
                roundManager.getWonRoundsPlayer().put(player, 0);
        }

        gameArena = arena;
        if (ladder.isBuild()) gameArena.setAvailable(false);

        // Load chunk
        for (Chunk chunk : gameArena.getCuboid().getChunks())
            chunk.load();

        for (Player player : players) {
            if (type.equals(MatchType.DUEL))
                matchStats.put(player, new PlayerMatchStat(player));
            Profile profile = Practice.getProfileManager().getProfiles().get(player);
            profile.setStatus(ProfileStatus.MATCH);
            profile.setPreviousLocation(player.getLocation());
        }

        switch (type) {
            case DUEL:
                Duel.startMatch(this);
                break;
            case PARTY_FFA:
                PartyFFA.startMatch(this);
                break;
            case PARTY_SPLIT:
                PartySplit.startMatch(this);
                break;
        }

        roundManager.startRound();
    }


    public void endMatch() {
        // Remove match players
        for (Player player : players)
            removePlayer(player, false);

        resetMap();

        status = MatchStatus.OLD;

        // Remove spectators
        for (Player spectator : spectators)
            removePlayer(spectator, false);
        spectators.clear();

        String arenaWorldName = Practice.getInstance().getConfig().getString("arena-world-name", "arenas");
        boolean shouldUnload = true;
        for (Player player : Bukkit.getWorld(arenaWorldName).getPlayers()) {
            Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

            if (match != null && match.getArena().getName().equals(gameArena.getName())) {
                shouldUnload = false;
            }
        }

        if (shouldUnload) {
            for (Chunk chunk : gameArena.getCuboid().getChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        player.kickPlayer("You are not in an active arena. Please join a valid game area.");
                    }
                }

                if (shouldUnload) {
                    chunk.unload();
                }
            }
        }

        Bukkit.getPluginManager().callEvent(new MatchEndEvent(this));
    }

    /**
     * If the block change is not already in the list, add it
     *
     * @param change The block that was changed.
     */
    public void addBlockChange(CachedBlock change) {
        for (CachedBlock c : blockChange) {
            if (c.getLocation().getX() == change.getLocation().getX() && c.getLocation().getY() == change.getLocation().getY() && c.getLocation().getZ() == change.getLocation().getZ())
                return;
        }
        blockChange.add(change);
    }

    /**
     * It removes all dropped items, rolls back the arena, and removes all items in the arena
     */
    public void resetMap() {
        for (Item is : droppedItems)
            is.remove();

        if (ladder.isBuild()) {
            Rollback.rollBackArena(this);

            for (Chunk chunk : gameArena.getCuboid().getChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity instanceof Item) entity.remove();
                }
            }
        }
    }


    /**
     * It sends a message to all players in the match, and if the spectator boolean is true, it sends the message to all
     * spectators
     *
     * @param message   The message you want to send to the players.
     * @param spectator If true, the message will be sent to the spectators.
     */
    public void sendMessage(String message, boolean spectator) {
        for (Player player : players) {
            Profile profile = Practice.getProfileManager().getProfiles().get(player);

            if (profile.getStatus().equals(ProfileStatus.MATCH) && Practice.getMatchManager().getLiveMatchByPlayer(player).equals(this))
                player.sendMessage(StringUtil.CC(message));
        }
        if (spectator) {
            for (Player specPlayer : spectators)
                specPlayer.sendMessage(StringUtil.CC(message));
        }
    }

    /**
     * Add a player to the list of spectators, hide them from the players in the match, teleport them to the spectator
     * spawn, and give them the spectator inventory
     *
     * @param player The player to add to the spectators list
     */
    public void addSpectator(Player player) {
        spectators.add(player);

        Practice.getInventoryManager().getSpectatorInventory().setSpectatorInventory(player);
        player.teleport(gameArena.getPosition3());

        if (!player.hasPermission("zonepractice.spectate.silent"))
            sendMessage(LanguageManager.getString("match.spectator-join").replaceAll("%player%", player.getName()), true);
    }

    /**
     * If the player is in the match or spectating the match, remove them from the match
     *
     * @param player The player to remove from the match.
     */
    public void removePlayer(Player player, boolean removeSpectator) {
        Profile profile = Practice.getProfileManager().getProfiles().get(player);

        if ((profile.getStatus().equals(ProfileStatus.MATCH) && Practice.getMatchManager().getLiveMatchByPlayer(player).equals(this))
                || (profile.getStatus().equals(ProfileStatus.SPECTATE) && Practice.getMatchManager().getLiveMatchBySpectator(player).equals(this))) {
            if (!status.equals(MatchStatus.OLD) && profile.getStatus().equals(ProfileStatus.SPECTATE) && !player.hasPermission("zonepractice.spectate.silent"))
                sendMessage(LanguageManager.getString("match.spectator-leave").replaceAll("%player%", player.getName()), true);

            if (removeSpectator)
                spectators.remove(player);

            Practice.getInventoryManager().getSpawnInventory().setInventory(player, true);
        }
    }
}
