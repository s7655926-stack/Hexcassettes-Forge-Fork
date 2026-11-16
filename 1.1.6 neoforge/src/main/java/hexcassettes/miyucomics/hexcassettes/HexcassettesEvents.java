package hexcassettes.miyucomics.hexcassettes;

import hexcassettes.miyucomics.hexcassettes.data.PlayerState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "hexcassettes")
public class HexcassettesEvents {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide) {
            Player player = event.getEntity();
            if (player instanceof ServerPlayer serverPlayer) {
                PlayerState state = HexcassettesAPI.getPlayerState(serverPlayer);
                state.clampToMax(HexcassettesConfig.maxCassettes());
                state.tick(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onPlaySoundAtEntity(PlayLevelSoundEvent.AtEntity event) {
        if (shouldSuppress(event)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlaySoundAtPosition(PlayLevelSoundEvent.AtPosition event) {
        if (shouldSuppress(event)) {
            event.setCanceled(true);
        }
    }

    private static boolean shouldSuppress(PlayLevelSoundEvent event) {
        if (!CassetteCastFlags.isCasting()) {
            return false;
        }
        if (CassetteCastFlags.isSoundAllowed()) {
            return false;
        }
        if (event.getLevel().isClientSide()) {
            return false;
        }
        SoundEvent sound = event.getSound() != null ? event.getSound().value() : null;
        if (sound == null) {
            return false;
        }
        ResourceLocation key = BuiltInRegistries.SOUND_EVENT.getKey(sound);
        return key != null && "hexcasting".equals(key.getNamespace());
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
