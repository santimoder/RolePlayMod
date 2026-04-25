package santi_moder.roleplaymod.client.events;

import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.RolePlayMod;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID)
public class BlockItemPickup {

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (event.getEntity().level().isClientSide) return;
        event.setCanceled(true);
    }
}