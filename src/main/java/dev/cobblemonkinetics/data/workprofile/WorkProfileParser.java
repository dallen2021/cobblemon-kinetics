package dev.cobblemonkinetics.data.workprofile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.cobblemonkinetics.integration.workstation.WorkstationAdapterDescriptor;
import dev.cobblemonkinetics.integration.workstation.WorkstationAdapterRegistry;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Validates the language-neutral work-profile contract without game state. */
public final class WorkProfileParser {

    public static final int SUPPORTED_FORMAT_VERSION = 1;

    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
        "^[a-z0-9_.-]+:[a-z0-9/._-]+$"
    );
    private static final Set<String> ROOT_FIELDS = Set.of(
        "format_version",
        "id",
        "title",
        "priority",
        "status",
        "selector",
        "constraints",
        "workstation",
        "contribution",
        "public_rationale"
    );

    public static WorkProfileDefinition parse(Reader reader) {
        return parse(JsonParser.parseReader(reader));
    }

    public static WorkProfileDefinition parse(JsonElement json) {
        JsonObject root = object(json, "$", ROOT_FIELDS);
        int formatVersion = integer(root, "format_version", 1, Integer.MAX_VALUE, "$" );
        if (formatVersion != SUPPORTED_FORMAT_VERSION) {
            throw invalid("$.format_version", "unsupported format " + formatVersion);
        }

        String id = resourceLocation(root, "id", "$" );
        String title = boundedString(root, "title", 1, 120, "$" );
        int priority = integer(root, "priority", -1000, 1000, "$" );
        WorkProfileDefinition.Status status = enumValue(
            boundedString(root, "status", 1, 32, "$" ),
            WorkProfileDefinition.Status.class,
            "$.status"
        );
        WorkProfileDefinition.Selector selector = selector(require(root, "selector", "$" ));
        WorkProfileDefinition.Constraints constraints = constraints(require(root, "constraints", "$" ));
        WorkProfileDefinition.Workstation workstation = workstation(require(root, "workstation", "$" ));
        WorkProfileDefinition.Contribution contribution = contribution(require(root, "contribution", "$" ));
        String publicRationale = boundedString(root, "public_rationale", 1, 500, "$" );

        return new WorkProfileDefinition(
            formatVersion,
            id,
            title,
            priority,
            status,
            selector,
            constraints,
            workstation,
            contribution,
            publicRationale
        );
    }

    private static WorkProfileDefinition.Selector selector(JsonElement element) {
        JsonObject selector = object(element, "$.selector", Set.of("kind", "types", "national_dex", "pokemon"));
        String kind = boundedString(selector, "kind", 1, 32, "$.selector");
        return switch (kind) {
            case "type" -> {
                requireExactFields(selector, Set.of("kind", "types", "national_dex"), "$.selector");
                List<String> types = resourceLocationArray(selector, "types", "$.selector");
                JsonObject dex = object(
                    require(selector, "national_dex", "$.selector"),
                    "$.selector.national_dex",
                    Set.of("min", "max")
                );
                requireExactFields(dex, Set.of("min", "max"), "$.selector.national_dex");
                int min = integer(dex, "min", 1, Integer.MAX_VALUE, "$.selector.national_dex");
                int max = integer(dex, "max", 1, Integer.MAX_VALUE, "$.selector.national_dex");
                if (min > max) {
                    throw invalid("$.selector.national_dex", "min must be less than or equal to max");
                }
                yield new WorkProfileDefinition.TypeSelector(types, new WorkProfileDefinition.DexRange(min, max));
            }
            case "pokemon" -> {
                requireExactFields(selector, Set.of("kind", "pokemon"), "$.selector");
                yield new WorkProfileDefinition.PokemonSelector(
                    resourceLocationArray(selector, "pokemon", "$.selector")
                );
            }
            default -> throw invalid("$.selector.kind", "expected type or pokemon");
        };
    }

    private static WorkProfileDefinition.Constraints constraints(JsonElement element) {
        Set<String> fields = Set.of(
            "requires_owner",
            "must_be_alive",
            "must_not_be_fainted",
            "must_not_be_battling",
            "must_be_idle"
        );
        JsonObject constraints = object(element, "$.constraints", fields);
        requireExactFields(constraints, fields, "$.constraints");
        return new WorkProfileDefinition.Constraints(
            bool(constraints, "requires_owner", "$.constraints"),
            bool(constraints, "must_be_alive", "$.constraints"),
            bool(constraints, "must_not_be_fainted", "$.constraints"),
            bool(constraints, "must_not_be_battling", "$.constraints"),
            bool(constraints, "must_be_idle", "$.constraints")
        );
    }

    private static WorkProfileDefinition.Workstation workstation(JsonElement element) {
        Set<String> fields = Set.of("adapter_id", "registry_ids", "required_attachment_tag", "radius");
        JsonObject workstation = object(element, "$.workstation", fields);
        requireExactFields(workstation, fields, "$.workstation");
        String adapterId = resourceLocation(workstation, "adapter_id", "$.workstation");
        List<String> registryIds = resourceLocationArray(workstation, "registry_ids", "$.workstation");
        String attachmentTag = resourceLocation(workstation, "required_attachment_tag", "$.workstation");
        double radius = decimal(workstation, "radius", 0.0, 64.0, false, "$.workstation");

        WorkstationAdapterDescriptor adapter = WorkstationAdapterRegistry.find(adapterId)
            .orElseThrow(() -> invalid("$.workstation.adapter_id", "unknown adapter " + adapterId));
        for (String registryId : registryIds) {
            if (!adapter.supportsRegistryId(registryId)) {
                throw invalid(
                    "$.workstation.registry_ids",
                    registryId + " is not supported by adapter " + adapterId
                );
            }
        }

        return new WorkProfileDefinition.Workstation(adapterId, registryIds, attachmentTag, radius);
    }

    private static WorkProfileDefinition.Contribution contribution(JsonElement element) {
        Set<String> fields = Set.of("mode", "rpm", "capacity_per_rpm", "efficiency_multiplier");
        JsonObject contribution = object(element, "$.contribution", fields);
        requireExactFields(contribution, fields, "$.contribution");
        String mode = boundedString(contribution, "mode", 1, 32, "$.contribution");
        if (!"fixed".equals(mode)) {
            throw invalid("$.contribution.mode", "only fixed contribution is supported in format 1");
        }
        return new WorkProfileDefinition.Contribution(
            mode,
            integer(contribution, "rpm", 1, 256, "$.contribution"),
            integer(contribution, "capacity_per_rpm", 1, 1024, "$.contribution"),
            decimal(contribution, "efficiency_multiplier", 0.0, 16.0, false, "$.contribution")
        );
    }

    private static JsonObject object(JsonElement element, String path, Set<String> allowedFields) {
        if (element == null || !element.isJsonObject()) {
            throw invalid(path, "expected object");
        }
        JsonObject object = element.getAsJsonObject();
        for (String field : object.keySet()) {
            if (!allowedFields.contains(field)) {
                throw invalid(path + "." + field, "unknown field");
            }
        }
        return object;
    }

    private static void requireExactFields(JsonObject object, Set<String> fields, String path) {
        for (String field : object.keySet()) {
            if (!fields.contains(field)) {
                throw invalid(path + "." + field, "unknown field");
            }
        }
        for (String field : fields) {
            require(object, field, path);
        }
    }

    private static JsonElement require(JsonObject object, String field, String path) {
        JsonElement value = object.get(field);
        if (value == null || value.isJsonNull()) {
            throw invalid(path + "." + field, "is required");
        }
        return value;
    }

    private static String boundedString(
        JsonObject object,
        String field,
        int minLength,
        int maxLength,
        String path
    ) {
        JsonElement value = require(object, field, path);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw invalid(path + "." + field, "expected string");
        }
        String string = value.getAsString();
        if (string.length() < minLength || string.length() > maxLength || string.isBlank()) {
            throw invalid(path + "." + field, "length must be between " + minLength + " and " + maxLength);
        }
        return string;
    }

    private static String resourceLocation(JsonObject object, String field, String path) {
        String value = boundedString(object, field, 3, 255, path);
        if (!RESOURCE_LOCATION.matcher(value).matches()) {
            throw invalid(path + "." + field, "invalid resource location " + value);
        }
        return value;
    }

    private static List<String> resourceLocationArray(JsonObject object, String field, String path) {
        JsonElement value = require(object, field, path);
        if (!value.isJsonArray()) {
            throw invalid(path + "." + field, "expected array");
        }
        JsonArray array = value.getAsJsonArray();
        if (array.isEmpty()) {
            throw invalid(path + "." + field, "must not be empty");
        }
        List<String> values = new ArrayList<>(array.size());
        Set<String> unique = new HashSet<>();
        for (int index = 0; index < array.size(); index++) {
            JsonElement item = array.get(index);
            if (!item.isJsonPrimitive() || !item.getAsJsonPrimitive().isString()) {
                throw invalid(path + "." + field + "[" + index + "]", "expected string");
            }
            String resourceLocation = item.getAsString();
            if (!RESOURCE_LOCATION.matcher(resourceLocation).matches()) {
                throw invalid(
                    path + "." + field + "[" + index + "]",
                    "invalid resource location " + resourceLocation
                );
            }
            if (!unique.add(resourceLocation)) {
                throw invalid(path + "." + field, "contains duplicate " + resourceLocation);
            }
            values.add(resourceLocation);
        }
        return List.copyOf(values);
    }

    private static int integer(JsonObject object, String field, int min, int max, String path) {
        JsonElement value = require(object, field, path);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw invalid(path + "." + field, "expected integer");
        }
        try {
            BigDecimal number = value.getAsBigDecimal().stripTrailingZeros();
            if (number.scale() > 0) throw new ArithmeticException("fractional");
            int result = number.intValueExact();
            if (result < min || result > max) throw new ArithmeticException("range");
            return result;
        } catch (ArithmeticException exception) {
            throw invalid(path + "." + field, "integer must be between " + min + " and " + max);
        }
    }

    private static double decimal(
        JsonObject object,
        String field,
        double min,
        double max,
        boolean inclusiveMin,
        String path
    ) {
        JsonElement value = require(object, field, path);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw invalid(path + "." + field, "expected number");
        }
        double result = value.getAsDouble();
        boolean belowMin = inclusiveMin ? result < min : result <= min;
        if (!Double.isFinite(result) || belowMin || result > max) {
            String lower = inclusiveMin ? "at least " : "greater than ";
            throw invalid(path + "." + field, "must be " + lower + min + " and at most " + max);
        }
        return result;
    }

    private static boolean bool(JsonObject object, String field, String path) {
        JsonElement value = require(object, field, path);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw invalid(path + "." + field, "expected boolean");
        }
        return value.getAsBoolean();
    }

    private static <E extends Enum<E>> E enumValue(String raw, Class<E> type, String path) {
        try {
            return Enum.valueOf(type, raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw invalid(path, "unknown value " + raw);
        }
    }

    private static WorkProfileValidationException invalid(String path, String message) {
        return new WorkProfileValidationException(path + ": " + message);
    }

    private WorkProfileParser() {
    }
}
