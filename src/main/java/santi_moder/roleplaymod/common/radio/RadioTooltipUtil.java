package santi_moder.roleplaymod.common.radio;

public class RadioTooltipUtil {

    public static String getPowerText(boolean powered) {
        return powered ? "Encendida" : "Apagada";
    }

    public static String getFrequencyText(float frequency) {
        return "Frecuencia: " + RadioManager.formatFrequency(frequency);
    }
}