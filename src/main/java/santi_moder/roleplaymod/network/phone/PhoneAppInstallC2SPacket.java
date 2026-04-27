package santi_moder.roleplaymod.network.phone;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.phone.PhoneAppId;
import santi_moder.roleplaymod.common.phone.PhoneData;
import santi_moder.roleplaymod.common.phone.PhoneItemResolver;

import java.util.function.Supplier;

public final class PhoneAppInstallC2SPacket {

    private final PhoneAppId appId;

    public PhoneAppInstallC2SPacket(PhoneAppId appId) {
        this.appId = appId;
    }

    public static void encode(PhoneAppInstallC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.appId);
    }

    public static PhoneAppInstallC2SPacket decode(FriendlyByteBuf buf) {
        return new PhoneAppInstallC2SPacket(buf.readEnum(PhoneAppId.class));
    }

    public static void handle(PhoneAppInstallC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || packet.appId == null) {
                return;
            }

            ItemStack phone = PhoneItemResolver.getActivePhone(player);
            if (phone.isEmpty()) {
                return;
            }

            PhoneData.installApp(phone, packet.appId);
        });

        context.setPacketHandled(true);
    }
}