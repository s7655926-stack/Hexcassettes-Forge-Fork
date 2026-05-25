package hexcassettes.miyucomics.hexcassettes.data;

import hexcassettes.miyucomics.hexcassettes.HexcassettesAPI;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;

public class PlayerState {

    public int ownedCassettes = 0;
    public int editSlot = -1;
    public final List<Integer> playingSlots = new ArrayList<>();

    private static final int MAX_PLAYING = 4;
    public final QueuedHex[] cassettePrograms = new QueuedHex[10];

    public void tick(ServerPlayer player) {
        List<Integer> toRemove = new ArrayList<>();

        for (int slot : playingSlots) {
            if (slot < 0 || slot >= cassettePrograms.length) {
                toRemove.add(slot);
                continue;
            }

            QueuedHex hex = cassettePrograms[slot];
            if (hex == null) {
                toRemove.add(slot);
                continue;
            }

            QueuedHex.CastResult result = hex.tickAndMaybeCast(player);

            if (result != QueuedHex.CastResult.WAITING) {
                System.out.println("[Hexcassettes] CAST RESULT = " + result + " (slot=" + slot + ")");
            }

            switch (result) {
                case SUCCESS -> hex.resetTimer();
                case FINISHED -> toRemove.add(slot);
                case NO_MEDIA -> {
                    player.playSound(
                            (SoundEvent) HexcassettesSounds.CASSETTE_EJECT.get(),
                            1.0F, 1.0F
                    );
                    toRemove.add(slot);
                }
                case ERROR -> {
                    player.playSound(
                            (SoundEvent) HexcassettesSounds.CASSETTE_FAIL.get(),
                            1.0F, 1.0F
                    );
                    toRemove.add(slot);
                }
            }
        }

        if (!toRemove.isEmpty()) {
            playingSlots.removeAll(toRemove);
            HexcassettesNetworking.sendSyncToClient(player, this);
            if (player.getServer() != null) {
                HexcassettesAPI.getServerState(player.getServer()).setDirty();
            }
        }
    }

    public void recordToEdit(QueuedHex queued) {
        if (editSlot >= 0 && editSlot < cassettePrograms.length) {
            cassettePrograms[editSlot] = queued;
            System.out.println("[Hexcassettes] recordToEdit: записано на слот " + editSlot);
        } else {
            System.out.println("[Hexcassettes] recordToEdit: нет активного editSlot → отмена");
        }
    }

    public void selectPlay(int slot) {
        if (slot < 0 || slot >= ownedCassettes) return;

        if (playingSlots.contains(slot)) {
            playingSlots.remove((Integer) slot);
        } else {
            if (playingSlots.size() >= MAX_PLAYING) {
                playingSlots.remove(0);
            }
            playingSlots.add(slot);
            if (cassettePrograms[slot] != null) {
                cassettePrograms[slot].resetTimer();
            }
        }
        System.out.println("[Hexcassettes] playingSlots = " + playingSlots);
    }

    public void selectEdit(int slot) {
        if (slot >= 0 && slot < ownedCassettes) {
            editSlot = (editSlot == slot) ? -1 : slot;
        } else {
            editSlot = -1;
        }

        if (editSlot >= 0 && playingSlots.contains(editSlot)) {
            playingSlots.remove((Integer) editSlot);
        }
    }

    public void clearEditSlot() {
        if (editSlot >= 0 && editSlot < cassettePrograms.length) {
            cassettePrograms[editSlot] = null;
        }
        editSlot = -1;
    }

    public void consumeTopCassette() {
        if (ownedCassettes <= 0) return;

        int top = ownedCassettes - 1;
        if (top < cassettePrograms.length) {
            cassettePrograms[top] = null;
        }

        ownedCassettes--;

        if (editSlot >= ownedCassettes) {
            editSlot = -1;
        }

        playingSlots.removeIf(s -> s >= ownedCassettes);
        System.out.println("[Hexcassettes] consumeTopCassette → owned=" + ownedCassettes);
    }

    public void addCassette() {
        if (ownedCassettes < 10) {
            ownedCassettes++;
            System.out.println("[Hexcassettes] addCassette → owned=" + ownedCassettes);
        }
    }

    public void clampToMax(int max) {
        ownedCassettes = Math.max(0, Math.min(ownedCassettes, max));

        if (editSlot >= ownedCassettes) {
            editSlot = -1;
        }

        playingSlots.removeIf(s -> s >= ownedCassettes || s < 0);
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("owned", ownedCassettes);
        tag.putInt("edit", editSlot);

        ListTag playList = new ListTag();
        for (int slot : playingSlots) {
            CompoundTag s = new CompoundTag();
            s.putInt("slot", slot);
            playList.add(s);
        }
        tag.put("playing", playList);

        CompoundTag programs = new CompoundTag();
        for (int i = 0; i < cassettePrograms.length; i++) {
            if (cassettePrograms[i] != null) {
                programs.put(String.valueOf(i), cassettePrograms[i].serialize());
            }
        }
        tag.put("programs", programs);

        return tag;
    }

    public static PlayerState deserialize(CompoundTag tag) {
        PlayerState state = new PlayerState();
        state.ownedCassettes = tag.getInt("owned");
        state.editSlot = tag.contains("edit") ? tag.getInt("edit") : -1;

        if (tag.contains("playing", 9)) {
            ListTag list = tag.getList("playing", 10);
            for (int i = 0; i < list.size(); i++) {
                state.playingSlots.add(list.getCompound(i).getInt("slot"));
            }
        }

        if (tag.contains("programs", 10)) {
            CompoundTag programs = tag.getCompound("programs");
            for (String key : programs.getAllKeys()) {
                try {
                    int idx = Integer.parseInt(key);
                    if (idx >= 0 && idx < state.cassettePrograms.length) {
                        state.cassettePrograms[idx] = QueuedHex.deserialize(programs.getCompound(key));
                    }
                } catch (Exception ignored) {}
            }
        }

        return state;
    }
}