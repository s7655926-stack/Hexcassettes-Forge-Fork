package hexcassettes.miyucomics.hexcassettes;

import hexcassettes.miyucomics.hexcassettes.data.PlayerState;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HexcassettesMain.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            PlayerState st = HexcassettesAPI.getPlayerState(sp);
            st.clampToMax(HexcassettesMain.MAX_CASSETTES);
            HexcassettesNetworking.sendSyncToClient(sp, st);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            PlayerState st = HexcassettesAPI.getPlayerState(sp);
            st.clampToMax(HexcassettesMain.MAX_CASSETTES);
            HexcassettesNetworking.sendSyncToClient(sp, st);
        }
    }

    @SubscribeEvent
    public static void onDim(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            PlayerState st = HexcassettesAPI.getPlayerState(sp);
            st.clampToMax(HexcassettesMain.MAX_CASSETTES);
            HexcassettesNetworking.sendSyncToClient(sp, st);
        }
    }
}