package santi_moder.roleplaymod.server.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.player.BleedingType;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.common.player.IPlayerData;
import santi_moder.roleplaymod.common.util.MedicalUtils;
import santi_moder.roleplaymod.network.MedicalEffectS2CPacket;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.SyncPlayerDataPacket;
import santi_moder.roleplaymod.server.combat.weapon.WeaponCategory;
import santi_moder.roleplaymod.server.combat.weapon.WeaponDamageProfile;
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
        applyDamage(target, type, hitPart, rawDamage, sourcePosition, null);
    }

    public static void applyDamage(
            ServerPlayer target,
            CustomDamageType type,
            BodyPart hitPart,
            float rawDamage,
            Vec3 sourcePosition,
            WeaponDamageProfile weaponProfile
    ) {
        if (target == null || target.level().isClientSide) return;

        target.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            DamageSeverity severity = DamageSeverity.fromDamage(rawDamage);
            int baseDamage = scaleDamage(type, rawDamage, severity);

            switch (type) {
                case PROJECTILE -> applyProjectileDamage(data, hitPart, baseDamage, severity, weaponProfile);
                case MELEE -> applyMeleeDamage(data, hitPart, baseDamage, severity);
                case FALL -> applyFallDamage(data, baseDamage, severity);
                case FIRE -> applyFireDamage(data, baseDamage, severity);
                case DROWN -> applyDrownDamage(data, baseDamage, severity);
                case EXPLOSION -> ExplosionDamageProcessor.applyExplosionTrauma(target, sourcePosition, rawDamage);
                case VOID -> applyVoidDamage(data);
                case GENERIC -> applyMeleeDamage(data, hitPart, baseDamage, severity);
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
                new MedicalEffectS2CPacket(MedicalEffectS2CPacket.Type.DAMAGE_HIT, 0.65F)
        );


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

    private static void applyProjectileDamage(
            IPlayerData data,
            BodyPart hitPart,
            int baseDamage,
            DamageSeverity severity,
            WeaponDamageProfile profile
    ) {
        if (hitPart == null) hitPart = BodyPart.TORSO;

        WeaponCategory category = profile == null ? WeaponCategory.GENERIC : profile.category();

        float bodyMultiplier = profile == null ? 1.0F : profile.bodyDamageMultiplier();
        float bloodMultiplier = profile == null ? 1.0F : profile.bloodLossMultiplier();
        float shockMultiplier = profile == null ? 1.0F : profile.shockMultiplier();

        int bodyDamage = Math.max(1, Math.round(baseDamage * bodyMultiplier));
        int shock = Math.max(1, Math.round(shockFor(severity) * shockMultiplier));

        boolean highPenetration = profile != null && profile.isHighPenetration();
        boolean forceHeavyBleeding = profile != null && profile.causesHeavyBleeding();

        /*
         * Headshot de arma de alta penetración.
         * Esto hace que sniper / rifle pesado sean realmente peligrosos,
         * pero no convierte pistolas o SMG en muerte instantánea siempre.
         */
        if (hitPart == BodyPart.HEAD && highPenetration && severity != DamageSeverity.LIGHT) {
            data.damageBodyPart(BodyPart.HEAD, 5);
            data.setSangre(0);
            data.setShock(100);
            data.applyBleed(BodyPart.HEAD, BleedingType.HEAVY);
            return;
        }

        switch (category) {
            case SMG -> {
                bodyDamage = Math.max(1, Math.round(bodyDamage * 0.85F));
                bloodMultiplier *= 0.85F;
                shock = Math.max(1, Math.round(shock * 0.90F));
            }

            case RIFLE -> {
                bloodMultiplier *= 1.10F;
                shock += 8;
            }

            case SHOTGUN -> {
                bodyDamage = Math.max(1, Math.round(bodyDamage * 1.15F));
                bloodMultiplier *= 1.20F;
                shock += 15;
                forceHeavyBleeding = true;
            }

            case SNIPER -> {
                bodyDamage = Math.max(1, Math.round(bodyDamage * 1.25F));
                bloodMultiplier *= 1.30F;
                shock += 35;
                forceHeavyBleeding = true;
            }

            case PISTOL -> {
                // Base equilibrada.
            }

            case EXPLOSIVE, MELEE, GENERIC -> {
                // Se manejan por su tipo principal o como daño genérico.
            }
        }

        switch (hitPart) {
            case HEAD -> {
                int partDamage = bodyDamage * 2;
                int bloodLoss = calculateBloodLoss(partDamage, 1.50F, bloodMultiplier);

                data.damageBodyPart(BodyPart.HEAD, partDamage);
                data.setSangre(data.getSangre() - bloodLoss);
                data.addShock(shock + 30);
            }

            case TORSO -> {
                int partDamage = bodyDamage;
                int bloodLoss = calculateBloodLoss(partDamage, 1.00F, bloodMultiplier);

                data.damageBodyPart(BodyPart.TORSO, partDamage);
                data.setSangre(data.getSangre() - bloodLoss);
                data.addShock(shock + 15);
            }

            case LEFT_ARM, RIGHT_ARM -> {
                int partDamage = bodyDamage;
                int bloodLoss = calculateBloodLoss(partDamage, 0.50F, bloodMultiplier);

                data.damageBodyPart(hitPart, partDamage);
                data.setSangre(data.getSangre() - bloodLoss);
                data.addShock(shock);
            }

            case LEFT_LEG, RIGHT_LEG -> {
                int partDamage = bodyDamage;
                int bloodLoss = calculateBloodLoss(partDamage, 0.60F, bloodMultiplier);

                data.damageBodyPart(hitPart, partDamage);
                data.setSangre(data.getSangre() - bloodLoss);
                data.addShock(shock + 5);
            }
        }

        applyProjectileBleeding(data, hitPart, severity, bodyDamage, forceHeavyBleeding);
    }

    private static void applyProjectileBleeding(
            IPlayerData data,
            BodyPart hitPart,
            DamageSeverity severity,
            int bodyDamage,
            boolean forceHeavyBleeding
    ) {
        if (hitPart == null) return;

        if (forceHeavyBleeding || severity == DamageSeverity.CRITICAL || bodyDamage >= 8) {
            data.applyBleed(hitPart, BleedingType.HEAVY);
            return;
        }

        if (severity == DamageSeverity.HEAVY || bodyDamage >= 5) {
            data.applyBleed(hitPart, BleedingType.MEDIUM);
        }
    }

    private static int calculateBloodLoss(int damage, float locationMultiplier, float weaponMultiplier) {
        return Math.max(1, Math.round(damage * locationMultiplier * weaponMultiplier));
    }

    private static void applyMeleeDamage(IPlayerData data, BodyPart hitPart, int damage, DamageSeverity severity) {
        if (hitPart == null) hitPart = BodyPart.TORSO;

        data.damageBodyPart(hitPart, damage);
        data.addShock(Math.max(2, shockFor(severity) / 2));

        if (hitPart == BodyPart.HEAD || hitPart == BodyPart.TORSO) {
            data.setSangre(data.getSangre() - Math.max(1, damage / 3));
        }

        if (severity == DamageSeverity.CRITICAL) {
            data.applyBleed(hitPart, BleedingType.MEDIUM);
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

        if (damage >= 7) {
            data.applyBleed(BodyPart.LEFT_LEG, BleedingType.MEDIUM);
            data.applyBleed(BodyPart.RIGHT_LEG, BleedingType.MEDIUM);
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
            data.applyBleed(part, BleedingType.HEAVY);
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