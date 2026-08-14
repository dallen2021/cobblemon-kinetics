package dev.cobblemonkinetics.data.workprofile;

import java.util.List;

public record WorkProfileDefinition(
    int formatVersion,
    String id,
    String title,
    int priority,
    Status status,
    Selector selector,
    Constraints constraints,
    Workstation workstation,
    Contribution contribution,
    String publicRationale
) {

    public enum Status {
        EXPERIMENTAL,
        APPROVED,
        DEPRECATED
    }

    public sealed interface Selector permits TypeSelector, PokemonSelector {
    }

    public record TypeSelector(List<String> types, DexRange nationalDex) implements Selector {
        public TypeSelector {
            types = List.copyOf(types);
        }
    }

    public record PokemonSelector(List<String> pokemon) implements Selector {
        public PokemonSelector {
            pokemon = List.copyOf(pokemon);
        }
    }

    public record DexRange(int min, int max) {
    }

    public record Constraints(
        boolean requiresOwner,
        boolean mustBeAlive,
        boolean mustNotBeFainted,
        boolean mustNotBeBattling,
        boolean mustBeIdle
    ) {
    }

    public record Workstation(
        String adapterId,
        List<String> registryIds,
        String requiredAttachmentTag,
        double radius
    ) {
        public Workstation {
            registryIds = List.copyOf(registryIds);
        }
    }

    public record Contribution(
        String mode,
        int rpm,
        int capacityPerRpm,
        double efficiencyMultiplier
    ) {
    }
}
