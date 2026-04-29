package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.logic.InventoryQuickMove;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.function.Supplier;

public class QuickMoveGroundItemPacket {

    private final int entityId;

    public QuickMoveGroundItemPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(QuickMoveGroundItemPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.entityId);
    }

    public static QuickMoveGroundItemPacket decode(FriendlyByteBuf buf) {
        return new QuickMoveGroundItemPacket(buf.readInt());
    }

    public static void handle(QuickMoveGroundItemPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            Entity entity = player.level().getEntity(pkt.entityId);
            if (!(entity instanceof ItemEntity itemEntity)) return;
            if (!itemEntity.isAlive()) return;
            if (itemEntity.getItem().isEmpty()) return;

            if (player.distanceToSqr(itemEntity) > 16.0D) return;

            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                EquipmentInventory equipment = data.getEquipmentInventory();

                ItemStack groundStack = itemEntity.getItem().copy();
                ItemStack remaining = InventoryQuickMove.moveCarried(groundStack, equipment, player);

                if (remaining.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(remaining.copy());
                }

                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();

                ModNetwork.INVENTORY_CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncInventoryPacket(
                                equipment.serializeNBT(),
                                player.containerMenu.getCarried().copy()
                        )
                );
            });
        });

        ctx.setPacketHandled(true);
    }
}