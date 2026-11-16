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

public class OpRepeatingCast implements Action {
    private static final int ARGS_REQUIRED = 3;
    private static final int MAX_REPEATS = 1000;
    private static final long MIN_INTERVAL_TICKS = 1L;

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
        Iota repeatsArg = stack.get(stack.size() - 1);
        Iota intervalArg = stack.get(stack.size() - 2);
        Iota spellArg = stack.get(stack.size() - 3);
        if (!(repeatsArg instanceof DoubleIota repeatsIota)) {
            throw mishap(MishapInvalidIota.ofType(repeatsArg, 0, "double"));
        }
        if (!(intervalArg instanceof DoubleIota intervalIota)) {
            throw mishap(MishapInvalidIota.ofType(intervalArg, 1, "double"));
        }
        if (!(spellArg instanceof ListIota spellIota)) {
            throw mishap(MishapInvalidIota.ofType(spellArg, 2, "list"));
        }
        int repeats = (int) repeatsIota.getDouble();
        double intervalTicksValue = intervalIota.getDouble();
        if (repeats <= 0 || repeats > MAX_REPEATS) {
            throw mishap(MishapInvalidIota.ofType(repeatsIota, 0, "double"));
        }
        if (intervalTicksValue < 0) {
            throw mishap(MishapInvalidIota.ofType(intervalIota, 1, "double"));
        }
        long intervalTicks = Math.max((long) intervalTicksValue, MIN_INTERVAL_TICKS);
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
        DelayedSpellScheduler.schedule(
                level,
                spellIota,
                env,
                intervalTicks,
                repeats,
                intervalTicks
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
