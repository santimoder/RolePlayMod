package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.quickaccess.QuickAccessManager;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.function.Supplier;

public class QuickAccessTogglePacket {

    public QuickAccessTogglePacket() {
    }

    public static void encode(QuickAccessTogglePacket pkt, FriendlyByteBuf buf) {
    }

    public static QuickAccessTogglePacket decode(FriendlyByteBuf buf) {
        return new QuickAccessTogglePacket();
    }

    public static void handle(QuickAccessTogglePacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                EquipmentInventory equipment = data.getEquipmentInventory();
                QuickAccessManager.togglePantsPistol(player, equipment);

                ModNetwork.INVENTORY_CHANNEL.sendTo(
                        new SyncInventoryPacket(
                                equipment.serializeNBT(),
                                player.containerMenu.getCarried().copy()
                        ),
                        player.connection.connection,
                        net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
            });
        });

        ctx.setPacketHandled(true);
    }
}