package dev.cobblemonkinetics.content.worker;

public final class WorkerEligibility {

    public static final int GEN_ONE_FIRST = 1;
    public static final int GEN_ONE_LAST = 151;

    public static WorkerRejection evaluate(
        WorkerFacts facts,
        boolean requirePlayerOwned,
        boolean generationOneOnly
    ) {
        if (!facts.alive()) {
            return WorkerRejection.NOT_ALIVE;
        }
        if (facts.fainted()) {
            return WorkerRejection.FAINTED;
        }
        if (facts.busy()) {
            return WorkerRejection.BUSY;
        }
        if (facts.battling()) {
            return WorkerRejection.BATTLING;
        }
        if (requirePlayerOwned && !facts.playerOwned()) {
            return WorkerRejection.NOT_PLAYER_OWNED;
        }
        if (generationOneOnly && !isGenerationOne(facts.nationalPokedexNumber())) {
            return WorkerRejection.OUTSIDE_GENERATION_ONE;
        }
        if (!facts.waterType()) {
            return WorkerRejection.MISSING_WATER_TYPE;
        }
        return WorkerRejection.ELIGIBLE;
    }

    public static boolean isGenerationOne(int nationalPokedexNumber) {
        return nationalPokedexNumber >= GEN_ONE_FIRST && nationalPokedexNumber <= GEN_ONE_LAST;
    }

    private WorkerEligibility() {
    }
}
