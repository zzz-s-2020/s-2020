package ru.zzz.demo.sber.shs.service.api;

/**
 * Indicates problems in device communication.
 */
public class DeviceManagementException extends RuntimeException {
    public DeviceManagementException(String s) {
        super(s);
    }

    public DeviceManagementException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public DeviceManagementException(Throwable throwable) {
        super(throwable);
    }
}
