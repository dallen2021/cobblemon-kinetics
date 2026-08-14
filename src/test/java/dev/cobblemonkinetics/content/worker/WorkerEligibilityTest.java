package dev.cobblemonkinetics.content.worker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkerEligibilityTest {

    private static final WorkerFacts ELIGIBLE_SQUIRTLE =
        new WorkerFacts(7, true, true, true, false, false, false);

    @Test
    void acceptsHealthyOwnedGenerationOneWaterPokemon() {
        assertEquals(
            WorkerRejection.ELIGIBLE,
            WorkerEligibility.evaluate(ELIGIBLE_SQUIRTLE, true, true)
        );
    }

    @Test
    void rejectsEachUnsafeWorkerState() {
        assertEquals(
            WorkerRejection.NOT_ALIVE,
            WorkerEligibility.evaluate(new WorkerFacts(7, true, true, false, false, false, false), true, true)
        );
        assertEquals(
            WorkerRejection.FAINTED,
            WorkerEligibility.evaluate(new WorkerFacts(7, true, true, true, true, false, false), true, true)
        );
        assertEquals(
            WorkerRejection.BUSY,
            WorkerEligibility.evaluate(new WorkerFacts(7, true, true, true, false, true, false), true, true)
        );
        assertEquals(
            WorkerRejection.BATTLING,
            WorkerEligibility.evaluate(new WorkerFacts(7, true, true, true, false, false, true), true, true)
        );
    }

    @Test
    void rejectsWildNonWaterAndLaterGenerationPokemon() {
        assertEquals(
            WorkerRejection.NOT_PLAYER_OWNED,
            WorkerEligibility.evaluate(new WorkerFacts(7, true, false, true, false, false, false), true, true)
        );
        assertEquals(
            WorkerRejection.MISSING_WATER_TYPE,
            WorkerEligibility.evaluate(new WorkerFacts(25, false, true, true, false, false, false), true, true)
        );
        assertEquals(
            WorkerRejection.OUTSIDE_GENERATION_ONE,
            WorkerEligibility.evaluate(new WorkerFacts(158, true, true, true, false, false, false), true, true)
        );
    }

    @Test
    void generationBoundaryIsInclusive() {
        assertTrue(WorkerEligibility.isGenerationOne(1));
        assertTrue(WorkerEligibility.isGenerationOne(151));
        assertFalse(WorkerEligibility.isGenerationOne(0));
        assertFalse(WorkerEligibility.isGenerationOne(152));
    }

    @Test
    void ownershipAndGenerationRestrictionsCanBeDisabled() {
        WorkerFacts laterGenerationWildWaterPokemon =
            new WorkerFacts(158, true, false, true, false, false, false);

        assertEquals(
            WorkerRejection.ELIGIBLE,
            WorkerEligibility.evaluate(laterGenerationWildWaterPokemon, false, false)
        );
        assertEquals(
            WorkerRejection.NOT_PLAYER_OWNED,
            WorkerEligibility.evaluate(laterGenerationWildWaterPokemon, true, false)
        );
        assertEquals(
            WorkerRejection.OUTSIDE_GENERATION_ONE,
            WorkerEligibility.evaluate(laterGenerationWildWaterPokemon, false, true)
        );
    }
}
