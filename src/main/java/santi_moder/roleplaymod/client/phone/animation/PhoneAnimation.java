package santi_moder.roleplaymod.client.phone.animation;

public final class PhoneAnimation {

    private final PhoneAnimationType type;
    private final int durationTicks;
    private int elapsedTicks;

    public PhoneAnimation(PhoneAnimationType type, int durationTicks) {
        this.type = type == null ? PhoneAnimationType.NONE : type;
        this.durationTicks = Math.max(1, durationTicks);
        this.elapsedTicks = 0;
    }

    public void tick() {
        if (!isFinished()) {
            elapsedTicks++;
        }
    }

    public boolean isFinished() {
        return elapsedTicks >= durationTicks;
    }

    public float progress() {
        return Math.min(1.0F, elapsedTicks / (float) durationTicks);
    }

    public float easeOut() {
        float t = progress();
        float inv = 1.0F - t;
        return 1.0F - inv * inv * inv;
    }

    public float easeInOut() {
        float t = progress();
        return t < 0.5F
                ? 4.0F * t * t * t
                : 1.0F - (float) Math.pow(-2.0F * t + 2.0F, 3.0) / 2.0F;
    }

    public PhoneAnimationType type() {
        return type;
    }
}