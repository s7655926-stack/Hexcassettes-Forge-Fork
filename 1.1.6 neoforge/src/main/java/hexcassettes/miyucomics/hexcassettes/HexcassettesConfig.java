package hexcassettes.miyucomics.hexcassettes;

import net.neoforged.neoforge.common.ModConfigSpec;

public class HexcassettesConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.IntValue ACTIVE_CASSETTES;
    public static final ModConfigSpec.IntValue MAX_CASSETTES;
    public static final ModConfigSpec.IntValue MIN_SOUND_INTERVAL;
    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("cassettes");
        ACTIVE_CASSETTES = builder
                .comment("Maximum number of cassettes that can be active (playing) at the same time.",
                        "Default: 4, hard limit: 64.")
                .defineInRange("activeCassettes", 4, 1, HexcassettesMain.MAX_CASSETTES);
        MAX_CASSETTES = builder
                .comment("Maximum number of cassettes a player can own (by eating cassette items).",
                        "Default: 10, hard limit: 64.")
                .defineInRange("maxCassettes", 10, 1, HexcassettesMain.MAX_CASSETTES);
        builder.pop();
        builder.push("sound");
        MIN_SOUND_INTERVAL = builder
                .comment("Minimum number of ticks between cassette sounds (the cast sound and cassette_loop).",
                        "If casts fire faster than this, the sounds are skipped so they don't spam:",
                        "e.g. with a 1-tick interval the sound plays once per this many ticks,",
                        "while rare casts (this value or slower) always play their sound.",
                        "Default: 60 (= 3 seconds). Set to 1 for no limit (sounds on every cast).")
                .defineInRange("minSoundInterval", 60, 1, 24000);
        builder.pop();
        COMMON_SPEC = builder.build();
    }

    public static int activeCassettes() {
        return ACTIVE_CASSETTES.get();
    }

    public static int maxCassettes() {
        return Math.min(MAX_CASSETTES.get(), HexcassettesMain.MAX_CASSETTES);
    }

    public static int minSoundInterval() {
        return MIN_SOUND_INTERVAL.get();
    }
}
