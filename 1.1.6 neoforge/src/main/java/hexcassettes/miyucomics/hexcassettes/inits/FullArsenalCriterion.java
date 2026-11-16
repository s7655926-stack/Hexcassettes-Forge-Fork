package hexcassettes.miyucomics.hexcassettes.inits;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
public class FullArsenalCriterion extends SimpleCriterionTrigger<FullArsenalCriterion.Condition> {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("hexcassettes", "full_arsenal");
    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }
    public void trigger(ServerPlayer player) {
        this.trigger(player, condition -> true);
    }
    public static class Condition implements SimpleInstance {
        public static final Codec<Condition> CODEC = Codec.unit(new Condition());
        @Override
        public Optional<ContextAwarePredicate> player() {
            return Optional.empty();
        }
    }
}
