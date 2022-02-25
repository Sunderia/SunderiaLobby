package fr.sunderia.sunderialobby;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import fr.sunderia.sunderialobby.utils.InventoryBuilder;
import fr.sunderia.sunderialobby.utils.ItemBuilder;
import fr.sunderia.sunderialobby.utils.ItemStackUtils;
import fr.sunderia.sunderialobby.utils.LocationUtils;
import io.github.leonardosnt.bungeechannelapi.BungeeChannelApi;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static fr.sunderia.sunderialobby.Items.*;

public class PlayerListener implements Listener {

    public static final BungeeChannelApi channelAPI = BungeeChannelApi.of(SunderiaLobby.getInstance());

    public static final InventoryBuilder MENU = new InventoryBuilder(ChatColor.GOLD + "Menu", 3, 5)
            .addItems(0, new ItemStack[9])
            .setItem(4, MenuItems.LOBBY.getStack())
            .addItems(0, Material.AIR)
            .addItems(MenuItems.SKYBLOCK,
                    MenuItems.RUSSIA_VS_UKRAINE)
            .setItem(22, MenuItems.DEV.getStack())
            .onOpen(event -> event.getInventory().setContents(Arrays.stream(event.getInventory().getContents())
                    .map(is -> ItemStackUtils.isAirOrNull(is) ? SunderiaLobby.getInstance().getRand().nextBoolean() ?
                            new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).setDisplayName(ChatColor.YELLOW + "").build() :
                            new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).setDisplayName(ChatColor.GRAY + "").build() : is)
                    .toArray(ItemStack[]::new)))
            .onUpdate(event ->
                    Arrays.stream(event.getInventory().getContents()).filter(is -> !is.getType().name().contains("STAINED_GLASS_PANE")).forEach(is -> {
                        ItemMeta meta = is.getItemMeta();
                        var item = MenuItems.getItemFromName(is.getItemMeta().getDisplayName());
                        if (item.isEmpty()) {
                            Bukkit.getLogger().info("[SunderiaLobby] Item not found: " + is.getItemMeta().getDisplayName());
                            return;
                        }
                        if (item.get().getServerName() != null) {
                            channelAPI.getPlayerCount(item.get().getServerName()).whenComplete((result, error) -> {
                                meta.setLore(List.of(ChatColor.GREEN + "There are " + (error == null ? result : 0) + " players online."));
                                is.setItemMeta(meta);
                            });
                        } else {
                            meta.setLore(List.of(ChatColor.GREEN + "There are 0 players online."));
                            is.setItemMeta(meta);
                        }
                    }), 5, 20 * 5)
            .onClick(event -> {
                        if (!event.getClick().isLeftClick() || !event.getClick().isRightClick()) {
                            return;
                        }
                        event.setCancelled(true);
                        event.setResult(Event.Result.DENY);
                        if(event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null || event.getCurrentItem().getType().name().contains("STAINED_GLASS_PANE")) {
                            return;
                        }
                        Player player = (Player) event.getWhoClicked();
                        var item = MenuItems.getItemFromName(event.getCurrentItem().getItemMeta().getDisplayName());
                        if(item.isEmpty()) {
                            return;
                        }
                        if(item.get().getServerName() == null) {
                            Bukkit.getScheduler().runTask(SunderiaLobby.getInstance(), () -> {
                                player.spigot().sendMessage(ChatMessageType.CHAT, TextComponent.fromLegacyText(ChatColor.RED + "This item is not available on this server."));
                                player.closeInventory();
                            });
                            return;
                        }
                        Bukkit.getScheduler().runTask(SunderiaLobby.getInstance(), () -> {
                            player.closeInventory();
                            channelAPI.connect((Player) event.getWhoClicked(), item.get().getServerName());
                        });
            });

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().clear();
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
        event.getPlayer().getInventory().setItem(0, COMPASS);
        event.getPlayer().getInventory().setItem(2, NPC_SPAWNER);
        event.getPlayer().setHealth(20);
        event.getPlayer().setFoodLevel(20);
        event.getPlayer().setSaturation(20);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntity(EntityExhaustionEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }
}
