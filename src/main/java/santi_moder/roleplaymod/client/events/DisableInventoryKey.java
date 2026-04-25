package santi_moder.roleplaymod.client.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "roleplaymod", value = Dist.CLIENT)
public class DisableInventoryKey {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        KeyMapping inventory = mc.options.keyInventory;

        // 🔒 Forzar inventario deshabilitado SIEMPRE
        if (inventory.getKey() != InputConstants.UNKNOWN) {
            inventory.setKey(InputConstants.UNKNOWN);
            KeyMapping.resetMapping();
        }
    }
}