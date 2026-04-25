package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.function.Supplier;

public class ClearCarriedPacket {

    public ClearCarriedPacket() {
    }

    public static void encode(ClearCarriedPacket pkt, FriendlyByteBuf buf) {
    }

    public static ClearCarriedPacket decode(FriendlyByteBuf buf) {
        return new ClearCarriedPacket();
    }

    public static void handle(ClearCarriedPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            player.containerMenu.setCarried(ItemStack.EMPTY);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();

            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                ModNetwork.INVENTORY_CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncInventoryPacket(
                                data.getEquipmentInventory().serializeNBT(),
                                player.containerMenu.getCarried()
                        )
                );
            });
        });

        ctx.setPacketHandled(true);
    }
}