package santi_moder.roleplaymod.common.player;

public enum BleedingType {

    NONE(0),
    LIGHT(1),
    MEDIUM(2),
    HEAVY(3);

    private final int bloodLoss;

    BleedingType(int bloodLoss) {
        this.bloodLoss = bloodLoss;
    }

    public int getBloodLoss() {
        return bloodLoss;
    }
}