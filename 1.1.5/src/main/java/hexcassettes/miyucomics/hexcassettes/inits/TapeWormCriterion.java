package hexcassettes.miyucomics.hexcassettes.inits;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TapeWormCriterion extends SimpleCriterionTrigger<TapeWormCriterion.Condition> {

    public static final ResourceLocation ID =
            new ResourceLocation("hexcassettes", "tape_worm");

    @Override
    protected Condition createInstance(
            JsonObject jsonObject,
            ContextAwarePredicate lootContextPredicate,
            DeserializationContext advancementEntityPredicateDeserializer
    ) {
        return new Condition();
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, condition -> true);
    }

    public static class Condition extends AbstractCriterionTriggerInstance {

        public Condition() {
            super(ID, ContextAwarePredicate.ANY);
        }
    }
}