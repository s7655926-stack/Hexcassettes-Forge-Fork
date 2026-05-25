package hexcassettes.miyucomics.hexcassettes.inits;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexRegistries;
import hexcassettes.miyucomics.hexcassettes.patterns.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;

public class HexcassettesPatterns {

    public static void register(RegisterEvent event) {
        event.register(HexRegistries.ACTION, helper -> {
            registerPattern(helper, "enqueue",  "wqwqwqwqwqwaw",   HexDir.EAST, new OpEnqueue(),  "wwwww");
            registerPattern(helper, "dequeue",  "wewewewewewdwa",  HexDir.EAST, new OpDequeue(),  "wwwwwa");
            registerPattern(helper, "killall",  "wewewewewewdwe",  HexDir.EAST, new OpKillAll(),  "wwwwwq");
            registerPattern(helper, "specs",    "wqwqwqwqwqwawd",  HexDir.EAST, new OpSpecs(),    "wwwwww");
            registerPattern(helper, "free",     "wqwqwqwqwqwawe",  HexDir.EAST, new OpFree(),     "wwwwwwa");
            registerPattern(helper, "inspect",  "wewewewewewdwq",  HexDir.EAST, new OpInspect(),  "wwwwwwq");
            registerPattern(helper, "foretell", "wewewewewewdwd",  HexDir.EAST, new OpForetell(), "wwwwwww");
            registerPattern(helper, "delayed_cast",   "wqwqwqwqwqwawdq", HexDir.EAST, new OpDelayedCast(),   "wwwwwwd");
            registerPattern(helper, "repeating_cast", "wqwqwqwqwqwaweq", HexDir.EAST, new OpRepeatingCast(), "wwwwwwe");
        });
    }

    private static void registerPattern(
            RegisterEvent.RegisterHelper<ActionRegistryEntry> helper,
            String name,
            String pattern,
            HexDir startDir,
            Action action,
            String fallbackPattern
    ) {
        ResourceLocation id = new ResourceLocation("hexcassettes", name);
        HexPattern hexPattern;

        try {
            hexPattern = HexPattern.fromAngles(pattern, startDir);
            System.out.println("[Hexcassettes] Using custom pattern for " + id + ": " + pattern);
        } catch (Exception e) {
            System.out.println("[Hexcassettes] Pattern " + pattern + " for " + id + " is invalid!");
            System.out.println("[Hexcassettes] Falling back to safe pattern: " + fallbackPattern);
            hexPattern = HexPattern.fromAngles(fallbackPattern, startDir);
        }

        helper.register(id, new ActionRegistryEntry(hexPattern, action));
        System.out.println("[Hexcassettes] Registered pattern: " + id);
    }
}
