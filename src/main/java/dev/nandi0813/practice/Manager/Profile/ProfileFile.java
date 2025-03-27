package dev.nandi0813.practice.Manager.Profile;

import dev.nandi0813.practice.Manager.File.ConfigManager;
import dev.nandi0813.practice.Manager.Ladder.Ladder;
import dev.nandi0813.practice.Practice;
import dev.nandi0813.practice.Util.ItemSerializationUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ProfileFile {

    private final Profile profile;
    private final File file;
    private final YamlConfiguration config;

    public ProfileFile(Profile profile) {
        this.profile = profile;
        file = new File(Practice.getInstance().getDataFolder() + "/profiles", profile.getUuid().toString().toLowerCase() + ".yml");
        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * It saves the player's profile data to the file
     */
    public void setProfileData() {
        config.set("uuid", profile.getUuid().toString());

        // Elo
        for (Ladder ladder : Practice.getLadderManager().getLadders()) {
            if (ladder.isRanked()) {
                Integer eloValue = profile.getElo().get(ladder);
                if (eloValue == null) {
                    eloValue = ConfigManager.getInt("ranked.default-elo"); 
                }
                config.set("stats.elo." + ladder.getName(), eloValue);
            }
        }

        // Summarized wins/losses
        config.set("stats.unranked.wins", profile.getUnrankedWins());
        config.set("stats.unranked.losses", profile.getUnrankedLosses());
        config.set("stats.ranked.wins", profile.getRankedWins());
        config.set("stats.ranked.losses", profile.getRankedLosses());

        // Ladder win/lose stats
        for (Ladder ladder : Practice.getLadderManager().getLadders()) {
            if (!ladder.isEnabled()) continue;

            if (profile.getLadderUnRankedWins().get(ladder) != null)
                config.set("stats.ladder-stats." + ladder.getName() + ".unranked.wins", profile.getLadderUnRankedWins().get(ladder));
            else
                config.set("stats.ladder-stats." + ladder.getName() + ".unranked.wins", 0);

            if (profile.getLadderUnRankedLosses().get(ladder) != null)
                config.set("stats.ladder-stats." + ladder.getName() + ".unranked.losses", profile.getLadderUnRankedLosses().get(ladder));
            else
                config.set("stats.ladder-stats." + ladder.getName() + ".unranked.losses", 0);

            if (ladder.isRanked()) {
                if (profile.getLadderRankedWins().get(ladder) != null)
                    config.set("stats.ladder-stats." + ladder.getName() + ".ranked.wins", profile.getLadderRankedWins().get(ladder));
                else
                    config.set("stats.ladder-stats." + ladder.getName() + ".ranked.wins", 0);

                if (profile.getLadderRankedLosses().get(ladder) != null)
                    config.set("stats.ladder-stats." + ladder.getName() + ".ranked.losses", profile.getLadderRankedLosses().get(ladder));
                else
                    config.set("stats.ladder-stats." + ladder.getName() + ".ranked.losses", 0);
            }
        }

        // Custom kits
        for (Ladder ladder : Practice.getLadderManager().getLadders()) {
            if (!ladder.isEnabled()) continue;

            if (profile.getCustomKits().containsKey(ladder) && profile.getCustomKits().get(ladder) != null)
                config.set("customkit.ladder" + ladder.getId() + ".inventory", ItemSerializationUtil.itemStackArrayToBase64(profile.getCustomKits().get(ladder)));
        }

        saveFile();
    }

    /**
     * It sets the default data for a player's stats
     */
    public void setDefaultData() {
        for (Ladder ladder : Practice.getLadderManager().getLadders()) {
            if (!ladder.isEnabled()) continue;

            if (ladder.isRanked())
                config.set("stats.elo." + ladder.getName(), ConfigManager.getInt("ranked.default-elo"));
        }

        config.set("stats.unranked.wins", 0);
        config.set("stats.unranked.losses", 0);
        config.set("stats.ranked.wins", 0);
        config.set("stats.ranked.losses", 0);

        for (Ladder ladder : Practice.getLadderManager().getLadders()) {
            if (!ladder.isEnabled()) continue;

            config.set("stats.ladder-stats." + ladder.getName() + ".unranked.wins", 0);
            config.set("stats.ladder-stats." + ladder.getName() + ".unranked.losses", 0);

            if (ladder.isRanked()) {
                config.set("stats.ladder-stats." + ladder.getName() + ".ranked.wins", 0);
                config.set("stats.ladder-stats." + ladder.getName() + ".ranked.losses", 0);
            }
        }

        saveFile();
    }

    /**
     * It loads the player's profile data from the config file
     */
    public void getProfileData() {
        ConfigurationSection eloSection = config.getConfigurationSection("elo");

        if (eloSection != null) {
            for (String ladderName : config.getConfigurationSection("stats.elo").getKeys(false)) {
                Ladder ladder = Practice.getLadderManager().getLadder(ladderName);
                if (ladder != null && ladder.isRanked()) {
                    int eloValue = config.getInt("stats.elo." + ladder.getName(), ConfigManager.getInt("ranked.default-elo"));
                    profile.getElo().put(ladder, eloValue);
                }
            }
        }

        profile.setUnrankedWins(config.getInt("stats.unranked.wins"));
        profile.setUnrankedLosses(config.getInt("stats.unranked.losses"));
        profile.setRankedWins(config.getInt("stats.ranked.wins"));
        profile.setRankedLosses(config.getInt("stats.ranked.losses"));

        for (String ladderName : config.getConfigurationSection("stats.ladder-stats").getKeys(false)) {
            Ladder ladder = Practice.getLadderManager().getLadder(ladderName);
            if (ladder != null) {
                profile.getLadderUnRankedWins().put(ladder, config.getInt("stats.ladder-stats." + ladder.getName() + ".unranked.wins"));
                profile.getLadderUnRankedLosses().put(ladder, config.getInt("stats.ladder-stats." + ladder.getName() + ".unranked.losses"));

                if (ladder.isRanked()) {
                    if (config.isSet("stats.ladder-stats." + ladder.getName() + ".ranked.wins"))
                        profile.getLadderRankedWins().put(ladder, config.getInt("stats.ladder-stats." + ladder.getName() + ".ranked.wins"));
                    if (config.isSet("stats.ladder-stats." + ladder.getName() + ".ranked.losses"))
                        profile.getLadderRankedLosses().put(ladder, config.getInt("stats.ladder-stats." + ladder.getName() + ".ranked.losses"));
                }
            }
        }

        for (Ladder ladder : Practice.getLadderManager().getLadders()) {
            if (config.isString("customkit.ladder" + ladder.getId() + ".inventory")) {
                try {
                    profile.getCustomKits().put(ladder, ItemSerializationUtil.itemStackArrayFromBase64(config.getString("customkit.ladder" + ladder.getId() + ".inventory")));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * It deletes a custom kit from the config
     *
     * @param ladder The ladder you want to delete the custom kit from.
     */
    public void deleteCustomKit(Ladder ladder) {
        config.set("customkit." + ladder.getName().toLowerCase(), null);
        saveFile();
    }

    public void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}