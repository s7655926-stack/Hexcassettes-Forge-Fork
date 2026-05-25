package hexcassettes.miyucomics.hexcassettes.patterns;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import hexcassettes.miyucomics.hexcassettes.HexcassettesAPI;
import hexcassettes.miyucomics.hexcassettes.HexcassettesMain;
import hexcassettes.miyucomics.hexcassettes.ModSounds;
import hexcassettes.miyucomics.hexcassettes.data.PlayerState;
import hexcassettes.miyucomics.hexcassettes.data.QueuedHex;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OpInspect implements SpellAction {

    @Override
    public int getArgc() {
        return 0;
    }

    @Override
    public boolean awardsCastingStat(@NotNull CastingEnvironment env) {
        return false;
    }

    @Override
    public boolean hasCastingSound(@NotNull CastingEnvironment env) {
        return false;
    }

    @Override
    public @NotNull OperationResult operate(
            @NotNull CastingEnvironment env,
            @NotNull CastingImage image,
            @NotNull SpellContinuation continuation
    ) {
        return SpellAction.DefaultImpls.operate(this, env, image, continuation);
    }

    @Override
    public SpellAction.@NotNull Result execute(
            @NotNull List<? extends Iota> args,
            @NotNull CastingEnvironment env
    ) {
        return executeWithUserdata(args, env, new CompoundTag());
    }

    @Override
    public SpellAction.@NotNull Result executeWithUserdata(
            @NotNull List<? extends Iota> args,
            @NotNull CastingEnvironment env,
            @NotNull CompoundTag userData
    ) {
        if (!(env.getCastingEntity() instanceof ServerPlayer player)) {
            return noOp();
        }

        PlayerState state = HexcassettesAPI.getPlayerState(player);

        int owned = Math.min(state.ownedCassettes, state.cassettePrograms.length);

        // Общая информация
        player.displayClientMessage(
                Component.literal("[Hexcassettes] Cassettes: " + state.ownedCassettes + "/" + HexcassettesMain.MAX_CASSETTES),
                false
        );

        // Edit slot
        if (state.editSlot >= 0 && state.editSlot < owned) {
            QueuedHex queued = state.cassettePrograms[state.editSlot];
            String status = queued != null ? "has program" : "empty";

            player.displayClientMessage(
                    Component.literal("[Hexcassettes] Edit slot #" + state.editSlot + ": " + status),
                    false
            );
        } else {
            player.displayClientMessage(
                    Component.literal("[Hexcassettes] Edit slot: none"),
                    false
            );
        }

        // Playing slots
        if (state.playingSlots.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("[Hexcassettes] Playing slots: none"),
                    false
            );
        } else {
            player.displayClientMessage(
                    Component.literal("[Hexcassettes] Playing slots (" + state.playingSlots.size() + "):"),
                    false
            );

            for (Integer playSlotObj : state.playingSlots) {
                if (playSlotObj == null) {
                    continue;
                }

                int playSlot = playSlotObj;

                QueuedHex queued = playSlot >= 0 && playSlot < state.cassettePrograms.length
                        ? state.cassettePrograms[playSlot]
                        : null;

                String status = queued != null ? "playing" : "empty/stopped";

                player.displayClientMessage(
                        Component.literal("  → Slot #" + playSlot + ": " + status),
                        false
                );
            }
        }

        // Все слоты
        player.displayClientMessage(
                Component.literal("[Hexcassettes] All slots:"),
                false
        );

        for (int i = 0; i < owned; i++) {
            QueuedHex queued = state.cassettePrograms[i];

            String status = queued != null ? "recorded" : "empty";
            String markers = "";

            if (i == state.editSlot) {
                markers += " [EDIT]";
            }

            if (state.playingSlots.contains(i)) {
                markers += " [PLAY]";
            }

            player.displayClientMessage(
                    Component.literal("  Slot #" + i + ": " + status + markers),
                    false
            );
        }

        // Активные программы кассет.
        // repeat_spell теперь тоже хранится как QueuedHex в cassettePrograms + playingSlots.
        int activePrograms = 0;

        for (Integer slotObj : state.playingSlots) {
            if (slotObj == null) {
                continue;
            }

            int slot = slotObj;

            if (slot >= 0 && slot < state.cassettePrograms.length && state.cassettePrograms[slot] != null) {
                activePrograms++;
            }
        }

        if (activePrograms <= 0) {
            player.displayClientMessage(
                    Component.literal("[Hexcassettes] Active cassette programs: none"),
                    false
            );
        } else {
            player.displayClientMessage(
                    Component.literal("[Hexcassettes] Active cassette programs: " + activePrograms),
                    false
            );
        }

        env.getWorld().playSound(
                null,
                player.blockPosition(),
                ModSounds.CASSETTE_LOOP.get(),
                SoundSource.PLAYERS,
                0.5f,
                1.5f
        );

        return noOp();
    }

    private SpellAction.Result noOp() {
        return new SpellAction.Result(NoOpSpell.INSTANCE, 0L, List.<ParticleSpray>of(), 0L);
    }
}