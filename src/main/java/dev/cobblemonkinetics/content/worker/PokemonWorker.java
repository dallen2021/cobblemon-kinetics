package dev.cobblemonkinetics.content.worker;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.cobblemonkinetics.config.CobblemonKineticsConfig;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class PokemonWorker {

    public static WorkerFacts facts(PokemonEntity entity) {
        boolean waterType = false;
        for (ElementalType type : entity.getPokemon().getTypes()) {
            if (type == ElementalTypes.WATER) {
                waterType = true;
                break;
            }
        }

        return new WorkerFacts(
            entity.getPokemon().getSpecies().getNationalPokedexNumber(),
            waterType,
            entity.getPokemon().isPlayerOwned() || entity.getOwnerUUID() != null,
            entity.isAlive() && !entity.isRemoved(),
            entity.getPokemon().getCurrentHealth() <= 0,
            entity.isBusy(),
            entity.isBattling()
        );
    }

    public static WorkerRejection evaluate(PokemonEntity entity) {
        return WorkerEligibility.evaluate(
            facts(entity),
            CobblemonKineticsConfig.REQUIRE_PLAYER_OWNED.get(),
            CobblemonKineticsConfig.GEN_ONE_ONLY.get()
        );
    }

    public static boolean belongsTo(PokemonEntity entity, Player player) {
        UUID owner = entity.getOwnerUUID();
        return player.getUUID().equals(owner)
            || player.getUUID().equals(entity.getPokemon().getOwnerUUID())
            || entity.getPokemon().belongsTo(player);
    }

    private PokemonWorker() {
    }
}
