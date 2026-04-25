package santi_moder.roleplaymod.client.radio;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class RadioKeyMappings {

    public static final String CATEGORY = "key.categories.roleplaymod.radio";

    public static final KeyMapping TOGGLE_RADIO = new KeyMapping(
            "key.roleplaymod.toggle_radio",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            CATEGORY
    );

    public static final KeyMapping FREQUENCY_UP = new KeyMapping(
            "key.roleplaymod.radio_frequency_up",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_BRACKET,
            CATEGORY
    );

    public static final KeyMapping FREQUENCY_DOWN = new KeyMapping(
            "key.roleplaymod.radio_frequency_down",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_BRACKET,
            CATEGORY
    );
}