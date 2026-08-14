package dev.cobblemonkinetics.content.worker;

public record WorkerFacts(
    int nationalPokedexNumber,
    boolean waterType,
    boolean playerOwned,
    boolean alive,
    boolean fainted,
    boolean busy,
    boolean battling
) {
}
