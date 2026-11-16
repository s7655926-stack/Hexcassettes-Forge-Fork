package hexcassettes.miyucomics.hexcassettes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, "hexcassettes");

    public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_INSERT =
            register("cassette_insert");
    public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_EJECT =
            register("cassette_eject");
    public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_FAIL =
            register("cassette_fail");
    public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_LOOP =
            register("cassette_loop");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("hexcassettes", name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
