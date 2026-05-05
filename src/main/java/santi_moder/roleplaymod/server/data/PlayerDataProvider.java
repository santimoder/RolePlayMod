package santi_moder.roleplaymod.server.data;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import santi_moder.roleplaymod.common.player.IPlayerData;
import santi_moder.roleplaymod.common.player.PlayerData;

public class PlayerDataProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<IPlayerData> PLAYER_DATA =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private final IPlayerData data = new PlayerData();
    private final LazyOptional<IPlayerData> optional = LazyOptional.of(() -> data);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> cap,
            @Nullable Direction side
    ) {
        return cap == PLAYER_DATA ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("sangre", data.getSangre());
        tag.putInt("stamina", data.getStamina());
        tag.putInt("sed", data.getSed());
        tag.putInt("fatiga", data.getFatiga());
        tag.putInt("sueno", data.getSueno());
        tag.putInt("shock", data.getShock());
        tag.putInt("unconsciousTicks", data.getUnconsciousTicks());

        tag.putInt("recentBloodLoss", data.getRecentBloodLoss());

        tag.putBoolean("inconsciente", data.isInconsciente());
        tag.putInt("inconsciencias", data.getContadorInconsciencias());

        tag.putBoolean("wasOnGround", data.wasOnGround());
        tag.putBoolean("staminaExhausted", data.isStaminaExhausted());
        tag.putInt("staminaRegenCooldown", data.getStaminaRegenCooldown());

        tag.put("equipment", data.getEquipmentInventory().serializeNBT());
        tag.put("body", data.serializeBodyParts());

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        data.setSangre(tag.getInt("sangre"));
        data.setStamina(tag.getInt("stamina"));
        data.setSed(tag.getInt("sed"));
        data.setFatiga(tag.getInt("fatiga"));
        data.setSueno(tag.getInt("sueno"));
        data.setShock(tag.getInt("shock"));

        data.addRecentBloodLoss(tag.getInt("recentBloodLoss"));

        data.setUnconsciousTicks(tag.getInt("unconsciousTicks"));

        data.setInconsciente(tag.getBoolean("inconsciente"));

        if (data instanceof PlayerData playerData) {
            playerData.setContadorInconsciencias(tag.getInt("inconsciencias"));
        }

        data.setWasOnGround(tag.getBoolean("wasOnGround"));
        data.setStaminaExhausted(tag.getBoolean("staminaExhausted"));
        data.setStaminaRegenCooldown(tag.getInt("staminaRegenCooldown"));

        if (tag.contains("equipment")) {
            data.getEquipmentInventory().deserializeNBT(tag.getCompound("equipment"));
        }

        if (tag.contains("body")) {
            data.deserializeBodyParts(tag.getCompound("body"));
        }

        data.applyBodyPartEffects();
    }
}