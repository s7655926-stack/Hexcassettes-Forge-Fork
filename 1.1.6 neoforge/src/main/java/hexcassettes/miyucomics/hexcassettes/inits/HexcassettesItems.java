package hexcassettes.miyucomics.hexcassettes.inits;

import hexcassettes.miyucomics.hexcassettes.CassetteItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HexcassettesItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, "hexcassettes");

    public static final DeferredHolder<Item, Item> CASSETTE =
            ITEMS.register("cassette", CassetteItem::new);

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (CreativeModeTabs.TOOLS_AND_UTILITIES.equals(event.getTabKey())) {
            event.accept(CASSETTE.get());
        }
    }
}
