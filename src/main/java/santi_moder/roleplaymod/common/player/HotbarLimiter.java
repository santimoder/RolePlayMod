package santi_moder.roleplaymod.common.player;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class HotbarLimiter {

    private static final int MAX_SLOT = 1; // solo slots 0 y 1 (teclas 1 y 2)

    // BLOQUEA RUEDA DEL MOUSE
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return;

        if (mc.player.getInventory().selected > MAX_SLOT) {
            mc.player.getInventory().selected = MAX_SLOT;
        }

        if (mc.player.getInventory().selected < 0) {
            mc.player.getInventory().selected = 0;
        }
    }

    // BLOQUEA NUMEROS Y CUALQUIER CAMBIO
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (!event.player.level().isClientSide) return;

        if (event.player.getInventory().selected > MAX_SLOT) {
            event.player.getInventory().selected = MAX_SLOT;
        }

        if (event.player.getInventory().selected < 0) {
            event.player.getInventory().selected = 0;
        }
    }
}