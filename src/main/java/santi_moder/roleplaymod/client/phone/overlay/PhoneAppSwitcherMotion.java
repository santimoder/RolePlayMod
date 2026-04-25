package santi_moder.roleplaymod.client.phone.overlay;

public final class PhoneAppSwitcherMotion {

    private static final double LERP_FACTOR = 0.24D;
    private static final double SNAP_EPSILON = 0.001D;

    private double scrollPosition;
    private double targetScrollPosition;

    public void tick() {
        scrollPosition += (targetScrollPosition - scrollPosition) * LERP_FACTOR;

        if (Math.abs(targetScrollPosition - scrollPosition) < SNAP_EPSILON) {
            scrollPosition = targetScrollPosition;
        }
    }

    public void resetTo(double scrollPosition, double targetScrollPosition) {
        this.scrollPosition = scrollPosition;
        this.targetScrollPosition = targetScrollPosition;
    }

    public void setImmediatePosition(double position) {
        this.scrollPosition = position;
        this.targetScrollPosition = position;
    }

    public void setTargetPosition(double targetPosition, double min, double max) {
        this.targetScrollPosition = clamp(targetPosition, min, max);
    }

    public void moveTargetBy(double delta, double min, double max) {
        this.targetScrollPosition = clamp(this.targetScrollPosition + delta, min, max);
    }

    public double getScrollPosition() {
        return scrollPosition;
    }

    public double getTargetScrollPosition() {
        return targetScrollPosition;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}