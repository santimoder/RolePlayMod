package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.server.medical.MedicalTreatmentHandler;

import java.util.UUID;
import java.util.function.Supplier;

public final class StartTreatmentC2SPacket {

    private final int backpackSlot;
    private final BodyPart bodyPart;
    private final UUID targetUuid;

    public StartTreatmentC2SPacket(int backpackSlot, BodyPart bodyPart) {
        this(backpackSlot, bodyPart, null);
    }

    public StartTreatmentC2SPacket(int backpackSlot, BodyPart bodyPart, UUID targetUuid) {
        this.backpackSlot = backpackSlot;
        this.bodyPart = bodyPart;
        this.targetUuid = targetUuid;
    }

    public static void encode(StartTreatmentC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.backpackSlot);
        buf.writeEnum(packet.bodyPart);
        buf.writeBoolean(packet.targetUuid != null);

        if (packet.targetUuid != null) {
            buf.writeUUID(packet.targetUuid);
        }
    }

    public static StartTreatmentC2SPacket decode(FriendlyByteBuf buf) {
        int backpackSlot = buf.readInt();
        BodyPart bodyPart = buf.readEnum(BodyPart.class);

        UUID targetUuid = null;
        if (buf.readBoolean()) {
            targetUuid = buf.readUUID();
        }

        return new StartTreatmentC2SPacket(backpackSlot, bodyPart, targetUuid);
    }

    public static void handle(StartTreatmentC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer healer = ctx.getSender();
            if (healer == null) return;

            MedicalTreatmentHandler.startBandageTreatment(
                    healer,
                    packet.backpackSlot,
                    packet.bodyPart,
                    packet.targetUuid
            );
        });

        ctx.setPacketHandled(true);
    }
}