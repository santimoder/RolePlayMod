package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.function.Supplier;

public class DropCarriedPacket {

    private final boolean fullStack;

    public DropCarriedPacket() {
        this(true);
    }

    public DropCarriedPacket(boolean fullStack) {
        this.fullStack = fullStack;
    }

    public static void encode(DropCarriedPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.fullStack);
    }

    public static DropCarriedPacket decode(FriendlyByteBuf buf) {
        return new DropCarriedPacket(buf.readBoolean());
    }

    public static void handle(DropCarriedPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            ItemStack carried = player.containerMenu.getCarried().copy();
            if (carried.isEmpty()) return;

            ItemStack toDrop;

            if (pkt.fullStack || carried.getCount() <= 1) {
                toDrop = carried.copy();
                player.containerMenu.setCarried(ItemStack.EMPTY);
            } else {
                toDrop = carried.copy();
                toDrop.setCount(1);

                carried.shrink(1);
                player.containerMenu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried.copy());
            }

            player.drop(toDrop, false);

            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();

            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                ModNetwork.INVENTORY_CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncInventoryPacket(
                                data.getEquipmentInventory().serializeNBT(),
                                player.containerMenu.getCarried().copy()
                        )
                );
            });
        });

        ctx.setPacketHandled(true);
    }
}