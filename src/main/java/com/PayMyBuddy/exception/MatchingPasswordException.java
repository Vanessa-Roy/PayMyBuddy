package com.PayMyBuddy.exception;

/**
 * Represents an exception thrown when two passwords are not the same.
 */
public class MatchingPasswordException extends Exception {
    /**
     * Instantiates a new Matching password exception.
     */
    public MatchingPasswordException() {
        super("The matching password doesn't match with the password");
    }
}
