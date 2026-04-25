package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.whatsapp.server.WhatsappServerService;
import santi_moder.roleplaymod.common.whatsapp.sync.WhatsappInitialStateSnapshot;
import santi_moder.roleplaymod.network.ModNetwork;

import java.util.function.Supplier;

public final class WhatsappRequestInitialStateC2SPacket {

    public WhatsappRequestInitialStateC2SPacket() {
    }

    public static void encode(WhatsappRequestInitialStateC2SPacket packet, FriendlyByteBuf buf) {
    }

    public static WhatsappRequestInitialStateC2SPacket decode(FriendlyByteBuf buf) {
        return new WhatsappRequestInitialStateC2SPacket();
    }

    public static void handle(WhatsappRequestInitialStateC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            WhatsappInitialStateSnapshot snapshot = WhatsappServerService.buildInitialSnapshot(player);

            ModNetwork.sendWhatsappToClient(
                    new WhatsappInitialStateS2CPacket(snapshot),
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player)
            );
        });

        context.setPacketHandled(true);
    }
}