package santi_moder.roleplaymod.client.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "roleplaymod", value = Dist.CLIENT)
public class DisableJumpKey {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        KeyMapping jump = mc.options.keyJump;

        // 🔒 Forzar salto deshabilitado SIEMPRE
        if (jump.getKey() != InputConstants.UNKNOWN) {
            jump.setKey(InputConstants.UNKNOWN);
            KeyMapping.resetMapping();
        }

        // Seguridad extra: nunca permitir estado de salto
        mc.player.setJumping(false);
    }
}