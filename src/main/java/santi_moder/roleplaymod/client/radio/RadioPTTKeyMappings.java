package santi_moder.roleplaymod.client.radio;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class RadioPTTKeyMappings {

    public static final KeyMapping RADIO_PTT = new KeyMapping(
            "key.roleplaymod.radio_ptt",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            RadioKeyMappings.CATEGORY
    );
}