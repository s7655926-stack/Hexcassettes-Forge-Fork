package hexcassettes.miyucomics.hexcassettes.inits;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class FullArsenalCriterion extends SimpleCriterionTrigger<FullArsenalCriterion.Condition> {

    public static final ResourceLocation ID =
            new ResourceLocation("hexcassettes", "full_arsenal");

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
