package santi_moder.roleplaymod.client.phone.animation;

public final class PhoneTransitionContext {

    private final int sourceX;
    private final int sourceY;
    private final int sourceWidth;
    private final int sourceHeight;

    public PhoneTransitionContext(int sourceX, int sourceY, int sourceWidth, int sourceHeight) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
    }

    public static PhoneTransitionContext empty() {
        return new PhoneTransitionContext(0, 0, 0, 0);
    }

    public boolean hasSourceRect() {
        return sourceWidth > 0 && sourceHeight > 0;
    }

    public int sourceX() {
        return sourceX;
    }

    public int sourceY() {
        return sourceY;
    }

    public int sourceWidth() {
        return sourceWidth;
    }

    public int sourceHeight() {
        return sourceHeight;
    }
}