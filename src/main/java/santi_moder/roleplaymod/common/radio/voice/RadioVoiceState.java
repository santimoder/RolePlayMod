package santi_moder.roleplaymod.common.radio.voice;

public class RadioVoiceState {

    private boolean transmitting;
    private float activeFrequency;

    public boolean isTransmitting() {
        return transmitting;
    }

    public void setTransmitting(boolean transmitting) {
        this.transmitting = transmitting;
    }

    public float getActiveFrequency() {
        return activeFrequency;
    }

    public void setActiveFrequency(float activeFrequency) {
        this.activeFrequency = activeFrequency;
    }

    public void clear() {
        this.transmitting = false;
        this.activeFrequency = 0F;
    }
}