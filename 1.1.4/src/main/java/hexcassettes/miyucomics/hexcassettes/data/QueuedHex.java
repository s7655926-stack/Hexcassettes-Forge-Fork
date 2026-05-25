package hexcassettes.miyucomics.hexcassettes.data;

import at.petrak.hexcasting.api.casting.SpellList;
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class QueuedHex {

    private final CompoundTag hexNbt;
    private final int totalDelay;
    public int remainingTicks;
    public int remainingRepeats;

    public QueuedHex(CompoundTag hexNbt, int delayTicks, int repeats) {
        this.hexNbt = hexNbt;
        this.totalDelay = Math.max(1, delayTicks);
        this.remainingTicks = this.totalDelay;
        this.remainingRepeats = Math.max(0, repeats);
    }

    public void resetTimer() {
        this.remainingTicks = totalDelay;
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("hex", hexNbt);
        tag.putInt("totalDelay", totalDelay);
        tag.putInt("remainingTicks", remainingTicks);
        tag.putInt("remainingRepeats", remainingRepeats);
        return tag;
    }

    public static QueuedHex deserialize(CompoundTag tag) {
        CompoundTag hex = tag.getCompound("hex");
        int totalDelay = Math.max(1, tag.getInt("totalDelay"));
        int remainingTicks = tag.getInt("remainingTicks");
        int repeats = tag.contains("remainingRepeats") ? tag.getInt("remainingRepeats") : 0;

        QueuedHex q = new QueuedHex(hex, totalDelay, repeats);
        q.remainingTicks = remainingTicks > 0 ? remainingTicks : totalDelay;
        return q;
    }

    public CastResult tickAndMaybeCast(ServerPlayer player) {
        remainingTicks--;
        if (remainingTicks > 0) {
            return CastResult.WAITING;
        }

        if (remainingRepeats > 0) {
            remainingRepeats--;
            if (remainingRepeats == 0) {
                return CastResult.FINISHED;
            }
        }

        try {
            ServerLevel level = player.serverLevel();
            Iota deserialized = IotaType.deserialize(hexNbt, level);

            if (!(deserialized instanceof ListIota listIota)) {
                return CastResult.ERROR;
            }

            List<Iota> iotas = new ArrayList<>();
            SpellList.SpellListIterator iter = listIota.getList().iterator();
            while (iter.hasNext()) {
                iotas.add(iter.next());
            }

            if (iotas.isEmpty()) {
                return CastResult.ERROR;
            }

            if (!hasAnyMedia(player)) {
                return CastResult.NO_MEDIA;
            }

            StaffCastEnv env = new StaffCastEnv(player, InteractionHand.MAIN_HAND);
            CastingVM vm = new CastingVM(new CastingImage(), env);
            vm.queueExecuteAndWrapIotas(iotas, level);
            resetTimer();
            return CastResult.SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();
            return CastResult.ERROR;
        }
    }

    private boolean hasAnyMedia(ServerPlayer player) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (key != null) {
                    String id = key.toString();
                    if (id.contains("amethyst") || id.contains("media")
                            || id.contains("thought") || id.contains("charged")
                            || id.contains("battery") || id.contains("focus")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public enum CastResult {
        WAITING,
        SUCCESS,
        NO_MEDIA,
        ERROR,
        FINISHED
    }
}