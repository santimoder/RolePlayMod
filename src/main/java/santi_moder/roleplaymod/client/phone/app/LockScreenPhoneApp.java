package santi_moder.roleplaymod.client.phone.app;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneAppId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LockScreenPhoneApp extends AbstractPhoneApp {

    private static final int DATE_Y_OFFSET = 34;
    private static final int TIME_Y_OFFSET = 48;

    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_SOFT_WHITE = 0xFFDADADA;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d MMM", new Locale("es", "ES"));

    public LockScreenPhoneApp() {
        super(PhoneAppId.LOCK_SCREEN);
    }

    @Override
    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = screen.getPhoneCenterX();
        int phoneY = screen.getPhoneY();

        String timeText = LocalTime.now().format(TIME_FORMATTER);
        String dateText = capitalizeFirst(LocalDate.now().format(DATE_FORMATTER));

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                dateText,
                centerX,
                phoneY + DATE_Y_OFFSET,
                COLOR_SOFT_WHITE
        );

        drawLargeCenteredText(
                guiGraphics,
                screen,
                timeText,
                centerX,
                phoneY + TIME_Y_OFFSET,
                COLOR_WHITE,
                2.4F
        );
    }

    private void drawLargeCenteredText(
            GuiGraphics guiGraphics,
            PhoneScreen screen,
            String text,
            int centerX,
            int y,
            int color,
            float scale
    ) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);

        float scaledX = centerX / scale;
        float scaledY = y / scale;
        float textX = scaledX - (screen.getPhoneFont().width(text) / 2.0F);

        guiGraphics.drawString(screen.getPhoneFont(), text, (int) textX, (int) scaledY, color, false);
        guiGraphics.pose().popPose();
    }

    private String capitalizeFirst(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    @Override
    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button) {
        return false;
    }
}