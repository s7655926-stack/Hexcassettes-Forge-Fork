package hexcassettes.miyucomics.hexcassettes;

import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesItems;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesPatterns;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod("hexcassettes")
public class HexcassettesMain {
    public static final String MOD_ID = "hexcassettes";
    public static final int MAX_CASSETTES = 64;
    public static final int MAX_LABEL_LENGTH = 32;

    public HexcassettesMain(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(
                ModConfig.Type.COMMON,
                HexcassettesConfig.COMMON_SPEC,
                "hexcassettes-common.toml"
        );
        HexcassettesItems.ITEMS.register(modBus);
        HexcassettesSounds.SOUND_EVENTS.register(modBus);
        modBus.addListener(HexcassettesPatterns::register);
        modBus.addListener(HexcassettesItems::addCreative);
        modBus.addListener(HexcassettesNetworking::init);
    }
}
