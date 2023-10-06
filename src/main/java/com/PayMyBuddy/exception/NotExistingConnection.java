package com.PayMyBuddy.exception;

/**
 * Represents an exception thrown when the connection about two users is none.
 */
public class NotExistingConnection extends Exception {
    /**
     * Instantiates a new Not existing connection.
     */
    public NotExistingConnection() {
        super("The connection doesn't exist between these two users");
    }
}
