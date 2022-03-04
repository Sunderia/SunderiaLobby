package fr.sunderia.lobby.commands.info;

import com.google.common.collect.ImmutableList;
import fr.sunderia.lobby.SunderiaLobby;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class PluginCommand extends BukkitCommand {
    private final CommandInfo info;
    protected final SunderiaLobby plugin;

    protected PluginCommand(SunderiaLobby lobby) {
        super("");
        this.plugin = lobby;
        info = getClass().getDeclaredAnnotation(CommandInfo.class);
        Objects.requireNonNull(info, "CommandInfo annotation is missing");
        setName(info.name());
        setAliases(ImmutableList.copyOf(info.aliases()));
        setDescription(info.description());
        setUsage(info.usage());
        setPermission(info.permission());
        setPermissionMessage(info.permissionMessage());
    }

    public CommandInfo getInfo() {
        return info;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!info.permission().isEmpty() && !sender.hasPermission(info.permission())) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        if(info.requiresPlayer()) {
            if(!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
            } else {
                onCommand(player, args);
            }
            return true;
        }
        onCommand(sender, args);
        return true;
    }

    public void onCommand(Player player, String[] args) {}
    public void onCommand(CommandSender sender, String[] args) {}
}
