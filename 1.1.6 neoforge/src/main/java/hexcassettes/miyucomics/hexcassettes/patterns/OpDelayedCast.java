package hexcassettes.miyucomics.hexcassettes.patterns;

import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import at.petrak.hexcasting.api.utils.TreeList;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class OpDelayedCast implements Action {
    private static final int ARGS_REQUIRED = 3;

    @Override
    public @NotNull OperationResult operate(
            CastingEnvironment env,
            CastingImage image,
            SpellContinuation continuation
    ) {
        List<Iota> stack = image.getStack();
        if (stack.size() < ARGS_REQUIRED) {
            throw mishap(new MishapNotEnoughArgs(ARGS_REQUIRED, stack.size()));
        }
        Iota afterArg = stack.get(stack.size() - 1);
        Iota delayArg = stack.get(stack.size() - 2);
        Iota beforeArg = stack.get(stack.size() - 3);
        if (!(afterArg instanceof ListIota afterSpell)) {
            throw mishap(MishapInvalidIota.ofType(afterArg, 0, "list"));
        }
        if (!(delayArg instanceof DoubleIota delayIota)) {
            throw mishap(MishapInvalidIota.ofType(delayArg, 1, "double"));
        }
        if (!(beforeArg instanceof ListIota beforeSpell)) {
            throw mishap(MishapInvalidIota.ofType(beforeArg, 2, "list"));
        }
        double delayTicksValue = delayIota.getDouble();
        if (delayTicksValue < 0) {
            throw mishap(MishapInvalidIota.ofType(delayIota, 0, "double"));
        }
        long delayTicks = (long) delayTicksValue;
        List<Iota> newStack = new ArrayList<>(stack.subList(0, stack.size() - ARGS_REQUIRED));
        CastingImage newImage = image.copy(
                TreeList.from(newStack),
                image.getParenCount(),
                image.getParenthesized(),
                image.getEscapeNext(),
                image.getSimulateNext(),
                image.getOpsConsumed(),
                image.getComponents()
        );
        ServerLevel level = env.getWorld();
        DelayedSpellScheduler.executeSpell(
                beforeSpell,
                env,
                level,
                DelayedSpellScheduler.soundAllowedFor(env)
        );
        DelayedSpellScheduler.schedule(
                level,
                afterSpell,
                env,
                delayTicks,
                1,
                0
        );
        return new OperationResult(
                newImage,
                List.<OperatorSideEffect>of(),
                continuation,
                HexEvalSounds.NORMAL_EXECUTE.get()
        );
    }

    private static RuntimeException mishap(Mishap mishap) {
        return sneakyThrow(mishap);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> RuntimeException sneakyThrow(Throwable throwable) throws E {
        throw (E) throwable;
    }
}
