package santi_moder.roleplaymod.client.phone.app;

public enum SettingsCategoryId {
    HOME("Configuracion"),
    PROFILE("Perfil"),
    WALLPAPER("Fondo de pantalla"),
    DISPLAY("Pantalla y brillo"),
    NOTIFICATIONS("Notificaciones"),
    SOUND("Sonido"),
    SECURITY("Face ID y codigo");

    private final String title;

    SettingsCategoryId(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}