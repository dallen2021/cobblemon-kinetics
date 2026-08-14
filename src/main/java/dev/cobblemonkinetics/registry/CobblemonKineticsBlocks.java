package dev.cobblemonkinetics.registry;

import dev.cobblemonkinetics.CobblemonKinetics;
import dev.cobblemonkinetics.content.hydraulic.HydroCouplerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CobblemonKineticsBlocks {

    private static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(CobblemonKinetics.MOD_ID);

    public static final DeferredBlock<HydroCouplerBlock> HYDRO_COUPLER = BLOCKS.register(
        "hydro_coupler",
        () -> new HydroCouplerBlock(
            BlockBehaviour.Properties.of()
                .strength(3.0F, 6.0F)
                .sound(SoundType.COPPER)
                .requiresCorrectToolForDrops()
        )
    );

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    private CobblemonKineticsBlocks() {
    }
}
