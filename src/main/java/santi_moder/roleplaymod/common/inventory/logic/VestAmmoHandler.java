package santi_moder.roleplaymod.common.inventory.logic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.SyncInventoryPacket;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

@Mod.EventBusSubscriber
public class VestAmmoHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            EquipmentInventory equipment = data.getEquipmentInventory();
            if (equipment == null) return;

            VestAmmoSync.syncConsumption(player, equipment);

            ModNetwork.INVENTORY_CHANNEL.sendTo(
                    new SyncInventoryPacket(
                            equipment.serializeNBT(),
                            player.containerMenu.getCarried().copy()
                    ),
                    player.connection.connection,
                    net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
            );
        });
    }
}