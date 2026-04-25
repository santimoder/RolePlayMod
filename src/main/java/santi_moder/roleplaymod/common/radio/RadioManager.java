package santi_moder.roleplaymod.common.radio;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class RadioManager {

    public static boolean hasUsableRadio(Player player) {
        LocatedRadio radio = RadioLocator.findUsableRadio(player);
        return radio != null && radio.isValid();
    }

    public static LocatedRadio getActiveRadio(Player player) {
        return RadioLocator.findUsableRadio(player);
    }

    public static boolean isRadioPowered(Player player) {
        LocatedRadio radio = getActiveRadio(player);
        if (radio == null) return false;
        return RadioData.isPowered(radio.getStack());
    }

    public static float getFrequency(Player player) {
        LocatedRadio radio = getActiveRadio(player);
        if (radio == null) return RadioData.DEFAULT_FREQUENCY;
        return RadioData.getFrequency(radio.getStack());
    }

    public static boolean canTransmit(Player player) {
        LocatedRadio radio = getActiveRadio(player);
        if (radio == null) return false;

        ItemStack stack = radio.getStack();
        return !stack.isEmpty() && RadioData.isPowered(stack);
    }

    public static void toggleRadio(Player player) {
        LocatedRadio radio = getActiveRadio(player);
        if (radio == null) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(Component.literal("No tenés una radio utilizable"), true);
            }
            return;
        }

        ItemStack stack = radio.getStack();
        if (stack.isEmpty()) return;

        RadioData.ensureDefaults(stack);
        RadioData.togglePowered(stack);
        radio.save(stack);
        syncIfNeeded(player);

        if (!player.level().isClientSide) {
            boolean powered = RadioData.isPowered(stack);

            player.displayClientMessage(
                    Component.literal("Radio " + (powered ? "encendida" : "apagada")),
                    true
            );

            player.level().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LEVER_CLICK,
                    SoundSource.PLAYERS,
                    0.5F,
                    powered ? 1.2F : 0.8F
            );
        }
    }

    public static void increaseFrequency(Player player) {
        LocatedRadio radio = getActiveRadio(player);
        if (radio == null) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(Component.literal("No tenés una radio utilizable"), true);
            }
            return;
        }

        ItemStack stack = radio.getStack();
        if (stack.isEmpty()) return;

        RadioData.ensureDefaults(stack);
        RadioData.increaseFrequency(stack);
        radio.save(stack);
        syncIfNeeded(player);

        if (!player.level().isClientSide) {
            player.displayClientMessage(
                    Component.literal("Frecuencia: " + formatFrequency(RadioData.getFrequency(stack))),
                    true
            );

            player.level().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.PLAYERS,
                    0.35F,
                    1.0F
            );
        }
    }

    public static void decreaseFrequency(Player player) {
        LocatedRadio radio = getActiveRadio(player);
        if (radio == null) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(Component.literal("No tenés una radio utilizable"), true);
            }
            return;
        }

        ItemStack stack = radio.getStack();
        if (stack.isEmpty()) return;

        RadioData.ensureDefaults(stack);
        RadioData.decreaseFrequency(stack);
        radio.save(stack);
        syncIfNeeded(player);

        if (!player.level().isClientSide) {
            player.displayClientMessage(
                    Component.literal("Frecuencia: " + formatFrequency(RadioData.getFrequency(stack))),
                    true
            );

            player.level().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.PLAYERS,
                    0.35F,
                    0.9F
            );
        }
    }

    private static void syncIfNeeded(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            RadioInventorySync.sync(serverPlayer);
        }
    }

    public static String formatFrequency(float frequency) {
        return String.format(Locale.US, "%.1f", frequency);
    }
}