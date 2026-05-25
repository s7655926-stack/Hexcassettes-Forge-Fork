package hexcassettes.miyucomics.hexcassettes;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class CassetteCastEnv extends PlayerBasedCastEnv {

    private final ServerPlayer caster;
    private final InteractionHand castingHand;

    // ✅ Флаг: маны не хватило
    public boolean failedFromNoMedia = false;

    public CassetteCastEnv(ServerPlayer caster, InteractionHand castingHand) {
        super(caster, castingHand);
        this.caster = caster;
        this.castingHand = castingHand;
    }

    @Override
    public long extractMediaEnvironment(long costLeft, boolean sentienize) {
        if (costLeft <= 0) return 0;

        long remaining = costLeft;

        // ✅ Проходим по всему инвентарю игрока
        for (int i = 0; i < caster.getInventory().getContainerSize(); i++) {
            ItemStack stack = caster.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            // ✅ Ищем предметы с маной (амулеты, сосуды, аметисты)
            ADMediaHolder holder = IXplatAbstractions.INSTANCE.findMediaHolder(stack);
            if (holder == null) continue;
            if (!holder.canProvide()) continue;

            // ✅ Забираем ману (simulate=false → реально тратим)
            long extracted = holder.withdrawMedia(remaining, false);
            remaining -= extracted;

            if (remaining <= 0) return 0;
        }

        // ✅ Если осталось > 0 → маны не хватило
        if (remaining > 0) {
            failedFromNoMedia = true;
        }

        return remaining;
    }

    @Override
    public InteractionHand getCastingHand() {
        return this.castingHand;
    }

    @Override
    public FrozenPigment getPigment() {
        return null;
    }

    @Override
    public void produceParticles(ParticleSpray particles, FrozenPigment pigment) {
        // пустое — кассеты кастуют без частиц
    }
}