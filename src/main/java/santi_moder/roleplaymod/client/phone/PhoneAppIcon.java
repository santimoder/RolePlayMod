package santi_moder.roleplaymod.client.phone;

import santi_moder.roleplaymod.common.phone.PhoneAppId;

public class PhoneAppIcon {

    private final PhoneAppId appId;
    private final String label;
    private final int x;
    private final int y;
    private final int size;

    public PhoneAppIcon(PhoneAppId appId, String label, int x, int y, int size) {
        this.appId = appId;
        this.label = label;
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public PhoneAppId getAppId() {
        return appId;
    }

    public String getLabel() {
        return label;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size;
    }

    public int getLabelCenterX() {
        return x + size / 2;
    }

    public int getLabelY() {
        return y + size + 3;
    }
}