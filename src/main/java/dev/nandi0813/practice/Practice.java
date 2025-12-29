package dev.nandi0813.practice;

import com.comphenix.protocol.ProtocolLibrary;
import dev.nandi0813.practice.Command.Arena.ArenaCommand;
import dev.nandi0813.practice.Command.Arena.ArenaTabCompleter;
import dev.nandi0813.practice.Command.Ladder.LadderCommand;
import dev.nandi0813.practice.Command.Ladder.LadderTabCompleter;
import dev.nandi0813.practice.Command.Leaderboard.LeaderboardCommand;
import dev.nandi0813.practice.Command.Leaderboard.LeaderboardTabCompleter;
import dev.nandi0813.practice.Command.Matchinv.MatchinvCommand;
import dev.nandi0813.practice.Command.Party.PartyCommand;
import dev.nandi0813.practice.Command.Party.PartyTabCompleter;
import dev.nandi0813.practice.Command.Practice.PracticeCommand;
import dev.nandi0813.practice.Command.Practice.PracticeTabCompleter;
import dev.nandi0813.practice.Command.Spectate.SpectateCommand;
import dev.nandi0813.practice.Command.Spectate.SpectateTabCompleter;
import dev.nandi0813.practice.Command.Stats.StatsCommand;
import dev.nandi0813.practice.Listener.*;
import dev.nandi0813.practice.Manager.Arena.ArenaManager;
import dev.nandi0813.practice.Manager.File.BackendManager;
import dev.nandi0813.practice.Manager.File.ConfigManager;
import dev.nandi0813.practice.Manager.File.LadderFile;
import dev.nandi0813.practice.Manager.File.LanguageManager;
import dev.nandi0813.practice.Manager.Gui.GUIManager;
import dev.nandi0813.practice.Manager.Inventory.InventoryManager;
import dev.nandi0813.practice.Manager.Ladder.LadderManager;
import dev.nandi0813.practice.Manager.Match.MatchManager;
import dev.nandi0813.practice.Manager.Party.PartyManager;
import dev.nandi0813.practice.Manager.Profile.ProfileManager;
import dev.nandi0813.practice.Manager.Queue.QueueManager;
import dev.nandi0813.practice.Manager.Sidebar.SidebarManager;
import dev.nandi0813.practice.Util.Enderpearl.EnderpearlListener;
import dev.nandi0813.practice.Util.EntityHider.EntityHider;
import dev.nandi0813.practice.Util.EntityHider.EntityHiderListener;
import dev.nandi0813.practice.Util.PAPIExpansion;
import dev.nandi0813.practice.Util.PlayerHider;
import dev.nandi0813.practice.Util.UpdateChecker;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Practice extends JavaPlugin {

    @Getter
    private static Practice instance;

    @Getter
    private static ArenaManager arenaManager;
    @Getter
    private static LadderManager ladderManager;
    @Getter
    private static ProfileManager profileManager;
    @Getter
    private static GUIManager guiManager;
    @Getter
    private static MatchManager matchManager;
    @Getter
    private static QueueManager queueManager;
    @Getter
    private static PartyManager partyManager;
    @Getter
    private static InventoryManager inventoryManager;
    @Getter
    private static SidebarManager sidebarManager;
    @Getter
    private static EntityHider entityHider;

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager.createConfig(this);
        LanguageManager.createFile(this);
        BackendManager.createFile(this);
        LadderFile.createFile(this);

        loadManagers();

        ladderManager.loadLadders();
        arenaManager.loadArenas();
        profileManager.loadProfiles();
        sidebarManager.enable();
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, guiManager::buildGUIs, 20L * 2);

        registerCommands();
        registerListeners(this, Bukkit.getPluginManager());
        registerPacketListener();

        matchManager.startRankedTimer();
        UpdateChecker.check(this);

        // Register PAPI expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) new PAPIExpansion().register();

        // BStats
        new Metrics(this, 16054);
    }

    public void loadManagers() {
        arenaManager = new ArenaManager();
        ladderManager = new LadderManager();
        profileManager = new ProfileManager();
        guiManager = new GUIManager();
        matchManager = new MatchManager(Practice.getInstance());
        queueManager = new QueueManager(Practice.getInstance());
        partyManager = new PartyManager();
        inventoryManager = new InventoryManager();
        sidebarManager = new SidebarManager(Practice.getInstance());
        entityHider = new EntityHider(Practice.getInstance(), EntityHider.Policy.BLACKLIST);
    }

    public static void registerCommands() {
        PluginCommand arenaCommand = Bukkit.getServer().getPluginCommand("arena");
        CommandExecutor arenaExecutor = new ArenaCommand();
        TabCompleter arenaTabCompleter = new ArenaTabCompleter();
        arenaCommand.setExecutor(arenaExecutor);
        arenaCommand.setTabCompleter(arenaTabCompleter);

        PluginCommand ladderCommand = Bukkit.getServer().getPluginCommand("ladder");
        CommandExecutor ladderExecutor = new LadderCommand();
        TabCompleter ladderTabCompleter = new LadderTabCompleter();
        ladderCommand.setExecutor(ladderExecutor);
        ladderCommand.setTabCompleter(ladderTabCompleter);

        PluginCommand practiceCommand = Bukkit.getServer().getPluginCommand("practice");
        CommandExecutor practiceExecutor = new PracticeCommand();
        TabCompleter practiceTabCompleter = new PracticeTabCompleter();
        practiceCommand.setExecutor(practiceExecutor);
        practiceCommand.setTabCompleter(practiceTabCompleter);

        PluginCommand spectateCommand = Bukkit.getServer().getPluginCommand("spectate");
        CommandExecutor spectateExecutor = new SpectateCommand();
        TabCompleter spectateTabCompleter = new SpectateTabCompleter();
        spectateCommand.setExecutor(spectateExecutor);
        spectateCommand.setTabCompleter(spectateTabCompleter);

        PluginCommand partyCommand = Bukkit.getServer().getPluginCommand("party");
        CommandExecutor partyExecutor = new PartyCommand();
        TabCompleter partyTabCompleter = new PartyTabCompleter();
        partyCommand.setExecutor(partyExecutor);
        partyCommand.setTabCompleter(partyTabCompleter);

        PluginCommand leaderboardCommand = Bukkit.getServer().getPluginCommand("leaderboard");
        CommandExecutor leaderboardExecutor = new LeaderboardCommand();
        TabCompleter leaderboardTabCompleter = new LeaderboardTabCompleter();
        leaderboardCommand.setExecutor(leaderboardExecutor);
        leaderboardCommand.setTabCompleter(leaderboardTabCompleter);

        PluginCommand matchinvCommand = Bukkit.getServer().getPluginCommand("matchinv");
        CommandExecutor matchinvExecutor = new MatchinvCommand();
        matchinvCommand.setExecutor(matchinvExecutor);

        PluginCommand statisticsCommand = Bukkit.getServer().getPluginCommand("statistics");
        CommandExecutor statsExecutor = new StatsCommand();
        statisticsCommand.setExecutor(statsExecutor);
    }

    public static void registerListeners(Practice practice, PluginManager pm) {
        pm.registerEvents(new PlayerJoin(), practice);
        pm.registerEvents(new PlayerQuit(), practice);
        pm.registerEvents(new PlayerInteract(), practice);
        pm.registerEvents(new EnderpearlListener(), practice);
        pm.registerEvents(new WeatherChange(), practice);
        pm.registerEvents(new ItemConsume(), practice);
        pm.registerEvents(new ProjectileLaunch(), practice);
        pm.registerEvents(new EntityHiderListener(), practice);
        pm.registerEvents(new PlayerCommandPreprocess(), practice);
        pm.registerEvents(new PlayerHider(), practice);
        pm.registerEvents(new MultiGameListener(), practice);
    }

    public static void registerPacketListener() {
        try {
            ProtocolLibrary.getProtocolManager().addPacketListener(new EntityHiderListener());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        ladderManager.saveLadders();
        arenaManager.saveArenas();
        profileManager.saveProfiles();
        matchManager.disableLiveMatches();
        sidebarManager.disable();
        entityHider.close();
    }

}

