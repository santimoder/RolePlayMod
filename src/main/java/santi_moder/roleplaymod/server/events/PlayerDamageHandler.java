package santi_moder.roleplaymod.server.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.common.player.hitbox.BodyHitResult;
import santi_moder.roleplaymod.common.player.hitbox.BodyHitboxResolver;
import santi_moder.roleplaymod.server.combat.CustomDamageType;
import santi_moder.roleplaymod.server.combat.DamageProcessor;
import santi_moder.roleplaymod.server.combat.ProjectileMovementTrace;
import santi_moder.roleplaymod.server.combat.ProjectilePositionTracker;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = "roleplaymod")
public final class PlayerDamageHandler {

    private static final boolean DEBUG_DAMAGE_MESSAGES = true;

    private PlayerDamageHandler() {
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        CustomDamageType damageType = resolveDamageType(event);
        BodyPart hitPart = resolveHitPart(event, player, damageType);
        Vec3 sourcePosition = resolveSourcePosition(event, player);

        DamageProcessor.applyDamage(
                player,
                damageType,
                hitPart,
                event.getAmount(),
                sourcePosition
        );

        event.setCanceled(true);
        player.setHealth(player.getMaxHealth());

        if (DEBUG_DAMAGE_MESSAGES) {
            player.sendSystemMessage(Component.literal(
                    "Daño custom: " + damageType.name() + " en " + hitPart.name()
            ));
        }
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

        if (event.getSource().getEntity() instanceof LivingEntity) {
            return CustomDamageType.MELEE;
        }

        return CustomDamageType.GENERIC;
    }

    private static BodyPart resolveHitPart(
            LivingHurtEvent event,
            Player target,
            CustomDamageType damageType
    ) {
        if (damageType == CustomDamageType.FALL) {
            return BodyPart.LEFT_LEG;
        }

        if (damageType == CustomDamageType.DROWN
                || damageType == CustomDamageType.FIRE
                || damageType == CustomDamageType.EXPLOSION
                || damageType == CustomDamageType.VOID) {
            return BodyPart.TORSO;
        }

        Entity directEntity = event.getSource().getDirectEntity();

        if (directEntity != null) {
            Optional<BodyPart> directHit = resolveFromDirectEntity(directEntity, target);

            if (directHit.isPresent()) {
                return directHit.get();
            }
        }

        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            return detectFallbackBodyPart(target, attacker.position());
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

        Vec3 rayStart = damageEntity.position().subtract(direction.scale(3.0D));
        Vec3 rayEnd = damageEntity.position().add(direction.scale(3.0D));

        Optional<BodyHitResult> fallbackResult = BodyHitboxResolver.resolve(target, rayStart, rayEnd);

        return fallbackResult.map(BodyHitResult::bodyPart);
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

    private static BodyPart detectFallbackBodyPart(Player player, Vec3 impactPos) {
        double relativeY = (impactPos.y - player.getY()) / player.getBbHeight();

        if (relativeY >= 0.85D) return BodyPart.HEAD;
        if (relativeY >= 0.35D) return BodyPart.TORSO;

        double relativeX = impactPos.x - player.getX();

        return relativeX < 0.0D ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
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
}