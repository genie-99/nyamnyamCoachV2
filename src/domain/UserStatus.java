package domain;

public enum UserStatus {

    ACTIVE("활성"),
    INACTIVE("비활성");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}