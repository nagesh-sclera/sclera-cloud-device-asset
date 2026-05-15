package io.sclera.enums;

import java.util.Arrays;

public enum SyncType {

    NO_CHANGES(0, "No modifications are required for this data."),
    UPSERT(1, "Insert the data as a new record if it doesn't exist, or update it if it does."),
    DELETE(2, "Remove the data from the database.");

    private final int code;
    private final String message;

    SyncType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static SyncType fromCode(int code) {
        return Arrays.stream(SyncType.values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid SyncType code: " + code));
    }
}
