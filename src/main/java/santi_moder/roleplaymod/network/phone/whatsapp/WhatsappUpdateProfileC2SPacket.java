package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.whatsapp.server.WhatsappServerService;

import java.util.function.Supplier;

public final class WhatsappUpdateProfileC2SPacket {

    private final String displayName;
    private final String about;

    public WhatsappUpdateProfileC2SPacket(String displayName, String about) {
        this.displayName = displayName == null ? "" : displayName;
        this.about = about == null ? "" : about;
    }

    public static void encode(WhatsappUpdateProfileC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.displayName, 30);
        buf.writeUtf(packet.about, 20);
    }

    public static WhatsappUpdateProfileC2SPacket decode(FriendlyByteBuf buf) {
        return new WhatsappUpdateProfileC2SPacket(
                buf.readUtf(30),
                buf.readUtf(20)
        );
    }

    public static void handle(WhatsappUpdateProfileC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            WhatsappServerService.handleUpdateProfile(
                    player,
                    packet.displayName,
                    packet.about
            );
        });

        context.setPacketHandled(true);
    }
}