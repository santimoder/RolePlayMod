package santi_moder.roleplaymod.common.player;

import net.minecraft.nbt.CompoundTag;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;

import java.util.EnumMap;
import java.util.Map;

public class PlayerData implements IPlayerData {

    private static final int BLOOD_LOSS_WINDOW_TICKS = 20 * 10; // 10 segundos

    private int recentBloodLoss = 0;
    private int recentBloodLossTicks = 0;

    private static final int DEFAULT_BODY_HP = 30;
    private static final int HEAD_MAX_HP = 20;
    private static final int TORSO_MAX_HP = 60;
    private static final int ARM_MAX_HP = 25;
    private static final int LEG_MAX_HP = 30;

    private final Map<BodyPart, Integer> bodyHp = new EnumMap<>(BodyPart.class);
    private final Map<BodyPart, BleedingType> bleedings = new EnumMap<>(BodyPart.class);

    private final EquipmentInventory equipmentInventory = new EquipmentInventory();

    private int sangre = 100;
    private int stamina = 100;
    private int sed = 100;
    private int fatiga = 0;
    private int sueno = 100;
    private int shock = 0;
    private int unconsciousTicks = 0;

    private int staminaRegenBuffer = 0;

    private boolean inconsciente = false;
    private int contadorInconsciencias = 0;
    private boolean wasOnGround = true;
    private boolean staminaExhausted = false;
    private int staminaRegenCooldown = 0;

    private boolean canAttack = true;
    private boolean canSprint = true;
    private float staminaMultiplier = 1.0f;
    private boolean visionBlurred = false;

    public PlayerData() {
        for (BodyPart part : BodyPart.values()) {
            bodyHp.put(part, getMaxBodyHp(part));
            bleedings.put(part, BleedingType.NONE);
        }
    }

    @Override
    public void resetPhysicalStats() {
        this.sangre = 80;
        this.stamina = 50;
        this.sed = 50;
        this.fatiga = 30;
        this.sueno = 50;
        this.shock = 0;
        this.staminaRegenCooldown = 0;
        this.staminaExhausted = false;
        this.staminaRegenBuffer = 0;
        this.unconsciousTicks = 0;
        this.recentBloodLoss = 0;
        this.recentBloodLossTicks = 0;

        applyBodyPartEffects();
    }

    @Override
    public void resetAfterDeath() {
        this.sangre = 100;
        this.stamina = 100;
        this.sed = 100;
        this.fatiga = 0;
        this.sueno = 100;
        this.shock = 0;
        this.unconsciousTicks = 0;

        this.recentBloodLoss = 0;
        this.recentBloodLossTicks = 0;

        this.inconsciente = false;
        this.contadorInconsciencias = 0;
        this.wasOnGround = true;
        this.staminaExhausted = false;
        this.staminaRegenCooldown = 0;
        this.staminaRegenBuffer = 0;

        for (BodyPart part : BodyPart.values()) {
            bodyHp.put(part, getMaxBodyHp(part));
            bleedings.put(part, BleedingType.NONE);
        }

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
        int previous = this.sangre;
        int next = clampPercent(value);

        this.sangre = next;

        int lost = previous - next;
        if (lost > 0) {
            addRecentBloodLoss(lost);
        }
    }

    @Override
    public int getRecentBloodLoss() {
        return recentBloodLoss;
    }

    @Override
    public void addRecentBloodLoss(int amount) {
        if (amount <= 0) return;

        recentBloodLoss = Math.min(100, recentBloodLoss + amount);
        recentBloodLossTicks = BLOOD_LOSS_WINDOW_TICKS;
    }

    @Override
    public void tickRecentBloodLossWindow() {
        if (recentBloodLossTicks <= 0) {
            recentBloodLoss = 0;
            return;
        }

        recentBloodLossTicks -= 20;

        if (recentBloodLossTicks <= 0) {
            recentBloodLoss = 0;
            recentBloodLossTicks = 0;
        }
    }

    @Override
    public void resetRecentBloodLoss() {
        recentBloodLoss = 0;
        recentBloodLossTicks = 0;
    }

    @Override
    public int getStamina() {
        return stamina;
    }

    @Override
    public void setStamina(int value) {
        stamina = clampPercent(value);
    }

    @Override
    public int getSed() {
        return sed;
    }

    @Override
    public void setSed(int value) {
        sed = clampPercent(value);
    }

    @Override
    public int getFatiga() {
        return fatiga;
    }

    @Override
    public void setFatiga(int value) {
        fatiga = clampPercent(value);
    }

    @Override
    public int getSueno() {
        return sueno;
    }

    @Override
    public void setSueno(int value) {
        sueno = clampPercent(value);
    }

    @Override
    public int getShock() {
        return shock;
    }

    @Override
    public void setShock(int value) {
        shock = clampPercent(value);
    }

    @Override
    public void addShock(int amount) {
        if (amount <= 0) return;
        setShock(this.shock + amount);
    }

    @Override
    public void tickShockRecovery() {
        if (shock <= 0) return;

        int recovery = 1;

        if (sangre >= 80) recovery = 3;
        else if (sangre >= 60) recovery = 2;
        else if (sangre <= 25) recovery = 0;

        if (getWorstBleeding() == BleedingType.HEAVY) {
            recovery = Math.max(0, recovery - 1);
        }

        if (recovery > 0) {
            setShock(shock - recovery);
        }
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
    public int getUnconsciousTicks() {
        return unconsciousTicks;
    }

    @Override
    public void setUnconsciousTicks(int ticks) {
        unconsciousTicks = Math.max(0, ticks);
    }

    @Override
    public void tickUnconsciousTicks() {
        if (unconsciousTicks > 0) {
            unconsciousTicks = Math.max(0, unconsciousTicks - 20);
        }
    }

    @Override
    public int getContadorInconsciencias() {
        return contadorInconsciencias;
    }

    public void setContadorInconsciencias(int value) {
        contadorInconsciencias = Math.max(0, value);
    }

    @Override
    public boolean wasOnGround() {
        return wasOnGround;
    }

    @Override
    public void setWasOnGround(boolean value) {
        wasOnGround = value;
    }

    @Override
    public int getStaminaRegenCooldown() {
        return staminaRegenCooldown;
    }

    @Override
    public void setStaminaRegenCooldown(int ticks) {
        staminaRegenCooldown = Math.max(0, ticks);
    }

    @Override
    public void tickStaminaCooldown() {
        if (staminaRegenCooldown > 0) {
            staminaRegenCooldown--;
        }
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
        staminaExhausted = value;
    }

    @Override
    public void addStaminaRegenBuffer(int amount) {
        staminaRegenBuffer = Math.max(0, staminaRegenBuffer + amount);
    }

    @Override
    public boolean shouldConsumeFoodForStamina() {
        return staminaRegenBuffer >= 20;
    }

    @Override
    public void consumeStaminaRegenBuffer() {
        staminaRegenBuffer = Math.max(0, staminaRegenBuffer - 20);
    }

    private int getMaxBodyHp(BodyPart part) {
        return switch (part) {
            case HEAD -> HEAD_MAX_HP;
            case TORSO -> TORSO_MAX_HP;
            case LEFT_ARM, RIGHT_ARM -> ARM_MAX_HP;
            case LEFT_LEG, RIGHT_LEG -> LEG_MAX_HP;
        };
    }

    @Override
    public int getBodyHp(BodyPart part) {
        return bodyHp.getOrDefault(part, getMaxBodyHp(part));
    }

    @Override
    public void setBodyHp(BodyPart part, int value) {
        bodyHp.put(part, clampBodyHp(part, value));
    }

    @Override
    public BleedingType getBleeding(BodyPart part) {
        return bleedings.getOrDefault(part, BleedingType.NONE);
    }

    @Override
    public void setBleeding(BodyPart part, BleedingType type) {
        bleedings.put(part, type == null ? BleedingType.NONE : type);
    }

    @Override
    public void damageBodyPart(BodyPart part, int amount) {
        if (part == null || amount <= 0) return;

        int newHp = Math.max(0, getBodyHp(part) - amount);
        setBodyHp(part, newHp);

        int maxHp = getMaxBodyHp(part);
        float hpRatio = newHp / (float) maxHp;

        if (newHp <= 0) {
            applyBleed(part,
                    (part == BodyPart.HEAD || part == BodyPart.TORSO)
                            ? BleedingType.HEAVY
                            : BleedingType.MEDIUM
            );
            return;
        }

        // Solo daño serio genera bleeding
        if (hpRatio <= 0.25F) {
            applyBleed(part, BleedingType.MEDIUM);
        }
    }

    @Override
    public void applyBleed(BodyPart part, BleedingType type) {
        if (part == null || type == null) return;

        if (type.ordinal() > getBleeding(part).ordinal()) {
            setBleeding(part, type);
        }
    }

    @Override
    public void tickBleeding() {
        int totalLoss = 0;

        for (BleedingType type : bleedings.values()) {
            if (type != null && type != BleedingType.NONE) {
                totalLoss += type.getBloodLoss();
            }
        }

        totalLoss = Math.min(totalLoss, 6);

        if (totalLoss > 0) {
            setSangre(sangre - totalLoss);
        }
    }

    @Override
    public BleedingType getWorstBleeding() {
        BleedingType worst = BleedingType.NONE;

        for (BleedingType type : bleedings.values()) {
            if (type != null && type.ordinal() > worst.ordinal()) {
                worst = type;
            }
        }

        return worst;
    }

    @Override
    public void applyBodyPartEffects() {

        int leftArm = getBodyHp(BodyPart.LEFT_ARM);
        int rightArm = getBodyHp(BodyPart.RIGHT_ARM);
        int leftLeg = getBodyHp(BodyPart.LEFT_LEG);
        int rightLeg = getBodyHp(BodyPart.RIGHT_LEG);
        int torso = getBodyHp(BodyPart.TORSO);
        int head = getBodyHp(BodyPart.HEAD);

        // =========================
        // BRAZOS
        // =========================
        boolean armsOk = leftArm > 5 && rightArm > 5;
        canAttack = armsOk;

        // =========================
        // PIERNAS (NUEVO SISTEMA)
        // =========================
        int avgLeg = (leftLeg + rightLeg) / 2;

        if (avgLeg <= 3) {
            canSprint = false;
            staminaMultiplier = 0.35f;
        }
        else if (avgLeg <= 8) {
            canSprint = false;
            staminaMultiplier = 0.6f;
        }
        else if (avgLeg <= 15) {
            canSprint = true;
            staminaMultiplier = 0.8f;
        }
        else {
            canSprint = true;
            staminaMultiplier = 1.0f;
        }

        // =========================
        // TORSO
        // =========================
        if (torso <= 25) {
            staminaMultiplier *= 0.7f;
        }

        // =========================
        // VISIÓN
        // =========================
        visionBlurred =
                head <= 8 ||
                        shock >= 65 ||
                        sangre <= 35;

        // =========================
        // OVERRIDES GLOBALES
        // =========================
        if (shock >= 75 || sangre <= 25) {
            canSprint = false;
        }

        if (shock >= 85) {
            canAttack = false;
        }
    }

    @Override
    public boolean canAttack() {
        return canAttack;
    }

    @Override
    public void setCanAttack(boolean value) {
        canAttack = value;
    }

    @Override
    public boolean canSprint() {
        return canSprint;
    }

    @Override
    public void setCanSprint(boolean value) {
        canSprint = value;
    }

    @Override
    public float getStaminaMultiplier() {
        return staminaMultiplier;
    }

    @Override
    public void setStaminaMultiplier(float value) {
        staminaMultiplier = Math.max(0.1f, value);
    }

    @Override
    public boolean isVisionBlurred() {
        return visionBlurred;
    }

    @Override
    public void setVisionBlurred(boolean value) {
        visionBlurred = value;
    }

    @Override
    public CompoundTag serializeBodyParts() {
        CompoundTag tag = new CompoundTag();

        for (BodyPart part : BodyPart.values()) {
            tag.putInt(part.name(), getBodyHp(part));
            tag.putInt(part.name() + "_bleeding", getBleeding(part).ordinal());
        }

        return tag;
    }

    @Override
    public void deserializeBodyParts(CompoundTag tag) {
        for (BodyPart part : BodyPart.values()) {
            if (tag.contains(part.name())) {
                setBodyHp(part, tag.getInt(part.name()));
            }

            if (tag.contains(part.name() + "_bleeding")) {
                int ordinal = tag.getInt(part.name() + "_bleeding");
                BleedingType[] values = BleedingType.values();

                if (ordinal >= 0 && ordinal < values.length) {
                    setBleeding(part, values[ordinal]);
                } else {
                    setBleeding(part, BleedingType.NONE);
                }
            }
        }

        applyBodyPartEffects();
    }

    @Override
    public EquipmentInventory getEquipmentInventory() {
        return equipmentInventory;
    }

    private int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private int clampBodyHp(BodyPart part, int value) {
        return Math.max(0, Math.min(getMaxBodyHp(part), value));
    }
}