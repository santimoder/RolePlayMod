package santi_moder.roleplaymod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.data.ClientDamageFeedback;
import santi_moder.roleplaymod.client.effects.DamageShaderHandler;

import java.util.function.Supplier;

public class MedicalEffectS2CPacket {

    public enum Type {
        DAMAGE_HIT,
        BLEED_PULSE
    }

    private final Type type;
    private final float intensity;

    public MedicalEffectS2CPacket(Type type) {
        this(type, 1.0F);
    }

    public MedicalEffectS2CPacket(Type type, float intensity) {
        this.type = type;
        this.intensity = intensity;
    }

    public static void encode(MedicalEffectS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.type);
        buf.writeFloat(packet.intensity);
    }

    public static MedicalEffectS2CPacket decode(FriendlyByteBuf buf) {
        return new MedicalEffectS2CPacket(buf.readEnum(Type.class), buf.readFloat());
    }

    public static void handle(MedicalEffectS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            switch (packet.type) {
                case DAMAGE_HIT -> {
                    ClientDamageFeedback.triggerDamage(packet.intensity);
                    DamageShaderHandler.trigger(packet.intensity);
                }
                case BLEED_PULSE -> ClientDamageFeedback.triggerBleeding(packet.intensity);
            }
        });

        ctx.setPacketHandled(true);
    }
}