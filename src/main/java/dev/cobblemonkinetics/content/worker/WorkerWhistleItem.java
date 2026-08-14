package dev.cobblemonkinetics.content.worker;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.cobblemonkinetics.content.hydraulic.HydroCouplerBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

import java.util.UUID;
import java.util.Locale;

public final class WorkerWhistleItem extends Item {

    private static final String WORKER_ID_KEY = "WorkerPokemonId";
    private static final String OWNER_ID_KEY = "WorkerOwnerId";

    public WorkerWhistleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
        ItemStack stack,
        Player player,
        LivingEntity target,
        InteractionHand hand
    ) {
        if (!(target instanceof PokemonEntity pokemon)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (dev.cobblemonkinetics.config.CobblemonKineticsConfig.REQUIRE_PLAYER_OWNED.get()
            && !PokemonWorker.belongsTo(pokemon, player)) {
            player.displayClientMessage(
                Component.translatable("cobblemon_kinetics.message.whistle.not_owner"),
                true
            );
            return InteractionResult.FAIL;
        }

        WorkerRejection rejection = PokemonWorker.evaluate(pokemon);
        if (rejection != WorkerRejection.ELIGIBLE) {
            player.displayClientMessage(
                Component.translatable(
                    "cobblemon_kinetics.message.whistle.ineligible",
                    pokemon.getName(),
                    Component.translatable(
                        "cobblemon_kinetics.worker_rejection." + rejection.name().toLowerCase(Locale.ROOT)
                    )
                ),
                true
            );
            return InteractionResult.FAIL;
        }

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putUUID(WORKER_ID_KEY, pokemon.getPokemon().getUuid());
        tag.putUUID(OWNER_ID_KEY, player.getUUID());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        player.displayClientMessage(
            Component.translatable("cobblemon_kinetics.message.whistle.selected", pokemon.getName()),
            true
        );
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof HydroCouplerBlockEntity coupler)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            if (!coupler.clearWorker(serverPlayer.getUUID())) {
                serverPlayer.displayClientMessage(
                    Component.translatable("cobblemon_kinetics.message.whistle.not_station_owner"),
                    true
                );
                return InteractionResult.FAIL;
            }
            serverPlayer.displayClientMessage(
                Component.translatable("cobblemon_kinetics.message.whistle.cleared"),
                true
            );
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = context.getItemInHand()
            .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            .copyTag();
        if (!tag.hasUUID(WORKER_ID_KEY) || !tag.hasUUID(OWNER_ID_KEY)) {
            serverPlayer.displayClientMessage(
                Component.translatable("cobblemon_kinetics.message.whistle.select_first"),
                true
            );
            return InteractionResult.FAIL;
        }

        UUID ownerId = tag.getUUID(OWNER_ID_KEY);
        if (!serverPlayer.getUUID().equals(ownerId)) {
            serverPlayer.displayClientMessage(
                Component.translatable("cobblemon_kinetics.message.whistle.not_owner"),
                true
            );
            return InteractionResult.FAIL;
        }

        if (!coupler.assignWorker(tag.getUUID(WORKER_ID_KEY), ownerId)) {
            serverPlayer.displayClientMessage(
                Component.translatable("cobblemon_kinetics.message.whistle.assignment_blocked"),
                true
            );
            return InteractionResult.FAIL;
        }

        serverPlayer.displayClientMessage(
            Component.translatable("cobblemon_kinetics.message.whistle.assigned"),
            true
        );
        return InteractionResult.SUCCESS;
    }
}
