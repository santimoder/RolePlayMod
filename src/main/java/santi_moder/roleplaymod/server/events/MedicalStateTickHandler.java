package santi_moder.roleplaymod.server.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.common.player.BleedingType;
import santi_moder.roleplaymod.common.util.MedicalUtils;
import santi_moder.roleplaymod.network.MedicalEffectS2CPacket;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.SyncPlayerDataPacket;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID)
public final class MedicalStateTickHandler {

    private MedicalStateTickHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        if (player.tickCount % 20 != 0) return;

        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            BleedingType previousBleeding = data.getWorstBleeding();
            int previousBlood = data.getSangre();

            data.tickBleeding();

            int recentBloodLoss = previousBlood - data.getSangre();
            int unconsciousTicks = MedicalUtils.getUnconsciousDurationTicks(data, recentBloodLoss);

            if (unconsciousTicks > 0) {
                data.setInconsciente(true);
                data.setUnconsciousTicks(Math.max(data.getUnconsciousTicks(), unconsciousTicks));
            }

            data.tickShockRecovery();
            data.applyBodyPartEffects();

            if (previousBleeding != BleedingType.NONE && data.getSangre() < previousBlood) {
                float intensity = switch (previousBleeding) {
                    case LIGHT -> 0.25F;
                    case MEDIUM -> 0.45F;
                    case HEAVY -> 0.75F;
                    case NONE -> 0.0F;
                };

                ModNetwork.STATS_CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new MedicalEffectS2CPacket(MedicalEffectS2CPacket.Type.BLEED_PULSE, intensity)
                );
            }

            if (data.isInconsciente()) {
                data.tickUnconsciousTicks();

                if (MedicalUtils.canWakeUp(data)) {
                    data.setInconsciente(false);
                }
            }

            if (MedicalUtils.checkAndKill(player, data)) {
                return;
            }

            if (!player.isDeadOrDying()) {
                player.setHealth(player.getMaxHealth());
            }

            ModNetwork.STATS_CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncPlayerDataPacket(data)
            );
        });
    }
}