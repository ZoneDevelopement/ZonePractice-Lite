package dev.nandi0813.practice.Manager.Profile;

import dev.nandi0813.practice.Manager.Ladder.Ladder;
import dev.nandi0813.practice.Practice;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Profile {

    @Getter
    private final UUID uuid;
    @Getter
    private final OfflinePlayer player;
    @Getter
    private final Map<Ladder, ItemStack[]> customKits = new ConcurrentHashMap<>();
    @Getter
    private final ProfileFile file;

    @Getter
    @Setter
    private Location previousLocation;

    // Temporary Settings
    @Getter
    @Setter
    private ProfileStatus status;
    @Getter
    @Setter
    private boolean party;

    // Statistics
    @Getter
    @Setter
    private int unrankedWins;
    @Getter
    @Setter
    private int unrankedLosses;
    @Getter
    @Setter
    private int rankedWins;
    @Getter
    @Setter
    private int rankedLosses;
    @Getter
    private final Map<Ladder, Integer> ladderUnRankedWins = new ConcurrentHashMap<>();
    @Getter
    private final Map<Ladder, Integer> ladderUnRankedLosses = new ConcurrentHashMap<>();
    @Getter
    private final Map<Ladder, Integer> ladderRankedWins = new ConcurrentHashMap<>();
    @Getter
    private final Map<Ladder, Integer> ladderRankedLosses = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private Map<Ladder, Integer> elo = new ConcurrentHashMap<>();

    public Profile(UUID uuid) {
        this.uuid = uuid;
        player = Bukkit.getOfflinePlayer(uuid);
        status = ProfileStatus.OFFLINE;

        file = new ProfileFile(this);
    }

    /**
     * If the plugin is enabled, run the setProfileData function asynchronously, otherwise run it synchronously.
     */
    public void saveData() {
        if (Practice.getInstance().isEnabled())
            Bukkit.getScheduler().runTaskAsynchronously(Practice.getInstance(), file::setProfileData);
        else
            file.setProfileData();
    }

    /**
     * Get the data from the file, and run it asynchronously.
     */
    public void getData() {
        file.getProfileData();
    }
}
