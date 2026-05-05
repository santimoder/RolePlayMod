package santi_moder.roleplaymod.server.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

public final class MedicalDebugCommand {

    private MedicalDebugCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("rpmed")
                        .then(Commands.literal("debug")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();

                                    player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                                        player.sendSystemMessage(Component.literal("===== MEDICAL DEBUG =====")
                                                .withStyle(ChatFormatting.RED));

                                        player.sendSystemMessage(Component.literal(
                                                "Sangre: " + data.getSangre()
                                                        + " | Shock: " + data.getShock()
                                                        + " | Inconsciente: " + data.isInconsciente()
                                                        + " | Ticks inconsciente: " + data.getUnconsciousTicks()
                                        ).withStyle(ChatFormatting.WHITE));

                                        player.sendSystemMessage(Component.literal(
                                                "Recent blood loss: " + data.getRecentBloodLoss()
                                                        + " | CanAttack: " + data.canAttack()
                                                        + " | CanSprint: " + data.canSprint()
                                                        + " | StaminaMult: " + data.getStaminaMultiplier()
                                        ).withStyle(ChatFormatting.GRAY));

                                        for (BodyPart part : BodyPart.values()) {
                                            player.sendSystemMessage(Component.literal(
                                                    part.name()
                                                            + " HP: " + data.getBodyHp(part)
                                                            + " | Bleeding: " + data.getBleeding(part).name()
                                            ).withStyle(ChatFormatting.YELLOW));
                                        }

                                        player.sendSystemMessage(Component.literal("=========================")
                                                .withStyle(ChatFormatting.RED));
                                    });

                                    return 1;
                                }))
        );
    }
}
