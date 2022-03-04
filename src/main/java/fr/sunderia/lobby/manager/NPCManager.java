package fr.sunderia.lobby.manager;

import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;
import fr.sunderia.lobby.SunderiaLobby;
import fr.sunderia.lobby.utils.JsonConfigUtils;
import fr.sunderia.lobby.utils.PlayerSkinUtils;
import fr.sunderia.lobby.utils.serializer.GameProfileSerializer;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCManager implements Listener {

    private final Map<String, NPC> npcs;
    private final Map<NPC, ServerPlayer> serverPlayers;
    private final JsonConfigUtils config;
    private final SunderiaLobby plugin;

    public NPCManager(SunderiaLobby lobby) throws IOException {
        this.plugin = lobby;
        this.config = new JsonConfigUtils(new File(lobby.getDataFolder(), "npcs.json"),
                new JsonConfigUtils.TypeAdapter(GameProfile.class, new GameProfileSerializer()),
                new JsonConfigUtils.TypeAdapter(PropertyMap.class, new PropertyMap.Serializer()),
                new JsonConfigUtils.TypeAdapter(UUID.class, new UUIDTypeAdapter()));
        this.npcs = config.readConfig(new TypeToken<Map<String, NPC>>() {}.getType());
        this.serverPlayers = new HashMap<>();
        lobby.getServer().getPluginManager().registerEvents(this, lobby);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        npcs.forEach((uuid, npc) -> {
            if(npc.getLocation().toLocation().getWorld() != event.getPlayer().getWorld()) return;
            spawnNPC(event.getPlayer(), npc);
        });
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        serverPlayers.keySet().stream().filter(npc -> npc.getLocation().getWorld().equals(event.getPlayer().getWorld().getUID()))
                .forEach(npc -> {
                    Location loc = npc.getLocation().toLocation().clone();
                    loc.setDirection(event.getPlayer().getLocation().subtract(loc).toVector());

                    sendRotationPackets(npc, loc, ((CraftPlayer) event.getPlayer()).getHandle().connection);
                });
    }

    public void spawnNPC(Player player, NPC npc) {
        Location loc = npc.getLocation().toLocation();
        CraftPlayer craftPlayer = (CraftPlayer) player;
        var world = ((CraftWorld) loc.getWorld()).getHandle();
        MinecraftServer server = world.getServer();
        ServerLevel level = world.getLevel();

        ServerPlayer serverNPC = createNPC(npc.getProfile().getName(), PlayerSkinUtils.getFromGameProfile(npc.profile), level, server, loc);
        if(!serverPlayers.containsKey(npc)) serverPlayers.put(npc, serverNPC);
        sendPackets(craftPlayer.getHandle(), serverNPC, npc.getLocation().toLocation());
    }

    public void spawnNPC(Player player, Skin skin, String name) {
        Location loc = player.getLocation();
        CraftWorld world = (CraftWorld) loc.getWorld();
        MinecraftServer server = world.getHandle().getServer();
        ServerLevel level = world.getHandle().getLevel();

        ServerPlayer serverNPC = createNPC(name, skin, level, server, loc);

        Bukkit.getOnlinePlayers().stream().map(CraftPlayer.class::cast).map(CraftPlayer::getHandle).forEach(serverPlayer -> sendPackets(serverPlayer, serverNPC, player.getLocation()));
        NPC npc = new NPC(serverNPC.getGameProfile(), new CustomLocation(player.getLocation()));
        npcs.put(serverNPC.getGameProfile().getId().toString(), npc);
        serverPlayers.put(npc, serverNPC);

        player.sendMessage("You have spawned an NPC");
    }

    private ServerPlayer createNPC(String name, Skin skin, ServerLevel level, MinecraftServer server, Location loc) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        profile.getProperties().put("textures", new Property("textures", skin.value(), skin.signature()));
        ServerPlayer serverNPC = new ServerPlayer(server, level, profile);
        serverNPC.setPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        serverNPC.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(17), (byte) 127);
        return serverNPC;
    }

    private void sendPackets(ServerPlayer player, ServerPlayer serverNPC, Location location) {
        var ps = player.connection;
        ps.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, serverNPC));
        ps.send(new ClientboundAddPlayerPacket(serverNPC));
        ps.send(new ClientboundSetEntityDataPacket(serverNPC.getId(), serverNPC.getEntityData(), true));
        sendRotationPackets(serverNPC, location, ps);
    }

    public void sendRotationPackets(NPC npc, Location loc, ServerGamePacketListenerImpl ps) {
        if(!serverPlayers.containsKey(npc)) {
            Bukkit.getLogger().info(npc.getProfile().getName() + " does not exists !");
            return;
        }
        sendRotationPackets(serverPlayers.get(npc), loc, ps);
    }

    private void sendRotationPackets(ServerPlayer serverNPC, Location location, ServerGamePacketListenerImpl ps) {
        //TODO: Fix packet not sending to everyone.
        Bukkit.getLogger().info("Sending rotation packets to " + ps.player.getGameProfile().getName() + " at yaw " + location.getYaw() + " pitch " + location.getPitch());
        ps.send(new ClientboundRotateHeadPacket(serverNPC, (byte) ((location.getYaw()%360)*256/360)));
        ps.send(new ClientboundMoveEntityPacket.Rot(serverNPC.getBukkitEntity().getEntityId(), (byte) ((location.getYaw()%360)*256/360), (byte) ((location.getPitch()%360)*256/360), false));
    }

    public void onDisable() throws IOException {
        this.config.writeConfig(this.npcs);
    }

    public static class NPC {
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

    public static class CustomLocation {
        private final double x, y, z, pitch, yaw;
        private final UUID world;
        private transient Location bukkitLocation;

        public CustomLocation(Location loc) {
            this(loc, false);
        }

        public CustomLocation(Location loc, boolean asInt) {
            this.x = asInt ? loc.getBlockX() : loc.getX();
            this.y = asInt ? loc.getBlockY() : loc.getY();
            this.z = asInt ? loc.getBlockZ() : loc.getZ();
            this.pitch = loc.getPitch();
            this.yaw = loc.getYaw();
            this.world = loc.getWorld().getUID();
            this.bukkitLocation = loc;
        }

        public CustomLocation(double x, double y, double z, double pitch, double yaw, UUID world) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.pitch = pitch;
            this.yaw = yaw;
            this.world = world;
            this.bukkitLocation = new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
        }

        public Location toLocation() {
            if(this.bukkitLocation == null) {
                this.bukkitLocation = new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
            }
            return this.bukkitLocation;
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

    public record Skin(String value, String signature) {}

    public Collection<NPC> getNPCs() {
        return npcs.values();
    }
}
