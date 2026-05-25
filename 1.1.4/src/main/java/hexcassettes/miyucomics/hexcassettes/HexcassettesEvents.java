package hexcassettes.miyucomics.hexcassettes;

import hexcassettes.miyucomics.hexcassettes.data.PlayerState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "hexcassettes")
public class HexcassettesEvents {

    private static int debugCounter = 0;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == Phase.END && !event.side.isClient()) {
            Player player = event.player;
            if (player instanceof ServerPlayer serverPlayer) {
                PlayerState state = HexcassettesAPI.getPlayerState(serverPlayer);
                state.clampToMax(10);

                debugCounter++;
                if (debugCounter >= 100) {
                    debugCounter = 0;
                    boolean has = false;
                    for (int i = 0; i < 10; i++) {
                        if (state.cassettePrograms[i] != null) {
                            has = true;
                            break;
                        }
                    }
                    System.out.println("[Hexcassettes][EVENT] owned=" + state.ownedCassettes
                            + " edit=" + state.editSlot
                            + " play=" + state.playingSlots
                            + " hasPrograms=" + has);
                }

                state.tick(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            HexcassettesAPI.sendSyncPacket(sp);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            HexcassettesAPI.sendSyncPacket(sp);
        }
    }

    @SubscribeEvent
    public static void onChangedDim(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            HexcassettesAPI.sendSyncPacket(sp);
        }
    }
}