package santi_moder.roleplaymod.common.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;

@AutoRegisterCapability
public interface IPlayerData {

    int getSangre();

    void setSangre(int value);

    int getStamina();

    void setStamina(int value);

    int getSed();

    void setSed(int value);

    int getFatiga();

    void setFatiga(int value);

    int getSueno();

    void setSueno(int value);

    int getShock();

    void setShock(int value);

    void addShock(int amount);

    void tickShockRecovery();

    boolean isInconsciente();

    void setInconsciente(boolean value);

    int getUnconsciousTicks();

    void setUnconsciousTicks(int ticks);

    void tickUnconsciousTicks();

    int getContadorInconsciencias();

    void incrementarInconsciencias();

    void resetInconsciencias();

    void resetPhysicalStats();

    void resetAfterDeath();

    boolean wasOnGround();

    void setWasOnGround(boolean value);

    int getStaminaRegenCooldown();

    void setStaminaRegenCooldown(int ticks);

    void tickStaminaCooldown();

    boolean canRegenerateStamina();

    boolean isStaminaExhausted();

    void setStaminaExhausted(boolean value);

    void addStaminaRegenBuffer(int amount);

    boolean shouldConsumeFoodForStamina();

    void consumeStaminaRegenBuffer();

    int getBodyHp(BodyPart part);

    void setBodyHp(BodyPart part, int value);

    BleedingType getBleeding(BodyPart part);

    void setBleeding(BodyPart part, BleedingType type);

    void damageBodyPart(BodyPart part, int amount);

    void tickBleeding();

    BleedingType getWorstBleeding();

    void applyBleed(BodyPart part, BleedingType type);

    void applyBodyPartEffects();

    boolean canAttack();

    void setCanAttack(boolean value);

    boolean canSprint();

    void setCanSprint(boolean value);

    float getStaminaMultiplier();

    void setStaminaMultiplier(float value);

    boolean isVisionBlurred();

    void setVisionBlurred(boolean value);

    CompoundTag serializeBodyParts();

    void deserializeBodyParts(CompoundTag tag);

    EquipmentInventory getEquipmentInventory();

    default void dropInventoryOnDeath(Player player) {
        EquipmentInventory inv = getEquipmentInventory();
        if (inv == null) return;
        inv.dropAndClear(player);
    }
}