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
    private final CompoundTag bodyPartsTag;

    // Constructor servidor
    public SyncPlayerDataPacket(IPlayerData data) {
        this.sangre = data.getSangre();
        this.stamina = data.getStamina();
        this.sed = data.getSed();
        if (data instanceof PlayerData pd) {
            this.bodyPartsTag = pd.serializeBodyParts();
        } else {
            this.bodyPartsTag = new CompoundTag();
        }
    }

    // Getters para cliente
    public int getSangre() { return sangre; }
    public int getStamina() { return stamina; }
    public int getSed() { return sed; }
    public CompoundTag getBodyPartsTag() { return bodyPartsTag; }

    // Encode
    public static void encode(SyncPlayerDataPacket pkt, net.minecraft.network.FriendlyByteBuf buf) {
        buf.writeInt(pkt.sangre);
        buf.writeInt(pkt.stamina);
        buf.writeInt(pkt.sed);
        buf.writeNbt(pkt.bodyPartsTag);
    }

    // Decode
    public static SyncPlayerDataPacket decode(FriendlyByteBuf buf) {
        int sangre = buf.readInt();
        int stamina = buf.readInt();
        int sed = buf.readInt();
        CompoundTag bodyTag = buf.readNbt();
        if (bodyTag == null) bodyTag = new CompoundTag(); // 🔹 evita NPE
        return new SyncPlayerDataPacket(sangre, stamina, sed, bodyTag);
    }

    // Constructor privado decode
    private SyncPlayerDataPacket(int sangre, int stamina, int sed, CompoundTag bodyPartsTag) {
        this.sangre = sangre;
        this.stamina = stamina;
        this.sed = sed;
        this.bodyPartsTag = bodyPartsTag;
    }

    public static void handle(SyncPlayerDataPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // 1) Actualizar cache cliente que usa HUD / BodyStatusScreen
            ClientPlayerData.setSangre(msg.getSangre());
            ClientPlayerData.setStamina(msg.getStamina());
            ClientPlayerData.setSed(msg.getSed());
            ClientPlayerData.applyBodyParts(msg.getBodyPartsTag());

            // 2) Actualizar también la capability cliente (opcional pero prolijo)
            mc.player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                data.setSangre(msg.getSangre());
                data.setStamina(msg.getStamina());
                data.setSed(msg.getSed());

                if (data instanceof PlayerData pd) {
                    pd.deserializeBodyParts(msg.getBodyPartsTag());
                }
            });
            System.out.println("[CLIENT] Packet recibido -> sangre=" + msg.getSangre()
                    + ", stamina=" + msg.getStamina()
                    + ", sed=" + msg.getSed());

        });
        ctx.setPacketHandled(true);
    }
}