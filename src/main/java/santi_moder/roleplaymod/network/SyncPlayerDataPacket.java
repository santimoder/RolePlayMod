package santi_moder.roleplaymod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.data.ClientPlayerData;
import santi_moder.roleplaymod.common.player.IPlayerData;
import santi_moder.roleplaymod.common.player.PlayerData;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.function.Supplier;

public class SyncPlayerDataPacket {

    private final int sangre;
    private final int stamina;
    private final int sed;
    private final int fatiga;
    private final int sueno;
    private final int shock;

    private final boolean inconsciente;
    private final boolean canAttack;
    private final boolean canSprint;
    private final boolean visionBlurred;

    private final float staminaMultiplier;

    private final CompoundTag bodyPartsTag;

    public SyncPlayerDataPacket(IPlayerData data) {
        this.sangre = data.getSangre();
        this.stamina = data.getStamina();
        this.sed = data.getSed();
        this.fatiga = data.getFatiga();
        this.sueno = data.getSueno();
        this.shock = data.getShock();

        this.inconsciente = data.isInconsciente();
        this.canAttack = data.canAttack();
        this.canSprint = data.canSprint();
        this.visionBlurred = data.isVisionBlurred();

        this.staminaMultiplier = data.getStaminaMultiplier();

        this.bodyPartsTag = data.serializeBodyParts();
    }

    private SyncPlayerDataPacket(
            int sangre,
            int stamina,
            int sed,
            int fatiga,
            int sueno,
            int shock,
            boolean inconsciente,
            boolean canAttack,
            boolean canSprint,
            boolean visionBlurred,
            float staminaMultiplier,
            CompoundTag bodyPartsTag
    ) {
        this.sangre = sangre;
        this.stamina = stamina;
        this.sed = sed;
        this.fatiga = fatiga;
        this.sueno = sueno;
        this.shock = shock;

        this.inconsciente = inconsciente;
        this.canAttack = canAttack;
        this.canSprint = canSprint;
        this.visionBlurred = visionBlurred;

        this.staminaMultiplier = staminaMultiplier;
        this.bodyPartsTag = bodyPartsTag == null ? new CompoundTag() : bodyPartsTag;
    }

    public static void encode(SyncPlayerDataPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.sangre);
        buf.writeInt(packet.stamina);
        buf.writeInt(packet.sed);
        buf.writeInt(packet.fatiga);
        buf.writeInt(packet.sueno);
        buf.writeInt(packet.shock);

        buf.writeBoolean(packet.inconsciente);
        buf.writeBoolean(packet.canAttack);
        buf.writeBoolean(packet.canSprint);
        buf.writeBoolean(packet.visionBlurred);

        buf.writeFloat(packet.staminaMultiplier);

        buf.writeNbt(packet.bodyPartsTag);
    }

    public static SyncPlayerDataPacket decode(FriendlyByteBuf buf) {
        int sangre = buf.readInt();
        int stamina = buf.readInt();
        int sed = buf.readInt();
        int fatiga = buf.readInt();
        int sueno = buf.readInt();
        int shock = buf.readInt();

        boolean inconsciente = buf.readBoolean();
        boolean canAttack = buf.readBoolean();
        boolean canSprint = buf.readBoolean();
        boolean visionBlurred = buf.readBoolean();

        float staminaMultiplier = buf.readFloat();

        CompoundTag bodyPartsTag = buf.readNbt();
        if (bodyPartsTag == null) bodyPartsTag = new CompoundTag();

        return new SyncPlayerDataPacket(
                sangre,
                stamina,
                sed,
                fatiga,
                sueno,
                shock,
                inconsciente,
                canAttack,
                canSprint,
                visionBlurred,
                staminaMultiplier,
                bodyPartsTag
        );
    }

    public static void handle(SyncPlayerDataPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            ClientPlayerData.setSangre(packet.sangre);
            ClientPlayerData.setStamina(packet.stamina);
            ClientPlayerData.setSed(packet.sed);
            ClientPlayerData.setShock(packet.shock);
            ClientPlayerData.setInconsciente(packet.inconsciente);
            ClientPlayerData.applyBodyParts(packet.bodyPartsTag);

            mc.player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                data.setSangre(packet.sangre);
                data.setStamina(packet.stamina);
                data.setSed(packet.sed);
                data.setFatiga(packet.fatiga);
                data.setSueno(packet.sueno);
                data.setShock(packet.shock);

                data.setInconsciente(packet.inconsciente);
                data.setCanAttack(packet.canAttack);
                data.setCanSprint(packet.canSprint);
                data.setVisionBlurred(packet.visionBlurred);
                data.setStaminaMultiplier(packet.staminaMultiplier);

                data.deserializeBodyParts(packet.bodyPartsTag);
                data.applyBodyPartEffects();
            });
        });

        ctx.setPacketHandled(true);
    }
}