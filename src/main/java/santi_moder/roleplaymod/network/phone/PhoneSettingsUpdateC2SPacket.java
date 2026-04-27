package santi_moder.roleplaymod.network.phone;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.phone.PhoneData;
import santi_moder.roleplaymod.common.phone.PhoneItemResolver;

import java.util.function.Supplier;

public final class PhoneSettingsUpdateC2SPacket {

    private final String action;
    private final String value;

    public PhoneSettingsUpdateC2SPacket(String action, String value) {
        this.action = action == null ? "" : action;
        this.value = value == null ? "" : value;
    }

    public static void encode(PhoneSettingsUpdateC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.action);
        buf.writeUtf(packet.value);
    }

    public static PhoneSettingsUpdateC2SPacket decode(FriendlyByteBuf buf) {
        return new PhoneSettingsUpdateC2SPacket(buf.readUtf(), buf.readUtf());
    }

    public static void handle(PhoneSettingsUpdateC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            ItemStack phone = PhoneItemResolver.getActivePhone(player);
            if (phone.isEmpty()) {
                return;
            }

            apply(phone, player, packet.action, packet.value);
        });

        context.setPacketHandled(true);
    }

    private static void apply(ItemStack phone, ServerPlayer player, String action, String value) {
        switch (action) {
            case "set_wallpaper" -> PhoneData.setWallpaper(phone, value);
            case "toggle_theme" -> PhoneData.toggleThemeMode(phone);
            case "cycle_text_size" -> PhoneData.cycleTextSize(phone);
            case "cycle_icon_size" -> PhoneData.cycleIconSize(phone);

            case "cycle_call_volume" -> PhoneData.cycleCallVolume(phone);
            case "cycle_notification_volume" -> PhoneData.cycleNotificationVolume(phone);
            case "toggle_silent" -> PhoneData.toggleSilentMode(phone);

            case "set_profile_name" -> PhoneData.setProfileName(phone, value);
            case "set_profile_surname" -> PhoneData.setProfileSurname(phone, value);
            case "set_profile_birthdate" -> PhoneData.setProfileBirthdate(phone, value);
            case "cycle_profile_photo" -> PhoneData.cycleProfilePhoto(phone);

            case "set_passcode" -> PhoneData.setPasscode(phone, value);
            case "enable_passcode" -> {
                PhoneData.setPasscode(phone, value);
                PhoneData.setHasPassword(phone, true);
                PhoneData.setLocked(phone, true);
            }
            case "disable_passcode" -> {
                PhoneData.setHasPassword(phone, false);
                PhoneData.setLocked(phone, false);
                PhoneData.clearFaceId(phone);
            }
            case "clear_face_id" -> PhoneData.clearFaceId(phone);
            case "register_face_id" -> {
                PhoneData.registerFaceId(phone, player);
                PhoneData.setLocked(phone, true);
            }
        }
    }
}