package dev.cobblemonkinetics.registry;

import dev.cobblemonkinetics.CobblemonKinetics;
import dev.cobblemonkinetics.content.worker.WorkerWhistleItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CobblemonKineticsItems {

    private static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(CobblemonKinetics.MOD_ID);

    public static final DeferredItem<BlockItem> HYDRO_COUPLER = ITEMS.register(
        "hydro_coupler",
        () -> new BlockItem(CobblemonKineticsBlocks.HYDRO_COUPLER.get(), new Item.Properties())
    );

    public static final DeferredItem<WorkerWhistleItem> WORKER_WHISTLE = ITEMS.register(
        "worker_whistle",
        () -> new WorkerWhistleItem(new Item.Properties().stacksTo(1))
    );

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    private CobblemonKineticsItems() {
    }
}
