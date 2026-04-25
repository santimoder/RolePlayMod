package santi_moder.roleplaymod.server.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "roleplaymod")
public class DisableVanillaHunger {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        var food = player.getFoodData();

        // 🔒 BLOQUEA EL SISTEMA VANILLA DE HAMBRE
        float exhaustion = food.getExhaustionLevel();
        if (exhaustion > 0) {
            food.addExhaustion(-exhaustion);
        }
    }
}

