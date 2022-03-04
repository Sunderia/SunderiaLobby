package fr.sunderia.lobby;

import fr.sunderia.lobby.utils.InventoryBuilder;
import fr.sunderia.lobby.utils.ItemBuilder;
import fr.sunderia.lobby.utils.ItemStackUtils;
import io.github.leonardosnt.bungeechannelapi.BungeeChannelApi;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

import static fr.sunderia.lobby.Items.COMPASS;

public class PlayerListener implements Listener {

    public static final BungeeChannelApi channelAPI = BungeeChannelApi.of(SunderiaLobby.getInstance());

    public static final InventoryBuilder MENU = new InventoryBuilder(ChatColor.GOLD + "Menu", 3, 5)
            .addItems(0, new ItemStack[9])
            .setItem(4, MenuItems.LOBBY.getStack())
            .addItems(0, Material.AIR)
            .addItems(MenuItems.SKYBLOCK,
                    MenuItems.RUSSIA_VS_UKRAINE)
            .setItem(22, MenuItems.DEV.getStack())
            .setCancelled()
            .onOpen(event -> event.getInventory().setContents(Arrays.stream(event.getInventory().getContents())
                    .map(is -> {
                        if (ItemStackUtils.isAirOrNull(is)) return SunderiaLobby.getInstance().getRand().nextBoolean() ?
                                new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).setDisplayName(ChatColor.YELLOW + "").build() :
                                new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).setDisplayName(ChatColor.GRAY + "").build();
                        return is;
                    })
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
                if (event.getClick().isLeftClick() || event.getClick().isRightClick()) {
                    if (event.getCurrentItem() == null || event.getCurrentItem().getType().name().contains("STAINED_GLASS_PANE")) {
                        return;
                    }
                    Player player = (Player) event.getWhoClicked();
                    var item = MenuItems.getItemFromName(event.getCurrentItem().getItemMeta().getDisplayName());
                    if (item.isEmpty()) {
                        return;
                    }
                    if (item.get().getServerName() == null) {
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
                }
            });

    private final SunderiaLobby plugin;

    public PlayerListener(SunderiaLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getLogger().info("[SunderiaLobby] " + event.getPlayer().getName() + " joined the server.");
        event.getPlayer().getInventory().clear();
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
        event.getPlayer().getInventory().setItem(0, COMPASS);
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
