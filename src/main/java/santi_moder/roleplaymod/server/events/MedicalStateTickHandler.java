package santi_moder.roleplaymod.server.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.common.util.MedicalUtils;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.SyncPlayerDataPacket;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "roleplaymod")
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
            data.tickShockRecovery();
            data.applyBodyPartEffects();

            if (MedicalUtils.shouldBeUnconscious(data) && !data.isInconsciente()) {
                data.setInconsciente(true);
                data.incrementarInconsciencias();
            }

            if (data.isInconsciente() && MedicalUtils.canWakeUp(data)) {
                data.setInconsciente(false);
            }

            MedicalUtils.checkAndKill(player, data);

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