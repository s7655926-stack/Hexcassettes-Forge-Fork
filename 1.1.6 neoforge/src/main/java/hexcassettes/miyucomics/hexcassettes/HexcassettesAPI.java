package hexcassettes.miyucomics.hexcassettes;

import hexcassettes.miyucomics.hexcassettes.HexcassettesConfig;
import hexcassettes.miyucomics.hexcassettes.data.PlayerState;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;

public class HexcassettesAPI extends SavedData {
    // In hexcasting 0.12.0 the old IotaType.serialize/deserialize statics are gone;
    // iota NBT round-trips through IotaType.TYPED_CODEC with NbtOps.
    public static Tag serializeIota(Iota iota, ServerLevel level) {
        return IotaType.TYPED_CODEC.encodeStart(
                level.registryAccess().createSerializationContext(NbtOps.INSTANCE),
                iota
        ).getOrThrow();
    }

    public static Iota deserializeIota(Tag tag, ServerLevel level) {
        return IotaType.TYPED_CODEC.parse(
                level.registryAccess().createSerializationContext(NbtOps.INSTANCE),
                tag
        ).getOrThrow();
    }

    private final HashMap<UUID, PlayerState> players = new HashMap<>();

    public static HexcassettesAPI createFromNbt(CompoundTag nbt, HolderLookup.Provider provider) {
        HexcassettesAPI data = new HexcassettesAPI();
        if (nbt.contains("players", 10)) {
            CompoundTag playersTag = nbt.getCompound("players");
            for (String key : playersTag.getAllKeys()) {
                UUID uuid = UUID.fromString(key);
                PlayerState state = PlayerState.deserialize(playersTag.getCompound(key));
                state.clampToMax(HexcassettesConfig.maxCassettes());
                data.players.put(uuid, state);
            }
        }
        return data;
    }

    public static HexcassettesAPI getServerState(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(HexcassettesAPI::new, HexcassettesAPI::createFromNbt),
                        "hexcassettes");
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
        state.clampToMax(HexcassettesConfig.maxCassettes());
        return state;
    }

    public static void sendSyncPacket(ServerPlayer player) {
        PlayerState state = getPlayerState(player);
        state.clampToMax(HexcassettesConfig.maxCassettes());
        HexcassettesNetworking.sendSyncToClient(player, state);
    }

    public static void dequeueAll(ServerPlayer player) {
        PlayerState state = getPlayerState(player);
        for (int i = 0; i < state.cassettePrograms.length; i++) {
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
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerState> entry : players.entrySet()) {
            PlayerState state = entry.getValue();
            state.clampToMax(HexcassettesConfig.maxCassettes());
            playersTag.put(entry.getKey().toString(), state.serialize());
        }
        nbt.put("players", playersTag);
        return nbt;
    }
}
