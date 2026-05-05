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
import santi_moder.roleplaymod.server.combat.weapon.AmmoCaliberProfile;
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
            BodyPart safePart = hitPart == null ? BodyPart.TORSO : hitPart;

            if (type == CustomDamageType.EXPLOSION) {
                ExplosionDamageProcessor.applyExplosionTrauma(target, sourcePosition, rawDamage);
                return;
            }

            if (type == CustomDamageType.PROJECTILE) {
                if (weaponProfile == null || !weaponProfile.enabled()) {
                    sync(target, data);
                    return;
                }
            }

            float effectiveDamage = getEffectiveRawDamage(type, rawDamage, weaponProfile);

            if (type == CustomDamageType.PROJECTILE) {
                effectiveDamage = applyDistanceFalloff(effectiveDamage, target, sourcePosition, weaponProfile);
            }

            DamageSeverity severity = DamageSeverity.fromDamage(effectiveDamage);

            DamageResult result = switch (type) {
                case PROJECTILE -> calculateProjectileDamage(safePart, effectiveDamage, severity, weaponProfile);
                case MELEE -> calculateMeleeDamage(safePart, effectiveDamage, severity);
                case FALL -> calculateFallDamage(effectiveDamage, severity);
                case FIRE -> calculateFireDamage(effectiveDamage, severity);
                case DROWN -> calculateDrownDamage(effectiveDamage, severity);
                case VOID -> calculateVoidDamage();
                case GENERIC -> calculateMeleeDamage(safePart, effectiveDamage, severity);
                case EXPLOSION -> DamageResult.empty();
            };

            applyResult(data, result);

            int unconsciousTicks = MedicalUtils.getUnconsciousDurationTicks(data, result.bloodLoss());

            if (unconsciousTicks > 0) {
                data.setInconsciente(true);
                data.setUnconsciousTicks(Math.max(data.getUnconsciousTicks(), unconsciousTicks));
            }

            if (type == CustomDamageType.FALL && effectiveDamage + 3 >= 17) {
                data.setInconsciente(true);
                data.setUnconsciousTicks(Math.max(data.getUnconsciousTicks(), 70 * 20));
            }

            finishDamage(target, data, result.effectIntensity());
        });
    }

    public static void finishDamage(ServerPlayer target, IPlayerData data) {
        finishDamage(target, data, 0.65F);
    }

    public static void finishDamage(ServerPlayer target, IPlayerData data, float effectIntensity) {
        data.applyBodyPartEffects();

        if (MedicalUtils.checkAndKill(target, data)) {
            sync(target, data);
            return;
        }

        if (!target.isDeadOrDying()) {
            target.setHealth(target.getMaxHealth());
        }

        ModNetwork.STATS_CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> target),
                new MedicalEffectS2CPacket(
                        MedicalEffectS2CPacket.Type.DAMAGE_HIT,
                        clampFloat(effectIntensity, 0.25F, 1.0F)
                )
        );

        sync(target, data);
    }

    private static float getEffectiveRawDamage(
            CustomDamageType type,
            float rawDamage,
            WeaponDamageProfile profile
    ) {
        if (type == CustomDamageType.PROJECTILE && profile != null && profile.enabled()) {
            return Math.max(1.0F, profile.baseDamage());
        }

        return Math.max(1.0F, rawDamage);
    }



    private static float applyDistanceFalloff(
            float damage,
            ServerPlayer target,
            Vec3 sourcePosition,
            WeaponDamageProfile profile
    ) {
        if (target == null || profile == null || sourcePosition == null) return damage;
        if (profile.maxRange() <= 0.0F) return damage;

        double distance = target.position().distanceTo(sourcePosition);

        if (distance <= profile.effectiveRange()) {
            return damage;
        }

        if (distance >= profile.maxRange()) {
            return Math.max(0.75F, damage * getMinimumRangeMultiplier(profile.category()));
        }

        double t = (distance - profile.effectiveRange()) / (profile.maxRange() - profile.effectiveRange());

        float minMultiplier = getMinimumRangeMultiplier(profile.category());
        float multiplier = (float) (1.0D - (t * (1.0F - minMultiplier)));

        return Math.max(0.75F, damage * multiplier);
    }

    private static float getMinimumRangeMultiplier(WeaponCategory category) {
        return switch (category) {
            case SHOTGUN -> 0.04F;
            case PISTOL -> 0.30F;
            case SMG -> 0.25F;
            case RIFLE -> 0.42F;
            case SNIPER -> 0.55F;
            case EXPLOSIVE -> 0.50F;
            case MELEE, GENERIC -> 1.0F;
        };
    }

    private static DamageResult calculateProjectileDamage(
            BodyPart part,
            float rawDamage,
            DamageSeverity severity,
            WeaponDamageProfile profile
    ) {
        WeaponCategory category = profile == null ? WeaponCategory.GENERIC : profile.category();

        AmmoCaliberProfile caliber = profile == null ? null : profile.caliberProfile();

        float caliberBloodMultiplier = caliber == null ? 1.0F : caliber.bloodMultiplier();
        float caliberShockMultiplier = caliber == null ? 1.0F : caliber.shockMultiplier();

        float weaponBodyMultiplier = profile == null ? 1.0F : profile.bodyDamageMultiplier();
        float weaponBloodMultiplier = profile == null ? 1.0F : profile.bloodLossMultiplier();
        float weaponShockMultiplier = profile == null ? 1.0F : profile.shockMultiplier();

        boolean heavyBleedWeapon = profile != null && profile.causesHeavyBleeding();
        boolean highPenetration = profile != null && profile.isHighPenetration();

        float categoryBodyMultiplier = switch (category) {
            case PISTOL -> 0.72F;
            case SMG -> 0.58F;
            case RIFLE -> 0.72F;
            case SHOTGUN -> 0.85F;
            case SNIPER -> 1.05F;
            case EXPLOSIVE -> 1.20F;
            case MELEE, GENERIC -> 0.70F;
        };

        float categoryBloodMultiplier = switch (category) {
            case PISTOL -> 0.80F;
            case SMG -> 0.72F;
            case RIFLE -> 0.88F;
            case SHOTGUN -> 1.05F;
            case SNIPER -> 1.20F;
            case EXPLOSIVE -> 1.25F;
            case MELEE, GENERIC -> 0.75F;
        };

        float categoryShockMultiplier = switch (category) {
            case PISTOL -> 1.05F;
            case SMG -> 0.95F;
            case RIFLE -> 1.15F;
            case SHOTGUN -> 1.35F;
            case SNIPER -> 1.55F;
            case EXPLOSIVE -> 1.50F;
            case MELEE, GENERIC -> 0.85F;
        };

        int baseBodyDamage = Math.max(1, Math.round(
                rawDamage * 0.70F * weaponBodyMultiplier * categoryBodyMultiplier
        ));

        float partBodyMultiplier = switch (part) {
            case HEAD -> switch (category) {
                case PISTOL -> 1.45F;
                case SMG -> 1.35F;
                case RIFLE -> 1.55F;
                case SHOTGUN -> 1.35F;
                case SNIPER -> 2.10F;
                default -> 1.30F;
            };
            case TORSO -> 0.95F;
            case LEFT_ARM, RIGHT_ARM -> 0.45F;
            case LEFT_LEG, RIGHT_LEG -> 0.55F;
        };

        float partBloodMultiplier = bloodMultiplier(part);
        float partShockMultiplier = shockMultiplier(part);

        int bodyDamage = Math.max(1, Math.round(baseBodyDamage * partBodyMultiplier));

        int bloodLoss = Math.max(0, Math.round(
                bodyDamage
                        * 0.30F
                        * caliberBloodMultiplier
                        * weaponBloodMultiplier
                        * categoryBloodMultiplier
                        * partBloodMultiplier
        ));

        int shock = Math.max(1, Math.round(
                shockFor(severity)
                        * caliberShockMultiplier
                        * weaponShockMultiplier
                        * categoryShockMultiplier
                        * partShockMultiplier
        ));

        BleedingType bleeding = resolveProjectileBleeding(
                severity,
                category,
                bodyDamage,
                heavyBleedWeapon,
                highPenetration,
                part
        );

        if (part == BodyPart.HEAD) {
            shock += switch (category) {
                case PISTOL -> 28;
                case SMG -> 24;
                case RIFLE -> 38;
                case SHOTGUN -> 42;
                case SNIPER -> 70;
                default -> 20;
            };

            bloodLoss += switch (category) {
                case PISTOL, SMG -> 4;
                case RIFLE -> 7;
                case SHOTGUN -> 8;
                case SNIPER -> 16;
                default -> 3;
            };

            if (category == WeaponCategory.SNIPER && highPenetration) {
                bodyDamage += 10;
                bleeding = BleedingType.HEAVY;
            } else {
                bleeding = maxBleeding(bleeding, BleedingType.MEDIUM);
            }
        }

        if (part == BodyPart.TORSO) {
            shock += switch (category) {
                case PISTOL -> 8;
                case SMG -> 7;
                case RIFLE -> 16;
                case SHOTGUN -> 20;
                case SNIPER -> 35;
                default -> 6;
            };

            if (category == WeaponCategory.SNIPER) {
                bleeding = maxBleeding(bleeding, BleedingType.HEAVY);
            } else if (category == WeaponCategory.SHOTGUN || category == WeaponCategory.RIFLE) {
                bleeding = maxBleeding(bleeding, BleedingType.MEDIUM);
            }
        }

        if (part == BodyPart.LEFT_ARM || part == BodyPart.RIGHT_ARM) {
            bloodLoss = Math.max(0, Math.round(bloodLoss * 0.65F));
            shock = Math.max(1, Math.round(shock * 0.70F));
        }

        if (part == BodyPart.LEFT_LEG || part == BodyPart.RIGHT_LEG) {
            bloodLoss = Math.max(0, Math.round(bloodLoss * 0.75F));
            shock = Math.max(1, Math.round(shock * 0.80F));
        }

        return new DamageResult(
                new PartDamage(part, bodyDamage, bleeding),
                clampBloodLoss(bloodLoss),
                clampShock(shock),
                effectIntensity(severity, category)
        );
    }

    private static DamageResult calculateMeleeDamage(
            BodyPart part,
            float rawDamage,
            DamageSeverity severity
    ) {
        int damage = Math.max(1, Math.round(rawDamage * switch (severity) {
            case LIGHT -> 0.45F;
            case MEDIUM -> 0.65F;
            case HEAVY -> 0.85F;
            case CRITICAL -> 1.05F;
        }));

        damage = Math.max(1, Math.round(damage * switch (part) {
            case HEAD -> 1.25F;
            case TORSO -> 1.00F;
            case LEFT_ARM, RIGHT_ARM -> 0.75F;
            case LEFT_LEG, RIGHT_LEG -> 0.85F;
        }));

        int bloodLoss = switch (part) {
            case HEAD -> Math.max(1, damage / 2);
            case TORSO -> Math.max(1, damage / 3);
            case LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG ->
                    severity == DamageSeverity.CRITICAL ? Math.max(1, damage / 4) : 0;
        };

        int shock = Math.max(2, shockFor(severity) / 2);

        BleedingType bleeding = severity == DamageSeverity.CRITICAL
                ? BleedingType.MEDIUM
                : BleedingType.NONE;

        return new DamageResult(
                new PartDamage(part, damage, bleeding),
                bloodLoss,
                shock,
                effectIntensity(severity, WeaponCategory.MELEE)
        );
    }

    private static DamageResult calculateFallDamage(float rawDamage, DamageSeverity severity) {
        int fallDamage = Math.max(1, Math.round(rawDamage));

        // Minecraft normalmente empieza a dañar después de ~3 bloques.
        // Entonces aproximamos altura real como daño + 3.
        int estimatedFallBlocks = fallDamage + 3;

        DamageResult result = DamageResult.empty();

        int legDamage;
        int torsoDamage = 0;
        int headDamage = 0;
        int bloodLoss = 0;
        int shock;
        BleedingType legBleed;

        if (estimatedFallBlocks <= 5) {
            legDamage = 5;
            shock = 10;
            legBleed = BleedingType.NONE;

        } else if (estimatedFallBlocks <= 8) {
            legDamage = 9;
            torsoDamage = 1;
            shock = 18;
            legBleed = BleedingType.LIGHT;

        } else if (estimatedFallBlocks <= 12) {
            legDamage = 14;
            torsoDamage = 4;
            bloodLoss = 2;
            shock = 32;
            legBleed = BleedingType.LIGHT;

        } else if (estimatedFallBlocks <= 16) {
            legDamage = 22;
            torsoDamage = 12;
            headDamage = 2;
            bloodLoss = 6;
            shock = 70;
            legBleed = BleedingType.MEDIUM;

        } else if (estimatedFallBlocks <= 20) {
            legDamage = 26;
            torsoDamage = 22;
            headDamage = 4;
            bloodLoss = 10;
            shock = 90;
            legBleed = BleedingType.HEAVY;

        } else {
            legDamage = 30;
            torsoDamage = 35;
            headDamage = 8;
            bloodLoss = 18;
            shock = 100;
            legBleed = BleedingType.HEAVY;
        }

        result.addPartDamage(BodyPart.LEFT_LEG, legDamage, legBleed);
        result.addPartDamage(BodyPart.RIGHT_LEG, legDamage, legBleed);

        if (torsoDamage > 0) {
            result.addPartDamage(BodyPart.TORSO, torsoDamage, BleedingType.NONE);
        }

        if (headDamage > 0) {
            result.addPartDamage(BodyPart.HEAD, headDamage, BleedingType.NONE);
        }

        if (bloodLoss > 0) {
            result.addBloodLoss(bloodLoss);
        }

        result.addShock(shock);
        result.setEffectIntensity(estimatedFallBlocks >= 17 ? 1.0F : 0.75F);

        return result;
    }

    private static DamageResult calculateFireDamage(float rawDamage, DamageSeverity severity) {
        int burn = Math.max(1, Math.round(rawDamage * 0.55F));
        DamageResult result = DamageResult.empty();

        result.addPartDamage(BodyPart.TORSO, burn, BleedingType.NONE);
        result.addPartDamage(BodyPart.HEAD, Math.max(1, burn / 2), BleedingType.NONE);
        result.addPartDamage(BodyPart.LEFT_ARM, Math.max(1, burn / 2), BleedingType.NONE);
        result.addPartDamage(BodyPart.RIGHT_ARM, Math.max(1, burn / 2), BleedingType.NONE);
        result.addPartDamage(BodyPart.LEFT_LEG, Math.max(1, burn / 2), BleedingType.NONE);
        result.addPartDamage(BodyPart.RIGHT_LEG, Math.max(1, burn / 2), BleedingType.NONE);

        result.addBloodLoss(Math.max(0, burn / 4));
        result.addShock(Math.max(3, shockFor(severity) / 2));
        result.setEffectIntensity(effectIntensity(severity, WeaponCategory.GENERIC));

        return result;
    }

    private static DamageResult calculateDrownDamage(float rawDamage, DamageSeverity severity) {
        int damage = Math.max(1, Math.round(rawDamage * 0.65F));

        DamageResult result = DamageResult.empty();
        result.addPartDamage(BodyPart.TORSO, damage, BleedingType.NONE);
        result.addPartDamage(BodyPart.HEAD, Math.max(1, damage / 2), BleedingType.NONE);
        result.addShock(shockFor(severity) + 10);
        result.setEffectIntensity(effectIntensity(severity, WeaponCategory.GENERIC));

        return result;
    }

    private static DamageResult calculateVoidDamage() {
        DamageResult result = DamageResult.empty();

        for (BodyPart part : BodyPart.values()) {
            result.addPartDamage(part, 999, BleedingType.HEAVY);
        }

        result.addBloodLoss(100);
        result.addShock(100);
        result.setEffectIntensity(1.0F);

        return result;
    }

    private static void applyResult(IPlayerData data, DamageResult result) {
        for (PartDamage partDamage : result.partDamages()) {
            data.damageBodyPart(partDamage.part(), partDamage.damage());

            if (partDamage.bleeding() != BleedingType.NONE) {
                data.applyBleed(partDamage.part(), partDamage.bleeding());
            }
        }

        if (result.bloodLoss() > 0) {
            data.setSangre(data.getSangre() - result.bloodLoss());
        }

        if (result.shock() > 0) {
            data.addShock(result.shock());
        }

        data.applyBodyPartEffects();
    }

    private static BleedingType resolveProjectileBleeding(
            DamageSeverity severity,
            WeaponCategory category,
            int bodyDamage,
            boolean heavyBleedWeapon,
            boolean highPenetration,
            BodyPart part
    ) {
        if (heavyBleedWeapon && severity.ordinal() >= DamageSeverity.HEAVY.ordinal()) {
            return BleedingType.HEAVY;
        }

        if (category == WeaponCategory.SNIPER || category == WeaponCategory.SHOTGUN) {
            if (severity.ordinal() >= DamageSeverity.HEAVY.ordinal() || bodyDamage >= 8) {
                return BleedingType.HEAVY;
            }
        }

        if (highPenetration && (part == BodyPart.HEAD || part == BodyPart.TORSO)) {
            if (severity.ordinal() >= DamageSeverity.HEAVY.ordinal()) {
                return BleedingType.HEAVY;
            }
        }

        if (severity == DamageSeverity.CRITICAL || bodyDamage >= 8) {
            return BleedingType.MEDIUM;
        }

        if (severity == DamageSeverity.HEAVY || bodyDamage >= 5) {
            return BleedingType.LIGHT;
        }

        return BleedingType.NONE;
    }

    private static float bodyDamageMultiplier(BodyPart part) {
        return switch (part) {
            case HEAD -> 1.35F;
            case TORSO -> 0.95F;
            case LEFT_ARM, RIGHT_ARM -> 0.60F;
            case LEFT_LEG, RIGHT_LEG -> 0.70F;
        };
    }

    private static float bloodMultiplier(BodyPart part) {
        return switch (part) {
            case HEAD -> 1.25F;
            case TORSO -> 1.20F;
            case LEFT_ARM, RIGHT_ARM -> 0.65F;
            case LEFT_LEG, RIGHT_LEG -> 0.80F;
        };
    }

    private static float shockMultiplier(BodyPart part) {
        return switch (part) {
            case HEAD -> 1.65F;
            case TORSO -> 1.25F;
            case LEFT_ARM, RIGHT_ARM -> 0.75F;
            case LEFT_LEG, RIGHT_LEG -> 0.90F;
        };
    }

    private static int shockFor(DamageSeverity severity) {
        return switch (severity) {
            case LIGHT -> 5;
            case MEDIUM -> 13;
            case HEAVY -> 25;
            case CRITICAL -> 42;
        };
    }

    private static float effectIntensity(DamageSeverity severity, WeaponCategory category) {
        float base = switch (severity) {
            case LIGHT -> 0.35F;
            case MEDIUM -> 0.55F;
            case HEAVY -> 0.75F;
            case CRITICAL -> 1.0F;
        };

        if (category == WeaponCategory.SNIPER || category == WeaponCategory.SHOTGUN) {
            base += 0.10F;
        }

        return clampFloat(base, 0.25F, 1.0F);
    }

    private static BleedingType maxBleeding(BleedingType current, BleedingType candidate) {
        if (current == null) return candidate == null ? BleedingType.NONE : candidate;
        if (candidate == null) return current;
        return candidate.ordinal() > current.ordinal() ? candidate : current;
    }

    private static int clampBloodLoss(int value) {
        return Math.max(0, Math.min(35, value));
    }

    private static int clampShock(int value) {
        return Math.max(0, Math.min(85, value));
    }

    private static float clampFloat(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void sync(ServerPlayer target, IPlayerData data) {
        ModNetwork.STATS_CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> target),
                new SyncPlayerDataPacket(data)
        );
    }

    private record PartDamage(
            BodyPart part,
            int damage,
            BleedingType bleeding
    ) {
    }

    private static final class DamageResult {

        private final java.util.List<PartDamage> partDamages = new java.util.ArrayList<>();
        private int bloodLoss;
        private int shock;
        private float effectIntensity;

        private DamageResult(
                PartDamage partDamage,
                int bloodLoss,
                int shock,
                float effectIntensity
        ) {
            if (partDamage != null) {
                this.partDamages.add(partDamage);
            }

            this.bloodLoss = clampBloodLoss(bloodLoss);
            this.shock = clampShock(shock);
            this.effectIntensity = clampFloat(effectIntensity, 0.25F, 1.0F);
        }

        private static DamageResult empty() {
            return new DamageResult(null, 0, 0, 0.35F);
        }

        private void addPartDamage(BodyPart part, int damage, BleedingType bleeding) {
            if (part == null || damage <= 0) return;
            partDamages.add(new PartDamage(part, damage, bleeding == null ? BleedingType.NONE : bleeding));
        }

        private void addBloodLoss(int amount) {
            if (amount <= 0) return;
            bloodLoss = clampBloodLoss(bloodLoss + amount);
        }

        private void addShock(int amount) {
            if (amount <= 0) return;
            shock = clampShock(shock + amount);
        }

        private void setEffectIntensity(float value) {
            effectIntensity = clampFloat(value, 0.25F, 1.0F);
        }

        private java.util.List<PartDamage> partDamages() {
            return partDamages;
        }

        private int bloodLoss() {
            return bloodLoss;
        }

        private int shock() {
            return shock;
        }

        private float effectIntensity() {
            return effectIntensity;
        }
    }
}