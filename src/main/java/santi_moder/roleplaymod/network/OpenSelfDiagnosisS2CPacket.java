package santi_moder.roleplaymod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.data.ClientPlayerData;
import santi_moder.roleplaymod.client.screen.BodyStatusScreen;
import santi_moder.roleplaymod.common.player.IPlayerData;

import java.util.function.Supplier;

public final class OpenSelfDiagnosisS2CPacket {

    private final int sangre;
    private final int stamina;
    private final int sed;
    private final int shock;
    private final boolean inconsciente;
    private final CompoundTag bodyTag;

    public OpenSelfDiagnosisS2CPacket(IPlayerData data) {
        this.sangre = data.getSangre();
        this.stamina = data.getStamina();
        this.sed = data.getSed();
        this.shock = data.getShock();
        this.inconsciente = data.isInconsciente();
        this.bodyTag = data.serializeBodyParts();
    }

    private OpenSelfDiagnosisS2CPacket(
            int sangre,
            int stamina,
            int sed,
            int shock,
            boolean inconsciente,
            CompoundTag bodyTag
    ) {
        this.sangre = sangre;
        this.stamina = stamina;
        this.sed = sed;
        this.shock = shock;
        this.inconsciente = inconsciente;
        this.bodyTag = bodyTag == null ? new CompoundTag() : bodyTag;
    }

    public static void encode(OpenSelfDiagnosisS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.sangre);
        buf.writeInt(packet.stamina);
        buf.writeInt(packet.sed);
        buf.writeInt(packet.shock);
        buf.writeBoolean(packet.inconsciente);
        buf.writeNbt(packet.bodyTag);
    }

    public static OpenSelfDiagnosisS2CPacket decode(FriendlyByteBuf buf) {
        return new OpenSelfDiagnosisS2CPacket(
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readBoolean(),
                buf.readNbt()
        );
    }

    public static void handle(OpenSelfDiagnosisS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ClientPlayerData.setSangre(packet.sangre);
            ClientPlayerData.setStamina(packet.stamina);
            ClientPlayerData.setSed(packet.sed);
            ClientPlayerData.setShock(packet.shock);
            ClientPlayerData.setInconsciente(packet.inconsciente);
            ClientPlayerData.applyBodyParts(packet.bodyTag);

            Minecraft.getInstance().setScreen(BodyStatusScreen.self());
        });

        ctx.setPacketHandled(true);
    }
}