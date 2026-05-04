package santi_moder.roleplaymod.client.phone;

import santi_moder.roleplaymod.common.phone.PhoneAppId;

public record PhoneAppIcon(PhoneAppId appId, String label, int x, int y, int size) {

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