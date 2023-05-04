package by.kaminsky.enums;

public enum ServiceCommand {
    START("/start"),
    HELP("/help"),
    SAVE("/save"),
    DELETE("/del"),
    UPDATE("/upd");
    private final String value;

    ServiceCommand(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ServiceCommand fromValue(String v) {
        for (ServiceCommand c : ServiceCommand.values()) {
            if (c.value.equals(v.toLowerCase())) {
                return c;
            }
        }
        return null;
    }
}
