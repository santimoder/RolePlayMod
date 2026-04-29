package santi_moder.roleplaymod.client.radio.voice;

public final class RadioVoiceChatPttState {

    private static volatile boolean radioPttDown = false;

    private RadioVoiceChatPttState() {
    }

    public static void setRadioPttDown(boolean down) {
        radioPttDown = down;
    }

    public static boolean isRadioPttDown() {
        return radioPttDown;
    }
}