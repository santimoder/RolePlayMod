package santi_moder.roleplaymod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import santi_moder.roleplaymod.item.ModItems;
import santi_moder.roleplaymod.network.ModNetwork;

@Mod(RolePlayMod.MOD_ID)
public class RolePlayMod {

    public static final String MOD_ID = "roleplaymod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RolePlayMod() {
        // Registrar items u otros elementos que no dependan del setup
        ModItems.register();

        // Registrar listener de eventos de lifecycle
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
        LOGGER.info("[RADIO] RolePlayMod setup completado");
    }
}