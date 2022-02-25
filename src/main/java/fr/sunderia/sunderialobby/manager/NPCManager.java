package fr.sunderia.sunderialobby.manager;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;
import fr.sunderia.sunderialobby.SunderiaLobby;
import fr.sunderia.sunderialobby.utils.JsonConfigUtils;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class NPCManager implements Listener {

    private final Map<String, NPC> npcs;
    private final JsonConfigUtils config;
    private final SunderiaLobby plugin;

    public NPCManager(SunderiaLobby lobby) throws IOException {
        this.plugin = lobby;
        this.config = new JsonConfigUtils(new File(lobby.getDataFolder(), "npcs.json"),
                new JsonConfigUtils.TypeAdapter(GameProfile.class, new GameProfileSerializer()),
                new JsonConfigUtils.TypeAdapter(PropertyMap.class, new PropertyMap.Serializer()),
                new JsonConfigUtils.TypeAdapter(UUID.class, new UUIDTypeAdapter()));
        Type type = new TypeToken<Map<String, NPC>>() {}.getType();
        this.npcs = config.readConfig(type);
        lobby.getServer().getPluginManager().registerEvents(this, lobby);
    }

    public void spawnNPC(Player player, NPC npc) {
        Location loc = npc.getLocation().toLocation();
        CraftPlayer craftPlayer = (CraftPlayer) player;
        CraftWorld world = (CraftWorld) loc.getWorld();
        MinecraftServer server = world.getHandle().getServer();
        ServerLevel level = world.getHandle().getLevel();

        /*GameProfile profile = new GameProfile(UUID.randomUUID(), "NPC-" + plugin.getRand().nextInt(101));
        ServerPlayer npc = new ServerPlayer(server, level, profile);
        npc.setPos(loc.getX(), loc.getY(), loc.getZ());*/

        GameProfile profile = npc.getProfile();
        ServerPlayer serverNPC = new ServerPlayer(server, level, profile);
        serverNPC.setPos(loc.getX(), loc.getY(), loc.getZ());

        serverNPC.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(17), (byte) 127);
        var ps = craftPlayer.getHandle().connection;
        ps.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, serverNPC));
        ps.send(new ClientboundAddPlayerPacket(serverNPC));
        ps.send(new ClientboundSetEntityDataPacket(serverNPC.getId(), serverNPC.getEntityData(), true));
    }

    public void spawnNPC(Player p) {
        CraftPlayer craftPlayer = (CraftPlayer) p;

        MinecraftServer server = craftPlayer.getHandle().getServer();
        ServerLevel level = craftPlayer.getHandle().getLevel();

        GameProfile profile = new GameProfile(UUID.randomUUID(), "NPC-" + plugin.getRand().nextInt(101));
        ServerPlayer npc = new ServerPlayer(server, level, profile);
        npc.setPos(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());

        String value = "ewogICJ0aW1lc3RhbXAiIDogMTY0NTc5NTczMDA2MCwKICAicHJvZmlsZUlkIiA6ICIzNjJlZTMwYWQ1M2M0YzUwYmUxMmMyNDQxYjA2MjY3MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJtaW5lbW9icyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zZGZhYzY4YTFjM2YwZjBhNmU2NGJjMjk5YzBhYjYxNTUyMjJmNjE2YzY4ZDliMGZkNjg1NGNhMGUzOGFiYjliIgogICAgfSwKICAgICJDQVBFIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yMzQwYzBlMDNkZDI0YTExYjE1YThiMzNjMmE3ZTllMzJhYmIyMDUxYjI0ODFkMGJhN2RlZmQ2MzVjYTdhOTMzIgogICAgfQogIH0KfQ==";
        String signature = "yCu2iNIH7SeLdZD1ToxVB2lwc0RtpFWrwWmdL23qRVhqNWaiRGyrhcjCfmYX9FCaOcgiMx1wPn+tMUb+RscuFBki0Sdri7++WJeetPuKDyzaoT2Gpuf+XyUZdnncg1hxbmJ4+yXu5creM0WrHOVGPmEUQt2SPdYrq/kjc9+fD0p6S5e5zudlEdjq8JFy+sidz/IjoE2sTQ+yf9pgVjN5urG6pQOcsHDlaIEl5Da8N6QPUYNp2BmultV2CbRHUzVo1upO+/7xdO9wWRbqywXzS7CfeigWoF4tqkBsIvTvPOZvK+aKAoiBbzNnIm+UOvEmiJQztzfx+boPwbm++1g3tBj4+0ZQghqynaDf7H2QCi+5eaau79HYUI1NNkrKVnSB7muRO+BwJL4HUrCOHLOiOiKAIKDcTsUhmPTt5D7hSHCIQ9gPtLxnedPHiDGuFM1/msKYfI+5QQXmzO3I9t3XcYSlnaSHYlaIi0A6zXxT6k6S0SQ2/wBix1ag7ekVmY+6F1hUIoPya5YKynhiS5cqprg6VW9tkNFQ5CFsqLrZ4npi29WTQeSQAKGsCV7d7Az2s/NRbuy/TxPHuZWuh3yrxntHUL6UKdl1zQwbXyU1qL8TmMPuw5KD0JeEOJWhyK56ZAkOeE41PWxYiNyb6mUcNPe60nAT6Xh0QYYTSyX+FBY=";

        profile.getProperties().put("textures", new Property("textures", value, signature));

        //npc.getEntityData().set(new EntityDataAccessor<>(16, EntityDataSerializers.BYTE), (byte) 127);
        npc.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(17), (byte) 127);
        var ps = craftPlayer.getHandle().connection;
        ps.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc));
        ps.send(new ClientboundAddPlayerPacket(npc));
        ps.send(new ClientboundSetEntityDataPacket(npc.getId(), npc.getEntityData(), true));
        npcs.put(profile.getId().toString(), new NPC(profile, new CustomLocation(p.getLocation())));

        p.sendMessage("You have spawned an NPC");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        npcs.forEach((uuid, npc) -> {
            if(npc.getLocation().toLocation().getWorld() != event.getPlayer().getWorld()) return;
            spawnNPC(event.getPlayer(), npc);
        });
    }

    public void onDisable() throws IOException {
        this.config.writeConfig(this.npcs);
    }

    public class NPC {
        private final GameProfile profile;
        private final CustomLocation location;

        public NPC(GameProfile profile, CustomLocation loc) {
            this.profile = profile;
            this.location = loc;
        }

        public GameProfile getProfile() {
            return profile;
        }

        public CustomLocation getLocation() {
            return location;
        }
    }

    public class CustomLocation {
        private final double x, y, z, pitch, yaw;
        private final UUID world;

        public CustomLocation(Location loc) {
            this.x = loc.getX();
            this.y = loc.getY();
            this.z = loc.getZ();
            this.pitch = loc.getPitch();
            this.yaw = loc.getYaw();
            this.world = loc.getWorld().getUID();
        }

        public CustomLocation(Location loc, boolean asInt) {
            this.x = asInt ? loc.getBlockX() : loc.getX();
            this.y = asInt ? loc.getBlockY() : loc.getY();
            this.z = asInt ? loc.getBlockZ() : loc.getZ();
            this.pitch = loc.getPitch();
            this.yaw = loc.getYaw();
            this.world = loc.getWorld().getUID();
        }

        public CustomLocation(double x, double y, double z, double pitch, double yaw, UUID world) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.pitch = pitch;
            this.yaw = yaw;
            this.world = world;
        }

        public Location toLocation() {
            return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public double getPitch() {
            return pitch;
        }

        public double getYaw() {
            return yaw;
        }

        public UUID getWorld() {
            return world;
        }
    }

    /**
     * @author <a href="https://gist.github.com/Jofkos/79af290e94acdc7d7d5b">Jofkos</a>
     */
    private static class GameProfileSerializer implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {

        public GameProfile deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = (JsonObject) json;
            UUID id = object.has("id") ? (UUID) context.deserialize(object.get("id"), UUID.class) : null;
            String name = object.has("name") ? object.getAsJsonPrimitive("name").getAsString() : null;
            GameProfile profile = new GameProfile(id, name);

            if (object.has("properties")) {
                for (Map.Entry<String, Property> prop : ((PropertyMap) context.deserialize(object.get("properties"), PropertyMap.class)).entries()) {
                    profile.getProperties().put(prop.getKey(), prop.getValue());
                }
            }
            return profile;
        }

        public JsonElement serialize(GameProfile profile, Type type, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            if (profile.getId() != null)
                result.add("id", context.serialize(profile.getId()));
            if (profile.getName() != null)
                result.addProperty("name", profile.getName());
            if (!profile.getProperties().isEmpty())
                result.add("properties", context.serialize(profile.getProperties()));
            return result;
        }

    }

}
