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
import santi_moder.roleplaymod.client.screen.BodyStatusScreen;
import santi_moder.roleplaymod.client.screen.RPInventoryScreen;
import santi_moder.roleplaymod.item.ModItems;
import santi_moder.roleplaymod.item.PhoneItem;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.QuickAccessHotbarChangedPacket;
import santi_moder.roleplaymod.network.QuickAccessTogglePacket;
import santi_moder.roleplaymod.network.RequestInventoryPacket;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID, value = Dist.CLIENT)
public class ClientKeyHandler {

    private static int lastSelectedSlot = -1;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (ModKeyBindings.OPEN_BODY_STATUS.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new BodyStatusScreen());
            }
        }

        while (ModKeyBindings.OPEN_RP_INVENTORY.consumeClick()) {
            if (mc.screen != null) return;

            ModNetwork.INVENTORY_CHANNEL.sendToServer(new RequestInventoryPacket());
            mc.setScreen(new RPInventoryScreen());
        }

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

        if (mc.screen == null) {
            while (ModKeyBindings.QUICK_PANTS_PISTOL.consumeClick()) {
                if (mc.player.isShiftKeyDown()) {
                    ModNetwork.INVENTORY_CHANNEL.sendToServer(new QuickAccessTogglePacket());
                }
            }
        }

        int currentSelected = mc.player.getInventory().selected;
        if (currentSelected != lastSelectedSlot) {
            lastSelectedSlot = currentSelected;
            ModNetwork.INVENTORY_CHANNEL.sendToServer(new QuickAccessHotbarChangedPacket(currentSelected));
        }
    }
}