package hexcassettes.miyucomics.hexcassettes;

import hexcassettes.miyucomics.hexcassettes.client.CassetteGuiOverlay;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = HexcassettesMain.MOD_ID, value = net.neoforged.api.distmarker.Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class HexcassettesClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(CassetteGuiOverlay::onScreenRender);
        NeoForge.EVENT_BUS.addListener(CassetteGuiOverlay::onMouseScrolled);
    }
}
