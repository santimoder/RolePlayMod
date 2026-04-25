package santi_moder.roleplaymod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import santi_moder.roleplaymod.common.radio.RadioData;
import santi_moder.roleplaymod.common.radio.RadioManager;

import javax.annotation.Nullable;
import java.util.List;

public class RadioItem extends Item {

    public RadioItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            RadioData.ensureDefaults(stack);

            if (player.isShiftKeyDown()) {
                RadioData.togglePowered(stack);

                boolean powered = RadioData.isPowered(stack);
                player.displayClientMessage(
                        Component.literal("Radio " + (powered ? "encendida" : "apagada")),
                        true
                );

                level.playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.LEVER_CLICK,
                        SoundSource.PLAYERS,
                        0.5F,
                        powered ? 1.2F : 0.8F
                );
            } else {
                RadioData.increaseFrequency(stack);

                player.displayClientMessage(
                        Component.literal("Frecuencia: " + RadioManager.formatFrequency(RadioData.getFrequency(stack))),
                        true
                );

                level.playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.UI_BUTTON_CLICK.value(),
                        SoundSource.PLAYERS,
                        0.35F,
                        1.0F
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RadioData.ensureDefaults(stack);

        boolean powered = RadioData.isPowered(stack);
        float frequency = RadioData.getFrequency(stack);
        int volume = RadioData.getVolume(stack);
        String channelName = RadioData.getChannelName(stack);

        tooltip.add(Component.literal("Frecuencia: " + RadioManager.formatFrequency(frequency))
                .withStyle(ChatFormatting.YELLOW));

        tooltip.add(Component.literal("Estado: " + (powered ? "Encendida" : "Apagada"))
                .withStyle(powered ? ChatFormatting.GREEN : ChatFormatting.RED));

        tooltip.add(Component.literal("Volumen: " + volume)
                .withStyle(ChatFormatting.AQUA));

        if (!channelName.isEmpty()) {
            tooltip.add(Component.literal("Canal: " + channelName)
                    .withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.literal("Shift + Click Derecho: prender/apagar")
                .withStyle(ChatFormatting.DARK_GRAY));

        tooltip.add(Component.literal("Click Derecho: subir frecuencia")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}