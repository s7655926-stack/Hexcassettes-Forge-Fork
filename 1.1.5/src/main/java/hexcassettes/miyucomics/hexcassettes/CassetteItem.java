package hexcassettes.miyucomics.hexcassettes;

import hexcassettes.miyucomics.hexcassettes.data.PlayerState;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class CassetteItem extends Item {

    public CassetteItem() {
        super(new Item.Properties().stacksTo(64));
    }

    /**
     * Время использования предмета (в тиках).
     * 32 тика = ~1.6 секунды (анимация EAT).
     */
    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    /**
     * Анимация использования — как поедание еды.
     */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    /**
     * Вызывается при нажатии ПКМ.
     * Воспроизводит звук вставки кассеты и начинает анимацию использования.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Звук только на сервере, чтобы услышали все игроки рядом
        if (!level.isClientSide) {
            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    (SoundEvent) HexcassettesSounds.CASSETTE_INSERT.get(),
                    SoundSource.PLAYERS,
                    1.0F, 1.0F
            );
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    /**
     * Вызывается, когда анимация использования завершена (прошло getUseDuration тиков).
     * Добавляет одну кассету игроку, если не превышен лимит (10 штук).
     */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) {
            return stack;
        }

        // Вся логика — только на сервере
        if (!level.isClientSide) {
            PlayerState state = HexcassettesAPI.getPlayerState(player);
            state.clampToMax(10);

            if (state.ownedCassettes >= 10) {
                // Лимит достигнут — играем звук ошибки и не тратим предмет
                level.playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        (SoundEvent) HexcassettesSounds.CASSETTE_FAIL.get(),
                        SoundSource.PLAYERS,
                        1.0F, 1.0F
                );
                return stack;
            }

            // Добавляем кассету
            state.ownedCassettes++;
            state.clampToMax(10);

            // Помечаем SavedData как изменённые, чтобы сохранить на диск
            if (player.getServer() != null) {
                HexcassettesAPI.getServerState(player.getServer()).setDirty();
            }

            // Синхронизируем состояние с клиентом
            if (player instanceof ServerPlayer sp) {
                HexcassettesNetworking.sendSyncToClient(sp, state);
            }

            // Звук успешной вставки
            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    (SoundEvent) HexcassettesSounds.CASSETTE_LOOP.get(),
                    SoundSource.PLAYERS,
                    0.8F, 1.0F
            );
        }

        // Уменьшаем стак на 1, если не креатив
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return stack;
    }

    /**
     * Звук начала использования (воспроизводится клиентом автоматически).
     */
    @Override
    public SoundEvent getEatingSound() {
        return (SoundEvent) HexcassettesSounds.CASSETTE_INSERT.get();
    }
}