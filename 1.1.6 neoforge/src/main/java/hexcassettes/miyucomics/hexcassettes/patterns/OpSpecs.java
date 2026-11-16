package hexcassettes.miyucomics.hexcassettes.patterns;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
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
public class OpSpecs implements SpellAction {
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
    public SpellAction.@NotNull Result execute(
            @NotNull List<? extends Iota> args,
            @NotNull CastingEnvironment env
    ) {
        if (!(env.getCastingEntity() instanceof ServerPlayer player)) {
            return noOp();
        }
        try {
            if (!(args.get(0) instanceof ListIota list)) {
                env.getWorld().playSound(null, player.blockPosition(), ModSounds.CASSETTE_FAIL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                return noOp();
            }
            if (!(args.get(1) instanceof DoubleIota delayIota)) {
                env.getWorld().playSound(null, player.blockPosition(), ModSounds.CASSETTE_FAIL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                return noOp();
            }
            int delayTicks = Math.max(1, (int) delayIota.getDouble());
            PlayerState state = HexcassettesAPI.getPlayerState(player);
            if (state.editSlot < 0 || state.editSlot >= state.ownedCassettes) {
                env.getWorld().playSound(null, player.blockPosition(), ModSounds.CASSETTE_FAIL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                return noOp();
            }
            CompoundTag hexNbt = (CompoundTag) HexcassettesAPI.serializeIota(list, player.serverLevel());
            QueuedHex queued = new QueuedHex(hexNbt, delayTicks, 0);
            state.recordToEdit(queued);
            if (player.getServer() != null) {
                HexcassettesAPI.getServerState(player.getServer()).setDirty();
            }
            HexcassettesNetworking.sendSyncToClient(player, state);
            env.getWorld().playSound(null, player.blockPosition(), ModSounds.CASSETTE_INSERT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        } catch (Exception e) {
            env.getWorld().playSound(null, player.blockPosition(), ModSounds.CASSETTE_FAIL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            e.printStackTrace();
        }
        return noOp();
    }
    private SpellAction.Result noOp() {
        return new SpellAction.Result(NoOpSpell.INSTANCE, 0L, List.<ParticleSpray>of(), 0L);
    }
}
