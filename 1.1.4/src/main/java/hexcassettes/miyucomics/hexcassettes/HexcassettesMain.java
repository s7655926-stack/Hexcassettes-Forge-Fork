package hexcassettes.miyucomics.hexcassettes;

import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesItems;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesPatterns;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("hexcassettes")
public class HexcassettesMain {

    public static final String MOD_ID = "hexcassettes";
    public static final int MAX_CASSETTES = 10;
    public static final int MAX_LABEL_LENGTH = 32;

    public HexcassettesMain() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        HexcassettesItems.ITEMS.register(modBus);
        HexcassettesSounds.SOUND_EVENTS.register(modBus);
        modBus.addListener(HexcassettesPatterns::register);
        modBus.addListener(HexcassettesItems::addCreative);
        HexcassettesNetworking.init();
        System.out.println("[Hexcassettes] Main initialized");
    }
}