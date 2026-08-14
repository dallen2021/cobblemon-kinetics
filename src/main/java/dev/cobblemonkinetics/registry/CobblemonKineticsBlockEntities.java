package dev.cobblemonkinetics.registry;

import dev.cobblemonkinetics.CobblemonKinetics;
import dev.cobblemonkinetics.content.hydraulic.HydroCouplerBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CobblemonKineticsBlockEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CobblemonKinetics.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HydroCouplerBlockEntity>> HYDRO_COUPLER =
        BLOCK_ENTITIES.register(
            "hydro_coupler",
            () -> BlockEntityType.Builder.of(
                HydroCouplerBlockEntity::new,
                CobblemonKineticsBlocks.HYDRO_COUPLER.get()
            ).build(null)
        );

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }

    private CobblemonKineticsBlockEntities() {
    }
}
