package santi_moder.roleplaymod.client.phone.app.whatsapp;

public enum WhatsappTab {

    UPDATES("Novedades"),
    CALLS("Llamadas"),
    CHATS("Chats"),
    YOU("Tu");

    private final String label;

    WhatsappTab(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}