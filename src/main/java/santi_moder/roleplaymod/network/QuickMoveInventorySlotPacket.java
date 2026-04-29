package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.logic.InventoryQuickMove;
import santi_moder.roleplaymod.common.inventory.security.InventorySlotSecurity;
import santi_moder.roleplaymod.common.inventory.slots.InventorySlot;
import santi_moder.roleplaymod.common.inventory.slots.SlotType;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.function.Supplier;

public class QuickMoveInventorySlotPacket {

    private final int slotIndex;
    private final int slotType;
    private final int storageIndex;
    private final int vanillaIndex;

    public QuickMoveInventorySlotPacket(int slotIndex, int slotType, int storageIndex, int vanillaIndex) {
        this.slotIndex = slotIndex;
        this.slotType = slotType;
        this.storageIndex = storageIndex;
        this.vanillaIndex = vanillaIndex;
    }

    public static void encode(QuickMoveInventorySlotPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.slotIndex);
        buf.writeInt(pkt.slotType);
        buf.writeInt(pkt.storageIndex);
        buf.writeInt(pkt.vanillaIndex);
    }

    public static QuickMoveInventorySlotPacket decode(FriendlyByteBuf buf) {
        return new QuickMoveInventorySlotPacket(
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static void handle(QuickMoveInventorySlotPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            SlotType type = InventorySlotSecurity.resolveSlotType(pkt.slotType);
            if (type == null) return;

            if (!InventorySlotSecurity.isValidPacketSlot(type, pkt.slotIndex, pkt.storageIndex, pkt.vanillaIndex, player)) {
                return;
            }

            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                EquipmentInventory equipment = data.getEquipmentInventory();

                ItemStack carried = player.containerMenu.getCarried().copy();

                if (!carried.isEmpty()) {
                    ItemStack remaining = InventoryQuickMove.moveCarried(carried, equipment, player);
                    player.containerMenu.setCarried(remaining);
                } else {
                    InventorySlot slot = new InventorySlot(
                            0,
                            0,
                            pkt.slotIndex,
                            type,
                            pkt.storageIndex,
                            pkt.vanillaIndex
                    );

                    InventoryQuickMove.moveFromSlot(slot, equipment, player);
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