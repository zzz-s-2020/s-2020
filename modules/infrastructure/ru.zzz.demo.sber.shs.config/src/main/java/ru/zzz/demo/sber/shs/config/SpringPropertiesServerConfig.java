package ru.zzz.demo.sber.shs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

/**
 * Provides default values which may be overridden using an app.properties file.
 */
@Configuration("SHS.Infrastructure.ServerConfig")
@PropertySource(value = "file:app.properties", ignoreResourceNotFound = true)
class SpringPropertiesServerConfig implements ServerConfig {
    private final Environment env;

    @Autowired
    SpringPropertiesServerConfig(Environment env) {
        this.env = env;
    }

    @Override
    public int port() {
        return Integer.parseInt(env.getProperty("server.port", "9090"));
    }

    @Override
    public int serverAcceptorThreads() {
        return Integer.parseInt(env.getProperty("server.acceptor.threads", "1"));
    }

    @Override
    public int serverSelectorThreads() {
        return Integer.parseInt(env.getProperty("server.selector.threads", "10"));
    }

    @Override
    public int serverWorkerThreads() {
        return Integer.parseInt(env.getProperty("server.worker.threads", "10"));
    }

    @Override
    @NonNull
    public String dbConnectionString() {
        return env.getProperty("db.connectionString", "jdbc:oracle:thin:@localhost:1521:orcl");
    }

    @Override
    @NonNull
    public String dbUserName() {
        return env.getProperty("db.user", "shs_api");
    }

    @Override
    @NonNull
    public String dbUserPassword() {
        return env.getProperty("db.password", "shs_api");
    }
}
