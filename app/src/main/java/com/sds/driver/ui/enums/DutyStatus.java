package com.sds.driver.ui.enums;

public enum DutyStatus {

    UNASSIGNED("Unassigned"),
    ASSIGNED("Assigned"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    INPROGRESS("InProgress"),
    COMPLETED("Completed"),
    CANCELED("Cancelled"),
    ALL("All");

    private final String status;

    DutyStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    // Static method to get enum by status string
    public static DutyStatus getByStatus(String statusString) {
        for (DutyStatus ds : values()) {
            if (ds.status.equalsIgnoreCase(statusString)) {
                return ds;
            }
        }
        throw new IllegalArgumentException("No enum constant for status: " + statusString);
    }

    // Method to return array of enum names as strings
    public static String[] getStringValues() {
        return java.util.Arrays.stream(DutyStatus.values())
                .map(DutyStatus::getStatus)
                .toArray(String[]::new);
    }



}
