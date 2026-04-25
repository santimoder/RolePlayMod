package santi_moder.roleplaymod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.data.ClientInventoryData;
import santi_moder.roleplaymod.client.screen.RPInventoryScreen;

import java.util.function.Supplier;

public class SyncInventoryPacket {

    private final CompoundTag equipment;
    private final ItemStack carried;

    public SyncInventoryPacket(CompoundTag equipment, ItemStack carried) {
        this.equipment = equipment == null ? new CompoundTag() : equipment.copy();
        this.carried = carried == null ? ItemStack.EMPTY : carried.copy();
    }

    public static void encode(SyncInventoryPacket pkt, FriendlyByteBuf buf) {
        buf.writeNbt(pkt.equipment);
        buf.writeItem(pkt.carried);
    }

    public static SyncInventoryPacket decode(FriendlyByteBuf buf) {
        CompoundTag equipment = buf.readNbt();
        if (equipment == null) equipment = new CompoundTag();

        ItemStack carried = buf.readItem();
        if (carried == null) carried = ItemStack.EMPTY;

        return new SyncInventoryPacket(equipment, carried);
    }

    public static void handle(SyncInventoryPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            ClientInventoryData.update(pkt.equipment, pkt.carried);

            mc.player.containerMenu.setCarried(pkt.carried.copy());
            mc.player.getInventory().setChanged();

            if (mc.screen instanceof RPInventoryScreen screen) {
                screen.refreshSlots();
            }
        });

        ctx.setPacketHandled(true);
    }
}