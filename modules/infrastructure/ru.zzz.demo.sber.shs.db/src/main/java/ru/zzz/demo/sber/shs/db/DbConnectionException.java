package ru.zzz.demo.sber.shs.db;

/**
 * Indicates connection errors or permanent communication problems.
 */
public class DbConnectionException extends RuntimeException {
    public DbConnectionException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
