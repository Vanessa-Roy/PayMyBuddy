package com.PayMyBuddy.exception;

/**
 * Represents an exception thrown when two users already have a connection.
 */
public class AlreadyExistingConnection extends Exception {
    /**
     * Instantiates a new Already existing connection.
     */
    public AlreadyExistingConnection() {
        super("The connection already exists between these two users");
    }
}
