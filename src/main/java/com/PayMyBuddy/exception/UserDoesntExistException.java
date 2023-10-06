package com.PayMyBuddy.exception;

/**
 * Represents an exception thrown when a user has never been created.
 */
public class UserDoesntExistException extends Exception {
    /**
     * Instantiates a new User does not exist exception.
     */
    public UserDoesntExistException() {
        super("There is no account registered with this email");
    }
}
