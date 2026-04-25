package santi_moder.roleplaymod.common.radio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class RadioData {

    private static final String TAG_FREQUENCY = "radio_frequency";
    private static final String TAG_POWERED = "radio_powered";
    private static final String TAG_VOLUME = "radio_volume";
    private static final String TAG_CHANNEL_NAME = "radio_channel_name";

    public static final float DEFAULT_FREQUENCY = 101.5F;
    public static final float MIN_FREQUENCY = 80.0F;
    public static final float MAX_FREQUENCY = 999.9F;
    public static final float STEP = 0.5F;

    public static void ensureDefaults(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(TAG_FREQUENCY)) {
            tag.putFloat(TAG_FREQUENCY, DEFAULT_FREQUENCY);
        }

        if (!tag.contains(TAG_POWERED)) {
            tag.putBoolean(TAG_POWERED, false);
        }

        if (!tag.contains(TAG_VOLUME)) {
            tag.putInt(TAG_VOLUME, 5);
        }

        if (!tag.contains(TAG_CHANNEL_NAME)) {
            tag.putString(TAG_CHANNEL_NAME, "");
        }
    }

    public static float getFrequency(ItemStack stack) {
        ensureDefaults(stack);
        return stack.getOrCreateTag().getFloat(TAG_FREQUENCY);
    }

    public static void setFrequency(ItemStack stack, float frequency) {
        ensureDefaults(stack);
        stack.getOrCreateTag().putFloat(TAG_FREQUENCY, clampFrequency(frequency));
    }

    public static void increaseFrequency(ItemStack stack) {
        float current = getFrequency(stack);
        float next = current + STEP;

        if (next > MAX_FREQUENCY) {
            next = MIN_FREQUENCY;
        }

        setFrequency(stack, next);
    }

    public static void decreaseFrequency(ItemStack stack) {
        float current = getFrequency(stack);
        float next = current - STEP;

        if (next < MIN_FREQUENCY) {
            next = MAX_FREQUENCY;
        }

        setFrequency(stack, next);
    }

    public static boolean isPowered(ItemStack stack) {
        ensureDefaults(stack);
        return stack.getOrCreateTag().getBoolean(TAG_POWERED);
    }

    public static void setPowered(ItemStack stack, boolean powered) {
        ensureDefaults(stack);
        stack.getOrCreateTag().putBoolean(TAG_POWERED, powered);
    }

    public static void togglePowered(ItemStack stack) {
        setPowered(stack, !isPowered(stack));
    }

    public static int getVolume(ItemStack stack) {
        ensureDefaults(stack);
        return stack.getOrCreateTag().getInt(TAG_VOLUME);
    }

    public static void setVolume(ItemStack stack, int volume) {
        ensureDefaults(stack);
        stack.getOrCreateTag().putInt(TAG_VOLUME, Math.max(0, Math.min(10, volume)));
    }

    public static String getChannelName(ItemStack stack) {
        ensureDefaults(stack);
        return stack.getOrCreateTag().getString(TAG_CHANNEL_NAME);
    }

    public static void setChannelName(ItemStack stack, String name) {
        ensureDefaults(stack);
        stack.getOrCreateTag().putString(TAG_CHANNEL_NAME, name == null ? "" : name);
    }

    private static float clampFrequency(float value) {
        if (value < MIN_FREQUENCY) value = MIN_FREQUENCY;
        if (value > MAX_FREQUENCY) value = MAX_FREQUENCY;
        return Math.round(value * 10.0F) / 10.0F;
    }
}