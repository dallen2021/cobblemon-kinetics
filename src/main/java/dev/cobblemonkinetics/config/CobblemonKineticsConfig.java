package dev.cobblemonkinetics.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CobblemonKineticsConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue REPLACE_NATURAL_WATER_POWER;
    public static final ModConfigSpec.DoubleValue WORKER_RADIUS;
    public static final ModConfigSpec.BooleanValue REQUIRE_PLAYER_OWNED;
    public static final ModConfigSpec.BooleanValue GEN_ONE_ONLY;
    public static final ModConfigSpec.IntValue HYDRO_RPM;
    public static final ModConfigSpec.IntValue HYDRO_CAPACITY;
    public static final ModConfigSpec.BooleanValue SHOW_WORK_PARTICLES;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("Create water-wheel behavior in the curated gameplay loop.")
            .push("waterWheel");

        REPLACE_NATURAL_WATER_POWER = BUILDER
            .comment("When true, flowing fluids no longer power Create water wheels. Use a Hydro Coupler and Pokemon worker instead.")
            .translation("cobblemon_kinetics.config.replaceNaturalWaterPower")
            .define("replaceNaturalWaterPower", true);

        WORKER_RADIUS = BUILDER
            .comment("Maximum distance in blocks between an assigned Pokemon and its Hydro Coupler.")
            .translation("cobblemon_kinetics.config.workerRadius")
            .defineInRange("workerRadius", 6.0, 2.0, 16.0);

        REQUIRE_PLAYER_OWNED = BUILDER
            .comment("When true, wild and NPC-owned Pokemon cannot work.")
            .translation("cobblemon_kinetics.config.requirePlayerOwned")
            .define("requirePlayerOwned", true);

        GEN_ONE_ONLY = BUILDER
            .comment("Restrict workers to National Pokedex numbers 1 through 151 during the initial development phase.")
            .translation("cobblemon_kinetics.config.genOneOnly")
            .define("genOneOnly", true);

        HYDRO_RPM = BUILDER
            .comment("Rotation speed produced by an active Hydro Coupler.")
            .translation("cobblemon_kinetics.config.hydroRpm")
            .defineInRange("hydroRpm", 8, 1, 256);

        HYDRO_CAPACITY = BUILDER
            .comment("Stress capacity supplied per RPM by an active Hydro Coupler.")
            .translation("cobblemon_kinetics.config.hydroCapacity")
            .defineInRange("hydroCapacity", 64, 1, 1024);

        SHOW_WORK_PARTICLES = BUILDER
            .comment("Show a water stream between an active worker and its wheel.")
            .translation("cobblemon_kinetics.config.showWorkParticles")
            .define("showWorkParticles", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private CobblemonKineticsConfig() {
    }
}
