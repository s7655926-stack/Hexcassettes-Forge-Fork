package hexcassettes.miyucomics.hexcassettes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, "hexcassettes");

    public static final RegistryObject<SoundEvent> CASSETTE_INSERT =
            register("cassette_insert");
    public static final RegistryObject<SoundEvent> CASSETTE_EJECT =
            register("cassette_eject");
    public static final RegistryObject<SoundEvent> CASSETTE_FAIL =
            register("cassette_fail");
    public static final RegistryObject<SoundEvent> CASSETTE_LOOP =
            register("cassette_loop");

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = new ResourceLocation("hexcassettes", name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}