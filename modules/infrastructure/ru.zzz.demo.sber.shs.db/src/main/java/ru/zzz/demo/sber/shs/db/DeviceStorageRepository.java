package ru.zzz.demo.sber.shs.db;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * An interface of a devices storage (DB).
 * No connection polling is done. Everything goes through a single connection.
 */
public interface DeviceStorageRepository {
    /**
     * Creates a devise state record with a given address, val and is_on='Y' if it did not exist,
     * or updates existing one with a given volume, is_on='Y' and last_save_date=current_timestamp
     *
     * @param address device address
     * @return Mono(true)
     * <p>Mono({@link DbActionException}) if the device state was updated but the storage was not.
     * <p>Mono({@link DbConnectionException}) if the device state was updated but the storage was not.
     * @throws IllegalArgumentException if address is null
     */
    @NonNull
    Mono<Boolean> setDeviceValue(String address, int value);

    /**
     * Creates a devise state record with a given address, volume=0 and is_on='Y' if it did not exist,
     * or updates existing one with is_on='N' and last_save_date=current_timestamp
     *
     * @param address device address
     * @return Mono(true)
     * <p>Mono({@link DbActionException}) if the device state was updated but the storage was not.
     * <p>Mono({@link DbConnectionException}) if the device state was updated but the storage was not.
     * @throws IllegalArgumentException if address is null
     */
    @NonNull
    Mono<Boolean> setDeviceIsOff(String address);

    /**
     * Removes a devise state record.
     *
     * @param address device address
     * @return Mono(true)
     * <p>Mono({@link DbActionException}) if the device state was updated but the storage was not.
     * <p>Mono({@link DbConnectionException}) if the device state was updated but the storage was not.
     * @throws IllegalArgumentException if address is null
     */
    @NonNull
    Mono<Boolean> removeDevice(String address);

    /**
     * @return Flux(all device records as DTO)
     */
    @NonNull
    Flux<DeviceDto> readAll();
}
