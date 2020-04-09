package ru.zzz.demo.sber.shs.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zzz.demo.sber.shs.config.ServerConfig;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.SQLTransientException;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicReference;

@Repository("SHS.Infrastructure.SingleConnectionDeviceRepository")
class SingleConnectionDeviceRepository implements InitializingBean, DisposableBean, DeviceStorageRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleConnectionDeviceRepository.class);

    private final ServerConfig config;
    private final Object connectionLock = new Object();
    private final AtomicReference<Connection> conRef = new AtomicReference<>();

    @Autowired
    public SingleConnectionDeviceRepository(ServerConfig config) {
        this.config = config;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
    }

    @Override
    @NonNull
    public Mono<Boolean> setDeviceValue(String address, int value) {
        if (address == null || address.isEmpty()) throw new IllegalArgumentException();
        return tryConnect().map(c -> {
            try {
                try (CallableStatement stm = c.prepareCall("{call shs_api_pkg.set_device_value(?,?)}")) {
                    stm.setString(1, address);
                    stm.setInt(2, value);
                    stm.execute();
                }
            } catch (SQLTransientException | SQLRecoverableException e) {
                // The connection is ok, something's wrong with the query
                throw new DbActionException("Cannot get a list", e);
            } catch (SQLException e) {
                // We don't disconnect here but just throw to notify circuit breaker
                throw new DbConnectionException("Cannot get a list", e);
            }
            return true;
        });
    }

    @Override
    @NonNull
    public Mono<Boolean> setDeviceIsOff(String address) {
        if (address == null || address.isEmpty()) throw new IllegalArgumentException();
        return tryConnect().map(c -> {
            try (CallableStatement stm = c.prepareCall("{call shs_api_pkg.set_device_is_off(?)}")) {
                stm.setString(1, address);
                stm.execute();
            } catch (SQLTransientException | SQLRecoverableException e) {
                // The connection is ok, something's wrong with the query
                throw new DbActionException("Cannot get a list", e);
            } catch (SQLException e) {
                // We don't disconnect here but just throw to notify circuit breaker
                throw new DbConnectionException("Cannot get a list", e);
            }
            return true;
        });
    }

    @Override
    @NonNull
    public Mono<Boolean> removeDevice(String address) {
        if (address == null || address.isEmpty()) throw new IllegalArgumentException();
        return tryConnect().map(c -> {
            try (CallableStatement stm = c.prepareCall("{call shs_api_pkg.remove_device(?)}")) {
                stm.setString(1, address);
                stm.execute();
            } catch (SQLTransientException | SQLRecoverableException e) {
                // The connection is ok, something's wrong with the query
                throw new DbActionException("Cannot get a list", e);
            } catch (SQLException e) {
                // We don't disconnect here but just throw to notify circuit breaker
                throw new DbConnectionException("Cannot get a list", e);
            }
            return true;
        });
    }

    @Override
    @NonNull
    public Flux<DeviceDto> readAll() {
        return tryConnect().flatMapMany(c -> Flux.generate(
                () -> new Query(c, "select address, is_on, val, last_save_date_utc from shs_device_state "),
                (query, sink) -> {
                    try {
                        if (query.next()) {
                            DeviceDto dto =
                                    DeviceDto.of(query.getString("address"), isOn(query.getString("is_on")),
                                            query.getInt("val"),
                                            query.getTimestamp("last_save_date_utc").toLocalDateTime());
                            sink.next(dto);
                        } else {
                            sink.complete();
                        }
                    } catch (SQLTransientException | SQLRecoverableException e) {
                        // The connection is ok, something's wrong with the query
                        sink.error(new DbActionException("Cannot get a list", e));
                    } catch (SQLException e) {
                        // We don't disconnect here but just throw to notify circuit breaker
                        sink.error(new DbConnectionException("Cannot get a list", e));
                    }
                    return query;
                }, Query::close));
    }

    @Override
    public void destroy() throws Exception {
        Connection local = conRef.getAndSet(null);
        if (local != null) local.close();
    }

    @NonNull
    private Mono<Connection> tryConnect() {
        return Mono.fromCallable(() -> {
            synchronized (connectionLock) {
                try {
                    if (conRef.get() == null) {
                        LOGGER.debug("Connecting to {} as {}", config.dbConnectionString(),
                                config.dbUserName());
                        conRef.set(
                                DriverManager.getConnection(config.dbConnectionString(), config.dbUserName(),
                                        config.dbUserPassword()));
                        LOGGER.debug("Connected to {} as {}", config.dbConnectionString(),
                                config.dbUserName());
                    }
                } catch (SQLException e) {
                    LOGGER.warn("Connected to {} as {}", config.dbConnectionString(), config.dbUserName());
                    throw new DbConnectionException(
                            "Cannot connect to " + config.dbConnectionString() + " as " +
                                    config.dbUserName() + ": " + e.getMessage(), e);
                }
            }
            return conRef.get();
        });
    }

    private static boolean isOn(String s) {
        return "Y".equals(s);
    }

    private static class Query {
        private final PreparedStatement stm;
        private final ResultSet rs;

        Query(Connection c, String select) throws SQLException {
            stm = c.prepareStatement(select);
            rs = stm.executeQuery();
        }

        boolean next() throws SQLException {
            return rs.next();
        }

        String getString(String field) throws SQLException {
            return rs.getString(field);
        }

        int getInt(String field) throws SQLException {
            return rs.getInt(field);
        }

        Timestamp getTimestamp(String field) throws SQLException {
            return rs.getTimestamp(field);
        }

        public void close() {
            try {
                rs.close();
                stm.close();
            } catch (SQLException e) {
                throw new DbActionException("Cannot close query statement", e);
            }
        }
    }
}
