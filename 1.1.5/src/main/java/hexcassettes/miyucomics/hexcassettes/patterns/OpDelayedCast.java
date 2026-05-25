// file: patterns/OpDelayedCast.java
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

        // Guard: ensure enough iotas on the stack
        if (stack.size() < ARGS_REQUIRED) {
            throw mishap(new MishapNotEnoughArgs(ARGS_REQUIRED, stack.size()));
        }

        Iota afterArg = stack.get(stack.size() - 1);
        Iota delayArg = stack.get(stack.size() - 2);
        Iota beforeArg = stack.get(stack.size() - 3);

        if (!(afterArg instanceof ListIota afterSpell)) {
            throw mishap(new MishapInvalidIota(
                    afterArg,
                    0,
                    ListIota.TYPE.typeName()
            ));
        }

        if (!(delayArg instanceof DoubleIota delayIota)) {
            throw mishap(new MishapInvalidIota(
                    delayArg,
                    1,
                    DoubleIota.TYPE.typeName()
            ));
        }

        if (!(beforeArg instanceof ListIota beforeSpell)) {
            throw mishap(new MishapInvalidIota(
                    beforeArg,
                    2,
                    ListIota.TYPE.typeName()
            ));
        }

        double delayTicksValue = delayIota.getDouble();

        // Guard: delay must be non-negative
        if (delayTicksValue < 0) {
            throw mishap(new MishapInvalidIota(
                    delayIota,
                    0,
                    DoubleIota.TYPE.typeName()
            ));
        }

        long delayTicks = (long) delayTicksValue;

        // Pop arguments (remove top first to keep indices valid)
        List<Iota> newStack = new ArrayList<>(stack.subList(0, stack.size() - ARGS_REQUIRED));
        CastingImage newImage = image.copy(
                newStack,
                image.getParenCount(),
                image.getParenthesized(),
                image.getEscapeNext(),
                image.getOpsConsumed(),
                image.getUserData()
        );

        ServerLevel level = env.getWorld();

        DelayedSpellScheduler.executeSpell(
                beforeSpell,
                env,
                level
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
                HexEvalSounds.NORMAL_EXECUTE
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
