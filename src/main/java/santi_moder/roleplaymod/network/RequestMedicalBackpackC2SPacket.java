package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.inventory.item.ItemInventory;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RequestMedicalBackpackC2SPacket {

    public RequestMedicalBackpackC2SPacket() {
    }

    public static void encode(RequestMedicalBackpackC2SPacket packet, FriendlyByteBuf buf) {
    }

    public static RequestMedicalBackpackC2SPacket decode(FriendlyByteBuf buf) {
        return new RequestMedicalBackpackC2SPacket();
    }

    public static void handle(RequestMedicalBackpackC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                ItemStack backpack = data.getEquipmentInventory().getItem(1);

                List<ItemStack> items = new ArrayList<>();

                int size = ItemInventory.getSize(backpack);

                for (int i = 0; i < size; i++) {
                    items.add(ItemInventory.getItem(backpack, i).copy());
                }

                ModNetwork.STATS_CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncMedicalBackpackS2CPacket(items)
                );
            });
        });

        ctx.setPacketHandled(true);
    }
}