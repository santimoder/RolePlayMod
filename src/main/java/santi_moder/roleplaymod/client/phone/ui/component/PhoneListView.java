package santi_moder.roleplaymod.client.phone.ui.component;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

import java.util.List;

public final class PhoneListView {

    private final int x;
    private final int startY;
    private final int width;
    private final int rowHeight;
    private final int rowGap;

    public PhoneListView(int x, int startY, int width, int rowHeight, int rowGap) {
        this.x = x;
        this.startY = startY;
        this.width = width;
        this.rowHeight = rowHeight;
        this.rowGap = rowGap;
    }

    public static PhoneListView defaultSettingsList(PhoneScreen screen) {
        return new PhoneListView(
                screen.getPhoneX() + 12,
                screen.getPhoneY() + 52,
                screen.getPhoneWidth() - 24,
                PhoneUi.LIST_BUTTON_H,
                PhoneUi.LIST_BUTTON_GAP
        );
    }

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, List<String> labels) {
        for (int i = 0; i < labels.size(); i++) {
            int y = getRowY(i);
            PhoneUi.drawListButton(screen, guiGraphics, mouseX, mouseY, x, y, width, labels.get(i));
        }
    }

    public int getClickedIndex(PhoneScreen screen, double mouseX, double mouseY, int itemCount) {
        for (int i = 0; i < itemCount; i++) {
            int y = getRowY(i);
            if (screen.isInside(mouseX, mouseY, x, y, width, rowHeight)) {
                return i;
            }
        }
        return -1;
    }

    public int getRowY(int index) {
        return startY + index * (rowHeight + rowGap);
    }

    public int x() {
        return x;
    }

    public int startY() {
        return startY;
    }

    public int width() {
        return width;
    }

    public int rowHeight() {
        return rowHeight;
    }

    public int rowGap() {
        return rowGap;
    }
}