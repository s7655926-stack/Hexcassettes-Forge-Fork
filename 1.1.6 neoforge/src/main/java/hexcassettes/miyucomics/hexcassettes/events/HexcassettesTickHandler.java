package hexcassettes.miyucomics.hexcassettes.events;

import hexcassettes.miyucomics.hexcassettes.patterns.DelayedSpellScheduler;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = "hexcassettes")
public class HexcassettesTickHandler {
    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            DelayedSpellScheduler.tick(serverLevel);
        }
    }
}
