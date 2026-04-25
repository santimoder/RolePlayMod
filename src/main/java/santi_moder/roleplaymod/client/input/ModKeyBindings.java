package santi_moder.roleplaymod.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import santi_moder.roleplaymod.RolePlayMod;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyBindings {

    public static final String CATEGORY = "key.categories.roleplaymod";

    public static KeyMapping OPEN_BODY_STATUS;
    public static KeyMapping OPEN_RP_INVENTORY;
    public static KeyMapping QUICK_PANTS_PISTOL;
    public static KeyMapping OPEN_PHONE;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {

        OPEN_BODY_STATUS = new KeyMapping(
                "key.roleplaymod.body_status",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                CATEGORY
        );

        OPEN_RP_INVENTORY = new KeyMapping(
                "key.roleplaymod.inventory",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_E,
                CATEGORY
        );

        QUICK_PANTS_PISTOL = new KeyMapping(
                "key.roleplaymod.quick_pants_pistol",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_1,
                CATEGORY
        );

        OPEN_PHONE = new KeyMapping(
                "key.roleplaymod.open_phone",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                CATEGORY
        );

        event.register(OPEN_BODY_STATUS);
        event.register(OPEN_RP_INVENTORY);
        event.register(QUICK_PANTS_PISTOL);
        event.register(OPEN_PHONE);
    }
}