package santi_moder.roleplaymod.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.client.radio.voice.SimpleVoiceChatPttBridge;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModKeyBindings {

    public static final String CATEGORY_GENERAL = "key.categories.roleplaymod";
    public static final String CATEGORY_RADIO = "key.categories.roleplaymod.radio";

    public static KeyMapping OPEN_BODY_STATUS;
    public static KeyMapping OPEN_RP_INVENTORY;
    public static KeyMapping QUICK_PANTS_PISTOL;
    public static KeyMapping OPEN_PHONE;

    public static KeyMapping RADIO_PTT;
    public static KeyMapping TOGGLE_RADIO;
    public static KeyMapping RADIO_FREQUENCY_UP;
    public static KeyMapping RADIO_FREQUENCY_DOWN;

    private ModKeyBindings() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        OPEN_BODY_STATUS = new KeyMapping(
                "key.roleplaymod.body_status",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                CATEGORY_GENERAL
        );

        OPEN_RP_INVENTORY = new KeyMapping(
                "key.roleplaymod.inventory",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_E,
                CATEGORY_GENERAL
        );

        QUICK_PANTS_PISTOL = new KeyMapping(
                "key.roleplaymod.quick_pants_pistol",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_1,
                CATEGORY_GENERAL
        );

        OPEN_PHONE = new KeyMapping(
                "key.roleplaymod.open_phone",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                CATEGORY_GENERAL
        );

        RADIO_PTT = new KeyMapping(
                "key.roleplaymod.radio_ptt",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY_RADIO
        );

        TOGGLE_RADIO = new KeyMapping(
                "key.roleplaymod.toggle_radio",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                CATEGORY_RADIO
        );

        RADIO_FREQUENCY_UP = new KeyMapping(
                "key.roleplaymod.radio_frequency_up",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                CATEGORY_RADIO
        );

        RADIO_FREQUENCY_DOWN = new KeyMapping(
                "key.roleplaymod.radio_frequency_down",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                CATEGORY_RADIO
        );

        event.register(OPEN_BODY_STATUS);
        event.register(OPEN_RP_INVENTORY);
        event.register(QUICK_PANTS_PISTOL);
        event.register(OPEN_PHONE);

        event.register(RADIO_PTT);
        event.register(TOGGLE_RADIO);
        event.register(RADIO_FREQUENCY_UP);
        event.register(RADIO_FREQUENCY_DOWN);

        SimpleVoiceChatPttBridge.init();
    }
}