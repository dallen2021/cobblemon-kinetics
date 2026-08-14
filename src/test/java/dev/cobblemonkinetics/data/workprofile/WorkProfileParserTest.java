package dev.cobblemonkinetics.data.workprofile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkProfileParserTest {

    private static final String HYDRO_PROFILE =
        "/data/cobblemon_kinetics/work_profiles/hydro_operator.json";

    @Test
    void parsesTheGeneratedHydroProfile() throws IOException {
        try (Reader reader = new InputStreamReader(
            Objects.requireNonNull(
                WorkProfileParserTest.class.getResourceAsStream(HYDRO_PROFILE),
                "Bundled Hydro work profile is missing"
            ),
            StandardCharsets.UTF_8
        )) {
            WorkProfileDefinition profile = WorkProfileParser.parse(reader);

            assertEquals(1, profile.formatVersion());
            assertEquals("cobblemon_kinetics:hydro_operator", profile.id());
            assertEquals("cobblemon_kinetics:hydro_coupler", profile.workstation().adapterId());
            assertTrue(profile.selector() instanceof WorkProfileDefinition.TypeSelector);
        }
    }

    @Test
    void parsesEveryBundledWorkProfile() throws Exception {
        URL directory = Objects.requireNonNull(
            WorkProfileParserTest.class.getResource("/data/cobblemon_kinetics/work_profiles"),
            "Bundled work-profile directory is missing"
        );
        assertEquals("file", directory.getProtocol(), "Tests must inspect the processed resource directory");

        List<Path> profileFiles;
        try (Stream<Path> paths = Files.walk(Path.of(directory.toURI()))) {
            profileFiles = paths
                .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                .sorted()
                .toList();
        }
        assertTrue(!profileFiles.isEmpty(), "At least one generated work profile must be bundled");

        for (Path profileFile : profileFiles) {
            try (Reader reader = Files.newBufferedReader(profileFile, StandardCharsets.UTF_8)) {
                WorkProfileParser.parse(reader);
            }
        }
    }

    @Test
    void rejectsUnknownFormatPrivateFieldsAndAdapters() {
        JsonObject unknownFormat = validProfile();
        unknownFormat.addProperty("format_version", 2);
        assertThrows(WorkProfileValidationException.class, () -> WorkProfileParser.parse(unknownFormat));

        JsonObject privateField = validProfile();
        privateField.addProperty("private_note", "must never enter a mod export");
        assertThrows(WorkProfileValidationException.class, () -> WorkProfileParser.parse(privateField));

        JsonObject unknownAdapter = validProfile();
        unknownAdapter.getAsJsonObject("workstation").addProperty("adapter_id", "example:unknown");
        assertThrows(WorkProfileValidationException.class, () -> WorkProfileParser.parse(unknownAdapter));
    }

    @Test
    void rejectsInvalidRegistryIdentifiersAndFractionalRpm() {
        JsonObject invalidRegistry = validProfile();
        invalidRegistry.getAsJsonObject("workstation")
            .getAsJsonArray("registry_ids")
            .set(0, JsonParser.parseString("\"not a resource id\""));
        assertThrows(WorkProfileValidationException.class, () -> WorkProfileParser.parse(invalidRegistry));

        JsonObject fractionalRpm = validProfile();
        fractionalRpm.getAsJsonObject("contribution").addProperty("rpm", 8.5);
        assertThrows(WorkProfileValidationException.class, () -> WorkProfileParser.parse(fractionalRpm));
    }

    private static JsonObject validProfile() {
        try (Reader reader = new InputStreamReader(
            Objects.requireNonNull(
                WorkProfileParserTest.class.getResourceAsStream(HYDRO_PROFILE),
                "Bundled Hydro work profile is missing"
            ),
            StandardCharsets.UTF_8
        )) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
