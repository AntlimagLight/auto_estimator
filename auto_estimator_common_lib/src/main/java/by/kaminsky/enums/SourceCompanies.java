package by.kaminsky.enums;

public enum SourceCompanies {
    KAMINSKY("kaminsky.by"),
    BELWENT("belwent.by"),
    BELWENT_KERAM("schiedel-isokern.by"),
    PECHIBANI("pechibani.by"),
    OGONBY("100kaminov.by"),
    PECHNOYCENTR("pcentr.by"),
    PROPECHKIN("woodfuel.by"),
    STALNOY("stalnoy.by"),
    BY7745("7745.by"),
    MILE("mile.by"),
    SAMSTROY("samstroy.by");
    private final String value;

    SourceCompanies(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }


}
