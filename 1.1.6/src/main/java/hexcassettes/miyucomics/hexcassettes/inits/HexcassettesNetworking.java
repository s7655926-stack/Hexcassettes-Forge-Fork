package hexcassettes.miyucomics.hexcassettes.inits;
import hexcassettes.miyucomics.hexcassettes.HexcassettesAPI;
import hexcassettes.miyucomics.hexcassettes.HexcassettesConfig;
import hexcassettes.miyucomics.hexcassettes.client.ClientStorage;
import hexcassettes.miyucomics.hexcassettes.data.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
public class HexcassettesNetworking {
    private static final String VERSION = "1";
    private static int ID = 0;
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("hexcassettes", "main"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals
    );
    public static void init() {
        CHANNEL.registerMessage(ID++, SyncPacket.class,
                SyncPacket::encode, SyncPacket::decode, SyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID++, SelectEditPacket.class,
                SelectEditPacket::encode, SelectEditPacket::decode, SelectEditPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID++, SelectPlayPacket.class,
                SelectPlayPacket::encode, SelectPlayPacket::decode, SelectPlayPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID++, ConsumeTopPacket.class,
                ConsumeTopPacket::encode, ConsumeTopPacket::decode, ConsumeTopPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID++, ClearEditPacket.class,
                ClearEditPacket::encode, ClearEditPacket::decode, ClearEditPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID++, ToggleMutePacket.class,
                ToggleMutePacket::encode, ToggleMutePacket::decode, ToggleMutePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
    public static void sendSyncToClient(ServerPlayer player, PlayerState state) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncPacket(state.ownedCassettes, state.editSlot, new ArrayList<>(state.playingSlots),
                        new ArrayList<>(state.mutedSlots))
        );
    }
    public static void sendSelectEdit(int slot) {
        CHANNEL.sendToServer(new SelectEditPacket(slot));
    }
    public static void sendSelectPlay(int slot) {
        CHANNEL.sendToServer(new SelectPlayPacket(slot));
    }
    public static void sendConsumeTop() {
        CHANNEL.sendToServer(new ConsumeTopPacket());
    }
    public static void sendClearEdit() {
        CHANNEL.sendToServer(new ClearEditPacket());
    }
    public static void sendToggleMute(int slot) {
        CHANNEL.sendToServer(new ToggleMutePacket(slot));
    }
    public static class SyncPacket {
        private final int ownedCassettes;
        private final int editSlot;
        private final List<Integer> playingSlots;
        private final List<Integer> mutedSlots;
        public SyncPacket(int ownedCassettes, int editSlot, List<Integer> playingSlots, List<Integer> mutedSlots) {
            this.ownedCassettes = ownedCassettes;
            this.editSlot = editSlot;
            this.playingSlots = playingSlots;
            this.mutedSlots = mutedSlots;
        }
        public static void encode(SyncPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.ownedCassettes);
            buf.writeInt(msg.editSlot);
            buf.writeInt(msg.playingSlots.size());
            for (int slot : msg.playingSlots) {
                buf.writeInt(slot);
            }
            buf.writeInt(msg.mutedSlots.size());
            for (int slot : msg.mutedSlots) {
                buf.writeInt(slot);
            }
        }
        public static SyncPacket decode(FriendlyByteBuf buf) {
            int owned = buf.readInt();
            int edit = buf.readInt();
            int playCount = buf.readInt();
            List<Integer> playing = new ArrayList<>();
            for (int i = 0; i < playCount; i++) {
                playing.add(buf.readInt());
            }
            int mutedCount = buf.readInt();
            List<Integer> muted = new ArrayList<>();
            for (int i = 0; i < mutedCount; i++) {
                muted.add(buf.readInt());
            }
            return new SyncPacket(owned, edit, playing, muted);
        }
        public static void handle(SyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ClientStorage.ownedCassettes = msg.ownedCassettes;
                ClientStorage.editSlot = msg.editSlot;
                ClientStorage.mutedSlots.clear();
                if (msg.mutedSlots != null) {
                    ClientStorage.mutedSlots.addAll(msg.mutedSlots);
                }
                ClientStorage.playingSlots.clear();
                if (msg.playingSlots != null) {
                    ClientStorage.playingSlots.addAll(msg.playingSlots);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
    public static class SelectEditPacket {
        private final int slot;
        public SelectEditPacket(int slot) {
            this.slot = slot;
        }
        public static void encode(SelectEditPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.slot);
        }
        public static SelectEditPacket decode(FriendlyByteBuf buf) {
            return new SelectEditPacket(buf.readInt());
        }
        public static void handle(SelectEditPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.getServer() != null) {
                    PlayerState state = HexcassettesAPI.getPlayerState(player);
                    state.clampToMax(HexcassettesConfig.maxCassettes());
                    state.selectEdit(msg.slot);
                    HexcassettesAPI.getServerState(player.getServer()).setDirty();
                    HexcassettesNetworking.sendSyncToClient(player, state);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
    public static class SelectPlayPacket {
        private final int slot;
        public SelectPlayPacket(int slot) {
            this.slot = slot;
        }
        public static void encode(SelectPlayPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.slot);
        }
        public static SelectPlayPacket decode(FriendlyByteBuf buf) {
            return new SelectPlayPacket(buf.readInt());
        }
        public static void handle(SelectPlayPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.getServer() != null) {
                    PlayerState state = HexcassettesAPI.getPlayerState(player);
                    state.clampToMax(HexcassettesConfig.maxCassettes());
                    state.selectPlay(msg.slot);
                    HexcassettesAPI.getServerState(player.getServer()).setDirty();
                    HexcassettesNetworking.sendSyncToClient(player, state);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
    public static class ConsumeTopPacket {
        public static void encode(ConsumeTopPacket msg, FriendlyByteBuf buf) {}
        public static ConsumeTopPacket decode(FriendlyByteBuf buf) {
            return new ConsumeTopPacket();
        }
        public static void handle(ConsumeTopPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.getServer() != null) {
                    PlayerState state = HexcassettesAPI.getPlayerState(player);
                    state.clampToMax(HexcassettesConfig.maxCassettes());
                    state.consumeTopCassette();
                    HexcassettesAPI.getServerState(player.getServer()).setDirty();
                    HexcassettesNetworking.sendSyncToClient(player, state);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
    public static class ClearEditPacket {
        public static void encode(ClearEditPacket msg, FriendlyByteBuf buf) {}
        public static ClearEditPacket decode(FriendlyByteBuf buf) {
            return new ClearEditPacket();
        }
        public static void handle(ClearEditPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.getServer() != null) {
                    PlayerState state = HexcassettesAPI.getPlayerState(player);
                    state.clampToMax(HexcassettesConfig.maxCassettes());
                    state.clearEditSlot();
                    HexcassettesAPI.getServerState(player.getServer()).setDirty();
                    HexcassettesNetworking.sendSyncToClient(player, state);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
    public static class ToggleMutePacket {
        private final int slot;
        public ToggleMutePacket(int slot) {
            this.slot = slot;
        }
        public static void encode(ToggleMutePacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.slot);
        }
        public static ToggleMutePacket decode(FriendlyByteBuf buf) {
            return new ToggleMutePacket(buf.readInt());
        }
        public static void handle(ToggleMutePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.getServer() != null) {
                    PlayerState state = HexcassettesAPI.getPlayerState(player);
                    state.clampToMax(HexcassettesConfig.maxCassettes());
                    if (msg.slot < 0 || msg.slot >= HexcassettesConfig.maxCassettes()) {
                        return;
                    }
                    if (state.mutedSlots.contains(msg.slot)) {
                        state.mutedSlots.remove(msg.slot);
                    } else {
                        state.mutedSlots.add(msg.slot);
                    }
                    HexcassettesAPI.getServerState(player.getServer()).setDirty();
                    HexcassettesNetworking.sendSyncToClient(player, state);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
