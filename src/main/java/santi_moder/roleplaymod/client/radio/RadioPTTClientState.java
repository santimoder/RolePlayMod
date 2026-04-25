package santi_moder.roleplaymod.client.radio;

public class RadioPTTClientState {

    private static boolean transmissionActive = false;

    private RadioPTTClientState() {
    }

    public static boolean isTransmissionActive() {
        return transmissionActive;
    }

    public static void setTransmissionActive(boolean active) {
        transmissionActive = active;
    }
}