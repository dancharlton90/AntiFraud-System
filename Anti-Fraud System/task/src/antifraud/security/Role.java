package antifraud.security;

public enum Role {
    ADMINISTRATOR("ADMINISTRATOR"),
    MERCHANT("MERCHANT"),
    SUPPORT("SUPPORT");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }

}