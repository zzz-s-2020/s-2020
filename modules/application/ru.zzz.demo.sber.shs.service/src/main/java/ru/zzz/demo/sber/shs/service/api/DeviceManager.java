package ru.zzz.demo.sber.shs.service.api;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import ru.zzz.demo.sber.shs.db.DbActionException;
import ru.zzz.demo.sber.shs.db.DbConnectionException;
import ru.zzz.demo.sber.shs.model.device.DeviceAddress;
import ru.zzz.demo.sber.shs.model.device.DeviceIsOffException;

import java.util.List;
import java.util.Optional;

/**
 * Device manager interface. Devices manager tracks all known devices,
 * organizes communication with them and reads and updates an external devices store (DB).
 */
public interface DeviceManager {
    /**
     * @return a list of addresses of all known devices.
     */
    @NonNull
    List<DeviceAddress> list();

    /**
     * @return a list of all known devices along with their status.
     */
    @NonNull
    List<DeviceDescriptor> listWithCurrentStatus();

    /**
     * @param address device address
     * @return device along with their status
     */
    Optional<DeviceDescriptor> getDevice(DeviceAddress address);

    /**
     * Registers new device in an off state.
     *
     * @param address device address
     * @return a publisher of an operation finish moment.
     * <p>Mono({@link DeviceManagementException}) on device communication problems.
     * <p>Mono({@link DbActionException}) if the device state was updated but the storage was not.
     * <p>Mono({@link DbConnectionException}) if the device state was updated but the storage was not.
     * @throws IllegalArgumentException if address is null
     */
    @NonNull
    Mono<Void> register(DeviceAddress address);

    /**
     * Unregisters a device.
     *
     * @param address device address
     * @return Mono(true) if device was removed successfully.
     * <p>Mono(false) if there was no device with a given address.
     * <p>Mono({@link DeviceManagementException}) on device communication problems.
     * <p>Mono({@link DbActionException}) if the device state was updated but the storage was not.
     * <p>Mono({@link DbConnectionException}) if the device state was updated but the storage was not.
     * @throws IllegalArgumentException if address is null
     */
    @NonNull
    Mono<Boolean> unregister(DeviceAddress address);

    /**
     * Switches a device to on state.
     *
     * @param address device address
     * @return a publisher of an operation finish moment.
     * <p>Mono({@link DeviceManagementException}) on device communication problems.
     * <p>Mono({@link DbActionException}) if the device state was updated but the storage was not.
     * <p>Mono({@link DbConnectionException}) if the device state was updated but the storage was not.
     * @throws IllegalArgumentException if address is null
     */
    @NonNull
    Mono<Void> on(DeviceAddress address);

    /**
     * Switches a device to off state.
     *
     * @param address device address
     * @return a publisher of an operation finish moment.
     * <p>Mono({@link DeviceManagementException}) on device communication problems.
     * <p>Mono({@link DbActionException}) if the device state was updated but the storage was not.
     * <p>Mono({@link DbConnectionException}) if the device state was updated but the storage was not.
     * @throws IllegalArgumentException if address is null
     */
    @NonNull
    Mono<Void> off(DeviceAddress address);


    /**
     * Increments a value of a device and returns tits new value on success.
     *
     * @param address device address
     * @return Mono(new value)
     * <p>Mono({@link DeviceManagementException}) on device communication problems.
     * <p>Mono({@link DeviceIsOffException}) if the device is known to be off (no device communication
     * issued).
     * <p>Mono({@link DbActionException}) if the device state was updated but the storage was not.
     * <p>Mono({@link DbConnectionException}) if the device state was updated but the storage was not.
     * @throws IllegalArgumentException if address is null
     */
    @NonNull
    Mono<Integer> increment(DeviceAddress address);

    @NonNull
    Mono<Integer> decrement(DeviceAddress address);
}
