import events.explode;
import items.mining_tnt;
import items.reverse_tnt;
import items.empty_item;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import thirtyvirus.uber.UberItems;
import thirtyvirus.uber.helpers.AbilityType;
import thirtyvirus.uber.helpers.UberAbility;
import thirtyvirus.uber.helpers.UberCraftingRecipe;
import thirtyvirus.uber.helpers.UberRarity;

import java.util.Arrays;
import java.util.Collections;

public class SmartTNT extends JavaPlugin {

    public void onEnable() {

        // enforce UberItems dependancy
        if (Bukkit.getPluginManager().getPlugin("UberItems") == null) {
            this.getLogger().severe("SmartTNT requires UberItems! disabled because UberItems dependency not found");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // register events and UberItems
        registerEvents();
        registerUberMaterials();
        registerUberItems();

        // post confirmation in chat
        getLogger().info(getDescription().getName() + " V: " + getDescription().getVersion() + " has been enabled");
    }
    public void onDisable() {
        // posts exit message in chat
        getLogger().info(getDescription().getName() + " V: " + getDescription().getVersion() + " has been disabled");
    }
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new explode(), this);
    }

    // NEW UBER ITEM CHECKLIST

    // - make a new class file, named with all lowercase lettering and underscores for spaces
    // - copy the UberItemTemplate class contents into the new class, extend UberItem
    // - make a putItem entry, follow the format of previous items and make sure to give a unique id
    // - write the unique item ability code in the appropriate method

    // - add the following line of code just after executing the item's ability:
    //      onItemUse(player, item); // confirm that the item's ability has been successfully used

    // - if the ability needs a cooldown, prefix it's code with a variation of the following line of code:
    //      if (!Utilities.enforceCooldown(getMain(), player, "name", 1, item, true)) return;

    // - if the item needs work done on create (like adding enchantments, adding other data) refer to onItemStackCreate
    // - if the item needs a prefix or suffix in its description,
    //   refer to the getSpecificLorePrefix and getSpecificLoreSuffix functions, then add the following:
    //      lore.add(ChatColor.RESET + "text goes here");

    // - if you need to store & retrieve ints and strings from items, you can use the following functions:
    //      Utilities.storeIntInItem(getMain(), item, 1, "number tag");
    //      if (Utilities.getIntFromItem(getMain(), item, "number tag") == 1) // { blah blah blah }
    //      (the same case for strings, just storeStringInItem and getStringFromItem)

    private void registerUberItems() {
        UberItems.putItem("empty_item", new empty_item(Material.DIAMOND, "Empty UberItem", UberRarity.COMMON,
                false, false, false, Collections.emptyList(), null));

        UberItems.putItem("mining_tnt", new mining_tnt(Material.TNT, "Mining TNT",
                UberRarity.RARE, true, true, false,
                Arrays.asList(
                        new UberAbility("Certified Specialist", AbilityType.RIGHT_CLICK, "Place TNT in the exact spot you're looking " + ChatColor.DARK_GRAY + ChatColor.ITALIC + "15 Block Radius"),
                        new UberAbility("Precise Blast", AbilityType.NONE, "Breaks blocks cleanly in a radius /newline " + ChatColor.DARK_GRAY + ChatColor.ITALIC + "Guaranteed Drops, Immune to Water!")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.AIR),
                        new ItemStack(Material.GRAVEL, 8),
                        new ItemStack(Material.AIR),
                        new ItemStack(Material.FLINT, 4),
                        new ItemStack(Material.TNT, 2),
                        new ItemStack(Material.FLINT, 4),
                        new ItemStack(Material.AIR),
                        new ItemStack(Material.GRAVEL, 8),
                        new ItemStack(Material.AIR, 4)), false, 1)));
        UberItems.putItem("reverse_tnt", new reverse_tnt(Material.TNT, "Reverse TNT",
                UberRarity.EPIC, true, true, false,
                Arrays.asList(
                        new UberAbility("Backup", AbilityType.RIGHT_CLICK, "Restores blocks broken /newline in the past 15 minutes")),
                new UberCraftingRecipe(Arrays.asList(
                        new ItemStack(Material.REDSTONE, 4),
                        new ItemStack(Material.CHEST),
                        new ItemStack(Material.REDSTONE, 4),
                        new ItemStack(Material.ENDER_PEARL, 16),
                        new ItemStack(Material.TNT),
                        new ItemStack(Material.ENDER_PEARL, 16),
                        new ItemStack(Material.REDSTONE, 4),
                        new ItemStack(Material.CLOCK),
                        new ItemStack(Material.REDSTONE, 4)), false, 1)));

    }
    private void registerUberMaterials() {

    }

}