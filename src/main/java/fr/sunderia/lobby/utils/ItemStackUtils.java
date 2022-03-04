package fr.sunderia.lobby.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackUtils {

    private ItemStackUtils() {}

    public static final ItemStack AIR = new ItemStack(Material.AIR);

    public static boolean isAnArmor(ItemStack is) {
        return is.getType().name().endsWith("_HELMET") || is.getType().name().endsWith("_CHESTPLATE") || is.getType().name().endsWith("_LEGGINGS") ||
                is.getType().name().endsWith("_BOOTS");
    }

    public static boolean isAirOrNull(ItemStack is) {
        return is == null || is.getType() == Material.AIR;
    }

}
