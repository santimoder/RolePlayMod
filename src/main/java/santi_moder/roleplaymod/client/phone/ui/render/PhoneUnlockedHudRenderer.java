package santi_moder.roleplaymod.client.phone.ui.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneData;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class PhoneUnlockedHudRenderer {

    private static final int STATUS_LEFT_PADDING = 10;
    private static final int STATUS_RIGHT_PADDING = 10;
    private static final int STATUS_TOP_PADDING = 8;

    private static final int BATTERY_WIDTH = 16;
    private static final int BATTERY_HEIGHT = 8;

    private static final int COLOR_WHITE = 0xFFFFFFFF;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private PhoneUnlockedHudRenderer() {
    }

    public static void render(PhoneScreen screen, GuiGraphics guiGraphics) {
        int phoneX = screen.getPhoneX();
        int phoneY = screen.getPhoneY();
        int phoneWidth = screen.getPhoneWidth();
        int innerRight = phoneX + phoneWidth - STATUS_RIGHT_PADDING;

        ItemStack phoneStack = screen.getPhoneStack();

        String timeText = LocalTime.now().format(TIME_FORMATTER);

        guiGraphics.drawString(
                screen.getPhoneFont(),
                timeText,
                phoneX + STATUS_LEFT_PADDING,
                phoneY + STATUS_TOP_PADDING,
                COLOR_WHITE,
                false
        );

        String networkText = PhoneData.hasSim(phoneStack) ? "4G" : "";
        if (!networkText.isBlank()) {
            int networkX = innerRight - screen.getPhoneFont().width(networkText) - 26;

            guiGraphics.drawString(
                    screen.getPhoneFont(),
                    networkText,
                    networkX,
                    phoneY + STATUS_TOP_PADDING,
                    COLOR_WHITE,
                    false
            );
        }

        drawBattery(guiGraphics, innerRight - 18, phoneY + STATUS_TOP_PADDING, BATTERY_WIDTH, BATTERY_HEIGHT);
    }

    private static void drawBattery(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int color = COLOR_WHITE;

        guiGraphics.fill(x, y, x + width, y + 1, color);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        guiGraphics.fill(x, y, x + 1, y + height, color);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);

        guiGraphics.fill(x + width, y + 2, x + width + 2, y + height - 2, color);
        guiGraphics.fill(x + 2, y + 2, x + width - 3, y + height - 2, color);
    }
}