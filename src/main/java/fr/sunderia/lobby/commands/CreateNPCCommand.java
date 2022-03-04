package fr.sunderia.lobby.commands;

import fr.sunderia.lobby.SunderiaLobby;
import fr.sunderia.lobby.commands.info.CommandInfo;
import fr.sunderia.lobby.commands.info.PluginCommand;
import fr.sunderia.lobby.manager.NPCManager;
import fr.sunderia.lobby.utils.PlayerSkinUtils;
import fr.sunderia.lobby.utils.UUIDFetcher;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static fr.sunderia.lobby.commands.CreateNPCCommand.COMMAND_NAME;

@CommandInfo(name = COMMAND_NAME, permission = "sunderialobby.command.createNPC", usage = "/createNPC <name> [skin name]", description = "Create a NPC", aliases = {"cn"},
        requiresPlayer = true)
public class CreateNPCCommand extends PluginCommand {

    public static final String COMMAND_NAME = "createNPC";
    private final NPCManager manager;

    public CreateNPCCommand(SunderiaLobby plugin) {
        super(plugin);
        this.manager = plugin.getNpcManager();
    }

    @Override
    public void onCommand(Player player, String[] args) {
        Optional<String> name = getArg(args, 0);
        if(name.isEmpty()) {
            player.sendMessage(getInfo().usage());
            return;
        }
        Optional<String> skinName = getArg(args, 1);
        AtomicReference<NPCManager.Skin> skin = new AtomicReference<>();
        Optional<String> uuid = UUIDFetcher.getOptionalUUID(skinName.orElse(null));
        if(uuid.isEmpty()) {
            skin.set(PlayerSkinUtils.getFromPlayer(player));
            manager.spawnNPC(player, skin.get(), name.get());
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    NPCManager.Skin skin = PlayerSkinUtils.getFromName(uuid.get());
                    if(skin == null) {
                        skin = PlayerSkinUtils.getFromPlayer(player);
                    }
                    manager.spawnNPC(player, skin, name.get());
                }
            }.runTask(plugin);
        }
    }


    private Optional<String> getArg(String[] args, int index) {
        if(args.length > index) {
            return Optional.of(args[index]);
        }
        return Optional.empty();
    }
}
