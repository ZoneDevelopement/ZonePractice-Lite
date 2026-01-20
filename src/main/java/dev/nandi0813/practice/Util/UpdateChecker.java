package dev.nandi0813.practice.Util;

import dev.nandi0813.practice.Manager.File.ConfigManager;
import dev.nandi0813.practice.Practice;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class UpdateChecker {

    private final Practice plugin;
    private final String githubRepo;
    private final String branch;

    public UpdateChecker(Practice plugin, String githubRepo, String branch) {
        this.plugin = plugin;
        this.githubRepo = githubRepo;
        this.branch = branch;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/" + this.githubRepo + "/" + this.branch + "/pom.xml");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.startsWith("<version>") && line.endsWith("</version>")) {
                                String version = line.substring(9, line.length() - 10); // Strip tags
                                consumer.accept(version);
                                return;
                            }
                        }
                    }
                } else {
                    plugin.getLogger().warning("[UpdateChecker] Failed to fetch pom.xml (HTTP " + connection.getResponseCode() + ")");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[UpdateChecker] Error checking update: " + e.getMessage());
            }
        });
    }

    public static void check(Practice practice) {
        practice.getLogger().info("[UpdateChecker] Checking for updates...");

        if (ConfigManager.getBoolean("notify-updates")) {
            new UpdateChecker(
                    practice,
                    "ZoneDevelopement/ZonePractice-Lite", // Repo
                    "master"                     // Branch
            ).getVersion(remoteVersion -> {

                String currentVersion = practice.getDescription().getVersion();

                if (!currentVersion.equalsIgnoreCase(remoteVersion)) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&aThere is a new update available for &6ZonePractice Lite&a.\n" +
                                    "&aCurrent: &c" + currentVersion + " &aLatest: &c" + remoteVersion + "\n" +
                                    "&aDownload: https://github.com/ZoneDevelopement/ZonePractice-Lite"));
                } else {
                    practice.getLogger().info("[UpdateChecker] No update found. You are running the latest version (" + currentVersion + ").");
                }
            });
        } else {
            practice.getLogger().info("[UpdateChecker] Disabled in config.");
        }
    }
}
