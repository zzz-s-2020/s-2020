package ru.zzz.demo.sber.shs.db;

/**
 * Indicates operation errors with stable connection.
 */
public class DbActionException extends RuntimeException {
    public DbActionException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
