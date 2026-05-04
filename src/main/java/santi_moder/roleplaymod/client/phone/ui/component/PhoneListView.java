package santi_moder.roleplaymod.client.phone.ui.component;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

import java.util.List;

public record PhoneListView(int x, int startY, int width, int rowHeight, int rowGap) {

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
}