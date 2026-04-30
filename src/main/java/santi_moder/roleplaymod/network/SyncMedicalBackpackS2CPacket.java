package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.data.ClientMedicalBackpackData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncMedicalBackpackS2CPacket {

    private final List<ItemStack> items;

    public SyncMedicalBackpackS2CPacket(List<ItemStack> items) {
        this.items = items;
    }

    public static void encode(SyncMedicalBackpackS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.items.size());

        for (ItemStack stack : packet.items) {
            buf.writeItem(stack);
        }
    }

    public static SyncMedicalBackpackS2CPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            items.add(buf.readItem());
        }

        return new SyncMedicalBackpackS2CPacket(items);
    }

    public static void handle(SyncMedicalBackpackS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> ClientMedicalBackpackData.setItems(packet.items));

        ctx.setPacketHandled(true);
    }
}