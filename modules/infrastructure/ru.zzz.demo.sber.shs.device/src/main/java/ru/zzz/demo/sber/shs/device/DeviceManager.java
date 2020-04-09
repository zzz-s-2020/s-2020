package ru.zzz.demo.sber.shs.device;

import org.springframework.lang.NonNull;

/**
 * This interface emulates a synchronous variant of message passing interface to a device stub.
 */
public interface DeviceManager {
    /**
     * Registers a new Device stub if it didn't exist and issues a comment on it.
     *
     * @param msg a requested commend.
     * @return reply. Errors are indicated as a {@link Reply.Error}.
     *
     * @throws IllegalArgumentException is an msg is null.
     */
    @NonNull
    Reply accept(Request msg);
}
