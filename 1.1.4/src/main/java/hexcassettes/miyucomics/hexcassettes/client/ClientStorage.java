package hexcassettes.miyucomics.hexcassettes.client;

import java.util.HashSet;
import java.util.Set;

public class ClientStorage {

    public static int ownedCassettes = 0;
    public static int editSlot = -1;
    public static final Set<Integer> playingSlots = new HashSet<>();

    public static boolean isEditSelected(int slot) {
        return editSlot == slot;
    }

    public static boolean isPlaySelected(int slot) {
        return playingSlots.contains(slot);
    }

    public static void clear() {
        ownedCassettes = 0;
        editSlot = -1;
        playingSlots.clear();
    }
}