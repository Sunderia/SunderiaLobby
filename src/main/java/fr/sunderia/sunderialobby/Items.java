package fr.sunderia.sunderialobby;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import fr.sunderia.sunderialobby.manager.NPCManager;
import fr.sunderia.sunderialobby.utils.ItemBuilder;
import fr.sunderia.sunderialobby.utils.LocationUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;

public class Items {

    public static final NPCManager npcManager = SunderiaLobby.getInstance().getNpcManager();

    public static final ItemStack COMPASS = new ItemBuilder(Material.COMPASS).setDisplayName(ChatColor.GOLD + "Menu").onInteract(event -> {
        if(event.getHand() != EquipmentSlot.HAND) return;
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.getPlayer().openInventory(PlayerListener.MENU.build());
        }
    }).setGlow().build();

    public static final ItemStack TESTPACKET = new ItemBuilder(Material.STICK).setDisplayName(ChatColor.GOLD + "Test Packet").onInteract(event -> {
        if(event.getHand() != EquipmentSlot.HAND) return;
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();

            if(event.getClickedBlock().getType() != Material.CALCITE) return;
            var locations = LocationUtils.getNearbyLocations(event.getClickedBlock().getLocation(), 1);
            for (Location location : locations) {
                PacketContainer changeBlock = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
                changeBlock.getBlockPositionModifier().write(0, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                changeBlock.getBlockData().write(0, WrappedBlockData.createData(Material.AIR));
                try {
                    SunderiaLobby.getInstance().getProtocolManager().sendServerPacket(player, changeBlock);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("Cannot send packet " + changeBlock, e);
                }
            }
        }
    }).build();

    public static final ItemStack NPC_SPAWNER = new ItemBuilder(Material.FEATHER).setDisplayName(ChatColor.GOLD + "NPC Spawner").onInteract(event -> {
        npcManager.spawnNPC(event.getPlayer());
    }).build();

    public static final ItemStack NPC_REMOVER = new ItemBuilder(Material.FEATHER).setDisplayName(ChatColor.RED + "NPC Remover").onInteract(event -> {

    }).build();


}
