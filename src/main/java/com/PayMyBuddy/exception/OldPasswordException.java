package com.PayMyBuddy.exception;

/**
 * Represents an exception thrown when two passwords are not the same.
 */
public class OldPasswordException extends Exception {
    /**
     * Instantiates a new Old password exception.
     */
    public OldPasswordException() {
        super("The old password doesn't match with the registered password");
    }
}
