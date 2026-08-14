package dev.cobblemonkinetics.integration.workstation;

import java.util.Set;

/**
 * Describes the narrow code-side boundary that a published work profile may
 * target. Data can tune a reviewed adapter, but it cannot name arbitrary game
 * internals or an unrelated block.
 */
public record WorkstationAdapterDescriptor(String id, Set<String> registryIds) {

    public WorkstationAdapterDescriptor {
        registryIds = Set.copyOf(registryIds);
    }

    public boolean supportsRegistryId(String registryId) {
        return registryIds.contains(registryId);
    }
}
