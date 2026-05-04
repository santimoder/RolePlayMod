package santi_moder.roleplaymod.server.combat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public final class ProjectilePositionTracker {

    private static final int MAX_TRACK_AGE_TICKS = 40;

    private static final Map<UUID, TrackedPosition> POSITIONS = new HashMap<>();

    private ProjectilePositionTracker() {
    }

    public static void remember(Entity entity) {
        if (entity == null || entity.level().isClientSide) return;

        POSITIONS.put(
                entity.getUUID(),
                new TrackedPosition(
                        entity.position(),
                        entity.level().getGameTime()
                )
        );
    }

    public static Optional<ProjectileMovementTrace> getMovementTrace(Entity entity) {
        if (entity == null || entity.level().isClientSide) {
            return Optional.empty();
        }

        TrackedPosition tracked = POSITIONS.get(entity.getUUID());

        if (tracked == null) {
            return Optional.empty();
        }

        Vec3 previous = tracked.position();
        Vec3 current = entity.position();

        if (previous.distanceToSqr(current) <= 0.0001D) {
            return Optional.empty();
        }

        return Optional.of(new ProjectileMovementTrace(previous, current));
    }

    public static void cleanup(long gameTime) {
        Iterator<Map.Entry<UUID, TrackedPosition>> iterator = POSITIONS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, TrackedPosition> entry = iterator.next();

            if (gameTime - entry.getValue().lastSeenGameTime() > MAX_TRACK_AGE_TICKS) {
                iterator.remove();
            }
        }
    }

    private record TrackedPosition(
            Vec3 position,
            long lastSeenGameTime
    ) {
    }
}