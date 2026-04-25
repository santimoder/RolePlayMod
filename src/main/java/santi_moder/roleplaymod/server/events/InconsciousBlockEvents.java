package santi_moder.roleplaymod.server.events;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

@Mod.EventBusSubscriber(modid = "roleplaymod")
public class InconsciousBlockEvents {

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent event) {
        event.getEntity().getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            if (data.isInconsciente()) {
                event.setCanceled(true);
            }
        });
    }
}
