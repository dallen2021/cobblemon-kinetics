package dev.cobblemonkinetics.content.hydraulic;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import dev.cobblemonkinetics.config.CobblemonKineticsConfig;
import dev.cobblemonkinetics.content.worker.PokemonWorker;
import dev.cobblemonkinetics.content.worker.WorkerAssignmentRegistry;
import dev.cobblemonkinetics.content.worker.WorkerRejection;
import dev.cobblemonkinetics.registry.CobblemonKineticsBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class HydroCouplerBlockEntity extends GeneratingKineticBlockEntity {

    private static final String WORKER_ID_KEY = "WorkerPokemonId";
    private static final String OWNER_ID_KEY = "WorkerOwnerId";
    private static final int VALIDATION_INTERVAL_TICKS = 10;

    private UUID workerPokemonId;
    private UUID workerOwnerId;
    private boolean workerActive;
    private int validationCountdown;
    private int lastAppliedRpm = -1;
    private int lastAppliedCapacity = -1;

    public HydroCouplerBlockEntity(BlockPos pos, BlockState state) {
        super(CobblemonKineticsBlockEntities.HYDRO_COUPLER.get(), pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level instanceof ServerLevel serverLevel && workerPokemonId != null) {
            if (!WorkerAssignmentRegistry.claim(serverLevel, workerPokemonId, worldPosition)) {
                workerActive = false;
            }
        }
        updateGeneratedRotation();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (validationCountdown-- > 0) {
            return;
        }
        validationCountdown = VALIDATION_INTERVAL_TICKS;

        Optional<PokemonEntity> worker = findAssignedWorker(serverLevel);
        boolean shouldBeActive = worker.isPresent() && hasWaterWheelAttached();
        int configuredRpm = CobblemonKineticsConfig.HYDRO_RPM.get();
        int configuredCapacity = CobblemonKineticsConfig.HYDRO_CAPACITY.get();
        boolean outputChanged = configuredRpm != lastAppliedRpm || configuredCapacity != lastAppliedCapacity;
        lastAppliedRpm = configuredRpm;
        lastAppliedCapacity = configuredCapacity;
        if (shouldBeActive != workerActive || outputChanged) {
            workerActive = shouldBeActive;
            updateGeneratedRotation();
            notifyUpdate();
        }

        if (shouldBeActive && CobblemonKineticsConfig.SHOW_WORK_PARTICLES.get()) {
            emitWaterStream(serverLevel, worker.orElseThrow());
        }
    }

    public boolean assignWorker(UUID pokemonId, UUID ownerId) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (workerOwnerId != null && !workerOwnerId.equals(ownerId)) {
            return false;
        }
        if (pokemonId.equals(workerPokemonId)) {
            if (!WorkerAssignmentRegistry.claim(serverLevel, pokemonId, worldPosition)) {
                return false;
            }
            workerOwnerId = ownerId;
            setChanged();
            notifyUpdate();
            return true;
        }
        if (!WorkerAssignmentRegistry.claim(serverLevel, pokemonId, worldPosition)) {
            return false;
        }

        UUID previousWorkerId = workerPokemonId;
        if (previousWorkerId != null) {
            WorkerAssignmentRegistry.release(serverLevel, previousWorkerId, worldPosition);
        }
        workerPokemonId = pokemonId;
        workerOwnerId = ownerId;
        workerActive = false;
        setChanged();
        notifyUpdate();
        updateGeneratedRotation();
        return true;
    }

    public boolean clearWorker(UUID requestingOwnerId) {
        if (workerOwnerId != null && !workerOwnerId.equals(requestingOwnerId)) {
            return false;
        }
        releaseClaim();
        workerPokemonId = null;
        workerOwnerId = null;
        if (workerActive) {
            workerActive = false;
            updateGeneratedRotation();
        }
        setChanged();
        notifyUpdate();
        return true;
    }

    public boolean hasAssignedWorker() {
        return workerPokemonId != null;
    }

    public boolean isWorkerActive() {
        return workerActive;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!workerActive || !hasWaterWheelAttached()) {
            return 0;
        }
        Direction facing = getBlockState().getValue(HydroCouplerBlock.FACING);
        return convertToDirection(CobblemonKineticsConfig.HYDRO_RPM.get(), facing);
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = workerActive ? CobblemonKineticsConfig.HYDRO_CAPACITY.get().floatValue() : 0;
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.translatable(
            workerActive
                ? "cobblemon_kinetics.tooltip.hydro_coupler.active"
                : hasAssignedWorker()
                    ? "cobblemon_kinetics.tooltip.hydro_coupler.waiting"
                    : "cobblemon_kinetics.tooltip.hydro_coupler.unassigned"
        ));
        return true;
    }

    @Override
    public void invalidate() {
        releaseClaim();
        super.invalidate();
    }

    private Optional<PokemonEntity> findAssignedWorker(ServerLevel serverLevel) {
        if (workerPokemonId == null || workerOwnerId == null) {
            return Optional.empty();
        }
        if (!WorkerAssignmentRegistry.claim(serverLevel, workerPokemonId, worldPosition)) {
            return Optional.empty();
        }

        double radius = CobblemonKineticsConfig.WORKER_RADIUS.get();
        AABB searchBox = new AABB(worldPosition).inflate(radius);
        Vec3 center = Vec3.atCenterOf(worldPosition);

        return serverLevel.getEntitiesOfClass(PokemonEntity.class, searchBox, entity ->
                workerPokemonId.equals(entity.getPokemon().getUuid())
                    && (!CobblemonKineticsConfig.REQUIRE_PLAYER_OWNED.get()
                        || workerOwnerId.equals(entity.getOwnerUUID())
                        || workerOwnerId.equals(entity.getPokemon().getOwnerUUID()))
                    && entity.distanceToSqr(center) <= radius * radius
                    && PokemonWorker.evaluate(entity) == WorkerRejection.ELIGIBLE
            ).stream()
            .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(center)));
    }

    private boolean hasWaterWheelAttached() {
        if (level == null) {
            return false;
        }
        Direction facing = getBlockState().getValue(HydroCouplerBlock.FACING);
        BlockPos targetPos = worldPosition.relative(facing);
        BlockState target = level.getBlockState(targetPos);
        boolean isWheel = AllBlocks.WATER_WHEEL.has(target) || AllBlocks.LARGE_WATER_WHEEL.has(target);
        return isWheel
            && target.getBlock() instanceof IRotate rotatingBlock
            && rotatingBlock.hasShaftTowards(level, targetPos, target, facing.getOpposite());
    }

    private void emitWaterStream(ServerLevel serverLevel, PokemonEntity worker) {
        Vec3 start = worker.getEyePosition();
        Direction facing = getBlockState().getValue(HydroCouplerBlock.FACING);
        Vec3 end = Vec3.atCenterOf(worldPosition.relative(facing));
        Vec3 delta = end.subtract(start);

        for (int step = 1; step <= 6; step++) {
            Vec3 point = start.add(delta.scale(step / 6.0));
            serverLevel.sendParticles(
                ParticleTypes.SPLASH,
                point.x,
                point.y,
                point.z,
                2,
                0.06,
                0.06,
                0.06,
                0.02
            );
        }
    }

    private void releaseClaim() {
        if (level instanceof ServerLevel serverLevel && workerPokemonId != null) {
            WorkerAssignmentRegistry.release(serverLevel, workerPokemonId, worldPosition);
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (workerPokemonId != null) {
            tag.putUUID(WORKER_ID_KEY, workerPokemonId);
        }
        if (workerOwnerId != null) {
            tag.putUUID(OWNER_ID_KEY, workerOwnerId);
        }
        tag.putBoolean("WorkerActive", workerActive);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        workerPokemonId = tag.hasUUID(WORKER_ID_KEY) ? tag.getUUID(WORKER_ID_KEY) : null;
        workerOwnerId = tag.hasUUID(OWNER_ID_KEY) ? tag.getUUID(OWNER_ID_KEY) : null;
        workerActive = tag.getBoolean("WorkerActive");
    }
}
