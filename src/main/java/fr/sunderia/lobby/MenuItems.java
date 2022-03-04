package fr.sunderia.lobby;

import fr.sunderia.lobby.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Optional;

public enum MenuItems {

    LOBBY(new ItemBuilder(Material.QUARTZ_BLOCK).setDisplayName(ChatColor.GRAY + "Lobby"), "lobby"),
    SKYBLOCK(new ItemBuilder(Material.GRASS_BLOCK).setDisplayName(ChatColor.GREEN + "SkyBlock"), null),
    ROLEPLAY(new ItemBuilder(Material.REDSTONE_BLOCK).setDisplayName(ChatColor.GREEN + "RolePlay"), null),
    SHEEPBREAK(new ItemBuilder(Material.WHITE_WOOL).setDisplayName(ChatColor.GREEN + "SheepBreak"), null),
    RUSSIA_VS_UKRAINE(new ItemBuilder(Material.DIAMOND_SWORD).setDisplayName(ChatColor.RED + "Russia Vs Ukraine " + ChatColor.GRAY + "[Coming Soon]"), null),
    DEV(new ItemBuilder(Material.COMMAND_BLOCK).setDisplayName(ChatColor.LIGHT_PURPLE + "Dev"), "dev"),
    ;

    private final String itemName, serverName;
    private final ItemStack stack;

    MenuItems(ItemBuilder builder, String serverName) {
       this(builder.getItemMeta().getDisplayName(), builder, serverName);
    }

    MenuItems(String itemName, ItemBuilder itemBuilder, String serverName) {
        this.itemName = itemName;
        this.serverName = serverName;
        this.stack = itemBuilder.build();
    }

    public String getItemName() {
        return itemName;
    }

    public String getServerName() {
        return serverName;
    }

    public ItemStack getStack() {
        return stack;
    }

    public static Optional<MenuItems> getItemFromName(String itemName) {
        return Arrays.stream(values()).filter(items -> items.itemName.equalsIgnoreCase(itemName)).findFirst();
    }
}
