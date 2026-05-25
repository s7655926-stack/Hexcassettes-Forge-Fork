package hexcassettes.miyucomics.hexcassettes;

import net.minecraftforge.common.ForgeConfigSpec;

public class HexcassettesConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec.IntValue ACTIVE_CASSETTES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("cassettes");
        ACTIVE_CASSETTES = builder
                .comment("Maximum number of cassettes that can be active at the same time.")
                .defineInRange("activeCassettes", 4, 1, HexcassettesMain.MAX_CASSETTES);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    public static int activeCassettes() {
        return ACTIVE_CASSETTES.get();
    }
}
