package hexcassettes.miyucomics.hexcassettes.patterns;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import hexcassettes.miyucomics.hexcassettes.HexcassettesAPI;
import hexcassettes.miyucomics.hexcassettes.ModSounds;
import hexcassettes.miyucomics.hexcassettes.data.PlayerState;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
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
    public SpellAction.@NotNull Result execute(
            @NotNull List<? extends Iota> args,
            @NotNull CastingEnvironment env
    ) {
        if (!(env.getCastingEntity() instanceof ServerPlayer player)) {
            return noOp();
        }
        PlayerState state = HexcassettesAPI.getPlayerState(player);
        for (int i = 0; i < state.cassettePrograms.length; i++) {
            state.cassettePrograms[i] = null;
        }
        state.playingSlots.clear();
        state.editSlot = -1;
        if (player.getServer() != null) {
            HexcassettesAPI.getServerState(player.getServer()).setDirty();
        }
        HexcassettesNetworking.sendSyncToClient(player, state);
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
