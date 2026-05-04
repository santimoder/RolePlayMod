package santi_moder.roleplaymod.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.client.input.ModKeyBindings;
import santi_moder.roleplaymod.client.phone.ClientPhoneOpener;
import santi_moder.roleplaymod.client.radio.voice.RadioVoiceChatPttState;
import santi_moder.roleplaymod.client.screen.RPInventoryScreen;
import santi_moder.roleplaymod.item.PhoneItem;
import santi_moder.roleplaymod.network.*;
import santi_moder.roleplaymod.network.radio.*;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID, value = Dist.CLIENT)
public class ClientKeyHandler {

    private static int lastSelectedSlot = -1;
    private static boolean radioPttDown = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // ================= RADIO PTT =================
        boolean radioPressed = ModKeyBindings.RADIO_PTT.isDown();

        if (mc.screen == null && radioPressed && !radioPttDown) {
            radioPttDown = true;

            RadioVoiceChatPttState.setRadioPttDown(true);
            ModNetwork.sendInventoryToServer(new StartRadioTransmissionPacket());
        }

        if ((!radioPressed || mc.screen != null) && radioPttDown) {
            radioPttDown = false;

            RadioVoiceChatPttState.setRadioPttDown(false);
            ModNetwork.sendInventoryToServer(new StopRadioTransmissionPacket());
        }

        // ================= RADIO CONTROLES =================
        if (mc.screen == null) {
            while (ModKeyBindings.TOGGLE_RADIO.consumeClick()) {
                ModNetwork.sendInventoryToServer(new ToggleRadioPacket());
            }

            while (ModKeyBindings.RADIO_FREQUENCY_UP.consumeClick()) {
                ModNetwork.sendInventoryToServer(new IncreaseRadioFrequencyPacket());
            }

            while (ModKeyBindings.RADIO_FREQUENCY_DOWN.consumeClick()) {
                ModNetwork.sendInventoryToServer(new DecreaseRadioFrequencyPacket());
            }
        }

        // ================= BODY STATUS =================
        while (ModKeyBindings.OPEN_BODY_STATUS.consumeClick()) {
            if (mc.screen == null) {
                ModNetwork.STATS_CHANNEL.sendToServer(new RequestTargetDiagnosisC2SPacket());
            }
        }

        // ================= INVENTARIO =================
        while (ModKeyBindings.OPEN_RP_INVENTORY.consumeClick()) {
            if (mc.screen != null) return;

            ModNetwork.INVENTORY_CHANNEL.sendToServer(new RequestInventoryPacket());
            mc.setScreen(new RPInventoryScreen());
        }

        // ================= TELEFONO =================
        while (ModKeyBindings.OPEN_PHONE.consumeClick()) {
            ItemStack mainHand = mc.player.getMainHandItem();
            ItemStack offHand = mc.player.getOffhandItem();

            if (mainHand.getItem() instanceof PhoneItem) {
                ClientPhoneOpener.open(InteractionHand.MAIN_HAND);
                return;
            }

            if (offHand.getItem() instanceof PhoneItem) {
                ClientPhoneOpener.open(InteractionHand.OFF_HAND);
                return;
            }
        }

        // ================= QUICK ACCESS =================
        if (mc.screen == null) {
            while (ModKeyBindings.QUICK_PANTS_PISTOL.consumeClick()) {
                if (mc.player.isShiftKeyDown()) {
                    ModNetwork.INVENTORY_CHANNEL.sendToServer(new QuickAccessTogglePacket());
                }
            }
        }

        // ================= HOTBAR =================
        int currentSelected = mc.player.getInventory().selected;
        if (currentSelected != lastSelectedSlot) {
            lastSelectedSlot = currentSelected;
            ModNetwork.INVENTORY_CHANNEL.sendToServer(new QuickAccessHotbarChangedPacket(currentSelected));
        }
    }
}