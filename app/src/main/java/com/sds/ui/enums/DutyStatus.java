package com.sds.ui.enums;

public enum DutyStatus {

    UNASSIGNED("Unassigned"),
    ASSIGNED("Assigned"),
    WAITING_CONFIRMATION("Waiting Confirmation"),
    CONFIRMED("Confirmed"),
    IN_PROGRESS("InProgress"),
    COMPLETED("Completed"),
    CANCELED("Cancelled");

    private final String status;

    DutyStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    // Method to return array of enum names as strings
    public static String[] getStringValues() {
        return java.util.Arrays.stream(DutyStatus.values())
                .map(DutyStatus::getStatus)
                .toArray(String[]::new);
    }



}
