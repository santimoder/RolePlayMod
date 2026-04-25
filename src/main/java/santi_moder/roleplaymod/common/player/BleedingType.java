package santi_moder.roleplaymod.common.player;

public enum BleedingType {

    NONE(0),
    LIGHT(1),      // -1 sangre / tick lógico
    MEDIUM(2),     // -2
    HEAVY(5);      // -5

    private final int bloodLoss;

    BleedingType(int bloodLoss) {
        this.bloodLoss = bloodLoss;
    }

    public int getBloodLoss() {
        return bloodLoss;
    }
}