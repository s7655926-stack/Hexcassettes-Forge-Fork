package hexcassettes.miyucomics.hexcassettes.inits;

import hexcassettes.miyucomics.hexcassettes.HexcassettesAPI;
import hexcassettes.miyucomics.hexcassettes.HexcassettesConfig;
import hexcassettes.miyucomics.hexcassettes.client.ClientStorage;
import hexcassettes.miyucomics.hexcassettes.data.PlayerState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class HexcassettesNetworking {
    private static final String VERSION = "1";

    public static void init(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(SyncPacket.TYPE, SyncPacket.STREAM_CODEC, SyncPacket::handle);
        registrar.playToServer(SelectEditPacket.TYPE, SelectEditPacket.STREAM_CODEC, SelectEditPacket::handle);
        registrar.playToServer(SelectPlayPacket.TYPE, SelectPlayPacket.STREAM_CODEC, SelectPlayPacket::handle);
        registrar.playToServer(ConsumeTopPacket.TYPE, ConsumeTopPacket.STREAM_CODEC, ConsumeTopPacket::handle);
        registrar.playToServer(ClearEditPacket.TYPE, ClearEditPacket.STREAM_CODEC, ClearEditPacket::handle);
        registrar.playToServer(ToggleMutePacket.TYPE, ToggleMutePacket.STREAM_CODEC, ToggleMutePacket::handle);
    }

    public static void sendSyncToClient(ServerPlayer player, PlayerState state) {
        PacketDistributor.sendToPlayer(player, new SyncPacket(state.ownedCassettes, state.editSlot,
                new ArrayList<>(state.playingSlots), new ArrayList<>(state.mutedSlots)));
    }

    public static void sendSelectEdit(int slot) {
        PacketDistributor.sendToServer(new SelectEditPacket(slot));
    }

    public static void sendSelectPlay(int slot) {
        PacketDistributor.sendToServer(new SelectPlayPacket(slot));
    }

    public static void sendConsumeTop() {
        PacketDistributor.sendToServer(new ConsumeTopPacket());
    }

    public static void sendClearEdit() {
        PacketDistributor.sendToServer(new ClearEditPacket());
    }

    public static void sendToggleMute(int slot) {
        PacketDistributor.sendToServer(new ToggleMutePacket(slot));
    }

    public record SyncPacket(
            int ownedCassettes,
            int editSlot,
            List<Integer> playingSlots,
            List<Integer> mutedSlots
    ) implements CustomPacketPayload {
        public static final Type<SyncPacket> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("hexcassettes", "sync"));
        public static final StreamCodec<FriendlyByteBuf, SyncPacket> STREAM_CODEC =
                StreamCodec.of(SyncPacket::encode, SyncPacket::decode);

        private static void encode(FriendlyByteBuf buf, SyncPacket msg) {
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

        private static SyncPacket decode(FriendlyByteBuf buf) {
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

        public static void handle(SyncPacket msg, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
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
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SelectEditPacket(int slot) implements CustomPacketPayload {
        public static final Type<SelectEditPacket> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("hexcassettes", "select_edit"));
        public static final StreamCodec<FriendlyByteBuf, SelectEditPacket> STREAM_CODEC =
                StreamCodec.of(SelectEditPacket::encode, SelectEditPacket::decode);

        private static void encode(FriendlyByteBuf buf, SelectEditPacket msg) {
            buf.writeInt(msg.slot);
        }

        private static SelectEditPacket decode(FriendlyByteBuf buf) {
            return new SelectEditPacket(buf.readInt());
        }

        public static void handle(SelectEditPacket msg, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player && player.getServer() != null) {
                    PlayerState state = HexcassettesAPI.getPlayerState(player);
                    state.clampToMax(HexcassettesConfig.maxCassettes());
                    state.selectEdit(msg.slot);
                    HexcassettesAPI.getServerState(player.getServer()).setDirty();
                    HexcassettesNetworking.sendSyncToClient(player, state);
                }
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SelectPlayPacket(int slot) implements CustomPacketPayload {
        public static final Type<SelectPlayPacket> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("hexcassettes", "select_play"));
        public static final StreamCodec<FriendlyByteBuf, SelectPlayPacket> STREAM_CODEC =
                StreamCodec.of(SelectPlayPacket::encode, SelectPlayPacket::decode);

        private static void encode(FriendlyByteBuf buf, SelectPlayPacket msg) {
            buf.writeInt(msg.slot);
        }

        private static SelectPlayPacket decode(FriendlyByteBuf buf) {
            return new SelectPlayPacket(buf.readInt());
        }

        public static void handle(SelectPlayPacket msg, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player && player.getServer() != null) {
                    PlayerState state = HexcassettesAPI.getPlayerState(player);
                    state.clampToMax(HexcassettesConfig.maxCassettes());
                    state.selectPlay(msg.slot);
                    HexcassettesAPI.getServerState(player.getServer()).setDirty();
                    HexcassettesNetworking.sendSyncToClient(player, state);
                }
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ConsumeTopPacket() implements CustomPacketPayload {
        public static final Type<ConsumeTopPacket> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("hexcassettes", "consume_top"));
        public static final StreamCodec<FriendlyByteBuf, ConsumeTopPacket> STREAM_CODEC =
                StreamCodec.unit(new ConsumeTopPacket());

        public static void handle(ConsumeTopPacket msg, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player && player.getServer() != null) {
                    PlayerState state = HexcassettesAPI.getPlayerState(player);
                    state.clampToMax(HexcassettesConfig.maxCassettes());
                    state.consumeTopCassette();
                    HexcassettesAPI.getServerState(player.getServer()).setDirty();
                    HexcassettesNetworking.sendSyncToClient(player, state);
                }
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClearEditPacket() implements CustomPacketPayload {
        public static final Type<ClearEditPacket> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("hexcassettes", "clear_edit"));
        public static final StreamCodec<FriendlyByteBuf, ClearEditPacket> STREAM_CODEC =
                StreamCodec.unit(new ClearEditPacket());

        public static void handle(ClearEditPacket msg, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player && player.getServer() != null) {
                    PlayerState state = HexcassettesAPI.getPlayerState(player);
                    state.clampToMax(HexcassettesConfig.maxCassettes());
                    state.clearEditSlot();
                    HexcassettesAPI.getServerState(player.getServer()).setDirty();
                    HexcassettesNetworking.sendSyncToClient(player, state);
                }
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ToggleMutePacket(int slot) implements CustomPacketPayload {
        public static final Type<ToggleMutePacket> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("hexcassettes", "toggle_mute"));
        public static final StreamCodec<FriendlyByteBuf, ToggleMutePacket> STREAM_CODEC =
                StreamCodec.of(ToggleMutePacket::encode, ToggleMutePacket::decode);

        private static void encode(FriendlyByteBuf buf, ToggleMutePacket msg) {
            buf.writeInt(msg.slot);
        }

        private static ToggleMutePacket decode(FriendlyByteBuf buf) {
            return new ToggleMutePacket(buf.readInt());
        }

        public static void handle(ToggleMutePacket msg, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player && player.getServer() != null) {
                    PlayerState state = HexcassettesAPI.getPlayerState(player);
                    state.clampToMax(HexcassettesConfig.maxCassettes());
                    if (msg.slot < 0 || msg.slot >= HexcassettesConfig.maxCassettes()) {
                        return;
                    }
                    if (state.mutedSlots.contains(msg.slot)) {
                        state.mutedSlots.remove((Integer) msg.slot);
                    } else {
                        state.mutedSlots.add(msg.slot);
                    }
                    HexcassettesAPI.getServerState(player.getServer()).setDirty();
                    HexcassettesNetworking.sendSyncToClient(player, state);
                }
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
