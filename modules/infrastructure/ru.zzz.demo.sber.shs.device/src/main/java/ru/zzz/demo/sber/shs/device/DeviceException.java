package ru.zzz.demo.sber.shs.device;

/**
 * May emulate some communication issues or indicate improper input.
 */
public class DeviceException extends RuntimeException {
    public DeviceException(String s) {
        super(s);
    }
}
