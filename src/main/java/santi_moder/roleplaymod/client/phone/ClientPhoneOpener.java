package santi_moder.roleplaymod.client.phone;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

public class ClientPhoneOpener {

    private ClientPhoneOpener() {
    }

    public static void open(InteractionHand hand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (mc.screen instanceof PhoneScreen currentPhoneScreen) {
            currentPhoneScreen.closePhone();
            return;
        }

        mc.setScreen(new PhoneScreen(hand));
    }
}