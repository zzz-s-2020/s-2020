package ru.zzz.demo.sber.shs.model.device;

/**
 * Indicates that operation has issued on a device that was off at the moment of the operation.
 */
public class DeviceIsOffException extends RuntimeException {
    public DeviceIsOffException(DeviceAddress addr) {
        super("Device " + addr.toString() + " is off.");
    }
}
