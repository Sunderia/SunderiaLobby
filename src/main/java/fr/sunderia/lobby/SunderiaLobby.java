package fr.sunderia.lobby;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.Reflection;
import fr.sunderia.lobby.commands.CreateNPCCommand;
import fr.sunderia.lobby.commands.info.CommandInfo;
import fr.sunderia.lobby.commands.info.PluginCommand;
import fr.sunderia.lobby.manager.NPCManager;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

public class SunderiaLobby extends JavaPlugin {

    private Random rand;
    private static SunderiaLobby instance;
    private ProtocolManager protocolManager;
    private NPCManager npcManager;

    @Override
    public void onLoad() {
        instance = this;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        try {
            rand = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            rand = new Random();
        }
    }

    @Override
    public void onEnable() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        try {
            this.npcManager = new NPCManager(this);
            registerCommands();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getLogger().info("SunderiaLobby has been enabled.");
    }

    private void registerCommands() throws IOException {
        ClassPath.from(getClassLoader())
                .getTopLevelClassesRecursive(Reflection.getPackageName(CreateNPCCommand.class))
                .stream()
                .map(ClassPath.ClassInfo::load)
                .filter(clazz -> clazz.isAnnotationPresent(CommandInfo.class))
                .forEach(clazz -> {
                    getLogger().info("Registering command " + clazz.getAnnotation(CommandInfo.class).name());
                    PluginCommand command = Objects.requireNonNull((PluginCommand) newInstance(clazz));
                    SimpleCommandMap map = ((CraftServer) getServer()).getCommandMap();
                    map.register(this.getDescription().getName(), command);
                });
    }

    private Object newInstance(Class<?> clazz) {
        try {
            return clazz.getConstructor(SunderiaLobby.class).newInstance(this);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        if (npcManager != null) {
            try {
                npcManager.onDisable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static SunderiaLobby getInstance() {
        return instance;
    }

    public Random getRand() {
        return rand;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }
}
