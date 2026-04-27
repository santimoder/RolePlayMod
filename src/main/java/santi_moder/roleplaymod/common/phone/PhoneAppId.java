package santi_moder.roleplaymod.common.phone;

import net.minecraft.resources.ResourceLocation;
import santi_moder.roleplaymod.RolePlayMod;

public enum PhoneAppId {

    LOCK_SCREEN("Pantalla bloqueada", false, true, null),
    PASSCODE_SCREEN("Codigo", false, true, null),
    HOME("Home", false, true, null),

    PHONE("Telefono", true, false, "phone.png"),
    MESSAGES("Mensajes", true, false, "messages.png"),
    FACETIME("FaceTime", true, false, "facetime.png"),
    CALENDAR("Calendario", true, false, "calendar.png"),
    CALCULATOR("Calculadora", true, false, "calculator.png"),
    NOTES("Notas", true, false, "notes.png"),
    CONTACTS("Contactos", true, false, "contacts.png"),
    CAMERA("Camara", true, false, "camera.png"),
    WHATSAPP("WhatsApp", true, false, "whatsapp.png"),
    PHOTOS("Fotos", true, false, "photos.png"),
    MAPS("Maps", true, false, "maps.png"),
    SAFARI("Safari", true, false, "safari.png"),
    INSTAGRAM("Instagram", true, false, "instagram.png"),
    TWITTER("Twitter", true, false, "twitter.png"),
    SPOTIFY("Spotify", true, false, "spotify.png"),
    WEATHER("Clima", true, false, "weather.png"),
    CLOCK("Reloj", true, false, "clock.png"),
    APP_STORE("AppStore", true, false, "app_store.png"),
    SETTINGS("Configuracion", true, false, "settings.png");

    private final String displayName;
    private final boolean visibleOnHome;
    private final boolean systemApp;
    private final ResourceLocation icon;

    PhoneAppId(String displayName, boolean visibleOnHome, boolean systemApp, String iconFile) {
        this.displayName = displayName;
        this.visibleOnHome = visibleOnHome;
        this.systemApp = systemApp;
        this.icon = iconFile != null
                ? new ResourceLocation(RolePlayMod.MOD_ID, "textures/phone/apps/" + iconFile)
                : null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isVisibleOnHome() {
        return visibleOnHome;
    }

    public boolean isSystemApp() {
        return systemApp;
    }

    public ResourceLocation getIcon() {
        return icon;
    }
}