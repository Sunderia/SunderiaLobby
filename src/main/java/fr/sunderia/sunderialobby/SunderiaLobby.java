package fr.sunderia.sunderialobby;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.sunderia.sunderialobby.manager.NPCManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getLogger().info("SunderiaLobby has been enabled.");
        try {
            this.npcManager = new NPCManager(this);
        } catch (IOException e) {
            e.printStackTrace();
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
