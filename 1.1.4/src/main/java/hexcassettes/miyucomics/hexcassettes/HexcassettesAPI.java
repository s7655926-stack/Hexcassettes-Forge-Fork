package hexcassettes.miyucomics.hexcassettes;

import hexcassettes.miyucomics.hexcassettes.data.PlayerState;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

public class HexcassettesAPI extends SavedData {

    private final HashMap<UUID, PlayerState> players = new HashMap<>();

    public static HexcassettesAPI createFromNbt(CompoundTag nbt) {
        HexcassettesAPI data = new HexcassettesAPI();
        if (nbt.contains("players", 10)) {
            CompoundTag playersTag = nbt.getCompound("players");
            for (String key : playersTag.getAllKeys()) {
                UUID uuid = UUID.fromString(key);
                PlayerState state = PlayerState.deserialize(playersTag.getCompound(key));
                state.clampToMax(10);
                data.players.put(uuid, state);
            }
        }
        return data;
    }

    public static HexcassettesAPI getServerState(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(HexcassettesAPI::createFromNbt, HexcassettesAPI::new, "hexcassettes");
    }

    public static PlayerState getPlayerState(Player player) {
        if (player.level().isClientSide) {
            return new PlayerState();
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return new PlayerState();
        }

        HexcassettesAPI api = getServerState(server);
        PlayerState state = api.players.computeIfAbsent(player.getUUID(), uuid -> new PlayerState());
        state.clampToMax(10);
        return state;
    }

    public static void sendSyncPacket(ServerPlayer player) {
        PlayerState state = getPlayerState(player);
        state.clampToMax(10);
        HexcassettesNetworking.sendSyncToClient(player, state);
    }

    public static void dequeueAll(ServerPlayer player) {
        PlayerState state = getPlayerState(player);
        for (int i = 0; i < 10; i++) {
            state.cassettePrograms[i] = null;
        }
        state.editSlot = -1;
        state.playingSlots.clear();
        sendSyncPacket(player);
        if (player.getServer() != null) {
            getServerState(player.getServer()).setDirty();
        }
    }

    public static void dequeueByName(ServerPlayer player, String label) {
        // TODO: не реализовано
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerState> entry : players.entrySet()) {
            PlayerState state = entry.getValue();
            state.clampToMax(10);
            playersTag.put(entry.getKey().toString(), state.serialize());
        }
        nbt.put("players", playersTag);
        return nbt;
    }
}