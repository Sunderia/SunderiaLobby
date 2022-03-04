package fr.sunderia.lobby.utils;

import com.google.common.collect.ImmutableList;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class LocationUtils {

    private LocationUtils() {}

    public static List<Block> getNearbyBlocks(Location blockLocation, int radius) {
        List<Block> blocks = new ArrayList<>();
        for(int x = blockLocation.getBlockX() - radius; x <= blockLocation.getBlockX() + radius; x++) {
            for(int y = blockLocation.getBlockY() - radius; y <= blockLocation.getBlockY() + radius; y++) {
                for(int z = blockLocation.getBlockZ() - radius; z <= blockLocation.getBlockZ() + radius; z++) {
                    blocks.add(blockLocation.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return new ImmutableList.Builder<Block>().addAll(blocks).build();
    }

    public static Location[] getNearbyLocations(Location blockLocation, int radius) {
        return getNearbyBlocks(blockLocation, radius).stream().map(Block::getLocation).toArray(Location[]::new);
    }

}
