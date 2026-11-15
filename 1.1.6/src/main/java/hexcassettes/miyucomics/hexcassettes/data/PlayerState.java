package hexcassettes.miyucomics.hexcassettes.data;
import hexcassettes.miyucomics.hexcassettes.HexcassettesAPI;
import hexcassettes.miyucomics.hexcassettes.HexcassettesConfig;
import hexcassettes.miyucomics.hexcassettes.HexcassettesMain;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
public class PlayerState {
    public int ownedCassettes = 0;
    public int editSlot = -1;
    public final Set<Integer> mutedSlots = new HashSet<>();
    public long lastSoundTick = Long.MIN_VALUE / 2;
    public final List<Integer> playingSlots = new ArrayList<>();
    public final QueuedHex[] cassettePrograms = new QueuedHex[HexcassettesMain.MAX_CASSETTES];
    public boolean shouldPlaySounds(ServerPlayer player, int slot) {
        if (slot >= 0 && mutedSlots.contains(slot)) {
            return false;
        }
        long now = player.level().getGameTime();
        if (now - lastSoundTick < HexcassettesConfig.minSoundInterval()) {
            return false;
        }
        lastSoundTick = now;
        return true;
    }
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
            boolean playSounds = shouldPlaySounds(player, slot);
            QueuedHex.CastResult result = hex.tickAndMaybeCast(player, playSounds);
            switch (result) {
                case SUCCESS -> {
                    if (playSounds) {
                        player.level().playSound(
                                null,
                                player.getX(), player.getY(), player.getZ(),
                                (SoundEvent) HexcassettesSounds.CASSETTE_LOOP.get(),
                                SoundSource.PLAYERS,
                                0.8F, 1.0F
                        );
                    }
                    hex.resetTimer();
                }
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
        } else {
        }
    }
    public void selectPlay(int slot) {
        if (slot < 0 || slot >= ownedCassettes) return;
        if (playingSlots.contains(slot)) {
            playingSlots.remove((Integer) slot);
        } else {
            int maxPlaying = HexcassettesConfig.activeCassettes();
            while (playingSlots.size() >= maxPlaying) {
                playingSlots.remove(0);
            }
            playingSlots.add(slot);
            if (cassettePrograms[slot] != null) {
                cassettePrograms[slot].resetTimer();
            }
        }
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
    }
    public void addCassette() {
        if (ownedCassettes < HexcassettesConfig.maxCassettes()) {
            ownedCassettes++;
        }
    }
    public void clampToMax(int max) {
        ownedCassettes = Math.max(0, Math.min(ownedCassettes, max));
        if (editSlot >= ownedCassettes) {
            editSlot = -1;
        }
        playingSlots.removeIf(s -> s >= ownedCassettes || s < 0);
        mutedSlots.removeIf(s -> s >= ownedCassettes || s < 0);
        int maxPlaying = HexcassettesConfig.activeCassettes();
        while (playingSlots.size() > maxPlaying) {
            playingSlots.remove(0);
        }
    }
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("owned", ownedCassettes);
        tag.putInt("edit", editSlot);
        ListTag mutedList = new ListTag();
        for (int slot : mutedSlots) {
            CompoundTag s = new CompoundTag();
            s.putInt("slot", slot);
            mutedList.add(s);
        }
        tag.put("muted", mutedList);
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
        if (tag.contains("muted", 9)) {
            ListTag mutedList = tag.getList("muted", 10);
            for (int i = 0; i < mutedList.size(); i++) {
                state.mutedSlots.add(mutedList.getCompound(i).getInt("slot"));
            }
        }
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
