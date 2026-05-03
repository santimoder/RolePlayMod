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
        if (target == null || target.level().isClientSide) return;

        target.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            Vec3 origin = explosionPos != null ? explosionPos : target.position();

            double distance = target.position().distanceTo(origin);
            double radius = Math.max(5.0D, Math.min(24.0D, rawDamage * 2.25D));
            double distanceFactor = 1.0D - Math.min(1.0D, distance / radius);

            if (distanceFactor <= 0.08D) {
                DamageProcessor.finishDamage(target, data, 0.25F);
                return;
            }

            int totalBloodLoss = 0;
            int totalShock = 0;
            int damagedParts = 0;
            int highestPartDamage = 0;

            for (BodyHitbox hitbox : BodyHitboxResolver.createHitboxes(target)) {
                BodyPart part = hitbox.part();

                double exposure = calculateExposure(target, origin, hitbox.box());
                if (exposure <= 0.05D) continue;

                int partDamage = calculatePartDamage(
                        rawDamage,
                        distanceFactor,
                        exposure,
                        part
                );

                if (partDamage <= 0) continue;

                damagedParts++;
                highestPartDamage = Math.max(highestPartDamage, partDamage);

                data.damageBodyPart(part, partDamage);

                BleedingType bleeding = resolveBleeding(part, partDamage);
                if (bleeding != BleedingType.NONE) {
                    data.applyBleed(part, bleeding);
                }

                totalBloodLoss += calculateBloodLoss(part, partDamage);
                totalShock += calculateShock(part, partDamage);
            }

            if (damagedParts <= 0) {
                DamageProcessor.finishDamage(target, data, 0.25F);
                return;
            }

            totalBloodLoss = Math.min(totalBloodLoss, 35);
            totalShock = Math.min(totalShock, 85);

            data.setSangre(data.getSangre() - totalBloodLoss);
            data.addShock(totalShock);

            float intensity = calculateEffectIntensity(distanceFactor, highestPartDamage, damagedParts);

            DamageProcessor.finishDamage(target, data, intensity);
        });
    }

    private static int calculatePartDamage(
            float rawDamage,
            double distanceFactor,
            double exposure,
            BodyPart part
    ) {
        double traumaScale = 0.72D;

        int damage = (int) Math.ceil(
                rawDamage
                        * traumaScale
                        * distanceFactor
                        * exposure
                        * multiplier(part)
        );

        return Math.max(0, damage);
    }

    private static BleedingType resolveBleeding(BodyPart part, int partDamage) {
        if (partDamage >= 10) {
            return BleedingType.HEAVY;
        }

        if (partDamage >= 6) {
            return BleedingType.MEDIUM;
        }

        if (partDamage >= 3 && (part == BodyPart.HEAD || part == BodyPart.TORSO)) {
            return BleedingType.LIGHT;
        }

        return BleedingType.NONE;
    }

    private static int calculateBloodLoss(BodyPart part, int partDamage) {
        return switch (part) {
            case HEAD -> Math.max(1, Math.round(partDamage * 0.45F));
            case TORSO -> Math.max(1, Math.round(partDamage * 0.55F));
            case LEFT_ARM, RIGHT_ARM -> Math.max(0, Math.round(partDamage * 0.25F));
            case LEFT_LEG, RIGHT_LEG -> Math.max(0, Math.round(partDamage * 0.35F));
        };
    }

    private static int calculateShock(BodyPart part, int partDamage) {
        return switch (part) {
            case HEAD -> Math.max(4, partDamage * 4);
            case TORSO -> Math.max(3, partDamage * 3);
            case LEFT_ARM, RIGHT_ARM -> Math.max(2, partDamage * 2);
            case LEFT_LEG, RIGHT_LEG -> Math.max(2, partDamage * 2);
        };
    }

    private static float calculateEffectIntensity(
            double distanceFactor,
            int highestPartDamage,
            int damagedParts
    ) {
        float intensity = 0.30F;

        intensity += (float) distanceFactor * 0.35F;
        intensity += Math.min(0.35F, highestPartDamage / 20.0F);
        intensity += Math.min(0.15F, damagedParts * 0.025F);

        return Math.max(0.25F, Math.min(1.0F, intensity));
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
            case HEAD -> 0.75D;
            case TORSO -> 1.10D;
            case LEFT_ARM, RIGHT_ARM -> 0.55D;
            case LEFT_LEG, RIGHT_LEG -> 0.70D;
        };
    }
}