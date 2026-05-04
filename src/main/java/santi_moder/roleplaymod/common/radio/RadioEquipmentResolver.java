package santi_moder.roleplaymod.common.radio;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.player.IPlayerData;

public class RadioEquipmentResolver {

    public static final Capability<IPlayerData> PLAYER_DATA_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private RadioEquipmentResolver() {
    }

    public static EquipmentInventory getEquipment(Player player) {
        if (player == null) return null;

        return player.getCapability(PLAYER_DATA_CAPABILITY)
                .map(IPlayerData::getEquipmentInventory)
                .orElse(null);
    }
}