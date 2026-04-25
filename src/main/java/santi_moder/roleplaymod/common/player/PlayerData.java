package santi_moder.roleplaymod.common.player;

import net.minecraft.nbt.CompoundTag;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;

import java.util.EnumMap;
import java.util.Map;

public class PlayerData implements IPlayerData {

    private final Map<BodyPart, Integer> bodyHp = new EnumMap<>(BodyPart.class);
    private final Map<BodyPart, BleedingType> bleedings = new EnumMap<>(BodyPart.class);

    public PlayerData() {
        for (BodyPart part : BodyPart.values()) {
            bodyHp.put(part, 5);
            bleedings.put(part, BleedingType.NONE);
        }
    }

    public void damageBodyPart(BodyPart part, int amount) {
        int hp = getBodyHp(part);
        hp = Math.max(0, hp - amount);
        setBodyHp(part, hp);

        // Aplicar sangrado
        if (hp <= 0) {
            setBleeding(part, BleedingType.HEAVY);
        } else if (hp <= 2) {
            setBleeding(part, BleedingType.MEDIUM);
        } else {
            setBleeding(part, BleedingType.NONE);
        }
    }

    public BleedingType getWorstBleeding() {
        BleedingType worst = BleedingType.NONE;
        for (BleedingType type : bleedings.values()) {
            if (type.ordinal() > worst.ordinal()) worst = type;
        }
        return worst;
    }

    // =====================
    // EFECTOS POR PARTES DEL CUERPO
    // =====================

    // Penalizaciones
    private boolean canAttack = true;       // brazos
    private boolean canSprint = true;       // piernas
    private float staminaMultiplier = 1.0f; // torso (afecta regeneración y consumo)
    private boolean visionBlurred = false;  // cabeza


    private int sangre = 100;
    private int stamina = 100;
    private int sed = 100;
    private int fatiga = 0;
    private int sueno = 100;
    private int staminaRegenBuffer = 0;

    private boolean inconsciente = false;
    private int contadorInconsciencias = 0;
    private boolean wasOnGround = true;
    private boolean staminaExhausted = false;
    private int staminaRegenCooldown = 0;

    public void resetPhysicalStats() {
        this.sangre = 80;
        this.stamina = 50;
        this.sed = 50;
        this.fatiga = 30;
        this.sueno = 50;
        this.staminaRegenCooldown = 0;
        this.staminaExhausted = false;
    }

    @Override
    public void resetAfterDeath() {
        // Stats base al respawn
        this.sangre = 100;
        this.stamina = 100;
        this.sed = 100;
        this.fatiga = 0;
        this.sueno = 100;

        // Estado general
        this.inconsciente = false;
        this.contadorInconsciencias = 0;
        this.wasOnGround = true;
        this.staminaExhausted = false;
        this.staminaRegenCooldown = 0;
        this.staminaRegenBuffer = 0;

        // Partes del cuerpo
        for (BodyPart part : BodyPart.values()) {
            bodyHp.put(part, 5);
            bleedings.put(part, BleedingType.NONE);
        }

        // Penalizaciones / efectos derivados
        this.canAttack = true;
        this.canSprint = true;
        this.staminaMultiplier = 1.0f;
        this.visionBlurred = false;
    }

    @Override
    public int getSangre() {
        return sangre;
    }

    @Override
    public void setSangre(int value) {
        sangre = clamp(value);
    }

    @Override
    public int getStamina() {
        return stamina;
    }

    @Override
    public void setStamina(int value) {
        stamina = clamp(value);
    }

    @Override
    public int getSed() {
        return sed;
    }

    @Override
    public void setSed(int value) {
        sed = clamp(value);
    }

    @Override
    public int getFatiga() {
        return fatiga;
    }

    @Override
    public void setFatiga(int value) {
        fatiga = clamp(value);
    }

    @Override
    public int getSueno() {
        return sueno;
    }

    @Override
    public void setSueno(int value) {
        sueno = clamp(value);
    }

    @Override
    public boolean isInconsciente() {
        return inconsciente;
    }

    @Override
    public void setInconsciente(boolean value) {
        inconsciente = value;
    }

    @Override
    public int getContadorInconsciencias() {
        return contadorInconsciencias;
    }

    @Override
    public void incrementarInconsciencias() {
        contadorInconsciencias++;
    }

    @Override
    public void resetInconsciencias() {
        contadorInconsciencias = 0;
    }

    @Override
    public boolean wasOnGround() {
        return wasOnGround;
    }

    @Override
    public void setWasOnGround(boolean value) {
        this.wasOnGround = value;
    }
    @Override
    public int getStaminaRegenCooldown() {
        return staminaRegenCooldown;
    }

    @Override
    public void setStaminaRegenCooldown(int ticks) {
        this.staminaRegenCooldown = Math.max(0, ticks);
    }

    @Override
    public void tickStaminaCooldown() {
        if (staminaRegenCooldown > 0) {
            staminaRegenCooldown--;
        }
    }
    public void setContadorInconsciencias(int value) {
        this.contadorInconsciencias = value;
    }

    public void addStaminaRegenBuffer(int amount) {
        staminaRegenBuffer += amount;
    }

    public boolean shouldConsumeFoodForStamina() {
        return staminaRegenBuffer >= 20;
    }

    public void consumeStaminaRegenBuffer() {
        staminaRegenBuffer -= 20;
    }

    @Override
    public boolean canRegenerateStamina() {
        return staminaRegenCooldown <= 0;
    }

    @Override
    public boolean isStaminaExhausted() {
        return staminaExhausted;
    }

    @Override
    public void setStaminaExhausted(boolean value) {
        this.staminaExhausted = value;
    }

    // Serialización parcial de partes del cuerpo
    public CompoundTag serializeBodyParts() {
        CompoundTag tag = new CompoundTag();
        for (BodyPart part : BodyPart.values()) {
            tag.putInt(part.name(), getBodyHp(part));
            tag.putInt(part.name() + "_bleeding", getBleeding(part).ordinal());
        }
        return tag;
    }

    // Deserialización parcial de partes del cuerpo
    public void deserializeBodyParts(CompoundTag tag) {
        for (BodyPart part : BodyPart.values()) {
            if (tag.contains(part.name())) {
                setBodyHp(part, tag.getInt(part.name()));
            }
            if (tag.contains(part.name() + "_bleeding")) {
                setBleeding(part, BleedingType.values()[tag.getInt(part.name() + "_bleeding")]);
            }
        }
    }

    // ================= BODY PARTS =================

    // Obtener HP de una parte específica
    public int getBodyHp(BodyPart part) {
        return bodyHp.get(part);
    }

    // Establecer HP de una parte específica
    public void setBodyHp(BodyPart part, int value) {
        bodyHp.put(part, Math.max(0, Math.min(5, value))); // max HP por parte = 5
    }

    // Obtener tipo de sangrado de una parte
    public BleedingType getBleeding(BodyPart part) {
        return bleedings.get(part);
    }

    // Establecer sangrado de una parte
    public void setBleeding(BodyPart part, BleedingType type) {
        bleedings.put(part, type);
    }

    public void applyBleed(BodyPart part, BleedingType type) {
        // solo aplica si el nuevo tipo es más grave
        if (type.ordinal() > getBleeding(part).ordinal()) {
            setBleeding(part, type);
        }
    }

    // ================= LOGICA DE SANGRADO =================

    // Disminuye la sangre general según sangrado de todas las partes
    public void tickBleeding() {
        for (BleedingType type : bleedings.values()) {
            sangre = clamp(sangre - type.getBloodLoss());
        }
    }

    // BRAZOS
    public boolean canAttack() { return canAttack; }
    public void setCanAttack(boolean value) { canAttack = value; }

    // PIERNAS
    public boolean canSprint() { return canSprint; }
    public void setCanSprint(boolean value) { canSprint = value; }

    // TORSO
    public float getStaminaMultiplier() { return staminaMultiplier; }
    public void setStaminaMultiplier(float value) { staminaMultiplier = value; }

    // CABEZA
    public boolean isVisionBlurred() { return visionBlurred; }
    public void setVisionBlurred(boolean value) { visionBlurred = value; }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private final EquipmentInventory equipmentInventory = new EquipmentInventory();

    public EquipmentInventory getEquipmentInventory() {
        return equipmentInventory;
    }

    public void applyBodyPartEffects() {
        // BRAZOS
        if (bodyHp.get(BodyPart.LEFT_ARM) <= 1 || bodyHp.get(BodyPart.RIGHT_ARM) <= 1) {
            canAttack = false;
        } else {
            canAttack = true;
        }

        // PIERNAS
        if (bodyHp.get(BodyPart.LEFT_LEG) <= 1 || bodyHp.get(BodyPart.RIGHT_LEG) <= 1) {
            canSprint = false;
        } else {
            canSprint = true;
        }

        // TORSO
        if (bodyHp.get(BodyPart.TORSO) <= 1) {
            staminaMultiplier = 0.5f;
        } else {
            staminaMultiplier = 1.0f;
        }

        // CABEZA
        if (bodyHp.get(BodyPart.HEAD) <= 2) {
            visionBlurred = true;
        } else {
            visionBlurred = false;
        }
    }
}
