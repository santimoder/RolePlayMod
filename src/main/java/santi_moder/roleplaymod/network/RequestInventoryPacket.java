package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.function.Supplier;

public class RequestInventoryPacket {

    public static void encode(RequestInventoryPacket pkt, FriendlyByteBuf buf) {}

    public static RequestInventoryPacket decode(FriendlyByteBuf buf) {
        return new RequestInventoryPacket();
    }

    public static void handle(RequestInventoryPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {

        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {

            ServerPlayer player = ctx.getSender();
            if (player == null) return;

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