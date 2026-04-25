package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.logic.InventoryInteraction;
import santi_moder.roleplaymod.common.inventory.slots.InventorySlot;
import santi_moder.roleplaymod.common.inventory.slots.SlotType;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.function.Supplier;

public class InventoryClickPacket {

    private final int slotIndex;
    private final int slotType;
    private final int storageIndex;
    private final int vanillaIndex;
    private final int mouseButton;

    public InventoryClickPacket(
            int slotIndex,
            int slotType,
            int storageIndex,
            int vanillaIndex,
            int mouseButton
    ) {
        this.slotIndex = slotIndex;
        this.slotType = slotType;
        this.storageIndex = storageIndex;
        this.vanillaIndex = vanillaIndex;
        this.mouseButton = mouseButton;
    }

    public static void encode(InventoryClickPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.slotIndex);
        buf.writeInt(pkt.slotType);
        buf.writeInt(pkt.storageIndex);
        buf.writeInt(pkt.vanillaIndex);
        buf.writeInt(pkt.mouseButton);
    }

    public static InventoryClickPacket decode(FriendlyByteBuf buf) {
        return new InventoryClickPacket(
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static void handle(InventoryClickPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                EquipmentInventory equipment = data.getEquipmentInventory();

                SlotType type = (pkt.slotType >= 0 && pkt.slotType < SlotType.values().length)
                        ? SlotType.values()[pkt.slotType]
                        : SlotType.EQUIPMENT;

                InventorySlot slot = new InventorySlot(
                        0,
                        0,
                        pkt.slotIndex,
                        type,
                        pkt.storageIndex,
                        pkt.vanillaIndex
                );

                ItemStack serverCarried = player.containerMenu.getCarried().copy();

                ItemStack newCarried = InventoryInteraction.clickSlot(
                        slot,
                        equipment,
                        serverCarried,
                        player,
                        pkt.mouseButton
                );

                player.containerMenu.setCarried(newCarried.copy());
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