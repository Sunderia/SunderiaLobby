package fr.sunderia.sunderialobby.utils;

import fr.sunderia.sunderialobby.SunderiaLobby;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Consumer;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder implements Listener {

    private ItemStack stack;
    private Consumer<PlayerInteractEvent> interactConsumer;

    public ItemBuilder(ItemStack stack) {
        this.stack = stack.clone();
    }

    public ItemBuilder(Material mat, int amount) {
        this(new ItemStack(mat, amount));
    }

    public ItemBuilder(Material mat) {
        this(mat, 1);
    }

    public ItemMeta getItemMeta() {
        return stack.getItemMeta();
    }

    public ItemBuilder setColor(Color color) {
        if(!(stack.getItemMeta() instanceof LeatherArmorMeta meta)) return this;
        meta.setColor(color);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder onInteract(Consumer<PlayerInteractEvent> eventConsumer) {
        this.interactConsumer = eventConsumer;
        return this;
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        if(!event.getPlayer().getInventory().getItemInMainHand().isSimilar(stack) || interactConsumer == null) return;
        interactConsumer.accept(event);
    }

    public ItemBuilder setGlow(boolean glow) {
        if(glow) {
            if(ItemStackUtils.isAnArmor(stack)) {
                addEnchant(Enchantment.KNOCKBACK, 1);
            } else {
                addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 1);
            }
            addItemFlag(ItemFlag.HIDE_ENCHANTS);
        } else {
            ItemMeta meta = getItemMeta();
            for(Enchantment enchantment : meta.getEnchants().keySet()) {
                meta.removeEnchant(enchantment);
            }
        }
        return this;
    }

    public ItemBuilder setGlow() {
        return setGlow(stack.getEnchantments().isEmpty());
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        ItemMeta meta = stack.getItemMeta();
        if(!(meta instanceof Damageable)) return this;
        meta.setUnbreakable(unbreakable);
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public ItemBuilder setItemMeta(ItemMeta meta) {
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setHead(OfflinePlayer player) {
        if(!(stack.getItemMeta() instanceof SkullMeta meta)) return this;
        if(!meta.hasOwner()) return this;
        meta.setOwningPlayer(player);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setDisplayName(String displayName) {
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(displayName);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setItemStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        ItemMeta meta = getItemMeta();
        meta.setLore(lore);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setLore(String lore) {
        ArrayList<String> loreList = new ArrayList<>();
        loreList.add(lore);
        ItemMeta meta = getItemMeta();
        meta.setLore(loreList);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        ItemMeta meta = getItemMeta();
        meta.addEnchant(enchantment, level, true);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder addProtection(int level) {
        return addEnchant(Enchantment.PROTECTION_EXPLOSIONS, level).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, level).addEnchant(Enchantment.PROTECTION_FIRE, level)
                .addEnchant(Enchantment.PROTECTION_PROJECTILE, level);
    }

    public ItemBuilder addItemFlag(ItemFlag flag) {
        ItemMeta meta = getItemMeta();
        meta.addItemFlags(flag);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag... flags) {
        for (ItemFlag flag : flags) {
            addItemFlag(flag);
        }
        return this;
    }

    public ItemBuilder setCustomModelData(int customModelData) {
        ItemMeta meta = getItemMeta();
        meta.setCustomModelData(customModelData);
        setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        if(interactConsumer != null) Bukkit.getServer().getPluginManager().registerEvents(this, SunderiaLobby.getInstance());
        return stack;
    }
}