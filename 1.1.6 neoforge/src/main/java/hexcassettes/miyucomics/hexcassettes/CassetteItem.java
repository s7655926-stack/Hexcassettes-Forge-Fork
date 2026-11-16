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

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
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

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) {
            return stack;
        }
        if (!level.isClientSide) {
            int max = HexcassettesConfig.maxCassettes();
            PlayerState state = HexcassettesAPI.getPlayerState(player);
            state.clampToMax(max);
            if (state.ownedCassettes >= max) {
                level.playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        (SoundEvent) HexcassettesSounds.CASSETTE_FAIL.get(),
                        SoundSource.PLAYERS,
                        1.0F, 1.0F
                );
                return stack;
            }
            state.ownedCassettes++;
            state.clampToMax(max);
            if (player.getServer() != null) {
                HexcassettesAPI.getServerState(player.getServer()).setDirty();
            }
            if (player instanceof ServerPlayer sp) {
                HexcassettesNetworking.sendSyncToClient(sp, state);
            }
            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    (SoundEvent) HexcassettesSounds.CASSETTE_LOOP.get(),
                    SoundSource.PLAYERS,
                    0.8F, 1.0F
            );
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public SoundEvent getEatingSound() {
        return (SoundEvent) HexcassettesSounds.CASSETTE_INSERT.get();
    }
}
