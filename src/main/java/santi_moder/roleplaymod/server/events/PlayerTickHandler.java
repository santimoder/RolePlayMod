package santi_moder.roleplaymod.server.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.common.player.BleedingType;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.network.MedicalEffectS2CPacket;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.SyncPlayerDataPacket;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "roleplaymod")
public class PlayerTickHandler {
    private static final int LOGIC_INTERVAL = 20; // 1 segundo
    private static final int STAMINA_REGEN_DELAY = 60; // 3 segundos (20 ticks * 3)
    private static final int BLEEDING_INTERVAL = 1200; // 1 minuto

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.player.level().isClientSide) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {

            var food = player.getFoodData();

            // =====================
            // LÓGICA CADA TICK
            // =====================
            data.tickStaminaCooldown();
            data.applyBodyPartEffects();

            if (player.hurtTime > 0 || player.invulnerableTime > 0) {
                player.hurtTime = 0;
                player.hurtDuration = 0;
                player.invulnerableTime = 0;
            }

            // Sprint consume stamina continuamente
            if (player.isSprinting()) {
                if (data.canSprint() && !data.isStaminaExhausted()) {

                    // consumir stamina cada 10 ticks = 0.5 segundos
                    if (player.tickCount % 10 == 0) {
                        int before = data.getStamina();
                        data.setStamina(Math.max(0, data.getStamina() - 1));
                        System.out.println("[SERVER][STAMINA] " + player.getName().getString()
                                + " sprinting=true before=" + before
                                + " after=" + data.getStamina());
                    }

                    data.setStaminaRegenCooldown(STAMINA_REGEN_DELAY);
                } else {
                    player.setSprinting(false);
                    System.out.println("[SERVER][STAMINA] " + player.getName().getString()
                            + " sprint cancelado");
                }
            }

            if (data.getStamina() <= 0) {
                data.setStaminaExhausted(true);
                player.setSprinting(false);
            }

            if (data.isStaminaExhausted() && data.getStamina() >= 20) {
                data.setStaminaExhausted(false);
            }

            if (data.isStaminaExhausted()) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                        40,
                        2,
                        true,
                        false
                ));
            }

            // =====================
            // LÓGICA CADA 20 TICKS
            // =====================
            if (player.tickCount % LOGIC_INTERVAL != 0) {
                ModNetwork.STATS_CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncPlayerDataPacket(data)
                );
                return;
            }

            food.setSaturation(0);

            if (player.tickCount % BLEEDING_INTERVAL == 0) {
                int sangreAntes = data.getSangre();

                data.tickBleeding();

                if (data.getSangre() < sangreAntes) {
                    int sangrePerdida = sangreAntes - data.getSangre();
                    float intensity = Math.min(1.0F, sangrePerdida / 8.0F);

                    ModNetwork.STATS_CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new MedicalEffectS2CPacket(MedicalEffectS2CPacket.Type.BLEED_PULSE, intensity)
                    );
                }
            }

            if (santi_moder.roleplaymod.common.util.MedicalUtils.checkAndKill(player, data)) {
                ModNetwork.STATS_CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncPlayerDataPacket(data)
                );
                return;
            }

            boolean shouldBeUnconscious =
                    santi_moder.roleplaymod.common.util.MedicalUtils.shouldBeUnconscious(data);

            if (shouldBeUnconscious && !data.isInconsciente()) {
                data.setInconsciente(true);
                data.incrementarInconsciencias();
                player.setSprinting(false);
                player.stopRiding();
            }

            if (!shouldBeUnconscious && data.isInconsciente()
                    && santi_moder.roleplaymod.common.util.MedicalUtils.canWakeUp(data)) {
                data.setInconsciente(false);
            }

            int MAX_STAMINA = 100;

            if (!player.isSprinting()
                    && player.onGround()
                    && data.canRegenerateStamina()
                    && data.getStamina() < MAX_STAMINA) {

                int regenAmount = (int) (2 * data.getStaminaMultiplier());
                int before = data.getStamina();
                data.setStamina(Math.min(MAX_STAMINA, data.getStamina() + regenAmount));
                int gained = data.getStamina() - before;

                data.addStaminaRegenBuffer(gained);

                if (data.shouldConsumeFoodForStamina() && food.getFoodLevel() > 0) {
                    food.setFoodLevel(Math.max(0, food.getFoodLevel() - 1));
                    data.consumeStaminaRegenBuffer();
                }
            }

            if (!data.wasOnGround() && player.onGround()) {
                float fallDist = player.fallDistance;

                if (fallDist > 3) {
                    int legDamage = (int) ((fallDist - 3) * 0.5f);
                    data.damageBodyPart(BodyPart.LEFT_LEG, legDamage);
                    data.damageBodyPart(BodyPart.RIGHT_LEG, legDamage);
                }

                if (fallDist > 6) {
                    int torsoDamage = (int) ((fallDist - 6) * 1f);
                    data.damageBodyPart(BodyPart.TORSO, torsoDamage);
                    data.setSangre(Math.max(0, data.getSangre() - (int) ((fallDist - 6) * 2)));

                    if (fallDist > 8) {
                        data.applyBleed(BodyPart.TORSO, BleedingType.HEAVY);
                    } else {
                        data.applyBleed(BodyPart.TORSO, BleedingType.MEDIUM);
                    }
                }

                player.fallDistance = 0;
            }

            data.setWasOnGround(player.onGround());

            if (player.onGround()
                    && data.canRegenerateStamina()
                    && data.getStamina() < 100) {
                data.setSed(data.getSed() - 1);
            }

            if (player.isSprinting()) {
                data.setFatiga(data.getFatiga() + 1);
            }

            if (!player.isSleeping()) {
                data.setSueno(data.getSueno() - 1);
            }

            if (data.getSed() <= 0) {
                data.setSangre(data.getSangre() - 1);
            }

            if (data.isInconsciente()) {
                player.setSprinting(false);
                player.setShiftKeyDown(false);
                player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
                player.hurtMarked = true;
            }

            ModNetwork.STATS_CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncPlayerDataPacket(data)
            );
        });
    }
}
