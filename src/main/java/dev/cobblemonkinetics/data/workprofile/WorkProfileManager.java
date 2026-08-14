package dev.cobblemonkinetics.data.workprofile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.cobblemonkinetics.CobblemonKinetics;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Loads validated profiles on server-data reload without activating gameplay adapters yet. */
public final class WorkProfileManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final WorkProfileManager INSTANCE = new WorkProfileManager();
    private volatile Map<String, WorkProfileDefinition> profiles = Map.of();

    private WorkProfileManager() {
        super(GSON, "work_profiles");
    }

    public Optional<WorkProfileDefinition> find(String id) {
        return Optional.ofNullable(profiles.get(id));
    }

    public Map<String, WorkProfileDefinition> all() {
        return profiles;
    }

    @Override
    protected void apply(
        Map<ResourceLocation, JsonElement> prepared,
        ResourceManager resourceManager,
        ProfilerFiller profiler
    ) {
        Map<String, WorkProfileDefinition> validated = new LinkedHashMap<>();
        int rejected = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : prepared.entrySet()) {
            try {
                WorkProfileDefinition profile = WorkProfileParser.parse(entry.getValue());
                WorkProfileDefinition duplicate = validated.putIfAbsent(profile.id(), profile);
                if (duplicate != null) {
                    throw new WorkProfileValidationException("$.id: duplicate profile " + profile.id());
                }
            } catch (RuntimeException exception) {
                rejected++;
                CobblemonKinetics.LOGGER.error(
                    "Rejected work profile {}: {}",
                    entry.getKey(),
                    exception.getMessage()
                );
            }
        }

        profiles = Map.copyOf(validated);
        CobblemonKinetics.LOGGER.info(
            "Loaded {} validated Cobblemon Kinetics work profile(s); rejected {}",
            profiles.size(),
            rejected
        );
    }
}
