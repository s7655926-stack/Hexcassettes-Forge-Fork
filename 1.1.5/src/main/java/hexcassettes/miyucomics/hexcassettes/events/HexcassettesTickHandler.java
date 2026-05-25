// file: events/HexcassettesTickHandler.java
package hexcassettes.miyucomics.hexcassettes.events;

import hexcassettes.miyucomics.hexcassettes.patterns.DelayedSpellScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "hexcassettes", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HexcassettesTickHandler {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        // Только фаза END чтобы не тикать дважды
        if (event.phase != TickEvent.Phase.END) return;

        // Только серверная сторона
        if (event.side != net.minecraftforge.fml.LogicalSide.SERVER) return;

        Level level = event.level;

        if (level instanceof ServerLevel serverLevel) {
            DelayedSpellScheduler.tick(serverLevel);
        }
    }
}