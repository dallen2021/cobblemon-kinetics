package dev.cobblemonkinetics.content.worker;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class WorkerAssignmentRegistry {

    private static final Map<ServerLevel, Map<UUID, BlockPos>> CLAIMS = new WeakHashMap<>();

    public static synchronized boolean claim(ServerLevel level, UUID pokemonId, BlockPos couplerPos) {
        Map<UUID, BlockPos> levelClaims = CLAIMS.computeIfAbsent(level, ignored -> new HashMap<>());
        BlockPos current = levelClaims.get(pokemonId);
        if (current != null && !current.equals(couplerPos)) {
            return false;
        }
        levelClaims.put(pokemonId, couplerPos.immutable());
        return true;
    }

    public static synchronized void release(ServerLevel level, UUID pokemonId, BlockPos couplerPos) {
        Map<UUID, BlockPos> levelClaims = CLAIMS.get(level);
        if (levelClaims == null) {
            return;
        }
        levelClaims.remove(pokemonId, couplerPos);
        if (levelClaims.isEmpty()) {
            CLAIMS.remove(level);
        }
    }

    private WorkerAssignmentRegistry() {
    }
}
