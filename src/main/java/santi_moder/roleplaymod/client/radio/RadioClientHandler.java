package santi_moder.roleplaymod.client.radio;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.radio.DecreaseRadioFrequencyPacket;
import santi_moder.roleplaymod.network.radio.IncreaseRadioFrequencyPacket;
import santi_moder.roleplaymod.network.radio.ToggleRadioPacket;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RadioClientHandler {

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(RadioKeyMappings.TOGGLE_RADIO);
        event.register(RadioKeyMappings.FREQUENCY_UP);
        event.register(RadioKeyMappings.FREQUENCY_DOWN);
    }

    @Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID, value = Dist.CLIENT)
    public static class RuntimeInputHandler {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            while (RadioKeyMappings.TOGGLE_RADIO.consumeClick()) {
                ModNetwork.sendInventoryToServer(new ToggleRadioPacket());
            }

            while (RadioKeyMappings.FREQUENCY_UP.consumeClick()) {
                ModNetwork.sendInventoryToServer(new IncreaseRadioFrequencyPacket());
            }

            while (RadioKeyMappings.FREQUENCY_DOWN.consumeClick()) {
                ModNetwork.sendInventoryToServer(new DecreaseRadioFrequencyPacket());
            }
        }
    }
}