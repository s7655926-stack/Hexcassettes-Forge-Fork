package hexcassettes.miyucomics.hexcassettes.patterns;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import hexcassettes.miyucomics.hexcassettes.HexcassettesAPI;
import hexcassettes.miyucomics.hexcassettes.ModSounds;
import hexcassettes.miyucomics.hexcassettes.data.PlayerState;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OpKillAll implements SpellAction {

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

        // Очищаем ВСЕ программы кассет
        for (int i = 0; i < state.cassettePrograms.length; i++) {
            state.cassettePrograms[i] = null;
        }

        // Останавливаем ВСЕ воспроизводящиеся кассеты.
        // repeat_spell тоже останавливается этим, потому что он хранится в playingSlots.
        state.playingSlots.clear();

        // Сбрасываем редактируемую кассету
        state.editSlot = -1;

        if (player.getServer() != null) {
            HexcassettesAPI.getServerState(player.getServer()).setDirty();
        }

        HexcassettesNetworking.sendSyncToClient(player, state);

        System.out.println("[Hexcassettes] Killed all cassette programs");

        env.getWorld().playSound(
                null,
                player.blockPosition(),
                ModSounds.CASSETTE_FAIL.get(),
                SoundSource.PLAYERS,
                1.0f,
                1.0f
        );

        return noOp();
    }

    private SpellAction.Result noOp() {
        return new SpellAction.Result(NoOpSpell.INSTANCE, 0L, List.<ParticleSpray>of(), 0L);
    }
}