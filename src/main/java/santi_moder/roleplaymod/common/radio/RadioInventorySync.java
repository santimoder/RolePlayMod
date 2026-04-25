package santi_moder.roleplaymod.common.radio;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.SyncInventoryPacket;

public class RadioInventorySync {

    private RadioInventorySync() {
    }

    public static void sync(ServerPlayer player) {
        if (player == null) return;

        EquipmentInventory equipment = RadioEquipmentResolver.getEquipment(player);
        if (equipment == null) return;

        ItemStack carried = player.containerMenu.getCarried();
        if (carried == null) {
            carried = ItemStack.EMPTY;
        }

        ModNetwork.sendInventoryToClient(
                new SyncInventoryPacket(equipment.serializeNBT(), carried.copy()),
                PacketDistributor.PLAYER.with(() -> player)
        );
    }
}