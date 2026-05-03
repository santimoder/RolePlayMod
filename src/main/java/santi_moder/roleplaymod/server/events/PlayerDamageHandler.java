package santi_moder.roleplaymod.server.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.common.player.hitbox.BodyHitResult;
import santi_moder.roleplaymod.common.player.hitbox.BodyHitboxResolver;
import santi_moder.roleplaymod.server.combat.CustomDamageType;
import santi_moder.roleplaymod.server.combat.DamageProcessor;
import santi_moder.roleplaymod.server.combat.ProjectileMovementTrace;
import santi_moder.roleplaymod.server.combat.ProjectilePositionTracker;
import santi_moder.roleplaymod.server.combat.weapon.TaCZWeaponResolver;
import santi_moder.roleplaymod.server.combat.weapon.WeaponCategory;
import santi_moder.roleplaymod.server.combat.weapon.WeaponDamageProfile;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID)
public final class PlayerDamageHandler {

    private static final boolean DEBUG_DAMAGE_MESSAGES = false;

    private PlayerDamageHandler() {
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        if (player.isDeadOrDying()) return;

        event.setCanceled(true);

        CustomDamageType damageType = resolveDamageType(event);
        BodyPart hitPart = resolveHitPart(event, player, damageType);
        Vec3 sourcePosition = resolveSourcePosition(event, player);

        WeaponDamageProfile weaponProfile = resolveWeaponProfile(event, damageType);

        DamageProcessor.applyDamage(
                player,
                damageType,
                hitPart,
                event.getAmount(),
                sourcePosition,
                weaponProfile
        );

        player.hurtTime = 0;
        player.hurtDuration = 0;
        player.invulnerableTime = 0;

        if (!player.isDeadOrDying()) {
            player.setHealth(player.getMaxHealth());
        }

        if (DEBUG_DAMAGE_MESSAGES) {
            player.sendSystemMessage(Component.literal(
                    "Daño custom: " + damageType.name()
                            + " / arma=" + weaponProfile.category().name()
                            + " / parte=" + hitPart.name()
                            + " / raw=" + event.getAmount()
            ));
        }
    }

    private static WeaponDamageProfile resolveWeaponProfile(
            LivingHurtEvent event,
            CustomDamageType damageType
    ) {
        if (damageType != CustomDamageType.PROJECTILE) {
            return WeaponDamageProfile.generic();
        }

        Entity attacker = event.getSource().getEntity();

        if (attacker instanceof ServerPlayer shooter) {
            return TaCZWeaponResolver.resolveFromPlayer(shooter);
        }

        return WeaponDamageProfile.generic();
    }

    private static CustomDamageType resolveDamageType(LivingHurtEvent event) {
        if (event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) return CustomDamageType.VOID;
        if (event.getSource().is(DamageTypes.FALL)) return CustomDamageType.FALL;
        if (event.getSource().is(DamageTypes.DROWN)) return CustomDamageType.DROWN;

        if (event.getSource().is(DamageTypes.IN_FIRE)
                || event.getSource().is(DamageTypes.ON_FIRE)
                || event.getSource().is(DamageTypes.LAVA)
                || event.getSource().is(DamageTypes.HOT_FLOOR)) {
            return CustomDamageType.FIRE;
        }

        if (event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            return CustomDamageType.EXPLOSION;
        }

        Entity directEntity = event.getSource().getDirectEntity();

        if (directEntity instanceof Projectile || looksLikeProjectile(directEntity)) {
            return CustomDamageType.PROJECTILE;
        }

        if (looksLikeGunDamage(event)) {
            return CustomDamageType.PROJECTILE;
        }

        if (event.getSource().getEntity() instanceof LivingEntity) {
            return CustomDamageType.MELEE;
        }

        return CustomDamageType.GENERIC;
    }

    @SubscribeEvent
    public static void onPlayerKnockback(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        event.setCanceled(true);
    }

    private static BodyPart resolveHitPart(
            LivingHurtEvent event,
            Player target,
            CustomDamageType damageType
    ) {
        if (damageType == CustomDamageType.FALL) return BodyPart.LEFT_LEG;
        if (damageType == CustomDamageType.FIRE) return BodyPart.TORSO;
        if (damageType == CustomDamageType.DROWN) return BodyPart.TORSO;
        if (damageType == CustomDamageType.EXPLOSION) return BodyPart.TORSO;
        if (damageType == CustomDamageType.VOID) return BodyPart.TORSO;

        Entity directEntity = event.getSource().getDirectEntity();

        if (directEntity != null && directEntity != target) {
            Optional<BodyPart> directHit = resolveFromDirectEntity(directEntity, target);

            if (directHit.isPresent()) {
                return directHit.get();
            }
        }

        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            Optional<BodyPart> aimedHit = resolveFromShooterAim(attacker, target);

            if (aimedHit.isPresent()) {
                return aimedHit.get();
            }

            if (damageType == CustomDamageType.MELEE) {
                return resolveMeleeByAttackerHeight(attacker, target);
            }
        }

        return BodyPart.TORSO;
    }

    private static Optional<BodyPart> resolveFromDirectEntity(Entity damageEntity, Player target) {
        Optional<ProjectileMovementTrace> trace = ProjectilePositionTracker.getMovementTrace(damageEntity);

        if (trace.isPresent()) {
            Optional<BodyHitResult> result = BodyHitboxResolver.resolve(
                    target,
                    trace.get().previousPosition(),
                    trace.get().currentPosition()
            );

            if (result.isPresent()) {
                return result.map(BodyHitResult::bodyPart);
            }
        }

        Vec3 motion = damageEntity.getDeltaMovement();

        if (motion.lengthSqr() <= 0.0001D) {
            return Optional.empty();
        }

        Vec3 direction = motion.normalize();

        Vec3 rayStart = damageEntity.position().subtract(direction.scale(4.0D));
        Vec3 rayEnd = damageEntity.position().add(direction.scale(4.0D));

        Optional<BodyHitResult> fallbackResult = BodyHitboxResolver.resolve(target, rayStart, rayEnd);

        return fallbackResult.map(BodyHitResult::bodyPart);
    }

    private static Optional<BodyPart> resolveFromShooterAim(LivingEntity attacker, Player target) {
        if (attacker.level() != target.level()) {
            return Optional.empty();
        }

        Vec3 start = attacker.getEyePosition();
        Vec3 look = attacker.getLookAngle();

        if (look.lengthSqr() > 0.0001D) {
            Vec3 end = start.add(look.normalize().scale(start.distanceTo(target.getEyePosition()) + 8.0D));

            Optional<BodyHitResult> lookResult = BodyHitboxResolver.resolve(target, start, end);

            if (lookResult.isPresent()) {
                return lookResult.map(BodyHitResult::bodyPart);
            }
        }

        /*
         * Fallback compatible con hitscan de mods como TaCZ:
         * si el DamageSource no trae proyectil, usamos una línea desde los ojos del atacante
         * hacia el centro real del target. Esto evita caer en piernas por usar attacker.position().
         */
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        Vec3 directionToTarget = targetCenter.subtract(start);

        if (directionToTarget.lengthSqr() <= 0.0001D) {
            return Optional.empty();
        }

        Vec3 end = start.add(directionToTarget.normalize().scale(start.distanceTo(targetCenter) + 2.0D));

        Optional<BodyHitResult> centerResult = BodyHitboxResolver.resolve(target, start, end);

        return centerResult.map(BodyHitResult::bodyPart);
    }

    private static BodyPart resolveMeleeByAttackerHeight(LivingEntity attacker, Player target) {
        double relativeY = (attacker.getEyeY() - target.getY()) / target.getBbHeight();

        if (relativeY >= 0.82D) return BodyPart.HEAD;
        if (relativeY >= 0.45D) return BodyPart.TORSO;

        return Math.random() < 0.5D ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
    }

    private static Vec3 resolveSourcePosition(LivingHurtEvent event, ServerPlayer player) {
        Vec3 sourcePosition = event.getSource().getSourcePosition();

        if (sourcePosition != null) return sourcePosition;

        if (event.getSource().getDirectEntity() != null) {
            return event.getSource().getDirectEntity().position();
        }

        if (event.getSource().getEntity() != null) {
            return event.getSource().getEntity().position();
        }

        return player.position();
    }

    private static boolean looksLikeProjectile(Entity entity) {
        if (entity == null) return false;

        String className = entity.getClass().getName().toLowerCase();

        return className.contains("bullet")
                || className.contains("projectile")
                || className.contains("ammo")
                || className.contains("shot")
                || className.contains("shell")
                || className.contains("pellet");
    }

    private static boolean looksLikeGunDamage(LivingHurtEvent event) {
        String sourceId = event.getSource().typeHolder()
                .unwrapKey()
                .map(key -> key.location().toString().toLowerCase())
                .orElse("");

        String msgId = event.getSource().getMsgId().toLowerCase();

        return sourceId.contains("tacz")
                || sourceId.contains("gun")
                || sourceId.contains("bullet")
                || sourceId.contains("shot")
                || sourceId.contains("firearm")
                || sourceId.contains("pistol")
                || sourceId.contains("rifle")
                || msgId.contains("tacz")
                || msgId.contains("gun")
                || msgId.contains("bullet")
                || msgId.contains("shot")
                || msgId.contains("firearm")
                || msgId.contains("pistol")
                || msgId.contains("rifle");
    }
}