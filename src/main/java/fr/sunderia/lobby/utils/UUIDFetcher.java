package fr.sunderia.lobby.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.util.UUIDTypeAdapter;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UUIDFetcher {

    private UUIDFetcher() {}

    public static final String SERVICE_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    public static final Gson gson = new GsonBuilder().create();

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    public static String getUUID(Player player) throws IOException {
        return getUUID(player.getName());
    }

    public static String getUUID(String name) throws IOException {
        if(CACHE.containsKey(name)) {
            return CACHE.get(name);
        } else {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(SERVICE_URL, name)).openConnection();
            connection.setReadTimeout(5000);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String json = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                String id = gson.fromJson(json, JsonObject.class).get("id").getAsString();
                CACHE.put(name, id);
                return id;
            } else {
                if(CACHE.containsKey(name)) {
                    return CACHE.get(name);
                }
                throw new IOException("Could not connect to UUID service");
            }
        }
    }

    public static Optional<String> getOptionalUUID(String name) {
        if(name == null) return Optional.empty();
        try {
            return Optional.of(getUUID(name));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static UUID toUUID(String uuid) {
        return UUIDTypeAdapter.fromString(uuid);
    }

}
