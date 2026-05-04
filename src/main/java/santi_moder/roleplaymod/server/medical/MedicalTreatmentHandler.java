package santi_moder.roleplaymod.server.medical;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.item.ItemInventory;
import santi_moder.roleplaymod.common.player.BleedingType;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.common.player.IPlayerData;
import santi_moder.roleplaymod.item.ModItems;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.SyncMedicalBackpackS2CPacket;
import santi_moder.roleplaymod.network.SyncPatientMedicalDataS2CPacket;
import santi_moder.roleplaymod.network.SyncPlayerDataPacket;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.*;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID)
public final class MedicalTreatmentHandler {

    private static final int TREATMENT_TICKS = 60; // 3 segundos

    private static final Map<UUID, PendingTreatment> PENDING = new HashMap<>();

    private MedicalTreatmentHandler() {
    }

    public static void startBandageTreatment(ServerPlayer healer, int backpackSlot, BodyPart bodyPart, UUID targetUuid) {
        if (healer == null || bodyPart == null) return;
        if (healer.isDeadOrDying()) return;

        ServerPlayer patient = resolvePatient(healer, targetUuid);
        if (patient == null || patient.isDeadOrDying()) return;

        if (healer.distanceToSqr(patient) > 16.0D) return;

        healer.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(healerData -> {
            if (healerData.isInconsciente()) return;

            patient.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(patientData -> {
                if (patientData.getBleeding(bodyPart) == BleedingType.NONE) return;

                EquipmentInventory equipment = healerData.getEquipmentInventory();
                ItemStack backpack = findMedicalBackpack(equipment);

                if (!isValidBackpackSlot(backpack, backpackSlot)) return;

                ItemStack selected = ItemInventory.getItem(backpack, backpackSlot);

                if (selected.isEmpty()) return;
                if (!selected.is(ModItems.BANDAGE.get())) return;

                if (PENDING.containsKey(healer.getUUID())) return;

                PENDING.put(
                        healer.getUUID(),
                        new PendingTreatment(
                                patient.getUUID(),
                                backpackSlot,
                                bodyPart,
                                TREATMENT_TICKS
                        )
                );
            });
        });
    }

    private static ServerPlayer resolvePatient(ServerPlayer healer, UUID targetUuid) {
        if (healer == null) return null;

        if (targetUuid == null) {
            return healer;
        }

        ServerPlayer target = healer.server.getPlayerList().getPlayer(targetUuid);

        if (target == null) return null;
        if (target.level() != healer.level()) return null;

        return target;
    }

    private static ItemStack findMedicalBackpack(EquipmentInventory equipment) {
        if (equipment == null) return ItemStack.EMPTY;

        for (int i = 0; i < equipment.getContainerSize(); i++) {
            ItemStack stack = equipment.getItem(i);

            if (stack.isEmpty()) continue;

            if (ItemInventory.getSize(stack) > 0) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean isValidBackpackSlot(ItemStack backpack, int slot) {
        return !backpack.isEmpty()
                && slot >= 0
                && slot < ItemInventory.getSize(backpack);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        PendingTreatment treatment = PENDING.get(player.getUUID());
        if (treatment == null) return;

        if (player.isDeadOrDying()) {
            PENDING.remove(player.getUUID());
            return;
        }

        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            if (data.isInconsciente()) {
                PENDING.remove(player.getUUID());
            }
        });

        if (!PENDING.containsKey(player.getUUID())) return;

        treatment.ticksLeft--;

        if (treatment.ticksLeft > 0) return;

        PENDING.remove(player.getUUID());
        finishBandageTreatment(player, treatment);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PENDING.remove(player.getUUID());
    }

    private static void finishBandageTreatment(ServerPlayer healer, PendingTreatment treatment) {
        ServerPlayer patient = resolvePatient(healer, treatment.patientUuid);
        if (patient == null || patient.isDeadOrDying()) return;

        if (healer.distanceToSqr(patient) > 16.0D) return;

        healer.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(healerData -> {
            if (healerData.isInconsciente()) return;

            patient.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(patientData -> {
                EquipmentInventory equipment = healerData.getEquipmentInventory();
                ItemStack backpack = findMedicalBackpack(equipment);

                if (!isValidBackpackSlot(backpack, treatment.backpackSlot)) {
                    syncMedicalState(healer, healerData, backpack);
                    return;
                }

                if (patientData.getBleeding(treatment.bodyPart) == BleedingType.NONE) {
                    syncMedicalState(healer, healerData, backpack);
                    syncPatientIfNeeded(healer, patient, patientData);
                    return;
                }

                ItemStack bandage = ItemInventory.getItem(backpack, treatment.backpackSlot);

                if (bandage.isEmpty() || !bandage.is(ModItems.BANDAGE.get())) {
                    syncMedicalState(healer, healerData, backpack);
                    return;
                }

                bandage.shrink(1);

                ItemInventory.setItem(
                        backpack,
                        treatment.backpackSlot,
                        bandage.isEmpty() ? ItemStack.EMPTY : bandage
                );

                backpack.getOrCreateTag().putLong(
                        "medical_update_time",
                        healer.level().getGameTime()
                );

                patientData.setBleeding(treatment.bodyPart, BleedingType.NONE);
                patientData.applyBodyPartEffects();

                ModNetwork.STATS_CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> healer),
                        new SyncPatientMedicalDataS2CPacket(
                                patient.getUUID(),
                                patient.getName().getString(),
                                patientData
                        )
                );

                syncMedicalState(healer, healerData, backpack);
                syncPatientIfNeeded(healer, patient, patientData);
            });
        });
    }

    private static void syncMedicalState(ServerPlayer player, IPlayerData data, ItemStack backpack) {
        ModNetwork.STATS_CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncPlayerDataPacket(data)
        );

        if (!backpack.isEmpty()) {
            ModNetwork.STATS_CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncMedicalBackpackS2CPacket(getBackpackItems(backpack))
            );
        }
    }

    private static void syncPatientIfNeeded(ServerPlayer healer, ServerPlayer patient, IPlayerData patientData) {
        if (patient == null || patientData == null) return;

        if (patient != healer) {
            ModNetwork.STATS_CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> patient),
                    new SyncPlayerDataPacket(patientData)
            );
        }
    }

    private static List<ItemStack> getBackpackItems(ItemStack backpack) {
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < ItemInventory.getSize(backpack); i++) {
            items.add(ItemInventory.getItem(backpack, i).copy());
        }

        return items;
    }

    private static final class PendingTreatment {
        private final UUID patientUuid;
        private final int backpackSlot;
        private final BodyPart bodyPart;
        private int ticksLeft;

        private PendingTreatment(UUID patientUuid, int backpackSlot, BodyPart bodyPart, int ticksLeft) {
            this.patientUuid = patientUuid;
            this.backpackSlot = backpackSlot;
            this.bodyPart = bodyPart;
            this.ticksLeft = ticksLeft;
        }
    }
}