package dev.nandi0813.practice.Manager.Ladder;

import dev.nandi0813.practice.Manager.File.LadderFile;
import dev.nandi0813.practice.Practice;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LadderManager {

    private final List<Ladder> ladders = new ArrayList<>();
    private final List<Ladder> unrankedLadders = new ArrayList<>();
    private final List<Ladder> rankedLadders = new ArrayList<>();

    /**
     * Return the ladder with the given name, or null if no ladder with that name exists.
     *
     * @param ladderName The name of the ladder you want to get.
     * @return A ladder object
     */
    public Ladder getLadder(String ladderName) {
        for (Ladder ladder : ladders)
            if (ladder.getName() != null && ladder.getName().equalsIgnoreCase(ladderName))
                return ladder;
        return null;
    }

    public Ladder getLadder(int id) {
        for (Ladder ladder : ladders)
            if (ladder.getId() == id)
                return ladder;
        return null;
    }

    /**
     * Loads all the ladders into the ladders list
     */
    public void loadLadders() {
        {
            ladders.clear();
            rankedLadders.clear();
            unrankedLadders.clear();

            FileConfiguration config = LadderFile.getConfig();

            for (String ladderName : config.getConfigurationSection("ladders").getKeys(false)) {
                Ladder ladder = new Ladder(Integer.parseInt(ladderName.replace("ladder", "")));
                ladders.add(ladder);

                if (ladder.isEnabled() && ladder.getIcon() != null) {
                    unrankedLadders.add(ladder);

                    if (ladder.isRanked()) {
                        rankedLadders.add(ladder);
                    }
                }
            }
        }
    }

    public void saveLadders() {
        for (Ladder ladder : ladders)
            ladder.saveData(false);

        LadderFile.save();
    }

}
