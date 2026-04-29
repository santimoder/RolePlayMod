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
            int damage = Math.max(1, (int) Math.ceil(rawDamage));

            switch (type) {
                case PROJECTILE -> applyProjectileDamage(data, hitPart, damage);
                case MELEE -> applyMeleeDamage(data, hitPart, damage);
                case FALL -> applyFallDamage(data, damage);
                case FIRE -> applyFireDamage(data, damage);
                case DROWN -> applyDrownDamage(data, damage);
                case EXPLOSION -> ExplosionDamageProcessor.applyExplosionTrauma(target, sourcePosition, rawDamage);
                case VOID -> applyVoidDamage(data);
                case GENERIC -> applyMeleeDamage(data, hitPart, damage);
            }

            if (type != CustomDamageType.EXPLOSION) {
                finishDamage(target, data);
            }
        });
    }

    public static void finishDamage(ServerPlayer target, IPlayerData data) {
        data.applyBodyPartEffects();
        MedicalUtils.checkAndKill(target, data);

        if (!target.isDeadOrDying()) {
            target.setHealth(target.getMaxHealth());
        }

        ModNetwork.STATS_CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> target),
                new SyncPlayerDataPacket(data)
        );
    }

    private static void applyProjectileDamage(IPlayerData data, BodyPart hitPart, int damage) {
        switch (hitPart) {
            case HEAD -> {
                data.damageBodyPart(BodyPart.HEAD, damage * 2);
                data.setSangre(data.getSangre() - damage * 2);
            }
            case TORSO -> {
                data.damageBodyPart(BodyPart.TORSO, damage);
                data.setSangre(data.getSangre() - Math.max(1, damage));
            }
            case LEFT_ARM -> {
                data.damageBodyPart(BodyPart.LEFT_ARM, damage);
                data.setSangre(data.getSangre() - Math.max(1, damage / 2));
            }
            case RIGHT_ARM -> {
                data.damageBodyPart(BodyPart.RIGHT_ARM, damage);
                data.setSangre(data.getSangre() - Math.max(1, damage / 2));
            }
            case LEFT_LEG -> {
                data.damageBodyPart(BodyPart.LEFT_LEG, Math.max(1, damage));
                data.setSangre(data.getSangre() - Math.max(1, damage / 2));
            }
            case RIGHT_LEG -> {
                data.damageBodyPart(BodyPart.RIGHT_LEG, Math.max(1, damage));
                data.setSangre(data.getSangre() - Math.max(1, damage / 2));
            }
        }
    }

    private static void applyMeleeDamage(IPlayerData data, BodyPart hitPart, int damage) {
        data.damageBodyPart(hitPart, damage);

        if (hitPart == BodyPart.HEAD || hitPart == BodyPart.TORSO) {
            data.setSangre(data.getSangre() - Math.max(1, damage / 2));
        }
    }

    private static void applyFallDamage(IPlayerData data, int damage) {
        int legDamage = Math.max(1, damage);
        int torsoDamage = Math.max(1, damage / 2);

        data.damageBodyPart(BodyPart.LEFT_LEG, legDamage);
        data.damageBodyPart(BodyPart.RIGHT_LEG, legDamage);

        if (damage >= 5) {
            data.damageBodyPart(BodyPart.TORSO, torsoDamage);
            data.setSangre(data.getSangre() - Math.max(1, damage / 2));
        }

        if (damage >= 9) {
            data.damageBodyPart(BodyPart.HEAD, Math.max(1, damage / 3));
        }
    }

    private static void applyFireDamage(IPlayerData data, int damage) {
        int burnDamage = Math.max(1, damage);

        data.damageBodyPart(BodyPart.HEAD, Math.max(1, burnDamage / 2));
        data.damageBodyPart(BodyPart.TORSO, burnDamage);
        data.damageBodyPart(BodyPart.LEFT_ARM, Math.max(1, burnDamage / 2));
        data.damageBodyPart(BodyPart.RIGHT_ARM, Math.max(1, burnDamage / 2));
        data.damageBodyPart(BodyPart.LEFT_LEG, Math.max(1, burnDamage / 2));
        data.damageBodyPart(BodyPart.RIGHT_LEG, Math.max(1, burnDamage / 2));

        data.setSangre(data.getSangre() - Math.max(1, burnDamage / 2));
    }

    private static void applyDrownDamage(IPlayerData data, int damage) {
        data.damageBodyPart(BodyPart.HEAD, Math.max(1, damage / 2));
        data.damageBodyPart(BodyPart.TORSO, damage);
        data.setSangre(data.getSangre() - Math.max(1, damage));
    }

    private static void applyVoidDamage(IPlayerData data) {
        data.setSangre(0);

        for (BodyPart part : BodyPart.values()) {
            data.setBodyHp(part, 0);
        }
    }
}