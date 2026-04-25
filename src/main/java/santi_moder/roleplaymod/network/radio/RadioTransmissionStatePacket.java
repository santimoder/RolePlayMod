package santi_moder.roleplaymod.network.radio;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.radio.RadioPTTClientState;
import santi_moder.roleplaymod.common.sound.ModSounds;

import java.util.function.Supplier;

public class RadioTransmissionStatePacket {

    private final boolean active;

    public RadioTransmissionStatePacket(boolean active) {
        this.active = active;
    }

    public static void encode(RadioTransmissionStatePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active);
    }

    public static RadioTransmissionStatePacket decode(FriendlyByteBuf buf) {
        return new RadioTransmissionStatePacket(buf.readBoolean());
    }

    public static void handle(RadioTransmissionStatePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            if (msg.active) {
                mc.level.playLocalSound(
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        ModSounds.RADIO_PTT_START,
                        SoundSource.PLAYERS,
                        0.6F,
                        1.0F,
                        false
                );
            } else {
                mc.level.playLocalSound(
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        ModSounds.RADIO_PTT_END,
                        SoundSource.PLAYERS,
                        0.6F,
                        1.0F,
                        false
                );
            }

            RadioPTTClientState.setTransmissionActive(msg.active);
        });

        ctx.setPacketHandled(true);
    }
}