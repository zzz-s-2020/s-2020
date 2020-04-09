package ru.zzz.demo.sber.shs.CircuitBreaker.storage;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zzz.demo.sber.shs.db.DbActionException;
import ru.zzz.demo.sber.shs.db.DbConnectionException;
import ru.zzz.demo.sber.shs.db.DeviceDto;
import ru.zzz.demo.sber.shs.db.DeviceStorageRepository;

/**
 * This is a circuit breaker which does nothing but delegate calls to the {@link DeviceStorageRepository}.
 * Writing proper circuit breaker or integrate something like Hystrix is too much work for the small project.
 */
public interface DeviceStorageCircuitBreaker {
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
