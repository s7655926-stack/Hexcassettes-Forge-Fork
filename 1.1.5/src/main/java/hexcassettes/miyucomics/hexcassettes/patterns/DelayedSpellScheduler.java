// file: patterns/DelayedSpellScheduler.java
package hexcassettes.miyucomics.hexcassettes.patterns;

import at.petrak.hexcasting.api.casting.SpellList;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DelayedSpellScheduler {

    public static class ScheduledTask {
        public final ServerLevel level;
        public final ListIota spell;
        public final CastingEnvironment env;
        public final long intervalTicks;
        public final int totalRepeats;
        public int doneRepeats;
        public long nextTick;

        public ScheduledTask(
                ServerLevel level,
                ListIota spell,
                CastingEnvironment env,
                long firstDelayTicks,
                int totalRepeats,
                long intervalTicks,
                long currentTick
        ) {
            this.level = level;
            this.spell = spell;
            this.env = env;
            this.intervalTicks = intervalTicks;
            this.totalRepeats = totalRepeats;
            this.doneRepeats = 0;
            this.nextTick = currentTick + firstDelayTicks;
        }

        public boolean isDone() {
            return doneRepeats >= totalRepeats;
        }
    }

    private static final List<ScheduledTask> TASKS = new ArrayList<>();

    public static void schedule(
            ServerLevel level,
            ListIota spell,
            CastingEnvironment env,
            long firstDelay,
            int repeats,
            long intervalTicks
    ) {
        long currentTick = level.getGameTime();
        TASKS.add(new ScheduledTask(
                level,
                spell,
                env,
                firstDelay,
                repeats,
                intervalTicks,
                currentTick
        ));
    }

    public static void tick(ServerLevel level) {
        long now = level.getGameTime();
        List<ScheduledTask> readyTasks = new ArrayList<>();

        Iterator<ScheduledTask> it = TASKS.iterator();
        while (it.hasNext()) {
            ScheduledTask task = it.next();

            if (task.level != level) continue;
            if (now < task.nextTick) continue;

            task.doneRepeats++;
            readyTasks.add(task);

            if (task.isDone()) {
                it.remove();
            } else {
                task.nextTick = now + task.intervalTicks;
            }
        }

        for (ScheduledTask task : readyTasks) {
            executeSpell(task.spell, task.env, level);
        }
    }

    public static void executeSpell(
            ListIota spell,
            CastingEnvironment env,
            ServerLevel level
    ) {
        try {
            var caster = env.getCastingEntity();

            if (caster == null) {
                System.out.println("[Hexcassettes][DelayedCast] Caster is null, skipping.");
                return;
            }

            level.playSound(
                    null,
                    caster.getX(), caster.getY(), caster.getZ(),
                    (SoundEvent) HexcassettesSounds.CASSETTE_LOOP.get(),
                    SoundSource.PLAYERS,
                    0.8F, 1.0F
            );

            List<Iota> iotas = new ArrayList<>();
            SpellList.SpellListIterator iter = spell.getList().iterator();
            while (iter.hasNext()) {
                iotas.add(iter.next());
            }

            if (iotas.isEmpty()) {
                System.out.println("[Hexcassettes][DelayedCast] Spell is empty, skipping.");
                return;
            }

            at.petrak.hexcasting.api.casting.eval.vm.CastingVM vm =
                    at.petrak.hexcasting.api.casting.eval.vm.CastingVM.empty(env);

            vm.queueExecuteAndWrapIotas(iotas, level);

        } catch (Exception e) {
            System.out.println("[Hexcassettes][DelayedCast] Failed to execute spell: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
