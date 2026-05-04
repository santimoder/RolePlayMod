package santi_moder.roleplaymod.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.data.ClientPatientMedicalData;
import santi_moder.roleplaymod.common.player.IPlayerData;

import java.util.UUID;
import java.util.function.Supplier;

public final class SyncPatientMedicalDataS2CPacket {

    private final UUID targetUuid;
    private final String targetName;
    private final int sangre;
    private final int shock;
    private final boolean inconsciente;
    private final CompoundTag bodyTag;

    public SyncPatientMedicalDataS2CPacket(UUID targetUuid, String targetName, IPlayerData data) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.sangre = data.getSangre();
        this.shock = data.getShock();
        this.inconsciente = data.isInconsciente();
        this.bodyTag = data.serializeBodyParts();
    }

    private SyncPatientMedicalDataS2CPacket(UUID targetUuid, String targetName, int sangre, int shock, boolean inconsciente, CompoundTag bodyTag) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.sangre = sangre;
        this.shock = shock;
        this.inconsciente = inconsciente;
        this.bodyTag = bodyTag == null ? new CompoundTag() : bodyTag;
    }

    public static void encode(SyncPatientMedicalDataS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.targetUuid);
        buf.writeUtf(packet.targetName);
        buf.writeInt(packet.sangre);
        buf.writeInt(packet.shock);
        buf.writeBoolean(packet.inconsciente);
        buf.writeNbt(packet.bodyTag);
    }

    public static SyncPatientMedicalDataS2CPacket decode(FriendlyByteBuf buf) {
        return new SyncPatientMedicalDataS2CPacket(
                buf.readUUID(),
                buf.readUtf(),
                buf.readInt(),
                buf.readInt(),
                buf.readBoolean(),
                buf.readNbt()
        );
    }

    public static void handle(SyncPatientMedicalDataS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> ClientPatientMedicalData.set(
                packet.targetUuid,
                packet.targetName,
                packet.sangre,
                packet.shock,
                packet.inconsciente,
                packet.bodyTag
        ));

        ctx.setPacketHandled(true);
    }
}