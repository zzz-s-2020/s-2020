package ru.zzz.demo.sber.shs.service.api;

/**
 * Device with this address already exists.
 */
public class AlreadyAssociatedException extends DeviceManagementException {
    public AlreadyAssociatedException(String s) {
        super(s);
    }
}
