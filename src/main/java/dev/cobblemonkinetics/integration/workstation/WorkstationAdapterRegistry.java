package dev.cobblemonkinetics.integration.workstation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry of reviewed workstation adapters. The first vertical slice only
 * validates Hydro data; live Hydro behavior remains on its existing code path.
 */
public final class WorkstationAdapterRegistry {

    public static final String HYDRO_ADAPTER_ID = "cobblemon_kinetics:hydro_coupler";
    public static final String HYDRO_REGISTRY_ID = "cobblemon_kinetics:hydro_coupler";

    private static final Map<String, WorkstationAdapterDescriptor> ADAPTERS = createAdapters();

    public static Optional<WorkstationAdapterDescriptor> find(String adapterId) {
        return Optional.ofNullable(ADAPTERS.get(adapterId));
    }

    public static Map<String, WorkstationAdapterDescriptor> all() {
        return ADAPTERS;
    }

    private static Map<String, WorkstationAdapterDescriptor> createAdapters() {
        Map<String, WorkstationAdapterDescriptor> adapters = new LinkedHashMap<>();
        register(adapters, new WorkstationAdapterDescriptor(HYDRO_ADAPTER_ID, Set.of(HYDRO_REGISTRY_ID)));
        return Map.copyOf(adapters);
    }

    private static void register(
        Map<String, WorkstationAdapterDescriptor> adapters,
        WorkstationAdapterDescriptor descriptor
    ) {
        if (adapters.putIfAbsent(descriptor.id(), descriptor) != null) {
            throw new IllegalStateException("Duplicate workstation adapter: " + descriptor.id());
        }
    }

    private WorkstationAdapterRegistry() {
    }
}
