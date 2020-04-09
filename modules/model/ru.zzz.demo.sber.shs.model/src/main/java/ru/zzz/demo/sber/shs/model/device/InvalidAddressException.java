package ru.zzz.demo.sber.shs.model.device;

/**
 * Indicates an empty or incorrect address.
 */
public class InvalidAddressException extends RuntimeException {
    public InvalidAddressException(String address) {
        super("Invalid address: [" + address + "]");
    }
}
