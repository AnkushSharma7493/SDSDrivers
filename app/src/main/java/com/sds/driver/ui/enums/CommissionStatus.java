package com.sds.driver.ui.enums;

public enum CommissionStatus {

    PENDING("PENDING"),
    RECEIVED("RECEIVED");

    private final String status;

    CommissionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static CommissionStatus getByStatus(String statusString) {
        for (CommissionStatus ds : values()) {
            if (ds.status.equalsIgnoreCase(statusString)) {
                return ds;
            }
        }
        throw new IllegalArgumentException("No enum constant for status: " + statusString);
    }

    public static String[] getStringValues() {
        return java.util.Arrays.stream(CommissionStatus.values())
                .map(CommissionStatus::getStatus)
                .toArray(String[]::new);
    }
}
