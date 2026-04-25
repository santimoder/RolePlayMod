package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.function.Supplier;

public class GroundItemClickPacket {

    private final int entityId;

    public GroundItemClickPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(GroundItemClickPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.entityId);
    }

    public static GroundItemClickPacket decode(FriendlyByteBuf buf) {
        return new GroundItemClickPacket(buf.readInt());
    }

    public static void handle(GroundItemClickPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            if (!player.containerMenu.getCarried().isEmpty()) return;

            Entity entity = player.level().getEntity(pkt.entityId);
            if (!(entity instanceof ItemEntity itemEntity)) return;
            if (!itemEntity.isAlive()) return;

            if (player.distanceToSqr(itemEntity) > 16.0D) return;

            ItemStack stack = itemEntity.getItem().copy();
            if (stack.isEmpty()) return;

            player.containerMenu.setCarried(stack.copy());
            itemEntity.discard();

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