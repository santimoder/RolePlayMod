package santi_moder.roleplaymod.server.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import santi_moder.roleplaymod.common.player.BleedingType;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.common.player.hitbox.BodyHitbox;
import santi_moder.roleplaymod.common.player.hitbox.BodyHitboxResolver;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

public final class ExplosionDamageProcessor {

    private ExplosionDamageProcessor() {
    }

    public static void applyExplosionTrauma(ServerPlayer target, Vec3 explosionPos, float rawDamage) {
        if (target.level().isClientSide) return;

        target.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            Vec3 origin = explosionPos != null ? explosionPos : target.position();

            double distance = target.position().distanceTo(origin);
            double radius = Math.max(5.0D, Math.min(24.0D, rawDamage * 2.25D));
            double distanceFactor = 1.0D - Math.min(1.0D, distance / radius);

            if (distanceFactor <= 0.05D) {
                DamageProcessor.finishDamage(target, data);
                return;
            }

            int totalBloodLoss = 0;
            int totalShock = 0;

            for (BodyHitbox hitbox : BodyHitboxResolver.createHitboxes(target)) {
                BodyPart part = hitbox.part();

                double exposure = calculateExposure(target, origin, hitbox.box());

                if (exposure <= 0.05D) continue;

                int partDamage = Math.max(1, (int) Math.ceil(
                        rawDamage * distanceFactor * exposure * multiplier(part)
                ));

                data.damageBodyPart(part, partDamage);

                if (partDamage >= 4) {
                    data.applyBleed(part, partDamage >= 8 ? BleedingType.HEAVY : BleedingType.MEDIUM);
                }

                totalBloodLoss += switch (part) {
                    case HEAD, TORSO -> Math.max(1, partDamage / 2);
                    case LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG -> Math.max(0, partDamage / 3);
                };

                totalShock += Math.max(1, partDamage * shockMultiplier(part));
            }

            data.setSangre(data.getSangre() - totalBloodLoss);
            data.addShock(Math.min(75, totalShock));

            DamageProcessor.finishDamage(target, data);
        });
    }

    private static double calculateExposure(ServerPlayer target, Vec3 explosionPos, AABB box) {
        Vec3 center = box.getCenter();

        HitResult hit = target.level().clip(new ClipContext(
                explosionPos,
                center,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                target
        ));

        if (hit.getType() == HitResult.Type.MISS) {
            return 1.0D;
        }

        double blockedDistance = hit.getLocation().distanceTo(explosionPos);
        double bodyDistance = center.distanceTo(explosionPos);

        return blockedDistance + 0.15D >= bodyDistance ? 1.0D : 0.25D;
    }

    private static double multiplier(BodyPart part) {
        return switch (part) {
            case HEAD -> 0.95D;
            case TORSO -> 1.40D;
            case LEFT_ARM, RIGHT_ARM -> 0.70D;
            case LEFT_LEG, RIGHT_LEG -> 0.90D;
        };
    }

    private static int shockMultiplier(BodyPart part) {
        return switch (part) {
            case HEAD -> 4;
            case TORSO -> 3;
            case LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG -> 2;
        };
    }
}