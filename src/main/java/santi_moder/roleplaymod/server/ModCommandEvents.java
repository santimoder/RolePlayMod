package santi_moder.roleplaymod.server;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.server.command.PhoneSimCommand;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID)
public final class ModCommandEvents {

    private ModCommandEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        PhoneSimCommand.register(event.getDispatcher());
    }
}