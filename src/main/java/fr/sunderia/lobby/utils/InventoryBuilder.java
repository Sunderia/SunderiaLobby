package fr.sunderia.lobby.utils;

import fr.sunderia.lobby.MenuItems;
import fr.sunderia.lobby.SunderiaLobby;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InventoryBuilder implements Listener {

    private final int spacing;
    private int rows = 3;
    private String name;
    private final List<ItemStack> itemStacks;
    private Consumer<InventoryClickEvent> clickEventConsumer = InventoryEvent::getInventory;
    private Consumer<InventoryOpenEvent> openEventConsumer = InventoryEvent::getInventory;
    private Consumer<InventoryEvent> updateEventConsumer = InventoryEvent::getInventory;
    private BukkitRunnable runnable;
    //1 Second
    private int runnableTime = 20;
    private int runnableDelay = 20;
    private boolean cancelEvent = false;

    public InventoryBuilder(@NotNull String name) {
        this(name, 3, 0);
    }

    public InventoryBuilder(@NotNull String name, int rows) {
        this(name, rows, 0);
    }
    
    public InventoryBuilder(@NotNull String name, int rows, int spacing) {
        this.name = name;
        this.itemStacks = new ArrayList<>(rows * 9);
        this.setRows(rows);
        this.spacing = spacing;
    }

    public InventoryBuilder onOpen(Consumer<InventoryOpenEvent> eventConsumer) {
        this.openEventConsumer = eventConsumer;
        return this;
    }

    public InventoryBuilder onClick(Consumer<InventoryClickEvent> eventConsumer) {
        this.clickEventConsumer = eventConsumer;
        return this;
    }

    public InventoryBuilder onUpdate(Consumer<InventoryEvent> eventConsumer) {
        return onUpdate(eventConsumer, runnableTime, runnableDelay);
    }

    public InventoryBuilder onUpdate(Consumer<InventoryEvent> eventConsumer, int time) {
        return onUpdate(eventConsumer, time, time);
    }

    public InventoryBuilder onUpdate(Consumer<InventoryEvent> eventConsumer, int delay, int time) {
        this.updateEventConsumer = eventConsumer;
        this.runnableTime = time;
        this.runnableDelay = delay;
        return this;
    }

    public InventoryBuilder setCancelled() {
        this.cancelEvent = !cancelEvent;
        return this;
    }

    @EventHandler
    private void onClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.CHEST || event.getInventory().getSize() != getSize() || !event.getView().getTitle().equalsIgnoreCase(name)) return;
        event.setCancelled(cancelEvent);
        this.clickEventConsumer.accept(event);
    }

    @EventHandler
    private void onOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() != InventoryType.CHEST || event.getInventory().getSize() != getSize() || !event.getView().getTitle().equalsIgnoreCase(name)) return;
        if (runnable == null) {
            this.runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    updateEventConsumer.accept(event);
                }
            };
            runnable.runTaskTimer(SunderiaLobby.getInstance(), runnableDelay, runnableTime);
        }
        this.openEventConsumer.accept(event);
    }

    @EventHandler
    private void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getType() != InventoryType.CHEST || event.getInventory().getSize() != getSize() || !event.getView().getTitle().equalsIgnoreCase(name)) return;
        if (runnable == null) return;
        this.runnable.cancel();
        this.runnable = null;
    }

    public InventoryBuilder setRows(int rows) {
        if (rows > 6 || rows < 1) rows = 3;
        this.rows = rows;
        return this;
    }

    public InventoryBuilder addItems(ItemStack... itemStacks) {
        return addItems(spacing, itemStacks);
    }

    public InventoryBuilder addItems(MenuItems... itemStacks) {
        return addItems(spacing, Arrays.stream(itemStacks).map(MenuItems::getStack).toArray(ItemStack[]::new));
    }

    public InventoryBuilder addItems(int spacing, Material mat) {
        return addItems(spacing, new ItemStack(mat));
    }

    public InventoryBuilder addItems(int spacing, ItemStack... itemStacks) {
        List<ItemStack> stacks = new ArrayList<>();
        AtomicInteger i = new AtomicInteger();
        if(spacing > 0) {
            Arrays.stream(itemStacks).forEach(is -> {
                if (i.get() != 0 && i.get() % 9 == 0) {
                    stacks.add(null);
                    i.getAndIncrement();
                    return;
                }
                stacks.add(itemStacks[i.getAndIncrement()]);
                for (int j = 0; j < spacing; j++) {
                    stacks.add(null);
                }
            });
        } else {
            if(itemStacks == null) {
                stacks.add(null);
            } else {
                stacks.addAll(Arrays.asList(itemStacks));
            }
        }
        this.itemStacks.addAll(stacks);
        return this;
    }

    public InventoryBuilder addItems(List<ItemStack> itemStacks) {
        return addItems(itemStacks.toArray(itemStacks.toArray(new ItemStack[0])));
    }
    
    public InventoryBuilder setItem(int i, ItemStack stack) {
        try {
            this.itemStacks.set(i, stack);
        } catch (IndexOutOfBoundsException e) {
            this.itemStacks.add(i, stack);
        }
        return this;
    }

    public InventoryBuilder setName(String name) {
        this.name = name;
        return this;
    }

    private int getSize() {
        return rows * 9;
    }

    public Inventory build() {
        Bukkit.getServer().getPluginManager().registerEvents(this, SunderiaLobby.getInstance());
        Inventory inv = Bukkit.createInventory(null, rows * 9, name);
        for (int i = 0; i < itemStacks.size(); i++) {
            inv.setItem(i, itemStacks.get(i));
        }
        return inv;
    }
}
