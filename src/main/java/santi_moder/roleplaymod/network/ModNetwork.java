package santi_moder.roleplaymod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.network.phone.PhoneAppInstallC2SPacket;
import santi_moder.roleplaymod.network.phone.PhoneSettingsUpdateC2SPacket;
import santi_moder.roleplaymod.network.phone.whatsapp.*;
import santi_moder.roleplaymod.network.radio.*;

import java.util.Optional;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel STATS_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RolePlayMod.MOD_ID, "stats_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public static final SimpleChannel INVENTORY_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RolePlayMod.MOD_ID, "inventory_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public static final SimpleChannel WHATSAPP_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RolePlayMod.MOD_ID, "whatsapp_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int statsPacketId = 0;
    private static int invPacketId = 0;
    private static int whatsappPacketId = 0;

    private static int nextStatsId() {
        return statsPacketId++;
    }

    private static int nextInvId() {
        return invPacketId++;
    }

    private static int nextWhatsappId() {
        return whatsappPacketId++;
    }

    public static void register() {

        // STATS
        STATS_CHANNEL.registerMessage(
                nextStatsId(),
                SyncPlayerDataPacket.class,
                SyncPlayerDataPacket::encode,
                SyncPlayerDataPacket::decode,
                SyncPlayerDataPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        // INVENTORY
        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                SyncInventoryPacket.class,
                SyncInventoryPacket::encode,
                SyncInventoryPacket::decode,
                SyncInventoryPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                InventoryClickPacket.class,
                InventoryClickPacket::encode,
                InventoryClickPacket::decode,
                InventoryClickPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                QuickMoveInventorySlotPacket.class,
                QuickMoveInventorySlotPacket::encode,
                QuickMoveInventorySlotPacket::decode,
                QuickMoveInventorySlotPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                QuickMoveGroundItemPacket.class,
                QuickMoveGroundItemPacket::encode,
                QuickMoveGroundItemPacket::decode,
                QuickMoveGroundItemPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                RequestInventoryPacket.class,
                RequestInventoryPacket::encode,
                RequestInventoryPacket::decode,
                RequestInventoryPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                GroundItemClickPacket.class,
                GroundItemClickPacket::encode,
                GroundItemClickPacket::decode,
                GroundItemClickPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                ClearCarriedPacket.class,
                ClearCarriedPacket::encode,
                ClearCarriedPacket::decode,
                ClearCarriedPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                DropInventorySlotPacket.class,
                DropInventorySlotPacket::encode,
                DropInventorySlotPacket::decode,
                DropInventorySlotPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                DropCarriedPacket.class,
                DropCarriedPacket::encode,
                DropCarriedPacket::decode,
                DropCarriedPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                QuickAccessTogglePacket.class,
                QuickAccessTogglePacket::encode,
                QuickAccessTogglePacket::decode,
                QuickAccessTogglePacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                QuickAccessHotbarChangedPacket.class,
                QuickAccessHotbarChangedPacket::encode,
                QuickAccessHotbarChangedPacket::decode,
                QuickAccessHotbarChangedPacket::handle
        );

        STATS_CHANNEL.registerMessage(
                nextStatsId(),
                StartTreatmentC2SPacket.class,
                StartTreatmentC2SPacket::encode,
                StartTreatmentC2SPacket::decode,
                StartTreatmentC2SPacket::handle
        );

        STATS_CHANNEL.registerMessage(
                nextStatsId(),
                RequestMedicalBackpackC2SPacket.class,
                RequestMedicalBackpackC2SPacket::encode,
                RequestMedicalBackpackC2SPacket::decode,
                RequestMedicalBackpackC2SPacket::handle
        );

        STATS_CHANNEL.registerMessage(
                nextStatsId(),
                SyncMedicalBackpackS2CPacket.class,
                SyncMedicalBackpackS2CPacket::encode,
                SyncMedicalBackpackS2CPacket::decode,
                SyncMedicalBackpackS2CPacket::handle
        );

        STATS_CHANNEL.registerMessage(
                nextStatsId(),
                MedicalEffectS2CPacket.class,
                MedicalEffectS2CPacket::encode,
                MedicalEffectS2CPacket::decode,
                MedicalEffectS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        STATS_CHANNEL.registerMessage(
                nextStatsId(),
                RequestTargetDiagnosisC2SPacket.class,
                RequestTargetDiagnosisC2SPacket::encode,
                RequestTargetDiagnosisC2SPacket::decode,
                RequestTargetDiagnosisC2SPacket::handle
        );

        STATS_CHANNEL.registerMessage(
                nextStatsId(),
                OpenTargetDiagnosisS2CPacket.class,
                OpenTargetDiagnosisS2CPacket::encode,
                OpenTargetDiagnosisS2CPacket::decode,
                OpenTargetDiagnosisS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        STATS_CHANNEL.registerMessage(
                nextStatsId(),
                OpenSelfDiagnosisS2CPacket.class,
                OpenSelfDiagnosisS2CPacket::encode,
                OpenSelfDiagnosisS2CPacket::decode,
                OpenSelfDiagnosisS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        STATS_CHANNEL.registerMessage(
                nextStatsId(),
                SyncPatientMedicalDataS2CPacket.class,
                SyncPatientMedicalDataS2CPacket::encode,
                SyncPatientMedicalDataS2CPacket::decode,
                SyncPatientMedicalDataS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        // RADIO
        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                ToggleRadioPacket.class,
                ToggleRadioPacket::encode,
                ToggleRadioPacket::decode,
                ToggleRadioPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                IncreaseRadioFrequencyPacket.class,
                IncreaseRadioFrequencyPacket::encode,
                IncreaseRadioFrequencyPacket::decode,
                IncreaseRadioFrequencyPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                DecreaseRadioFrequencyPacket.class,
                DecreaseRadioFrequencyPacket::encode,
                DecreaseRadioFrequencyPacket::decode,
                DecreaseRadioFrequencyPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                StartRadioTransmissionPacket.class,
                StartRadioTransmissionPacket::encode,
                StartRadioTransmissionPacket::decode,
                StartRadioTransmissionPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                StopRadioTransmissionPacket.class,
                StopRadioTransmissionPacket::encode,
                StopRadioTransmissionPacket::decode,
                StopRadioTransmissionPacket::handle
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                RadioTransmissionStatePacket.class,
                RadioTransmissionStatePacket::encode,
                RadioTransmissionStatePacket::decode,
                RadioTransmissionStatePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                PhoneSettingsUpdateC2SPacket.class,
                PhoneSettingsUpdateC2SPacket::encode,
                PhoneSettingsUpdateC2SPacket::decode,
                PhoneSettingsUpdateC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        INVENTORY_CHANNEL.registerMessage(
                nextInvId(),
                PhoneAppInstallC2SPacket.class,
                PhoneAppInstallC2SPacket::encode,
                PhoneAppInstallC2SPacket::decode,
                PhoneAppInstallC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        // WHATSAPP
        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappRequestInitialStateC2SPacket.class,
                WhatsappRequestInitialStateC2SPacket::encode,
                WhatsappRequestInitialStateC2SPacket::decode,
                WhatsappRequestInitialStateC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappInitialStateS2CPacket.class,
                WhatsappInitialStateS2CPacket::encode,
                WhatsappInitialStateS2CPacket::decode,
                WhatsappInitialStateS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappSendMessageC2SPacket.class,
                WhatsappSendMessageC2SPacket::encode,
                WhatsappSendMessageC2SPacket::decode,
                WhatsappSendMessageC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappMessageAddedS2CPacket.class,
                WhatsappMessageAddedS2CPacket::encode,
                WhatsappMessageAddedS2CPacket::decode,
                WhatsappMessageAddedS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappMarkChatReadC2SPacket.class,
                WhatsappMarkChatReadC2SPacket::encode,
                WhatsappMarkChatReadC2SPacket::decode,
                WhatsappMarkChatReadC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappMessageStatusUpdatedS2CPacket.class,
                WhatsappMessageStatusUpdatedS2CPacket::encode,
                WhatsappMessageStatusUpdatedS2CPacket::decode,
                WhatsappMessageStatusUpdatedS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappPresenceUpdatedS2CPacket.class,
                WhatsappPresenceUpdatedS2CPacket::encode,
                WhatsappPresenceUpdatedS2CPacket::decode,
                WhatsappPresenceUpdatedS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappToggleBlockContactC2SPacket.class,
                WhatsappToggleBlockContactC2SPacket::encode,
                WhatsappToggleBlockContactC2SPacket::decode,
                WhatsappToggleBlockContactC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappContactUpdatedS2CPacket.class,
                WhatsappContactUpdatedS2CPacket::encode,
                WhatsappContactUpdatedS2CPacket::decode,
                WhatsappContactUpdatedS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappClearChatC2SPacket.class,
                WhatsappClearChatC2SPacket::encode,
                WhatsappClearChatC2SPacket::decode,
                WhatsappClearChatC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappChatClearedS2CPacket.class,
                WhatsappChatClearedS2CPacket::encode,
                WhatsappChatClearedS2CPacket::decode,
                WhatsappChatClearedS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappCreateContactC2SPacket.class,
                WhatsappCreateContactC2SPacket::encode,
                WhatsappCreateContactC2SPacket::decode,
                WhatsappCreateContactC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappContactCreatedS2CPacket.class,
                WhatsappContactCreatedS2CPacket::encode,
                WhatsappContactCreatedS2CPacket::decode,
                WhatsappContactCreatedS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappOpenOrCreateChatC2SPacket.class,
                WhatsappOpenOrCreateChatC2SPacket::encode,
                WhatsappOpenOrCreateChatC2SPacket::decode,
                WhatsappOpenOrCreateChatC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappChatOpenedS2CPacket.class,
                WhatsappChatOpenedS2CPacket::encode,
                WhatsappChatOpenedS2CPacket::decode,
                WhatsappChatOpenedS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappTogglePinChatC2SPacket.class,
                WhatsappTogglePinChatC2SPacket::encode,
                WhatsappTogglePinChatC2SPacket::decode,
                WhatsappTogglePinChatC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappMarkChatUnreadC2SPacket.class,
                WhatsappMarkChatUnreadC2SPacket::encode,
                WhatsappMarkChatUnreadC2SPacket::decode,
                WhatsappMarkChatUnreadC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappArchiveChatC2SPacket.class,
                WhatsappArchiveChatC2SPacket::encode,
                WhatsappArchiveChatC2SPacket::decode,
                WhatsappArchiveChatC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappDeleteChatC2SPacket.class,
                WhatsappDeleteChatC2SPacket::encode,
                WhatsappDeleteChatC2SPacket::decode,
                WhatsappDeleteChatC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappChatDeletedS2CPacket.class,
                WhatsappChatDeletedS2CPacket::encode,
                WhatsappChatDeletedS2CPacket::decode,
                WhatsappChatDeletedS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappUpdateProfileC2SPacket.class,
                WhatsappUpdateProfileC2SPacket::encode,
                WhatsappUpdateProfileC2SPacket::decode,
                WhatsappUpdateProfileC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        WHATSAPP_CHANNEL.registerMessage(
                nextWhatsappId(),
                WhatsappProfileUpdatedS2CPacket.class,
                WhatsappProfileUpdatedS2CPacket::encode,
                WhatsappProfileUpdatedS2CPacket::decode,
                WhatsappProfileUpdatedS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

    }

    public static <MSG> void sendInventoryToServer(MSG msg) {
        INVENTORY_CHANNEL.sendToServer(msg);
    }

    public static <MSG> void sendStatsToClient(MSG msg, net.minecraftforge.network.PacketDistributor.PacketTarget target) {
        STATS_CHANNEL.send(target, msg);
    }

    public static <MSG> void sendInventoryToClient(MSG msg, net.minecraftforge.network.PacketDistributor.PacketTarget target) {
        INVENTORY_CHANNEL.send(target, msg);
    }

    public static <MSG> void sendWhatsappToServer(MSG msg) {
        WHATSAPP_CHANNEL.sendToServer(msg);
    }

    public static <MSG> void sendWhatsappToClient(MSG msg, net.minecraftforge.network.PacketDistributor.PacketTarget target) {
        WHATSAPP_CHANNEL.send(target, msg);
    }
}