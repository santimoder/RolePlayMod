package santi_moder.roleplaymod.server.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "roleplaymod")
public final class VanillaHealthLockHandler {

    private VanillaHealthLockHandler() {
    }

    @SubscribeEvent
    public static void onPlayerHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.isDeadOrDying()) return;

        if (player.getHealth() < player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }
}