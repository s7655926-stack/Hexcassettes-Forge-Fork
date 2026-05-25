package hexcassettes.miyucomics.hexcassettes.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Этот миксин больше не используется для тика кассет.
 * Вся логика воспроизведения перенесена в HexcassettesEvents.onPlayerTick(),
 * который работает гарантированно без миксин-плагина.
 *
 * Класс оставлен пустым, чтобы файл hexcassettes.mixins.json не сломался,
 * если миксин-плагин всё-таки подцепится.
 */
@Mixin(Player.class)
public class PlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void hexcassettes$noop(CallbackInfo ci) {
        // Тик кассет теперь делается через PlayerTickEvent в HexcassettesEvents.
        // Этот метод оставлен пустым специально.
    }
}