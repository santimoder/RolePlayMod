package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.item.ItemInventory;
import santi_moder.roleplaymod.common.inventory.security.InventorySlotSecurity;
import santi_moder.roleplaymod.common.inventory.slots.InventorySlot;
import santi_moder.roleplaymod.common.inventory.slots.SlotType;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.function.Supplier;

public class DropInventorySlotPacket {

    private final int slotIndex;
    private final int slotType;
    private final int storageIndex;
    private final int vanillaIndex;
    private final boolean fullStack;


    public DropInventorySlotPacket(int slotIndex, int slotType, int storageIndex, int vanillaIndex) {
        this(slotIndex, slotType, storageIndex, vanillaIndex, true);
    }

    public DropInventorySlotPacket(int slotIndex, int slotType, int storageIndex, int vanillaIndex, boolean fullStack) {
        this.slotIndex = slotIndex;
        this.slotType = slotType;
        this.storageIndex = storageIndex;
        this.vanillaIndex = vanillaIndex;
        this.fullStack = fullStack;
    }

    public static void encode(DropInventorySlotPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.slotIndex);
        buf.writeInt(pkt.slotType);
        buf.writeInt(pkt.storageIndex);
        buf.writeInt(pkt.vanillaIndex);
        buf.writeBoolean(pkt.fullStack);
    }

    public static DropInventorySlotPacket decode(FriendlyByteBuf buf) {
        return new DropInventorySlotPacket(
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readBoolean()
        );
    }

    public static void handle(DropInventorySlotPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
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

                ItemStack dropCopy;

                if (pkt.fullStack || stackToDrop.getCount() <= 1) {
                    dropCopy = stackToDrop.copy();
                    setStack(slot, equipment, player, ItemStack.EMPTY);
                } else {
                    dropCopy = stackToDrop.copy();
                    dropCopy.setCount(1);

                    ItemStack remaining = stackToDrop.copy();
                    remaining.shrink(1);

                    setStack(slot, equipment, player, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
                }

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

    private static ItemStack getStack(InventorySlot slot, EquipmentInventory equipment, ServerPlayer player) {
        return switch (slot.type()) {
            case EQUIPMENT, CLOTHS -> equipment.getItem(slot.index());

            case HOTBAR -> InventorySlotSecurity.isValidVanillaIndex(slot.vanillaIndex(), player)
                    ? player.getInventory().items.get(slot.vanillaIndex())
                    : ItemStack.EMPTY;

            case JACKET_STORAGE -> ItemInventory.getItem(equipment.getItem(5), slot.storageIndex());

            case PANTS_STORAGE -> ItemInventory.getItem(equipment.getItem(6), slot.storageIndex());

            case VEST_STORAGE -> ItemInventory.getItem(equipment.getItem(2), slot.storageIndex());

            case BELT_STORAGE -> ItemInventory.getItem(equipment.getItem(3), slot.storageIndex());

            case BACKPACK_STORAGE -> ItemInventory.getItem(equipment.getItem(1), slot.storageIndex());
        };
    }

    private static void setStack(InventorySlot slot, EquipmentInventory equipment, ServerPlayer player, ItemStack stack) {
        switch (slot.type()) {
            case EQUIPMENT, CLOTHS -> equipment.setItem(slot.index(), stack);

            case HOTBAR -> {
                if (InventorySlotSecurity.isValidVanillaIndex(slot.vanillaIndex(), player)) {
                    player.getInventory().items.set(
                            slot.vanillaIndex(),
                            stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy()
                    );
                }
            }

            case JACKET_STORAGE -> ItemInventory.setItem(equipment.getItem(5), slot.storageIndex(), stack);

            case PANTS_STORAGE -> ItemInventory.setItem(equipment.getItem(6), slot.storageIndex(), stack);

            case VEST_STORAGE -> ItemInventory.setItem(equipment.getItem(2), slot.storageIndex(), stack);

            case BELT_STORAGE -> ItemInventory.setItem(equipment.getItem(3), slot.storageIndex(), stack);

            case BACKPACK_STORAGE -> ItemInventory.setItem(equipment.getItem(1), slot.storageIndex(), stack);
        }
    }
}