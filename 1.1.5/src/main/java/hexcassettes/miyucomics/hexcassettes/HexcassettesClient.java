package hexcassettes.miyucomics.hexcassettes;

import hexcassettes.miyucomics.hexcassettes.client.CassetteGuiOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = HexcassettesMain.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HexcassettesClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(CassetteGuiOverlay::onScreenRender);
        System.out.println("[Hexcassettes] Client initialized");
    }
}