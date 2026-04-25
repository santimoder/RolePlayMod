package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.item.ItemInventory;
import santi_moder.roleplaymod.common.inventory.slots.InventorySlot;
import santi_moder.roleplaymod.common.inventory.slots.SlotType;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.function.Supplier;

public class DropInventorySlotPacket {

    private final int slotIndex;
    private final int slotType;
    private final int storageIndex;
    private final int vanillaIndex;

    public DropInventorySlotPacket(int slotIndex, int slotType, int storageIndex, int vanillaIndex) {
        this.slotIndex = slotIndex;
        this.slotType = slotType;
        this.storageIndex = storageIndex;
        this.vanillaIndex = vanillaIndex;
    }

    public static void encode(DropInventorySlotPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.slotIndex);
        buf.writeInt(pkt.slotType);
        buf.writeInt(pkt.storageIndex);
        buf.writeInt(pkt.vanillaIndex);
    }

    public static DropInventorySlotPacket decode(FriendlyByteBuf buf) {
        return new DropInventorySlotPacket(
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static void handle(DropInventorySlotPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
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

                ItemStack stackToDrop = getStack(slot, equipment, player);
                if (stackToDrop.isEmpty()) return;

                ItemStack dropCopy = stackToDrop.copy();
                setStack(slot, equipment, player, ItemStack.EMPTY);

                player.drop(dropCopy, false);
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

    private static ItemStack getStack(
            InventorySlot slot,
            EquipmentInventory equipment,
            ServerPlayer player
    ) {
        return switch (slot.getType()) {
            case EQUIPMENT, CLOTHS ->
                    equipment.getItem(slot.getIndex());

            case HOTBAR ->
                    player.getInventory().items.get(slot.getVanillaIndex());

            case JACKET_STORAGE ->
                    ItemInventory.getItem(equipment.getItem(5), slot.getStorageIndex());

            case PANTS_STORAGE ->
                    ItemInventory.getItem(equipment.getItem(6), slot.getStorageIndex());

            case VEST_STORAGE ->
                    ItemInventory.getItem(equipment.getItem(2), slot.getStorageIndex());

            case BELT_STORAGE ->
                    ItemInventory.getItem(equipment.getItem(3), slot.getStorageIndex());

            case BACKPACK_STORAGE ->
                    ItemInventory.getItem(equipment.getItem(1), slot.getStorageIndex());
        };
    }

    private static void setStack(
            InventorySlot slot,
            EquipmentInventory equipment,
            ServerPlayer player,
            ItemStack stack
    ) {
        switch (slot.getType()) {
            case EQUIPMENT, CLOTHS ->
                    equipment.setItem(slot.getIndex(), stack);

            case HOTBAR ->
                    player.getInventory().items.set(
                            slot.getVanillaIndex(),
                            stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy()
                    );

            case JACKET_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(5), slot.getStorageIndex(), stack);

            case PANTS_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(6), slot.getStorageIndex(), stack);

            case VEST_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(2), slot.getStorageIndex(), stack);

            case BELT_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(3), slot.getStorageIndex(), stack);

            case BACKPACK_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(1), slot.getStorageIndex(), stack);
        }
    }
}