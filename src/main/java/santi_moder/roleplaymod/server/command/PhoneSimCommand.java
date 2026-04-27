package santi_moder.roleplaymod.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.phone.PhoneData;
import santi_moder.roleplaymod.common.phone.sim.PhoneCarrier;
import santi_moder.roleplaymod.common.phone.sim.PhoneNumberFormatter;
import santi_moder.roleplaymod.common.phone.sim.PhoneNumberRegistry;
import santi_moder.roleplaymod.item.PhoneItem;

public final class PhoneSimCommand {

    private PhoneSimCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("rp_phone")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("sim")
                                .then(Commands.literal("install")
                                        .then(Commands.argument("carrier", StringArgumentType.word())
                                                .executes(context -> installSim(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "carrier")
                                                ))
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .executes(context -> removeSim(context.getSource()))
                                )
                                .then(Commands.literal("info")
                                        .executes(context -> info(context.getSource()))
                                )
                        )
        );
    }

    private static int installSim(CommandSourceStack source, String carrierName) {
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("Este comando solo puede usarlo un jugador."));
            return 0;
        }

        ItemStack phone = getPhoneInHand(player);
        if (phone.isEmpty()) {
            source.sendFailure(Component.literal("Tenes que tener un celular en la mano."));
            return 0;
        }

        PhoneCarrier carrier = PhoneCarrier.fromName(carrierName);
        PhoneNumberRegistry registry = PhoneNumberRegistry.get(player.serverLevel());

        String rawNumber = registry.reserveNewNumber(carrier);
        String formattedNumber = PhoneNumberFormatter.formatLocal(rawNumber);
        String simId = registry.generateSimId(carrier);

        PhoneData.installSim(phone, simId, formattedNumber);
        PhoneData.setCarrier(phone, carrier.displayName());

        source.sendSuccess(
                () -> Component.literal("SIM instalada: " + carrier.displayName() + " / " + formattedNumber),
                true
        );

        return 1;
    }

    private static int removeSim(CommandSourceStack source) {
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("Este comando solo puede usarlo un jugador."));
            return 0;
        }

        ItemStack phone = getPhoneInHand(player);
        if (phone.isEmpty()) {
            source.sendFailure(Component.literal("Tenes que tener un celular en la mano."));
            return 0;
        }

        PhoneData.removeSim(phone);

        source.sendSuccess(
                () -> Component.literal("SIM removida del celular."),
                true
        );

        return 1;
    }

    private static int info(CommandSourceStack source) {
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("Este comando solo puede usarlo un jugador."));
            return 0;
        }

        ItemStack phone = getPhoneInHand(player);
        if (phone.isEmpty()) {
            source.sendFailure(Component.literal("Tenes que tener un celular en la mano."));
            return 0;
        }

        PhoneData.initializeIfMissing(phone);

        String info = "Phone ID: " + PhoneData.getPhoneId(phone)
                + " | SIM: " + (PhoneData.hasSim(phone) ? "Si" : "No")
                + " | SIM ID: " + PhoneData.getSimId(phone)
                + " | Numero: " + PhoneData.getPhoneNumber(phone)
                + " | Empresa: " + PhoneData.getCarrier(phone);

        source.sendSuccess(() -> Component.literal(info), false);
        return 1;
    }

    private static ItemStack getPhoneInHand(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof PhoneItem) {
            PhoneData.initializeIfMissing(mainHand);
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof PhoneItem) {
            PhoneData.initializeIfMissing(offHand);
            return offHand;
        }

        return ItemStack.EMPTY;
    }
}