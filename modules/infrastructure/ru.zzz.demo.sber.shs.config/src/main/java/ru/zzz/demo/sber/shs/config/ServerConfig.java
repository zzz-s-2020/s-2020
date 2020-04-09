package ru.zzz.demo.sber.shs.config;

import org.springframework.lang.NonNull;

/**
 * Global server configuration. Currently it is not divided into meaningful areas, nothing is done to
 * support configuration changes.
 */
public interface ServerConfig {
    int port();

    int serverAcceptorThreads();

    int serverSelectorThreads();

    int serverWorkerThreads();

    @NonNull
    String dbConnectionString();

    @NonNull
    String dbUserName();

    @NonNull
    String dbUserPassword();
}
