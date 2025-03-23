package dev.nandi0813.practice.Manager.Ladder;

import dev.nandi0813.practice.Manager.File.LadderFile;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Ladder {

    private final int id;
    private String name = null;
    private boolean enabled = false;
    private ItemStack icon = null;

    private ItemStack[] armor = null;
    private ItemStack[] inventory = null;
    private List<PotionEffect> effects = new ArrayList<>();

    private KnockbackType knockbackType = KnockbackType.DEFAULT;
    private int hitDelay = 20;
    private boolean ranked = false;
    private boolean editable = true;
    private boolean regen = true;
    private boolean hunger = true;
    private boolean build = false;

    public Ladder(int id) {
        this.id = id;
        getData();
    }

    public void getData() {
        FileConfiguration config = LadderFile.getConfig();
        String path = "ladders.ladder" + id;

        if (!config.isConfigurationSection(path))
            return;

        String namePath = path + ".name";
        if (config.isSet(namePath) && config.isString(namePath))
            name = config.getString(namePath);

        String enabledPath = path + ".enabled";
        if (config.isSet(enabledPath) && config.isBoolean(enabledPath))
            enabled = config.getBoolean(enabledPath);

        String iconPath = path + ".icon";
        if (config.isSet(iconPath) && config.isItemStack(iconPath))
            icon = config.getItemStack(iconPath);

        String armorPath = path + ".armor";
        if (config.isSet(armorPath) && config.isList(armorPath)) {
            List<ItemStack> armorList = new ArrayList<>();
            for (Object obj : config.getList(armorPath)) {
                if (obj instanceof ItemStack) {
                    armorList.add((ItemStack) obj);
                }
            }
            armor = armorList.toArray(new ItemStack[0]);
        }

        if (config.isList(path + ".inventory")) {
            List<ItemStack> inventoryList = new ArrayList<>();
            for (Object obj : config.getList(path + ".inventory")) {
                if (obj instanceof ItemStack) {
                    inventoryList.add((ItemStack) obj);
                } else {
                    System.err.println("[Ladder] Invalid inventory item for ladder " + id + ": " + obj);
                }
            }
            inventory = inventoryList.toArray(new ItemStack[0]);
        }

        String effectPath = path + ".effects";
        if (config.isSet(effectPath) && config.isList(effectPath))
            effects = (List<PotionEffect>) config.getList(effectPath);

        String knockbackPath = path + ".knockback";
        if (config.isSet(knockbackPath) && config.isString(knockbackPath))
            knockbackType = KnockbackType.valueOf(config.getString(knockbackPath));

        String hitdelayPath = path + ".hitdelay";
        if (config.isSet(hitdelayPath) && config.isInt(hitdelayPath))
            hitDelay = config.getInt(hitdelayPath);

        String rankedPath = path + ".ranked";
        if (config.isSet(rankedPath) && config.isBoolean(rankedPath))
            ranked = config.getBoolean(rankedPath);

        String editablePath = path + ".editable";
        if (config.isSet(editablePath) && config.isBoolean(editablePath))
            editable = config.getBoolean(editablePath);

        String regenPath = path + ".regen";
        if (config.isSet(regenPath) && config.isBoolean(regenPath))
            regen = config.getBoolean(regenPath);

        String hungerPath = path + ".hunger";
        if (config.isSet(hungerPath) && config.isBoolean(hungerPath))
            hunger = config.getBoolean(hungerPath);

        String buildPath = path + ".build";
        if (config.isSet(buildPath) && config.isBoolean(buildPath))
            build = config.getBoolean(buildPath);

        if (enabled && !isReadyToEnable())
            enabled = false;
    }

    public boolean isReadyToEnable() {
        return name != null && icon != null && armor != null && inventory != null && knockbackType != null;
    }

    public void saveData(boolean saveFile) {
        FileConfiguration config = LadderFile.getConfig();
        String path = "ladders.ladder" + id;

        if (name != null) {
            String namePath = path + ".name";
            config.set(namePath, name);
        }

        String enabledPath = path + ".enabled";
        config.set(enabledPath, enabled);

        if (icon != null) {
            String iconPath = path + ".icon";
            config.set(iconPath, icon);
        }

        if (armor != null) {
            String armorPath = path + ".armor";
            List<ItemStack> armorList = new ArrayList<>();
            for (ItemStack item : armor) {
                armorList.add(item);
            }
            config.set(armorPath, armorList);
        }

        if (inventory != null) {
            String inventoryPath = path + ".inventory";
            List<ItemStack> inventoryList = new ArrayList<>();
            for (ItemStack item : inventory) {
                if (item == null) {
                    item = new ItemStack(Material.AIR);
                }

                inventoryList.add(item);
            }
            config.set(inventoryPath, inventoryList);
        }

        String effectPath = path + ".effects";
        if (effects != null && !effects.isEmpty()) {
            config.set(effectPath, effects);
        } else {
            config.set(effectPath, null);
        }

        if (knockbackType != null) {
            String knockbackPath = path + ".knockback";
            config.set(knockbackPath, knockbackType.name().toUpperCase());
        }

        String hitdelayPath = path + ".hitdelay";
        config.set(hitdelayPath, hitDelay);

        String rankedPath = path + ".ranked";
        config.set(rankedPath, ranked);

        String editablePath = path + ".editable";
        config.set(editablePath, editable);

        String regenPath = path + ".regen";
        config.set(regenPath, regen);

        String hungerPath = path + ".hunger";
        config.set(hungerPath, hunger);

        String buildPath = path + ".build";
        config.set(buildPath, build);

        if (saveFile)
            LadderFile.save();
    }
}