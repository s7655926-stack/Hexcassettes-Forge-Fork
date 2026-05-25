package hexcassettes.miyucomics.hexcassettes.patterns;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import hexcassettes.miyucomics.hexcassettes.HexcassettesAPI;
import hexcassettes.miyucomics.hexcassettes.ModSounds;
import hexcassettes.miyucomics.hexcassettes.data.PlayerState;
import hexcassettes.miyucomics.hexcassettes.data.QueuedHex;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OpForetell implements SpellAction {

    @Override
    public int getArgc() {
        return 2;
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

        try {
            if (!(args.get(0) instanceof DoubleIota delayIota)) {
                env.getWorld().playSound(null, player.blockPosition(), ModSounds.CASSETTE_FAIL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                System.out.println("[Hexcassettes] OpForetell: arg 0 is not a number!");
                return noOp();
            }
            if (!(args.get(1) instanceof ListIota hex)) {
                env.getWorld().playSound(null, player.blockPosition(), ModSounds.CASSETTE_FAIL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                System.out.println("[Hexcassettes] OpForetell: arg 1 is not a list!");
                return noOp();
            }

            int delayTicks = Math.max(1, (int) delayIota.getDouble());
            PlayerState state = HexcassettesAPI.getPlayerState(player);

            // Ищем первый свободный слот
            int freeSlot = -1;
            for (int i = 0; i < state.ownedCassettes; i++) {
                if (state.cassettePrograms[i] == null) {
                    freeSlot = i;
                    break;
                }
            }

            // Если свободных нет — используем editSlot
            if (freeSlot == -1) {
                freeSlot = state.editSlot;
            }

            if (freeSlot < 0 || freeSlot >= state.ownedCassettes) {
                env.getWorld().playSound(null, player.blockPosition(), ModSounds.CASSETTE_FAIL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                System.out.println("[Hexcassettes] OpForetell: no available slot!");
                return noOp();
            }

            CompoundTag hexNbt = IotaType.serialize(hex);
            QueuedHex queued = new QueuedHex(hexNbt, delayTicks, 0); // 0 = бесконечно
            state.cassettePrograms[freeSlot] = queued;

            // Если этот слот сейчас играет — снимаем с воспроизведения
            if (state.playingSlots.contains(freeSlot)) {
                state.playingSlots.remove((Integer) freeSlot);
            }

            if (player.getServer() != null) {
                HexcassettesAPI.getServerState(player.getServer()).setDirty();
            }
            HexcassettesNetworking.sendSyncToClient(player, state);

            System.out.println("[Hexcassettes] Foretell: slot=" + freeSlot + " delay=" + delayTicks);
            env.getWorld().playSound(null, player.blockPosition(), ModSounds.CASSETTE_INSERT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

        } catch (Exception e) {
            env.getWorld().playSound(null, player.blockPosition(), ModSounds.CASSETTE_FAIL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            System.out.println("[Hexcassettes] OpForetell FAILED: " + e);
            e.printStackTrace();
        }

        return noOp();
    }

    private SpellAction.Result noOp() {
        return new SpellAction.Result(NoOpSpell.INSTANCE, 0L, List.<ParticleSpray>of(), 0L);
    }
}