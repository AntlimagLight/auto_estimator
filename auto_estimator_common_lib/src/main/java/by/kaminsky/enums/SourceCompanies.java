package by.kaminsky.enums;

public enum SourceCompanies {
    KAMINSKY("kaminsky.by"),
    BELWENT("belwent.by"),
    PECHIBANI("pechibani.by"),
    OGONBY("100kaminov.by"),
    PECHNOYCENTR("pcentr.by"),
    PROPECHKIN("woodfuel.by"),
    STALNOY("stalnoy.by"),
    DFABR("dfarb.by");
    private final String value;

    SourceCompanies(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }


}
