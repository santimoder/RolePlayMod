package santi_moder.roleplaymod.server.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.common.player.PlayerData;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.SyncInventoryPacket;
import santi_moder.roleplaymod.network.SyncPlayerDataPacket;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID)

public class PlayerCapabilityEvents {

    private static final ResourceLocation ID =
            new ResourceLocation("roleplaymod", "player_data");

    // ==========================
    // CAPABILITY ATTACH
    // ==========================
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) return;

        event.addCapability(ID, new PlayerDataProvider());
        System.out.println("[CAP] PlayerDataProvider adjuntado a un Player");
    }

    // ==========================
    // PLAYER CLONE (RESPAWN)
    // ==========================
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();

        event.getEntity().getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(newData -> {
            event.getOriginal().getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(oldData -> {

                if (event.isWasDeath()) {
                    newData.resetAfterDeath();
                    return;
                }

                newData.setSangre(oldData.getSangre());
                newData.setStamina(oldData.getStamina());
                newData.setSed(oldData.getSed());
                newData.setFatiga(oldData.getFatiga());
                newData.setSueno(oldData.getSueno());
                newData.setShock(oldData.getShock());

                newData.setInconsciente(oldData.isInconsciente());
                newData.setUnconsciousTicks(oldData.getUnconsciousTicks());

                newData.setWasOnGround(oldData.wasOnGround());
                newData.setStaminaRegenCooldown(oldData.getStaminaRegenCooldown());
                newData.setStaminaExhausted(oldData.isStaminaExhausted());

                newData.deserializeBodyParts(oldData.serializeBodyParts());

                newData.setCanAttack(oldData.canAttack());
                newData.setCanSprint(oldData.canSprint());
                newData.setStaminaMultiplier(oldData.getStaminaMultiplier());
                newData.setVisionBlurred(oldData.isVisionBlurred());

                newData.getEquipmentInventory().deserializeNBT(
                        oldData.getEquipmentInventory().serializeNBT()
                );

                if (oldData instanceof PlayerData oldPd && newData instanceof PlayerData newPd) {
                    newPd.setContadorInconsciencias(oldPd.getContadorInconsciencias());
                }

                newData.applyBodyPartEffects();
            });
        });

        event.getOriginal().invalidateCaps();
    }

    // ==========================
    // DROPEAR INVENTARIO AL MORIR
    // ==========================
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {

            // Dropear y limpiar inventario del jugador muerto
            data.getEquipmentInventory().dropAndClear(player);

            // 🔹 SINCRONIZAR INVENTARIO CON EL CLIENTE
            ModNetwork.INVENTORY_CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new SyncInventoryPacket(
                            data.getEquipmentInventory().serializeNBT(),
                            serverPlayer.containerMenu.getCarried()
                    )
            );

            System.out.println("[SERVER] Inventario de " + player.getName().getString() + " sincronizado al morir.");
        });
    }

    // ==========================
    // LOGIN
    // ==========================
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {

            System.out.println("[SERVER] Enviando SyncPlayerDataPacket a " + player.getName().getString()
                    + " | sangre=" + data.getSangre()
                    + ", stamina=" + data.getStamina()
                    + ", sed=" + data.getSed());

            // 🔥 SYNC INICIAL DE STATS
            ModNetwork.STATS_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncPlayerDataPacket(data));

            // 🔥 SYNC INICIAL DE INVENTARIO
            ModNetwork.INVENTORY_CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncInventoryPacket(
                            data.getEquipmentInventory().serializeNBT(),
                            player.containerMenu.getCarried()
                    )
            );

            System.out.println("[SERVER] Inventario de " + player.getName().getString() + " sincronizado al login.");
        });
    }

    // ==========================
    // RESPAWN
    // ==========================
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {

            // 🔥 SYNC DE STATS DESPUÉS DEL RESPAWN
            ModNetwork.STATS_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncPlayerDataPacket(data));

            // 🔥 SYNC DE INVENTARIO
            ModNetwork.INVENTORY_CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncInventoryPacket(
                            data.getEquipmentInventory().serializeNBT(),
                            player.containerMenu.getCarried()
                    )
            );

            System.out.println("[SERVER] Stats e inventario de " + player.getName().getString() + " sincronizados al respawn.");
        });
    }
}