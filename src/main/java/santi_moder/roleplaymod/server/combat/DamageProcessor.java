package santi_moder.roleplaymod.server.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.common.player.IPlayerData;
import santi_moder.roleplaymod.common.util.MedicalUtils;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.SyncPlayerDataPacket;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

public final class DamageProcessor {

    private DamageProcessor() {
    }

    public static void applyDamage(
            ServerPlayer target,
            CustomDamageType type,
            BodyPart hitPart,
            float rawDamage,
            Vec3 sourcePosition
    ) {
        if (target.level().isClientSide) return;

        target.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            DamageSeverity severity = DamageSeverity.fromDamage(rawDamage);
            int damage = scaleDamage(type, rawDamage, severity);

            switch (type) {
                case PROJECTILE -> applyProjectileDamage(data, hitPart, damage, severity);
                case MELEE -> applyMeleeDamage(data, hitPart, damage, severity);
                case FALL -> applyFallDamage(data, damage, severity);
                case FIRE -> applyFireDamage(data, damage, severity);
                case DROWN -> applyDrownDamage(data, damage, severity);
                case EXPLOSION -> ExplosionDamageProcessor.applyExplosionTrauma(target, sourcePosition, rawDamage);
                case VOID -> applyVoidDamage(data);
                case GENERIC -> applyMeleeDamage(data, hitPart, damage, severity);
            }

            if (type != CustomDamageType.EXPLOSION) {
                finishDamage(target, data);
            }
        });
    }

    public static void finishDamage(ServerPlayer target, IPlayerData data) {
        data.applyBodyPartEffects();

        if (MedicalUtils.checkAndKill(target, data)) {
            return;
        }

        if (MedicalUtils.shouldBeUnconscious(data) && !data.isInconsciente()) {
            data.setInconsciente(true);
            data.incrementarInconsciencias();
        }

        if (!target.isDeadOrDying()) {
            target.setHealth(target.getMaxHealth());
        }

        ModNetwork.STATS_CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> target),
                new SyncPlayerDataPacket(data)
        );
    }

    private static int scaleDamage(CustomDamageType type, float rawDamage, DamageSeverity severity) {
        float multiplier = switch (type) {
            case PROJECTILE -> switch (severity) {
                case LIGHT -> 0.75F;
                case MEDIUM -> 1.00F;
                case HEAVY -> 1.25F;
                case CRITICAL -> 1.50F;
            };
            case MELEE -> switch (severity) {
                case LIGHT -> 0.50F;
                case MEDIUM -> 0.75F;
                case HEAVY -> 1.00F;
                case CRITICAL -> 1.20F;
            };
            case FALL -> 0.85F;
            case FIRE -> 0.55F;
            case DROWN -> 0.65F;
            case EXPLOSION -> 1.00F;
            case VOID -> 100.0F;
            case GENERIC -> 0.75F;
        };

        return Math.max(1, (int) Math.ceil(rawDamage * multiplier));
    }

    private static void applyProjectileDamage(IPlayerData data, BodyPart hitPart, int damage, DamageSeverity severity) {
        int shock = shockFor(severity);

        switch (hitPart) {
            case HEAD -> {
                data.damageBodyPart(BodyPart.HEAD, damage * 2);
                data.setSangre(data.getSangre() - damage * 2);
                data.addShock(shock + 25);
            }
            case TORSO -> {
                data.damageBodyPart(BodyPart.TORSO, damage);
                data.setSangre(data.getSangre() - Math.max(1, damage));
                data.addShock(shock + 15);
            }
            case LEFT_ARM, RIGHT_ARM -> {
                data.damageBodyPart(hitPart, damage);
                data.setSangre(data.getSangre() - Math.max(1, damage / 2));
                data.addShock(shock);
            }
            case LEFT_LEG, RIGHT_LEG -> {
                data.damageBodyPart(hitPart, damage);
                data.setSangre(data.getSangre() - Math.max(1, damage / 2));
                data.addShock(shock + 5);
            }
        }

        if (severity == DamageSeverity.HEAVY || severity == DamageSeverity.CRITICAL) {
            data.applyBleed(hitPart, severity == DamageSeverity.CRITICAL
                    ? santi_moder.roleplaymod.common.player.BleedingType.HEAVY
                    : santi_moder.roleplaymod.common.player.BleedingType.MEDIUM);
        }
    }

    private static void applyMeleeDamage(IPlayerData data, BodyPart hitPart, int damage, DamageSeverity severity) {
        data.damageBodyPart(hitPart, damage);
        data.addShock(Math.max(2, shockFor(severity) / 2));

        if (hitPart == BodyPart.HEAD || hitPart == BodyPart.TORSO) {
            data.setSangre(data.getSangre() - Math.max(1, damage / 3));
        }
    }

    private static void applyFallDamage(IPlayerData data, int damage, DamageSeverity severity) {
        data.damageBodyPart(BodyPart.LEFT_LEG, damage);
        data.damageBodyPart(BodyPart.RIGHT_LEG, damage);
        data.addShock(shockFor(severity) + damage);

        if (damage >= 5) {
            data.damageBodyPart(BodyPart.TORSO, Math.max(1, damage / 2));
            data.setSangre(data.getSangre() - Math.max(1, damage / 3));
        }

        if (damage >= 9) {
            data.damageBodyPart(BodyPart.HEAD, Math.max(1, damage / 3));
            data.addShock(20);
        }
    }

    private static void applyFireDamage(IPlayerData data, int damage, DamageSeverity severity) {
        int burn = Math.max(1, damage);

        data.damageBodyPart(BodyPart.TORSO, burn);
        data.damageBodyPart(BodyPart.HEAD, Math.max(1, burn / 2));
        data.damageBodyPart(BodyPart.LEFT_ARM, Math.max(1, burn / 2));
        data.damageBodyPart(BodyPart.RIGHT_ARM, Math.max(1, burn / 2));
        data.damageBodyPart(BodyPart.LEFT_LEG, Math.max(1, burn / 2));
        data.damageBodyPart(BodyPart.RIGHT_LEG, Math.max(1, burn / 2));

        data.setSangre(data.getSangre() - Math.max(1, burn / 3));
        data.addShock(Math.max(3, shockFor(severity) / 2));
    }

    private static void applyDrownDamage(IPlayerData data, int damage, DamageSeverity severity) {
        data.damageBodyPart(BodyPart.TORSO, damage);
        data.damageBodyPart(BodyPart.HEAD, Math.max(1, damage / 2));

        data.setSangre(data.getSangre() - Math.max(1, damage));
        data.addShock(shockFor(severity) + 10);
    }

    private static void applyVoidDamage(IPlayerData data) {
        data.setSangre(0);
        data.setShock(100);

        for (BodyPart part : BodyPart.values()) {
            data.setBodyHp(part, 0);
        }
    }

    private static int shockFor(DamageSeverity severity) {
        return switch (severity) {
            case LIGHT -> 4;
            case MEDIUM -> 12;
            case HEAVY -> 25;
            case CRITICAL -> 45;
        };
    }
}