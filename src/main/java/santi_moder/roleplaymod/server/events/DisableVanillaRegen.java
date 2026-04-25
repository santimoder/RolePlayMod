package santi_moder.roleplaymod.server.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "roleplaymod")
public class DisableVanillaRegen {

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {

        // Solo jugadores
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Cancelar TODA curación automática
        event.setCanceled(true);
    }
}