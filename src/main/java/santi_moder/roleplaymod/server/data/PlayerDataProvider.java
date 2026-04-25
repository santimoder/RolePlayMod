package santi_moder.roleplaymod.server.data;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import santi_moder.roleplaymod.common.player.IPlayerData;
import santi_moder.roleplaymod.common.player.PlayerData;

public class PlayerDataProvider implements ICapabilitySerializable<CompoundTag> {

    // 🔹 ESTA ES LA CAPABILITY
    public static final Capability<IPlayerData> PLAYER_DATA =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private final IPlayerData data = new PlayerData();
    private final LazyOptional<IPlayerData> optional = LazyOptional.of(() -> data);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
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
        tag.putBoolean("inconsciente", data.isInconsciente());
        tag.putInt("inconsciencias", ((PlayerData) data).getContadorInconsciencias());
        tag.put("equipment", data.getEquipmentInventory().serializeNBT());
        tag.put("body", ((PlayerData) data).serializeBodyParts());

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        data.setSangre(tag.getInt("sangre"));
        data.setStamina(tag.getInt("stamina"));
        data.setSed(tag.getInt("sed"));
        data.setFatiga(tag.getInt("fatiga"));
        data.setSueno(tag.getInt("sueno"));
        data.setInconsciente(tag.getBoolean("inconsciente"));
        ((PlayerData) data).setContadorInconsciencias(tag.getInt("inconsciencias"));
        data.getEquipmentInventory().deserializeNBT(tag.getCompound("equipment"));
        if (tag.contains("body")) {
            ((PlayerData) data).deserializeBodyParts(tag.getCompound("body"));
        }
    }
}