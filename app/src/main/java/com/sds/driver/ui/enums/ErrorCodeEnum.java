package com.sds.driver.ui.enums;

public enum ErrorCodeEnum {
    SDSE01("FireStoreDriverService.getAllDrivers"),
    SDSE02("FireStoreDriverService.getDriverByPhone"),
    SDSE03("FireStoreDriverService.getDriverByPhoneFireStore"),
    SDSE04("FireStoreDriverService.getAllDriversFireStore"),
    SDSE05("FireStoreDriverService.saveDriver"),
    SDSE06("FireStoreDriverService.deleteDriver"),
    SDSE07("FireStoreDriverService.updateCache"),
    SDSE08("FireStoreDriverService.deleteFromCache"),
    SDSE09("FireStoreDriverService.loadDutiesFromCache"),
    SDSE10("FireStoreDriverService.loadDutiesFromCacheByPhone"),
    SDSE11("FireStoreDriverService.getAllDrivers"),
    SDSE12("FireStoreDriverService.getAllDrivers");

    private final String errormsg;

    ErrorCodeEnum(String errormsg) {
        this.errormsg = errormsg;
    }

    public String getErrormsg() {
        return errormsg;
    }
}
