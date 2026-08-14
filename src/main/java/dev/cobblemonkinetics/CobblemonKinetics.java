package dev.cobblemonkinetics;

import com.mojang.logging.LogUtils;
import dev.cobblemonkinetics.config.CobblemonKineticsConfig;
import dev.cobblemonkinetics.data.workprofile.WorkProfileManager;
import dev.cobblemonkinetics.registry.CobblemonKineticsBlockEntities;
import dev.cobblemonkinetics.registry.CobblemonKineticsBlocks;
import dev.cobblemonkinetics.registry.CobblemonKineticsItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(CobblemonKinetics.MOD_ID)
public final class CobblemonKinetics {

    public static final String MOD_ID = "cobblemon_kinetics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CobblemonKinetics(IEventBus modBus, ModContainer container) {
        CobblemonKineticsBlocks.register(modBus);
        CobblemonKineticsItems.register(modBus);
        CobblemonKineticsBlockEntities.register(modBus);
        modBus.addListener(CobblemonKinetics::addCreativeTabContents);
        NeoForge.EVENT_BUS.addListener(CobblemonKinetics::addReloadListeners);

        container.registerConfig(
            ModConfig.Type.SERVER,
            CobblemonKineticsConfig.SPEC,
            "cobblemon-kinetics-server.toml"
        );

        LOGGER.info("Create: Cobblemon Kinetics is ready to register Pokemon workers");
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(CobblemonKineticsItems.HYDRO_COUPLER.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CobblemonKineticsItems.WORKER_WHISTLE.get());
        }
    }

    private static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(WorkProfileManager.INSTANCE);
    }
}
