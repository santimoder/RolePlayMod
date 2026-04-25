package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.whatsapp.server.WhatsappServerService;

import java.util.function.Supplier;

public final class WhatsappToggleBlockContactC2SPacket {

    private final String contactId;

    public WhatsappToggleBlockContactC2SPacket(String contactId) {
        this.contactId = contactId == null ? "" : contactId;
    }

    public String contactId() {
        return contactId;
    }

    public static void encode(WhatsappToggleBlockContactC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.contactId);
    }

    public static WhatsappToggleBlockContactC2SPacket decode(FriendlyByteBuf buf) {
        return new WhatsappToggleBlockContactC2SPacket(buf.readUtf());
    }

    public static void handle(WhatsappToggleBlockContactC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            WhatsappServerService.handleToggleBlockContact(player, packet.contactId());
        });

        context.setPacketHandled(true);
    }
}