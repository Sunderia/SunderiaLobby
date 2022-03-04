package fr.sunderia.lobby.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import fr.sunderia.lobby.manager.NPCManager;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class PlayerSkinUtils {

    private PlayerSkinUtils() {}

    public static NPCManager.Skin getFromPlayer(Player playerBukkit) {
        ServerPlayer playerNMS = ((CraftPlayer) playerBukkit).getHandle();
        GameProfile profile = playerNMS.getGameProfile();
        Property property = profile.getProperties().get("textures").iterator().next();
        return new NPCManager.Skin(property.getValue(), property.getSignature());
    }

    public static NPCManager.Skin getFromGameProfile(GameProfile profile) {
        Property property = profile.getProperties().get("textures").iterator().next();
        return new NPCManager.Skin(property.getValue(), property.getSignature());
    }

    public static NPCManager.Skin getFromName(String uuid) {
        try {
            URL skinURL = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader skinReader = new InputStreamReader(skinURL.openStream());
            JsonObject textureProperty = JsonParser.parseReader(skinReader).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            return new NPCManager.Skin(textureProperty.get("value").getAsString(), textureProperty.get("signature").getAsString());
        } catch (IOException e) {
            Bukkit.getLogger().warning("Could not get skin data from session servers!");
            e.printStackTrace();
            return null;
        }
    }

    public static NPCManager.Skin getFromName(UUID uuid) {
        return getFromName(UUIDTypeAdapter.fromUUID(uuid));
    }

}
