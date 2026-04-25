package santi_moder.roleplaymod.client.radio;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.radio.StartRadioTransmissionPacket;
import santi_moder.roleplaymod.network.radio.StopRadioTransmissionPacket;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RadioPTTClientHandler {

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(RadioPTTKeyMappings.RADIO_PTT);
    }

    @Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID, value = Dist.CLIENT)
    public static class RuntimeHandler {

        private static boolean wasDown = false;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            boolean isDown = RadioPTTKeyMappings.RADIO_PTT.isDown();

            if (isDown && !wasDown) {
                ModNetwork.sendInventoryToServer(new StartRadioTransmissionPacket());
            } else if (!isDown && wasDown) {
                ModNetwork.sendInventoryToServer(new StopRadioTransmissionPacket());
            }

            wasDown = isDown;
        }
    }
}