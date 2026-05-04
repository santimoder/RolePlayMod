package santi_moder.roleplaymod.client.phone.animation;

public record PhoneTransitionContext(int sourceX, int sourceY, int sourceWidth, int sourceHeight) {

    public static PhoneTransitionContext empty() {
        return new PhoneTransitionContext(0, 0, 0, 0);
    }

    public boolean hasSourceRect() {
        return sourceWidth > 0 && sourceHeight > 0;
    }
}