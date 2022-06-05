package antifraud.enums;

public enum Region {
    EAP("EAP"),
    ECA("ECA"),
    HIC("HIC"),
    LAC("LAC"),
    MENA("MENA"),
    SA("SA"),
    SSA("SSA");

    private final String region;

    Region(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return region;
    }
}
