package santi_moder.roleplaymod.common.phone;

public enum PhoneBrand {
    APPLE("Apple"),
    SAMSUNG("Samsung"),
    MOTOROLA("Motorola"),
    XIAOMI("Xiaomi"),
    HUAWEI("Huawei");

    private final String displayName;

    PhoneBrand(String displayName) {
        this.displayName = displayName;
    }

    public static PhoneBrand getDefault() {
        return APPLE;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAppleStyle() {
        return this == APPLE;
    }
}