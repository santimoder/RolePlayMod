package santi_moder.roleplaymod.server.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.player.BleedingType;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.common.player.PlayerData;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.SyncPlayerDataPacket;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import net.minecraft.world.phys.Vec3;

@Mod.EventBusSubscriber(modid = "roleplaymod")
public class PlayerDamageHandler {

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return; // solo server

        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {

            Vec3 impactPos = null;

            // 1️⃣ Si es proyectil, obtener posición exacta del impacto
            if (event.getSource().getEntity() instanceof AbstractArrow arrow) {
                impactPos = arrow.position();
            } else if (event.getSource().getEntity() instanceof ThrowableProjectile projectile) {
                impactPos = projectile.position();
            }

            // 2️⃣ Si es melee o otra fuente, usar posición del atacante
            if (impactPos == null && event.getSource().getEntity() instanceof LivingEntity attacker) {
                impactPos = attacker.position();
            }

            // 3️⃣ Default fallback → centro del jugador
            if (impactPos == null) {
                impactPos = player.position().add(0, player.getBbHeight() / 2.0, 0);
            }

            BodyPart hitPart = detectBodyPart(player, impactPos);

            // 🔥 Aplicar daño y sangrado según la parte
            int baseDamage = (int) event.getAmount();
            switch (hitPart) {
                case HEAD -> {
                    data.damageBodyPart(BodyPart.HEAD, baseDamage * 2); // daño extra
                    data.setSangre(data.getSangre() - baseDamage); // sangrado extra opcional
                }
                case TORSO -> {
                    data.damageBodyPart(BodyPart.TORSO, baseDamage);
                }
                case LEFT_ARM -> data.damageBodyPart(BodyPart.LEFT_ARM, baseDamage);
                case RIGHT_ARM -> data.damageBodyPart(BodyPart.RIGHT_ARM, baseDamage);
                case LEFT_LEG -> data.damageBodyPart(BodyPart.LEFT_LEG, baseDamage / 2);
                case RIGHT_LEG -> data.damageBodyPart(BodyPart.RIGHT_LEG, baseDamage / 2);
            }

            // Opcional: enviar mensaje al jugador
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("Golpe recibido en: " + hitPart.name())
            );

            ModNetwork.STATS_CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new SyncPlayerDataPacket(data)
            );
        });
    }

    private static BodyPart detectBodyPart(Player player, Vec3 impactPos) {

        double relativeY = (impactPos.y - player.getY()) / player.getBbHeight();

        // Altura básica
        if (relativeY >= 0.85) return BodyPart.HEAD;
        if (relativeY >= 0.3) { // torso y brazos
            double relativeX = impactPos.x - player.getX(); // izquierdo/derecho
            if (relativeX < player.getBbWidth() * 0.25) return BodyPart.LEFT_ARM;
            if (relativeX > player.getBbWidth() * 0.75) return BodyPart.RIGHT_ARM;
            return BodyPart.TORSO;
        }
        // Piernas
        double relativeX = impactPos.x - player.getX();
        if (relativeX < player.getBbWidth() * 0.5) return BodyPart.LEFT_LEG;
        return BodyPart.RIGHT_LEG;
    }
}