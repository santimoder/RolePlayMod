package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.server.medical.MedicalTreatmentHandler;

import java.util.function.Supplier;

public class StartTreatmentC2SPacket {

    private final int backpackSlot;
    private final BodyPart bodyPart;

    public StartTreatmentC2SPacket(int backpackSlot, BodyPart bodyPart) {
        this.backpackSlot = backpackSlot;
        this.bodyPart = bodyPart;
    }

    public static void encode(StartTreatmentC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.backpackSlot);
        buf.writeEnum(packet.bodyPart);
    }

    public static StartTreatmentC2SPacket decode(FriendlyByteBuf buf) {
        return new StartTreatmentC2SPacket(
                buf.readInt(),
                buf.readEnum(BodyPart.class)
        );
    }

    public static void handle(StartTreatmentC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            MedicalTreatmentHandler.startBandageTreatment(
                    player,
                    packet.backpackSlot,
                    packet.bodyPart
            );
        });

        ctx.setPacketHandled(true);
    }
}