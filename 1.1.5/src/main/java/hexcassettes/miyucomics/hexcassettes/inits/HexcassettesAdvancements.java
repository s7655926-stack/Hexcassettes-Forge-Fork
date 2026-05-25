package hexcassettes.miyucomics.hexcassettes.inits;

import net.minecraft.advancements.CriteriaTriggers;

public class HexcassettesAdvancements {

    public static FullArsenalCriterion FULL_ARSENAL;

    // public static QuineCriterion QUINE;
    // public static TapeWormCriterion TAPE_WORM;

    public static void init() {

        FULL_ARSENAL = CriteriaTriggers.register(new FullArsenalCriterion());

        // QUINE = CriteriaTriggers.register(new QuineCriterion());
        // TAPE_WORM = CriteriaTriggers.register(new TapeWormCriterion());
    }
}