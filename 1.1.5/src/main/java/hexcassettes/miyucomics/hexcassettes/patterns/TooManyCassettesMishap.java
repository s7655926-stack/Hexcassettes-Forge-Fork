package hexcassettes.miyucomics.hexcassettes.patterns;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TooManyCassettesMishap extends Mishap {

    @Override
    public FrozenPigment accentColor(CastingEnvironment env, Mishap.Context errorCtx) {

        return null;
    }

    @Override
    protected Component errorMessage(CastingEnvironment env, Mishap.Context errorCtx) {

        return Component.literal("Too many cassettes.");
    }

    @Override
    public void execute(CastingEnvironment env, Mishap.Context errorCtx, List<Iota> stack) {

        // TODO: mishap logic

    }
}
