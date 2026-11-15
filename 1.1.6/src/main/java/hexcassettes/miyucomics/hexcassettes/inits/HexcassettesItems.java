package hexcassettes.miyucomics.hexcassettes.inits;
import hexcassettes.miyucomics.hexcassettes.CassetteItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
public class HexcassettesItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, "hexcassettes");
    public static final RegistryObject<Item> CASSETTE =
            ITEMS.register("cassette", CassetteItem::new);
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (CreativeModeTabs.TOOLS_AND_UTILITIES.equals(event.getTabKey())) {
            event.accept(CASSETTE.get());
        }
    }
}
