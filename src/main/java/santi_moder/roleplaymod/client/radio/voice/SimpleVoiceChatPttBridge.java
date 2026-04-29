package santi_moder.roleplaymod.client.radio.voice;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.fml.ModList;
import santi_moder.roleplaymod.RolePlayMod;

import java.lang.reflect.Field;

public final class SimpleVoiceChatPttBridge {

    private static KeyMapping voiceChatPttKey;
    private static boolean available;
    private static boolean forcedDown;

    private SimpleVoiceChatPttBridge() {
    }

    public static void init() {
        if (!ModList.get().isLoaded("voicechat")) {
            available = false;
            RolePlayMod.LOGGER.warn("[RADIO] Simple Voice Chat no está cargado");
            return;
        }

        try {
            Class<?> keyEventsClass = Class.forName("de.maxhenkel.voicechat.voice.client.KeyEvents");

            for (Field field : keyEventsClass.getDeclaredFields()) {
                if (!KeyMapping.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                field.setAccessible(true);
                KeyMapping mapping = (KeyMapping) field.get(null);

                if (mapping == null) {
                    continue;
                }

                RolePlayMod.LOGGER.info("[RADIO] KeyMapping detectado en Simple Voice Chat: {}", mapping.getName());

                String name = mapping.getName().toLowerCase();

                if (name.contains("push") || name.contains("ptt") || name.contains("voice")) {
                    voiceChatPttKey = mapping;
                    available = true;

                    RolePlayMod.LOGGER.info("[RADIO] PTT de Simple Voice Chat enlazado: {}", mapping.getName());
                    return;
                }
            }

            available = false;
            RolePlayMod.LOGGER.warn("[RADIO] No se encontró KeyMapping PTT de Simple Voice Chat");
        } catch (Throwable throwable) {
            available = false;
            RolePlayMod.LOGGER.error("[RADIO] Error inicializando bridge PTT de Simple Voice Chat", throwable);
        }
    }

    public static void setRadioPttDown(boolean down) {
        if (!available || voiceChatPttKey == null) {
            RolePlayMod.LOGGER.warn("[RADIO] No se puede forzar PTT: bridge no disponible");
            return;
        }

        forcedDown = down;

        if (down) {
            forcePress();
        } else {
            release();
        }
    }

    public static void clientTick() {
        if (forcedDown && voiceChatPttKey != null) {
            forcePress();
        }
    }

    private static void forcePress() {
        voiceChatPttKey.setDown(true);

        if (voiceChatPttKey.getKey() != null) {
            KeyMapping.click(voiceChatPttKey.getKey());
        }
    }

    public static void release() {
        forcedDown = false;

        if (voiceChatPttKey != null) {
            voiceChatPttKey.setDown(false);
        }
    }

    public static boolean isAvailable() {
        return available;
    }
}